#!/bin/sh
#
# see lildebi-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# get full debug output
set -x

# this script runs in Debian, so force the path we use the Debian utilities
# rather than the busybox ones.
app_bin=$1
export PATH=/usr/sbin:/usr/bin:/sbin:/bin
export DEBIAN_FRONTEND=noninteractive

# *  install and start sshd so you can easily log in, and before
#    stop/start so the start script starts sshd.  Also,
# * 'policyrcd-script-zg2' sets up the machine for starting and stopping
#    everything via /etc/init.d/rc without messing with the core Android
#    stuff.
# * 'molly-guard' adds a confirmation prompt to poweroff, halt,
#    reboot, and shutdown.
apt-get -y install --no-install-recommends \
    ssh policyrcd-script-zg2 molly-guard
cp $app_bin/policy-rc.d /etc/policy-rc.d
chmod 0755 /etc/policy-rc.d

# sometimes the ssh host keys don't get created, so try again
test -e /etc/ssh/ssh_host_rsa_key || \
    ssh-keygen -f /etc/ssh/ssh_host_rsa_key -t rsa -N ''
test -e /etc/ssh/ssh_host_dsa_key || \
    ssh-keygen -f /etc/ssh/ssh_host_dsa_key -t dsa -N ''

# purge install packages in cache
apt-get clean

# run 'apt-get upgrade' to get the security updates
apt-get --yes update
apt-get --yes --fix-broken upgrade

# purge upgrade packages in cache
apt-get clean

echo "Debian is installed and ssh started!"
