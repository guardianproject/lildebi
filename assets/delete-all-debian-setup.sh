#!/data/data/info.guardianproject.lildebi/app_bin/sh

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

echo "========================================"
echo "./delete-all-debian-setup.sh"

test -e $1/lildebi-common || exit 1
. $1/lildebi-common

$1/stop-debian.sh

set -x

# force umount if stop-debian.sh failed
test -d $mnt/usr && umount -f $mnt
$losetup -d $loopdev

if [ x"$install_on_internal_storage" = xno ]; then
    rm $install_path
else
    rm -r $install_path
fi
rm $install_path.sha1

# delete the log from previous install
if [ -f $app_bin/../app_log/install.log ]; then
    rm $app_bin/../app_log/install.log
fi

mount -o remount,rw rootfs /
if [ -d $mnt ]; then
    rmdir $mnt
fi

# if the /bin symlink exists, delete it
if [ -h /bin ]; then
    rm /bin
fi

# if the /debian symlink exists, delete it
if [ -h /debian ]; then
    rm /debian
fi

# if the old /debian mount dir exists, delete it
if [ -d /debian ]; then
    rmdir /debian
fi
mount -o remount,ro rootfs /

#------------------------------------------------------------------------------#
# shortcuts for setting up the chroot in the terminal

if [ -e /data/local/bin/debian ]; then
    rm /data/local/bin/debian
fi

if [ -d /data/local/bin ]; then
    rmdir /data/local/bin
fi
