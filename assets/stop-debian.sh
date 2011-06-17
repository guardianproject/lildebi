#!/data/busybox/sh
#
# see lildebi-common for arguments, the args are converted to vars there. The
# first arg $1 is the "app payload" directory, where the included scripts are
# kept.

echo "--------------------------------------------------"
echo "./stop-debian.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

# stop ssh, this really should use the whole proper shutdown procedure

openfiles=`lsof | cut -b 68-255 | grep $mnt`

if [ ! -z "$openfiles" ]; then
    echo "Files that are still open:"
    echo $openfiles
else
    $busybox_path/umount -f  $mnt/dev/pts $mnt/proc $mnt/sys $mnt/tmp $mnt/mnt/sdcard

    umount $mnt
    
# remove loopback mount association
    losetup -d $loopdev
    
    echo ""
    echo "Debian chroot stopped and unmounted."
fi
