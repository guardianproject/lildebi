#!/system/bin/sh

echo "========================================"
echo "./remove-debian-setup.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

$1/stop-debian.sh

# force umount if stop-debian.sh failed (losetup not always needed)
test -d $mnt/usr && umount -f $mnt
losetup -d $loopdev 2> /dev/null

echo rmdir $mnt
rmdir $mnt
echo rm $imagefile
rm $imagefile

# if the /bin symlink exists, delete it
if [ -h /bin ]; then
    mount -o remount,rw rootfs /
    rm /bin
    mount -o remount,ro rootfs /
fi
