
Lil' Debi
=========

This is an app to setup and manage a Debian install in parallel on an Android
phone.  It uses debootstrap to build up the disk image as a chroot, and then
provides start and stop methods for handling mounting, fsck, starting/stopping
sshd, etc.

It is 100% free software, but its also alpha, so we are still sorting out the
build process for all the bits.  A couple of the binaries provided in the
assets/ folder are binaries from other free software projects.

Ultimately, our aim is to have the whole build process for every bit of this
app documented so that it can be freely inspected, modified, ported, etc.  We
want this app to build a trusted Debian install on the phone, so free software
is the only way to get there.  This is currently functional alpha software, so
do not rely on it to produce a trusted Debian install.  Please do try it out,
use it, and report criticisms, bugs, improvements, etc.


Build Setup
===========

On Debian/Ubuntu/Mint/etc.:

  sudo apt-get install autoconf automake libtool transfig wget patch \
       texinfo ant make openjdk-6-jdk

Both the Android SDK and the Android NDK are needed:

SDK: http://developer.android.com/sdk/
NDK: http://developer.android.com/sdk/ndk/


Building
========

Building Lil' Debi is a multi-step process including clone the sources,
getting busybox code as a submodule, building the native utilities, and then
finally building the Android app.  Here are all those steps in a form to run
in the terminal:

  git clone https://github.com/guardianproject/lildebi
  cd lildebi
  git submodule init
  git submodule update
  make -C external assets
  ./update-ant-build.sh
  ant debug

Once that has completed, you can install it however you would normally install
an .apk file.  You will find the .apk in the bin/ folder.  An easy way to
install it via the terminal is to run:

  adb install bin/LilDebi-debug.apk


Original Sources
================

debootstrap
-----------
http://packages.debian.org/unstable/debootstrap

debootstrap is downloaded directly from Debian, extracted, patched, and then
tar-bzipped into the included tarball assets/debootstrap.tar.bz2. See
external/debootstrap/Makefile for details.


pkgdetails
----------
https://dev.openwrt.org/browser/packages/utils/debootstrap/files/pkgdetails.c

pkgdetails comes from OpenWRT's debootstrap and is included in this git repo
and built using external/Makefile


busybox
-------
git://busybox.net/busybox.git

busybox is included as a git submodule and built from source by
externals/Makefile using a custom config file.


gpgv
----
https://github.com/guardianproject/gnupg-for-android

Building gpgv for Android is quite complicated, so the binary is included in
this project. The binary is built from source using the gnupg-for-android
build system.  The binary ends up in external/data/ called gpgv2-static.  To
build it yourself, follow the included instructions.

