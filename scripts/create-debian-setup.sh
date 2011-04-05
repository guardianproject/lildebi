#!/system/bin/sh

export PATH=/system/bin:/system/xbin:$PATH

# TODO switch to . debian-variables

mnt=/data/debian # in /dev because /data is mounted 'nodev'
sdcard=/mnt/sdcard
imagefile=$sdcard/debian.img
loopdev=/dev/block/loop4

app_payload=$sdcard/lildebi
imagesize=159999999
repo=http://ftp.us.debian.org/debian
distro=stable

sh_debootstrap="/system/bin/sh $mnt/usr/sbin/debootstrap"

# so that the debootstrap script can find its files
export DEBOOTSTRAP_DIR=$mnt/usr/share/debootstrap

# create the image file
echo "dd if=/dev/zero of=$imagefile seek=$imagesize bs=1 count=1"
dd if=/dev/zero of=$imagefile seek=$imagesize bs=1 count=1
# create the mount dir
test -e $mnt || mkdir $mnt
# set them up
if test -d $mnt && test -e $imagefile; then
    mke2fs -L debian_chroot -F $imagefile
    losetup $loopdev $imagefile
    mount -o loop,relatime,errors=remount-ro $loopdev $mnt
    cd $mnt
    tar xjf $app_payload/usr-share-debootstrap.tar.bz2
    cp $app_payload/pkgdetails $DEBOOTSTRAP_DIR/pkgdetails
    chmod 755 $DEBOOTSTRAP_DIR/pkgdetails
else
    echo "No mount dir found ($mnt) or no imagefile ($imagefile)"
    exit 1
fi

$sh_debootstrap --verbose --arch armel --foreign $distro $mnt $repo
chroot $mnt debootstrap --second-stage

# create mountpoints
test -e $mnt/dev || mkdir $mnt/dev
test -e $mnt/dev/pts || mkdir $mnt/dev/pts
test -e $mnt/mnt || mkdir $mnt/mnt
test -e $mnt/mnt/sdcard || mkdir $mnt/mnt/sdcard
test -e $mnt/proc || mkdir $mnt/proc
test -e $mnt/sys || mkdir $mnt/sys
test -e $mnt/tmp || mkdir $mnt/tmp

# create /etc/resolv.conf
test -e $mnt/etc || mkdir $mnt/etc
touch $mnt/etc/resolv.conf
echo 'nameserver 4.2.2.2' >> $mnt/etc/resolv.conf
echo 'nameserver 8.8.8.8' >> $mnt/etc/resolv.conf
echo 'nameserver 198.6.1.1' >> $mnt/etc/resolv.conf

# create /etc/hosts
echo '127.0.0.1    localhost' > $mnt/etc/hosts

# create live mtab
test -e $mnt/etc/mtab && rm $mnt/etc/mtab
ln -s /proc/mounts $mnt/etc/mtab

# apt sources
test -e $mnt/etc/apt || mkdir $mnt/etc/apt
echo "deb $repo $distro main" > $mnt/etc/apt/sources.list
