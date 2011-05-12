#!/system/bin/sh

scripts=$(cd `dirname $0` && pwd)
if [ -x "$scripts" ]; then
    . $scripts/lildebi-common
    binaries="${scripts}/../binaries"
    debootstrap="${scripts}/../debootstrap"
else
    . ./lildebi-common
    binaries=../binaries
    debootstrap=../debootstrap/
fi

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
 
mount -o bind $sdcard $mnt/$sdcard

echo " "
echo "Type EXIT to end session"
echo "Make sure you do a proper EXIT for a clean umount!"
echo " "
echo "Please reboot your device when you finished your work."

chroot $mnt /bin/bash

umount -l $mnt$sdcard

busybox umount -f  $mnt/dev/pts $mnt/proc $mnt/sys $mnt/tmp $mnt$sdcard
