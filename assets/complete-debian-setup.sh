#!/data/data/info.guardianproject.lildebi/app_bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# get full debug output
set -x

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

test -e $1/lildebi-common || exit
. $1/lildebi-common


# *  install and start sshd so you can easily log in, and before
#    stop/start so the start script starts sshd.  Also,
# * 'policyrcd-script-zg2' sets up the machine for starting and stopping
#    everything via /etc/init.d/rc without messing with the core Android
#    stuff.
# * 'molly-guard' adds a confirmation prompt to poweroff, halt,
#    reboot, and shutdown.
chroot $mnt apt-get -y install --no-install-recommends \
    ssh policyrcd-script-zg2 molly-guard
cp $app_bin/policy-rc.d $mnt/etc/policy-rc.d
chmod 755 $mnt/etc/policy-rc.d

# sometimes the ssh host keys don't get created, so try again
test -e $mnt/etc/ssh/ssh_host_rsa_key || \
    chroot $mnt ssh-keygen -f /etc/ssh/ssh_host_rsa_key -t rsa -N ''
test -e $mnt/etc/ssh/ssh_host_dsa_key || \
    chroot $mnt ssh-keygen -f /etc/ssh/ssh_host_dsa_key -t dsa -N ''

# purge install packages in cache
chroot $mnt apt-get clean

# run 'apt-get upgrade' to get the security updates
chroot $mnt apt-get -y upgrade

# purge upgrade packages in cache
chroot $mnt apt-get clean

echo "Debian is installed and ssh started!"
