#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

echo "--------------------------------------------------"
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
export PATH=/usr/bin:/usr/sbin:/bin:/sbin:$PATH

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

echo "app_bin: $app_bin"
echo "mnt: $mnt"
echo "sdcard: $sdcard"
echo "imagefile: $imagefile"
echo "loopdev: $loopdev"

if [ -x /system/bin/e2fsck ]; then
    echo "Running /system/bin/e2fsck:"
    /system/bin/e2fsck -pv $imagefile
    fsck_return=$?
    test $fsck_return -lt 4 || exit $fsck_return
fi

losetup $loopdev $imagefile

if [ -z `grep ext2 /proc/filesystems` ]; then
    echo "Loading ext2 kernel module:"
    modprobe ext2
fi

# root mount for everything Debian
mount -t ext2 $loopdev $mnt

mount -t devpts devpts $mnt/dev/pts
mount -t proc proc $mnt/proc
mount -t sysfs sysfs $mnt/sys
mount -t tmpfs tmpfs $mnt/tmp
mount -o bind $sdcard $mnt/mnt/sdcard

# mount other android mounts, these may vary, so test first
test-mount-bind /data
test-mount-bind /system
test-mount-bind /cache
test-mount-bind /dev/cpuctl
test-mount-bind /acct
test-mount-bind /mnt/obb
test-mount-bind /mnt/asec
test-mount-bind /mnt/secure/asec
test-mount-bind /mnt/secure/.android_secure
test-mount-bind /sqlite_stmt_journals
test-mount-bind /app-cache

echo "My ssh host key fingerprint and random art:"
/usr/bin/ssh-keygen -lv -f /etc/ssh/ssh_host_rsa_key

echo "Debian chroot mounted and started."
