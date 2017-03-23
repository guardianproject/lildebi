#!/data/data/info.guardianproject.lildebi/app_bin/sh

# get full debug output
set -x

apt-get update
apt-get install -y fakeroot libc-bin dpkg x11-xserver-utils xfce4-panel xfdesktop4 xfwm4 xfce4-session thunar xfconf xfce4-settings tango-icon-theme apt synaptic apt-xapian-index xterm locales fonts-droid libgl1-mesa-swx11

# copy the start script to root home folder
cp /data/data/info.guardianproject.lildebi/app_bin/start-xfce.sh /root/
