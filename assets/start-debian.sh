#!/data/data/info.guardianproject.lildebi/app_bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

test -e $1/lildebi-common || exit
. $1/lildebi-common

test_mount_bind() {
    test -d $1 && \
        grep " $1 " /proc/mounts && \
        mount -o bind $1 $mnt/$1
}

export TERM=linux
export HOME=/root
# set the path to use the included utils first
export PATH=$app_bin:/usr/bin:/usr/sbin:/bin:/sbin:$PATH

if [ ! -d $mnt ]; then
    echo "Your Debian setup is missing mountpoint."
    echo "    mkdir $mnt"
    mkdir $mnt
    exit
fi
if [ ! -d $sdcard ]; then
    echo "Your Debian setup is missing sdcard: $sdcard"
    exit
fi
# test if $image_path is a file
if [ ! -f $image_path ]; then
    echo "Your Debian setup is missing image_path: $image_path"
    exit
fi
# test if $loopdev is a block device
if [ ! -b $loopdev ]; then
    echo "Your Debian setup is missing loopdev: $loopdev"
    exit
fi

echo ""
echo "Configuration that will be started:"
echo "app_bin: $app_bin"
echo "mnt: $mnt"
echo "sdcard: $sdcard"
echo "image_path: $image_path"
echo "sha1file: $sha1file"
echo "loopdev: $loopdev"

if [ -e $sha1file ]; then
    echo "Checking SHA1 checksum of $image_path..."
    cp $sha1file `dirname $image_path`
    if `$app_bin/sha1sum -c $sha1file`; then
        echo "SHA1 checksum failed, exiting!"
        exit
    else
        echo "Done!"
    fi
fi

# use system or lildebi fsck to check Debian partition
echo ""
find_and_run_fsck


#------------------------------------------------------------------------------#
# mounts

echo ""
echo "> $losetup $loopdev $image_path"
$losetup $loopdev $image_path

# some platforms need to have the ext2 module installed to get ext2 support
if [ -z `grep ext2 /proc/filesystems` ]; then
    echo ""
    echo "Loading ext2 kernel module:"
    modprobe ext2
fi

echo ""
echo "root mount for everything Debian"
# root mount for everything Debian
mount -t `find_best_filesystem` $loopdev $mnt

# check error code for the above mount
mounterr=$?
if [ $mounterr -ne 0 ] ; then
    echo "Mounting '$mnt' failed, returned $mounterr"
    exit
fi

mount -t devpts devpts $mnt/dev/pts
mount -t proc proc $mnt/proc
mount -t sysfs sysfs $mnt/sys
mount -t tmpfs tmpfs $mnt/tmp
mount -o bind $sdcard $mnt/mnt/sdcard

# mount other android mounts, these may vary device to device, so test
# first. These are manually listed out here rather than automatically greped
# from /proc/mounts since some of the mounts should not be mounted as 'bind',
# e.g. things like /proc, /sys, /dev/pts, etc.
test_mount_bind /acct
test_mount_bind /app-cache
test_mount_bind /cache
test_mount_bind /data
test_mount_bind /dbdata
test_mount_bind /dev/cpuctl
test_mount_bind /efs
test_mount_bind /mnt/.lfs
test_mount_bind /mnt/asec
test_mount_bind /mnt/obb
test_mount_bind /mnt/secure/asec
test_mount_bind /mnt/sdcard/external_sd
test_mount_bind /mnt/sdcard/external_sd/.android_secure
test_mount_bind /mnt/secure/.android_secure
test_mount_bind /mnt/shell/emulated
test_mount_bind /pds
test_mount_bind /sqlite_stmt_journals
test_mount_bind /storage/emulated/0
test_mount_bind /storage/emulated/legacy
test_mount_bind /storage/extSdCard
test_mount_bind /storage/sdcard0
test_mount_bind /storage/sdcard1
test_mount_bind /storage/usbdisk
test_mount_bind /sys/kernel/debug
test_mount_bind /system

#------------------------------------------------------------------------------#
# shortcuts for setting up the chroot in the terminal

make_debian_symlink

if [ ! -e /debian/shell ]; then
    echo "installing '/debian/shell' for easy way to get to chroot from term"
    ln -s $app_bin/shell /debian/shell
fi

if [ ! -e /data/local/bin ]; then
    mkdir /data/local/bin
fi

if [ ! -e /data/local/bin/debian ]; then
    ln -s $app_bin/shell /data/local/bin/debian
fi

#
if [ $install_fsck = yes ]; then
    install_e2fsck_static
fi

#------------------------------------------------------------------------------#
# ssh

keygen=/usr/bin/ssh-keygen
if [ -x ${mnt}${keygen} ]; then
    echo ""
    echo "My ssh host key fingerprint and random art:"
    chroot $mnt /bin/bash -c \
        "for key in /etc/ssh/ssh_host_*_key; do $keygen -lv -f \$key; done"
fi

echo ""
echo "Debian chroot mounted and started."
