#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:/system/bin:/system/xbin:$PATH

test -e $1/lildebi-common || exit
. $1/lildebi-common

#------------------------------------------------------------------------------#
# modify rootfs

mount -o remount,rw rootfs /

# set /bin to busybox utils
if [ ! -e /bin ]; then
    echo "No '/bin' found, linking it to busybox utils"
    cd /
    ln -s $app_bin /bin
fi

if [ ! -e $mnt ]; then
    echo "Creating chroot mountpoint at $mnt"
    mkdir $mnt
    chmod 755 $mnt
fi

mount -o remount,ro rootfs /

#------------------------------------------------------------------------------#
# some platforms need to have the ext2 module installed to get ext2 support
if [ -z `grep ext2 /proc/filesystems` ]; then
    echo "Loading ext2 kernel module:"
    modprobe ext2
fi

#------------------------------------------------------------------------------#
# so that the debootstrap script can find its files
export DEBOOTSTRAP_DIR=$mnt/usr/share/debootstrap

#------------------------------------------------------------------------------#
# create the image file
echo "Create the image file:"

echo "> dd if=/dev/zero of=$imagefile seek=$imagesize bs=1M count=1"
test -e $imagefile || \
    dd if=/dev/zero of=$imagefile seek=$imagesize bs=1M count=1
# set them up
if test -d $mnt && test -e $imagefile; then
    echo "> mke2fs -L debian_chroot -F $imagefile"
    mke2fs -L debian_chroot -F $imagefile
    echo "> losetup $loopdev $imagefile"
    losetup $loopdev $imagefile
    echo "> mount -o loop,noatime,errors=remount-ro $loopdev $mnt || exit"
    mount -o loop,noatime,errors=remount-ro $loopdev $mnt || exit
    echo "> cd $mnt"
    cd $mnt
    echo "> tar xjf $app_bin/debootstrap.tar.bz2"
    tar xjf $app_bin/debootstrap.tar.bz2
    echo "> cp $app_bin/pkgdetails $DEBOOTSTRAP_DIR/pkgdetails"
    cp $app_bin/pkgdetails $DEBOOTSTRAP_DIR/pkgdetails
    echo "> chmod 755 $DEBOOTSTRAP_DIR/pkgdetails"
    chmod 755 $DEBOOTSTRAP_DIR/pkgdetails
else
    echo "No mount dir found ($mnt) or no imagefile ($imagefile)"
    exit 1
fi

#------------------------------------------------------------------------------#
# looking for GPG keyring used to validate signatures on downloaded packages

keyring_name=debian-archive-keyring.gpg
keyring=$app_bin/$keyring_name
if test -f $keyring; then
	echo "Using keyring for validating packages: $keyring"
	FIRST_KEYRING="--keyring=$keyring"

# debootstrap needs gpgv for the second stage too, but the gpgv Debian
# package is not installed yet, so install our included 'gpgv' and use
# that for now
    test -d $mnt/usr || mkdir $mnt/usr
    test -d $mnt/usr/local || mkdir $mnt/usr/local
    test -d $mnt/usr/local/bin || mkdir $mnt/usr/local/bin
    cp $app_bin/gpgv $mnt/usr/local/bin/
# we need a copy of the keyring in the chroot so the second stage of
# debootstrap can find it once its chrooted
    test -d $mnt/usr/local/share || mkdir $mnt/usr/local/share
    test -d $mnt/usr/local/share/keyrings || mkdir $mnt/usr/local/share/keyrings
    cp $keyring $mnt/usr/local/share/keyrings/
	SECOND_KEYRING="--keyring=/usr/local/share/keyrings/$keyring_name"
else
	echo "No keyring found, not validating packages! ($keyring)"
	FIRST_KEYRING=
	SECOND_KEYRING=
fi

#------------------------------------------------------------------------------#
echo "run debootstrap in two stages"

sh_debootstrap="$app_bin/sh $mnt/usr/sbin/debootstrap"

echo "> $sh_debootstrap --verbose $FIRST_KEYRING --arch armel --foreign $release $mnt $mirror || exit"
$sh_debootstrap --verbose $FIRST_KEYRING --arch armel --foreign $release $mnt $mirror || exit

