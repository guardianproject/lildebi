#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

echo "--------------------------------------------------"
echo "./start-debian.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

export TERM=linux
export HOME=/root
export PATH=/usr/bin:/usr/sbin:/bin:/sbin:$PATH

if [ ! -d $mnt ]; then
    echo "Your Debian setup is missing mountpoint: $mnt"
    exit
fi
if [ ! -d $sdcard ]; then
    echo "Your Debian setup is missing sdcard: $sdcard"
    exit
fi
if [ ! -e $imagefile ]; then
    echo "Your Debian setup is missing imagefile: $imagefile"
    exit
fi
if [ ! -e $loopdev ]; then
    echo "Your Debian setup is missing loopdev: $loopdev"
    exit
fi

echo "config: $mnt $sdcard $imagefile $loopdev"

losetup $loopdev $imagefile
mount -t ext2 $loopdev $mnt

mount -t devpts devpts $mnt/dev/pts
mount -t proc proc $mnt/proc
mount -t sysfs sysfs $mnt/sys
mount -t tmpfs tmpfs $mnt/tmp
 
mount -o bind $sdcard $mnt/mnt/sdcard

echo "Debian chroot mounted and started."
