#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

echo "----------------------------------------"
echo "./start-debian.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

test-mount-bind() {
    test -d $1 && \
        grep " $1 " /proc/mounts && \
        mount -o bind $1 $mnt/$1
}

export TERM=linux
export HOME=/root
# set the path to use the included utils first
export PATH=$app_bin:/usr/bin:/usr/sbin:/bin:/sbin:$PATH

if [ ! -d $mnt ]; then
    echo "Your Debian setup is missing mountpoint."
    echo "    mkdir $mnt"
    mkdir $mnt
    exit
fi
if [ ! -d $sdcard ]; then
    echo "Your Debian setup is missing sdcard: $sdcard"
    exit
fi
# test if $imagefile is a file
if [ ! -f $imagefile ]; then
    echo "Your Debian setup is missing imagefile: $imagefile"
    exit
fi
# test if $loopdev is a block device
if [ ! -b $loopdev ]; then
    echo "Your Debian setup is missing loopdev: $loopdev"
    exit
fi

echo ""
echo "Configuration that will be started:"
echo "app_bin: $app_bin"
echo "mnt: $mnt"
echo "sdcard: $sdcard"
echo "imagefile: $imagefile"
echo "sha1file: $sha1file"
echo "loopdev: $loopdev"

if [ -e $sha1file ]; then
    echo "Checking SHA1 checksum of $imagefile..."
    cp $sha1file `dirname $imagefile`
    if `$app_bin/sha1sum -c $sha1file`; then
        echo "SHA1 checksum failed, exiting!"
        exit
    else
        echo "Done!"
    fi
fi


echo ""
# use offical fsck if it exists, other use included one if it exists
if [ -x /system/bin/e2fsck ]; then
    fsck=/system/bin/e2fsck
    echo "> $fsck -pv $imagefile"
    $fsck -pv $imagefile
    fsck_return=$?
elif [ -x $app_bin/e2fsck.static ]; then
    fsck=$app_bin/e2fsck.static
    # Debian's e2fsck.static needs to check /etc/mtab to make sure the
    # filesystem being check is not currently mounted. on Android, /etc is
    # actually /system/etc, so in order to avoid modifying /system, we run
    # e2fsck.static in a special minimal chroot.
    test -e $fsck_chroot || create_e2fsck_chroot
    imagedir=`dirname $imagefile`
    mount -o bind $app_bin $fsck_chroot/app_bin
    mount -o bind /dev $fsck_chroot/dev
    mount -o bind /proc $fsck_chroot/proc
    mount -o bind $imagedir $fsck_chroot/$imagedir
    echo "> $fsck -pv $imagefile"
    chroot $fsck_chroot /app_bin/`basename $fsck` -pv $imagefile
    fsck_return=$?
    umount $fsck_chroot/app_bin
    umount $fsck_chroot/dev
    umount $fsck_chroot/proc
    umount $fsck_chroot/$imagedir
else
    echo "NO fsck FOUND, SKIPPING DISK CHECK!"
    fsck_return=0
fi

test $fsck_return -lt 4 || exit $fsck_return


#------------------------------------------------------------------------------#
# mounts

echo ""
echo "> losetup $loopdev $imagefile"
losetup $loopdev $imagefile

# some platforms need to have the ext2 module installed to get ext2 support
if [ -z `grep ext2 /proc/filesystems` ]; then
    echo ""
    echo "Loading ext2 kernel module:"
    modprobe ext2
fi

echo ""
echo "root mount for everything Debian"
# root mount for everything Debian
mount $loopdev $mnt

# check error code for the above mount
mounterr=$?
if [ $mounterr -ne 0 ] ; then
    echo "Mounting '$mnt' failed, returned $mounterr"
    exit
fi

mount -t devpts devpts $mnt/dev/pts
mount -t proc proc $mnt/proc
mount -t sysfs sysfs $mnt/sys
mount -t tmpfs tmpfs $mnt/tmp
mount -o bind $sdcard $mnt/mnt/sdcard

# mount other android mounts, these may vary, so test first
test-mount-bind /acct
test-mount-bind /app-cache
test-mount-bind /cache
test-mount-bind /data
test-mount-bind /dbdata
test-mount-bind /dev/cpuctl
test-mount-bind /efs
test-mount-bind /mnt/.lfs
test-mount-bind /mnt/asec
test-mount-bind /mnt/obb
test-mount-bind /mnt/secure/asec
test-mount-bind /mnt/sdcard/external_sd
test-mount-bind /mnt/sdcard/external_sd/.android_secure
test-mount-bind /mnt/secure/.android_secure
test-mount-bind /sqlite_stmt_journals
test-mount-bind /sys/kernel/debug
test-mount-bind /system

#------------------------------------------------------------------------------#
# shortcuts for setting up the chroot in the terminal

if [ ! -e /debian/shell ]; then
    ln -s $app_bin/shell /debian/shell
fi

if [ ! -e /data/local/bin ]; then
    mkdir /data/local/bin
fi

if [ ! -e /data/local/bin/debian ]; then
    ln -s $app_bin/shell /data/local/bin/debian
fi

#------------------------------------------------------------------------------#
# ssh

keygen=/usr/bin/ssh-keygen
if [ -x ${mnt}${keygen} ]; then
    echo ""
    echo "My ssh host key fingerprint and random art:"
    chroot $mnt /bin/bash -c \
        "for key in /etc/ssh/ssh_host_*_key; do $keygen -lv -f \$key; done"
fi

echo ""
echo "Debian chroot mounted and started."
