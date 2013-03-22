#!/data/data/info.guardianproject.lildebi/app_bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# get full debug output
set -x

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:/system/bin:/system/xbin:$PATH

test -e $1/lildebi-common || exit
. $1/lildebi-common

# run fsck to set up ext3 journaling, if it was configed successfully. Try the
# safe fsck first, then force it. Otherwise this install will be dead in the
# water if fsck throws a "manual intervention" error.
find_and_run_fsck -pfv
find_and_run_fsck -y
