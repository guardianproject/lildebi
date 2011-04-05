#!/system/bin/sh

export PATH=/system/bin:/system/xbin:$PATH

mnt=/data/debian # in /dev because /data is mounted 'nodev'
sdcard=/mnt/sdcard
app_payload=$sdcard/lildebi
imagefile=$sdcard/debian.img
imagesize=159999999
loopdev=/dev/block/loop4
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

test -e $mnt/etc/apt || mkdir -p $mnt/etc/apt
echo "deb $repo $distro main" > $mnt/etc/apt/sources.list

# create /etc/resolv.conf
touch $mnt/etc/resolv.conf
echo 'nameserver 4.2.2.2' >> $mnt/etc/resolv.conf
echo 'nameserver 8.8.8.8' >> $mnt/etc/resolv.conf
echo 'nameserver 198.6.1.1' >> $mnt/etc/resolv.conf

# create /etc/hosts
echo '127.0.0.1    localhost' > $mnt/etc/hosts
