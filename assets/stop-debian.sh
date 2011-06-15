#!/data/busybox/sh
#
# see lildebi-common for arguments, the args are converted to vars there. The
# first arg $1 is the "app payload" directory, where the included scripts are
# kept.

test -e $1/lildebi-common || exit
. $1/lildebi-common

# lazy umount
umount -l $mnt$sdcard

$busybox_path/umount -f  $mnt/dev/pts $mnt/proc $mnt/sys $mnt/tmp $mnt$sdcard
