#!/data/data/info.guardianproject.lildebi/app_bin/sh

# get full debug output
set -x

export PATH=/usr/sbin:/usr/bin:/sbin:/bin:/system/sbin:/system/bin:/system/xbin
export DISPLAY=127.0.0.1:0
rm -f /var/run/dbus/pid
dbus-daemon --system &
sleep 1
dbus-launch --exit-with-session xfce4-session &

# NOTES:
# Before running this script
# 1) install https://play.google.com/store/apps/details?id=x.org.server application
# 2) open this application and select the display configuration
# 3) After that you can run this script or set it as post-start script in Lil'Debi application
