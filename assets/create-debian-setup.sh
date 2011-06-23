#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

test -e $1/lildebi-common || exit
. $1/lildebi-common

sh_debootstrap="/system/bin/sh $mnt/usr/sbin/debootstrap"

# so we can find busybox tools
export PATH=$busybox_path:/system/bin:/system/xbin:$PATH
# so that the debootstrap script can find its files
export DEBOOTSTRAP_DIR=$mnt/usr/share/debootstrap

#------------------------------------------------------------------------------#
# setup busybox
if [ ! -e $busybox_path ]; then
    mkdir $busybox_path
    cp $dataDir/busybox $busybox
    chmod 755 $busybox
    cd $busybox_path && ./busybox --install
# this busybox's wget is not as good as the CyanogenMod wget, I think the
# difference is HTTPS support
    rm $busybox_path/wget
fi

#------------------------------------------------------------------------------#
# set /bin to busybox utils
if [ ! -e /bin ]; then
    mount -o remount,rw rootfs /
    cd /
    ln -s /data/busybox /bin
    mount -o remount,ro rootfs /
fi

#------------------------------------------------------------------------------#
# create the image file
echo "dd if=/dev/zero of=$imagefile seek=$imagesize bs=1M count=1"
test -e $imagefile || \
    dd if=/dev/zero of=$imagefile seek=$imagesize bs=1M count=1
# create the mount dir
test -e $mnt || mkdir $mnt
# set them up
if test -d $mnt && test -e $imagefile; then
    mke2fs -L debian_chroot -F $imagefile
    losetup $loopdev $imagefile
    mount -o loop,noatime,errors=remount-ro $loopdev $mnt || exit
    cd $mnt
    tar xjf $dataDir/usr-share-debootstrap.tar.bz2
    cp $dataDir/pkgdetails $DEBOOTSTRAP_DIR/pkgdetails
    chmod 755 $DEBOOTSTRAP_DIR/pkgdetails
else
    echo "No mount dir found ($mnt) or no imagefile ($imagefile)"
    exit 1
fi

#------------------------------------------------------------------------------#
# run debootstrap in two stages

$sh_debootstrap --verbose --arch armel --foreign $release $mnt $mirror

# how we're in the chroot, so we don't need to set DEBOOTSTRAP_DIR, but we do
# need a more Debian-ish PATH
unset DEBOOTSTRAP_DIR
# set path to Debian-style for chrooted commands
export PATH=/usr/sbin:/usr/bin:/sbin:/bin

chroot $mnt /debootstrap/debootstrap --second-stage

#------------------------------------------------------------------------------#
# create mountpoints
create_mountpoint() {
    test -d $1 && test -e $mnt/$1 || \
        mkdir $mnt/$1
}

# standard GNU/Linux mounts
create_mountpoint /dev
create_mountpoint /dev/pts
create_mountpoint /media
create_mountpoint /mnt
create_mountpoint /mnt/sdcard
create_mountpoint /proc
create_mountpoint /sys
create_mountpoint /tmp
# Android mounts
create_mountpoint /data
create_mountpoint /system
create_mountpoint /cache
create_mountpoint /dev/cpuctl
create_mountpoint /acct
create_mountpoint /mnt/obb
create_mountpoint /mnt/asec
create_mountpoint /mnt/secure
create_mountpoint /mnt/secure/asec
create_mountpoint /mnt/secure/.android_secure
create_mountpoint /sqlite_stmt_journals
create_mountpoint /app-cache

#------------------------------------------------------------------------------#
# create configs

# create /etc/resolv.conf
test -e $mnt/etc || mkdir $mnt/etc
touch $mnt/etc/resolv.conf
echo 'nameserver 4.2.2.2' >> $mnt/etc/resolv.conf
echo 'nameserver 8.8.8.8' >> $mnt/etc/resolv.conf
echo 'nameserver 198.6.1.1' >> $mnt/etc/resolv.conf

# create /etc/hosts
cp /etc/hosts $mnt/etc/hosts

# create live mtab
test -e $mnt/etc/mtab && rm $mnt/etc/mtab
ln -s /proc/mounts $mnt/etc/mtab

# apt sources
test -e $mnt/etc/apt || mkdir $mnt/etc/apt
touch $mnt/etc/apt/sources.list
echo "deb $mirror $release main" >> $mnt/etc/apt/sources.list
echo "deb http://security.debian.org/ $release/updates main" >> $mnt/etc/apt/sources.list

chroot $mnt apt-get update

# install and start sshd so you can easily log in
chroot $mnt apt-get -y install ssh

# stop and restart setup to make sure everything is mounted, etc.
$dataDir/stop-debian.sh
$dataDir/start-debian.sh

# purge install packages in cache
chroot $mnt apt-get autoclean

# run 'apt-get upgrade' to get the security updates
chroot $mnt apt-get -y upgrade

# purge upgrade packages in cache
chroot $mnt apt-get autoclean

echo "Debian is installed and ssh started!"
