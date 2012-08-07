#!/system/bin/sh

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

echo "========================================"
echo "./remove-debian-setup.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

$1/stop-debian.sh

# force umount if stop-debian.sh failed
test -d $mnt/usr && umount -f $mnt
losetup -d $loopdev

echo rm $imagefile
rm $imagefile

mount -o remount,rw rootfs /
if [ -d $mnt ]; then
    echo rmdir $mnt
    rmdir $mnt
fi

# if the /bin symlink exists, delete it
if [ -h /bin ]; then
    echo rm /bin
    rm /bin
fi
mount -o remount,ro rootfs /
