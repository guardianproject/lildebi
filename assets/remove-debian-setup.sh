#!/system/bin/sh

. ./lildebi-common

remove_root_symlinks

umount $mnt
rm -rf $mnt
rm -rf /data/busybox
rm -rf $app_payload
rm -f $imagefile

losetup -d $loopdev
