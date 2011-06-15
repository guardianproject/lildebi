#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

echo $1/lildebi-common
test -e $1/lildebi-common || exit
. $1/lildebi-common

echo "GO!" > $dataDir/lildebi.txt

echo "dataDir: $dataDir" >> $dataDir/lildebi.txt
echo "sdcard: $sdcard" >> $dataDir/lildebi.txt
echo "imagefile: $imagefile" >> $dataDir/lildebi.txt
echo "mnt: $mnt" >> $dataDir/lildebi.txt
echo "distro: $distro" >> $dataDir/lildebi.txt
echo "mirror: $mirror" >> $dataDir/lildebi.txt
echo "imagesize: $imagesize" >> $dataDir/lildebi.txt
