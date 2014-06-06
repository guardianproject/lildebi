#!/bin/sh

if [ -z $ANDROID_HOME ]; then
    if [ -e ~/.android/bashrc ]; then
        . ~/.android/bashrc
    else
        echo "ANDROID_HOME must be set!"
        exit
    fi
fi

TIMESTAMP=`git log -n1 --date=iso | sed -n 's,^Date:\s\s*\(.*\),\1,p'`

git reset --hard
git clean -fdx
git submodule foreach --recursive git reset --hard
git submodule foreach --recursive git clean -fdx
git submodule sync --recursive
git submodule foreach --recursive git submodule sync
git submodule update --init --recursive

make -C external/ assets

cp ~/.android/ant.properties .

./update-ant-build.sh
faketime "$TIMESTAMP" ant release

gpg --detach-sign bin/LilDebi-release.apk