# now we're in the chroot, so we don't need to set DEBOOTSTRAP_DIR, but we do
# need a more Debian-ish PATH
unset DEBOOTSTRAP_DIR
# use Debian tools from chroot for following chrooted commands. the rest of
# the script will find the included busybox utils in /bin, a link to $app_bin
export PATH=/usr/sbin:/usr/bin:/sbin:/bin:/usr/local/bin

echo "> chroot $mnt /debootstrap/debootstrap $SECOND_KEYRING --second-stage || exit"
chroot $mnt /debootstrap/debootstrap $SECOND_KEYRING --second-stage || exit

#------------------------------------------------------------------------------#
# create mountpoints
echo "creating mountpoints"

create_mountpoint() {
    test -d $1 && test -e $mnt/$1 || \
        mkdir $mnt/$1
}

# standard GNU/Linux mounts
create_mountpoint /dev
create_mountpoint /dev/pts
create_mountpoint /media
create_mountpoint /mnt
create_mountpoint /mnt/sdcard
create_mountpoint /proc
create_mountpoint /sys
create_mountpoint /tmp
# Android mounts
create_mountpoint /data
create_mountpoint /system
create_mountpoint /cache
create_mountpoint /dev/cpuctl
create_mountpoint /acct
create_mountpoint /mnt/obb
create_mountpoint /mnt/asec
create_mountpoint /mnt/secure
create_mountpoint /mnt/secure/asec
create_mountpoint /mnt/secure/.android_secure
create_mountpoint /sqlite_stmt_journals
create_mountpoint /app-cache

#------------------------------------------------------------------------------#
# create configs
echo "creating configs"

# create /etc/resolv.conf
test -e $mnt/etc || mkdir $mnt/etc
touch $mnt/etc/resolv.conf
echo 'nameserver 4.2.2.2' >> $mnt/etc/resolv.conf
echo 'nameserver 8.8.8.8' >> $mnt/etc/resolv.conf
echo 'nameserver 198.6.1.1' >> $mnt/etc/resolv.conf

# create /etc/hosts
cp /etc/hosts $mnt/etc/hosts

# create live mtab
test -e $mnt/etc/mtab && rm $mnt/etc/mtab
ln -s /proc/mounts $mnt/etc/mtab

# apt sources
test -e $mnt/etc/apt || mkdir $mnt/etc/apt
touch $mnt/etc/apt/sources.list
echo "deb $mirror $release main" >> $mnt/etc/apt/sources.list
echo "deb http://security.debian.org/ $release/updates main" >> $mnt/etc/apt/sources.list

echo "> apt-get update"
chroot $mnt apt-get update

# *  install and start sshd so you can easily log in, and before
#    stop/start so the start script starts sshd.  Also,
# * 'policyrcd-script-zg2' sets up the machine for starting and stopping
#    everything via /etc/init.d/rc without messing with the core Android
#    stuff.
# * 'molly-guard' adds a confirmation prompt to poweroff, halt,
#    reboot, and shutdown.
echo "> apt-get -y install ssh policyrcd-script-zg2 molly-guard"
chroot $mnt apt-get -y install ssh policyrcd-script-zg2 molly-guard
echo "> cp $app_bin/policy-rc.d $mnt/etc/policy-rc.d"
cp $app_bin/policy-rc.d $mnt/etc/policy-rc.d
echo "> chmod 755 $mnt/etc/policy-rc.d"
chmod 755 $mnt/etc/policy-rc.d


# stop and restart setup to make sure everything is mounted, etc.
echo "stop and restart setup to make sure everything is mounted, etc."
$app_bin/stop-debian.sh $app_bin $sdcard $imagefile $mnt
$app_bin/start-debian.sh $app_bin $sdcard $imagefile $mnt


# purge install packages in cache
echo "> apt-get autoclean"
chroot $mnt apt-get autoclean

# run 'apt-get upgrade' to get the security updates
echo "> apt-get -y upgrade"
chroot $mnt apt-get -y upgrade

# purge upgrade packages in cache
echo "> apt-get autoclean"
chroot $mnt apt-get autoclean


# install script that sets up a shell in the chroot for you
echo "installing '/debian/shell' for easy way to get to chroot from term"
if [ -d /debian ]; then
    cp $app_bin/shell /debian/shell
    chmod 755 /debian/shell
fi

echo "Debian is installed and ssh started!"
