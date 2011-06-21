#!/system/bin/sh

echo "========================================"
echo "./remove-debian-setup.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

$1/stop-debian.sh

remove_root_symlinks

# if the /bin symlink exists, delete it
if [ -h /bin ]; then
    mount -o remount,rw rootfs /
    rm /bin
    mount -o remount,ro rootfs /
fi

umount -f $mnt
rm -rf $mnt
rm -f $imagefile

losetup -d $loopdev
