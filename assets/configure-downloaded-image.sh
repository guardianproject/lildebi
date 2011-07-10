#!/system/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

test -e $1/lildebi-common || exit
. $1/lildebi-common

# create the mount dir
test -e $mnt || mkdir $mnt
