#!/bin/sh

. ~/.android/bashrc

TIMESTAMP=`git log -n1 --date=iso | sed -n 's,^Date:\s\s*\(.*\),\1,p'`

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
faketime "$TIMESTAMP" ant release

gpg --detach-sign bin/LilDebi-release.apk
