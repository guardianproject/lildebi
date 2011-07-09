#!/bin/sh
#
# This script is meant to be run within Debian to remove all of the init
# scripts that deal with the final shutdown steps.  This allows Lil' Debi to
# call "/etc/init.d/rc 0" to stop the Debian chroot without shutting down the
# whole phone.

for script in halt reboot hwclock.sh sendsigs umountfs umountroot; do
    echo "removing $script:"
    /usr/sbin/update-rc.d -f $script remove
done
