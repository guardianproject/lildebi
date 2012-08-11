#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there. The
# first arg $1 is the "app payload" directory, where the included scripts are
# kept.

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

echo "----------------------------------------"
echo "./stop-debian.sh"

test -e $1/lildebi-common || exit
. $1/lildebi-common

echo "Checking for open files in Debian chroot..."
openfiles=`lsof $mnt | grep -v $(basename $imagefile) | sed -n "s|.*\($mnt.*\)|\1|p"`

if [ ! -z "$openfiles" ]; then
    echo "Files that are still open:"
    for line in $openfiles; do 
        echo $line
    done

    echo ""
    echo "Not stopping debian because of open files, quit all processes and running shell sessions!"
else
    echo "unmounting everything"
    # sort reverse so it gets the nested mounts first
    for mount in `cut -d ' ' -f 2 /proc/mounts | grep $mnt/ | sort -r`; do
        $busybox_path/umount -f $mount
    done

    umount $mnt
    
    # remove loopback mount association
    TEST=`losetup $loopdev | grep $imagefile || echo stopped`
    if [ "$TEST" != "stopped" ]; then
        echo "> losetup -d $loopdev"
        losetup -d $loopdev
    else
        echo "losetup test result: $TEST"
        losetup $loopdev
    fi
    
    echo ""
    echo "Debian chroot stopped and unmounted."
fi
