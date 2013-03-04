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
echo "loopdev: $loopdev"

echo ""
for f in /system/bin/e2fsck /system/bin/fsck.ext2 $app_bin/fsck.ext2; do
    if [ -x $f ]; then
        fsck=$f
        break
    fi
done
if [ -z $fsck ]; then
    echo "NO fsck FOUND, SKIPPING DISK CHECK!"
else
    echo "> $fsck -pv $imagefile"
    $fsck -pv $imagefile
    fsck_return=$?
    test $fsck_return -lt 4 || exit $fsck_return
fi

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
