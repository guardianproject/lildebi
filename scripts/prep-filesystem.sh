#!/bin/sh

export PATH=/system/bin

debianroot=/data/debian

# make our Debian tree
mkdir $debianroot
mkdir $debianroot/bin
mkdir $debianroot/etc
mkdir $debianroot/lib
mkdir $debianroot/usr
mkdir $debianroot/var

# make symlinks and mount folders on the rootfs
mount -o remount,rw rootfs /
cd /

# symlink to the Debian dirs
ln -s $debianroot/bin /bin
ln -s $debianroot/lib /lib
ln -s $debianroot/usr /usr
ln -s $debianroot/var /var

# hmm, this could just be made in /dev since its also tmpfs
mkdir /tmp
chmod 1777 /tmp
mount -o rw,nodev,exec,nosuid -t tmpfs tmpfs /tmp
chmod 1777 /tmp

mount -o remount,ro rootfs /

