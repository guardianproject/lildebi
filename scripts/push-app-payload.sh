#!/bin/sh

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

adb -d push ${scripts}/lildebi-common $app_payload/
adb -d shell chmod 644 $app_payload/lildebi-common
adb -d push ${scripts}/create-debian-setup.sh $app_payload/
adb -d shell chmod 644 $app_payload/create-debian-setup.sh
adb -d push ${scripts}/usr-share-debootstrap.tar.bz2 $app_payload/
adb -d shell chmod 644 $app_payload/usr-share-debootstrap.tar.bz2

adb -d push ${scripts}/start-debian.sh $app_payload/
adb -d shell chmod 755 $app_payload/start-debian.sh

adb -d push ${binaries}/busybox $app_payload/
adb -d shell chmod 755 $app_payload/busybox

make -C ${debootstrap}/files pkgdetails
adb -d push ${debootstrap}/files/pkgdetails $app_payload/
adb -d shell chmod 755 $app_payload/pkgdetails
