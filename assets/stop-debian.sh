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
    exit 1
else
    echo "unmounting everything"
    # sort reverse so it gets the nested mounts first
    for mount in `cut -d ' ' -f 2 /proc/mounts | grep $mnt/ | sort -r`; do
        $busybox_path/umount -f $mount
    done

    umount -d $mnt

    echo "Deleting loopback device..."
    losetup -d `find_attached_loopdev`
    
    echo ""
    echo "Debian chroot stopped and unmounted."
    if [ -e $sha1file ]; then
        echo "Calculating new SHA1 checksum of $imagefile..."
        $app_bin/sha1sum $imagefile > $sha1file
        chmod 0600 $sha1file
        cp $sha1file `dirname $imagefile`
        echo "Done!"
    fi
fi
