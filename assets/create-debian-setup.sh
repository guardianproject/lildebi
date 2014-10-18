#!/data/data/info.guardianproject.lildebi/app_bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# get full debug output
set -x

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

test -e $1/lildebi-common || exit
. $1/lildebi-common

# include installation-specific settings like mirror and arch
. $app_bin/install.conf


#------------------------------------------------------------------------------#
# cdebootstrap needs /bin to find it utils

mount -o remount,rw rootfs /

# set /bin to busybox utils
if [ ! -e /bin ]; then
    echo "No '/bin' found, linking it to busybox utils"
    cd /
    ln -s $app_bin /bin
fi

mount -o remount,ro rootfs /


#------------------------------------------------------------------------------#
# create chroot mountpoint/directory

if [ -d $mnt ]; then
    echo "WARNING: Mountpoint directory '$mnt' exists! Using this folder!"
elif [ -e $mnt ]; then
    echo "ERROR: Mountpoint directory '$mnt' exists as a file! Exiting install..."
    exit 1
else
    echo "Creating chroot mountpoint at $mnt"
    mkdir $mnt
fi
chmod 755 $mnt

# create shortcut symlink to mountpoint
make_debian_symlink


#------------------------------------------------------------------------------#
# some platforms need to have the ext2 module installed to get ext2 support
if [ -z `grep ext2 /proc/filesystems` ]; then
    echo "Loading ext2 kernel module:"
    modprobe ext2
fi


#------------------------------------------------------------------------------#
# create the image file
if [ x"$install_on_internal_storage" = xno ]; then
    echo "Create the image file:"

    test -e $install_path || \
        dd if=/dev/zero of=$install_path seek=$imagesize bs=1M count=1
    # set them up
    if test -d $mnt && test -e $install_path; then
        mke2fs_options="-L debian_chroot -T `find_best_filesystem` -F $install_path"
    # the built-in mke2fs seems to be more reliable when the busybox mke2fs fails
        if test -x /system/bin/mke2fs; then
            /system/bin/mke2fs $mke2fs_options
        else
            mke2fs $mke2fs_options
        fi
        # run native and busybox losetup to test outputs
        losetup
        /system/xbin/losetup
        $losetup $loopdev $install_path
        losetup
        /system/xbin/losetup
        mount -o loop,noatime,errors=remount-ro $loopdev $mnt
        if $? -ne 0; then
            echo "Unable to mount loopback image!"
            exit 1
        fi
    else
        echo "No mount dir found ($mnt) or no install_path ($install_path)"
        exit 1
    fi
fi

    cd $mnt
    tar xf $app_bin/cdebootstrap.tar

#------------------------------------------------------------------------------#
# create mountpoints
echo "creating mountpoints"

create_mountpoint() {
    test -d $1 && test -e ${mnt}${1}
    if [ $? -ne 0 ] && [ ! -e ${mnt}${1} ]; then
        mkdir ${mnt}${1}
    fi
}

# standard GNU/Linux mounts
create_mountpoint /dev
create_mountpoint /dev/pts
create_mountpoint /media
create_mountpoint /mnt
create_mountpoint /proc
create_mountpoint /sys
create_mountpoint /sys/kernel
create_mountpoint /sys/kernel/debug
create_mountpoint /tmp
# Android mounts
create_mountpoint /acct
create_mountpoint /app-cache
create_mountpoint /cache
create_mountpoint /data
create_mountpoint /dbdata
create_mountpoint /dev/cpuctl
create_mountpoint /efs
create_mountpoint /mnt/.lfs
create_mountpoint /mnt/asec
create_mountpoint /mnt/emmc
create_mountpoint /mnt/obb
create_mountpoint /mnt/sdcard
create_mountpoint /mnt/sdcard/external_sd
create_mountpoint /mnt/sdcard/external_sd/.android_secure
create_mountpoint /mnt/secure
create_mountpoint /mnt/secure/asec
create_mountpoint /mnt/secure/.android_secure
create_mountpoint /mnt/shell
create_mountpoint /mnt/shell/emulated
create_mountpoint /pds
create_mountpoint /sqlite_stmt_journals
create_mountpoint /storage
create_mountpoint /storage/emulated
create_mountpoint /storage/emulated/0
create_mountpoint /storage/emulated/legacy
create_mountpoint /storage/extSdCard
create_mountpoint /storage/sdcard0
create_mountpoint /storage/sdcard1
create_mountpoint /storage/usbdisk
create_mountpoint /system


#------------------------------------------------------------------------------#
# looking for GPG keyring used to validate signatures on downloaded packages

keyring_name=debian-archive-keyring.gpg
keyring=$app_bin/$keyring_name
if test -f $keyring; then
	echo "Using keyring for validating packages: $keyring"
	KEYRING="--keyring=$keyring"
