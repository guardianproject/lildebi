cd /
ls
export TERM=linux
export HOME=/root
# set the path to use the included utils first
export PATH=$app_bin:/usr/bin:/usr/sbin:/bin:/sbin:$PATH

apt-get install ssh -y
