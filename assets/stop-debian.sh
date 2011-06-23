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
if [ -x $mnt/etc/init.d/ssh ]; then
    chroot $mnt /bin/bash -c "/etc/init.d/ssh stop"
fi

echo "Checking for open files in Debian chroot..."
openfiles=`lsof /data/debian | sed -n 's|.*\(/data/debian.*\)|\1|p'`

if [ ! -z "$openfiles" ]; then
    echo "Files that are still open:"
    for line in $openfiles; do 
        echo $line
    done
else
    $busybox_path/umount -f  $mnt/dev/pts $mnt/proc $mnt/sys $mnt/tmp $mnt/mnt/sdcard

    umount $mnt
    
# remove loopback mount association
    losetup -d $loopdev
    
    echo ""
    echo "Debian chroot stopped and unmounted."
fi