else
	echo "No keyring found, not validating packages! ($keyring)"
	KEYRING=
fi


#------------------------------------------------------------------------------#
echo "run cdebootstrap in one stage"

$mnt/usr/bin/cdebootstrap-static --verbose --foreign\
    --flavour=minimal --include=locales $KEYRING \
    --configdir=$mnt/usr/share/cdebootstrap-static \
    --helperdir=$mnt/usr/share/cdebootstrap-static \
    --arch $arch $release $mnt $mirror || exit

if [ x"$install_on_internal_storage" = xyes ]; then
    mount -o bind /dev $mnt/dev
fi
SHELL=/bin/sh chroot $mnt /sbin/cdebootstrap-foreign --second-stage

# This package sets up all the Android app users and permissions groups.  It
# needs to be installed as early as possible to claim the uids for group names
# like 'bluetooth' that also exist in Debian. (installing it like this is a
# temporary workaround until this package is included in Debian).
if [ -r $app_bin/android-permissions_0.1_all.deb ]; then
    cp $app_bin/android-permissions_0.1_all.deb $mnt/root/
    SHELL=/bin/sh chroot $mnt /usr/bin/dpkg -i /root/android-permissions_0.1_all.deb
fi

# figure out extra packages to include
if [ ! -x /system/bin/e2fsck ]; then
    install_e2fsck_static
fi

#------------------------------------------------------------------------------#
# create root symlinks that exist on the Android system
echo "creating root symlinks"

create_root_symlink() {
    if [ -L $1 ] && [ ! -e ${mnt}${1} ]; then
        link=`ls -l $1 | awk '{print $4}'`
        target=`ls -l $1 | awk '{print $6}'`
        ln -s $target ${mnt}${link}
    fi
}

for file in /*; do
    create_root_symlink $file
done

#------------------------------------------------------------------------------#
# create configs
echo "creating configs"

# create /etc/resolv.conf
test -e $mnt/etc || mkdir $mnt/etc
touch $mnt/etc/resolv.conf
echo 'nameserver 4.2.2.2' >> $mnt/etc/resolv.conf
echo 'nameserver 8.8.8.8' >> $mnt/etc/resolv.conf
echo 'nameserver 198.6.1.1' >> $mnt/etc/resolv.conf
chmod 644 $mnt/etc/resolv.conf

# create /etc/hosts
cp /etc/hosts $mnt/etc/hosts

# create live mtab
test -e $mnt/etc/mtab && rm $mnt/etc/mtab
ln -s /proc/mounts $mnt/etc/mtab

# apt sources
if [ ! -e $mnt/etc/apt/sources.list ]; then
    test -e $mnt/etc/apt || mkdir $mnt/etc/apt
    touch $mnt/etc/apt/sources.list
    echo "deb $mirror $release main" >> $mnt/etc/apt/sources.list
fi

# sid does not have security updates, everything else should
if [ $release != "sid" ] && [ $release != "unstable" ]; then
    echo "deb http://security.debian.org/ $release/updates main" >> $mnt/etc/apt/sources.list
fi

# Debian's e2fsck.static needs to check /etc/mtab to make sure the
# filesystem being check is not currently mounted. on Android, /etc is
# actually /system/etc, so in order to avoid modifying /system, we run
# e2fsck.static in a special minimal chroot.
echo "set up chroot for e2fsck"
create_e2fsck_chroot

#------------------------------------------------------------------------------#
# finish tweaking Debian install
echo "finish tweaking Debian install"

chroot $mnt apt-get -y update

# purge install packages in cache
chroot $mnt apt-get clean

# remove stop scripts
chroot $mnt /usr/sbin/update-rc.d -f halt remove
chroot $mnt /usr/sbin/update-rc.d -f hwclock.sh remove
chroot $mnt /usr/sbin/update-rc.d -f reboot remove
chroot $mnt /usr/sbin/update-rc.d -f sendsigs remove
chroot $mnt /usr/sbin/update-rc.d -f umountfs remove
chroot $mnt /usr/sbin/update-rc.d -f umountroot remove

# convert to ext3, if that's available. for some reason unknown to me, Android
# shows the loop devices as /dev/block/loop[0-7] while those same devices show
# up as /dev/loop[0-7] un Debian. tune2fs needs /proc mounted so it can read
# /proc/mounts via the /etc/mtab symlink.
if `grep -q -s ext3 /proc/filesystems`; then
    mount -t proc proc $mnt/proc
    chroot $mnt tune2fs -j `echo $loopdev | sed s,block/,,`
    umount $mnt/proc
fi


#------------------------------------------------------------------------------#
# clean up after debootstrap

chroot $mnt dpkg --purge cdebootstrap-helper-rc.d
if [ x"$install_on_internal_storage" = xyes ]; then
    umount $mnt/dev
fi
