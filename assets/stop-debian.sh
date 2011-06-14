#!/data/busybox/sh

scripts=$(cd `dirname $0` && pwd)
if [ -x "$scripts" ]; then
    . $scripts/lildebi-common
    binaries="${scripts}/../binaries"
    debootstrap="${scripts}/../debootstrap"
else
    . ./lildebi-common
    binaries=../binaries
    debootstrap=../debootstrap/
fi

umount -l $mnt$sdcard

/data/busybox/umount -f  $mnt/dev/pts $mnt/proc $mnt/sys $mnt/tmp $mnt$sdcard
