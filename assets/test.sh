#!/data/data/info.guardianproject.lildebi/app_bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

echo "--------------------------------------------------"
echo "./test.sh"

echo $1/lildebi-common
test -e $1/lildebi-common || exit
. $1/lildebi-common

echo "app_bin: $app_bin"
echo "sdcard: $sdcard"
echo "image_path: $image_path"
echo "mnt: $mnt"
echo "release: $release"
echo "mirror: $mirror"
echo "imagesize: $imagesize"
