#!/data/data/info.guardianproject.lildebi/app_bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there. The
# first arg $1 is the "app payload" directory, where the included scripts are
# kept.

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

test -e $1/lildebi-common || exit 1
. $1/lildebi-common

kill_processes () {
    local signal="$1"

    for root in /proc/*/root; do
      if [ ! -r "$root" ] || [ ! "`readlink "$root"`" = "$mnt" ]; then
        continue
      fi
      pid="${root#/proc/}"
      pid="${pid%/root}"
      kill -"$signal" "$pid" 2>/dev/null || true
    done

    # /debian/shell starts outside of the chroot, so it must be killed separately
    for pid in `ps | grep '/[d]ebian/shell' | cut -b1-5`; do
      kill -$signal $pid
    done
}

echo -n "Asking all processes to terminate..."
# first send TERM
kill_processes TERM
sleep 1
echo "done"
# then send KILL to force the rest to die
echo -n "Killing remaining processes..."
kill_processes KILL
echo "done"

echo -n "Checking for open files in Debian chroot..."
openfiles=`lsof $mnt | grep -v $(basename $install_path) | sed -n "s|.*\($mnt.*\)|\1|p"`
echo "done"

if [ ! -z "$openfiles" ]; then
    echo "Files that are still open:"
    for line in $openfiles; do
        echo $line
    done

    echo ""
    echo "Not stopping debian because of open files, quit all processes and running shell sessions!"
    exit 1
else
    echo -n "Unmounting everything..."
    # sort reverse so it gets the nested mounts first
    for mount in `cut -d ' ' -f 2 /proc/mounts | grep $mnt/ | sort -r`; do
        $busybox_path/umount -f $mount
    done

    if [ x"$install_on_internal_storage" = xno ]; then
        umount -d $mnt || /system/bin/umount $mnt || echo "Failed to unmount $mnt!"
    fi
    echo "done"

    attached=`find_attached_loopdev`
    if [ ! -z $attached ]; then
        echo -n "Deleting loopback device..."
        $losetup -d $attached
        echo "done"
    fi

    echo ""
    echo "Debian chroot stopped and unmounted."
    if [ -e $sha1file ]; then
        echo -n "Calculating new SHA1 checksum of $install_path..."
        $app_bin/sha1sum $install_path > $sha1file
        chmod 0600 $sha1file
        cp $sha1file `dirname $install_path`
        echo "Done!"
    fi
fi
