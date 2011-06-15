#!/system/bin/sh

. ./lildebi-common

remove_root_symlinks

if [ ! -e /bin ]; then
    mount -o remount,rw rootfs /
    rm /bin
    mount -o remount,ro rootfs /
fi

umount $mnt
rm -rf $mnt
rm -rf $dataDir
rm -f $imagefile

losetup -d $loopdev
