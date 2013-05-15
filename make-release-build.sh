#!/bin/sh

. ~/.android/bashrc

cd external/busybox
git reset --hard
git clean -fdx
cd ../..
git reset --hard
git clean -fdx
git submodule init
git submodule update
make -C external/ assets

./update-ant-build.sh
ant release

gpg --detach-sign bin/LilDebi-release-unsigned.apk
