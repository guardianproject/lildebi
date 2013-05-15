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

cp ~/.android/ant.properties .

./update-ant-build.sh
ant release

gpg --detach-sign bin/LilDebi-release.apk
