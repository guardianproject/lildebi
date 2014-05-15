
Lil' Debi
=========

This is an app to setup and manage a Debian install in parallel on an Android
phone.  It can build a Debian install from scratch or use an existing image.
It manages the starting and stopping of the Debian install.

It uses cdebootstrap to build up the disk image as a chroot, and then provides
start and stop methods for handling mounting, fsck, starting/stopping sshd,
etc.

It is 100% free software. Ultimately, our aim is to have the whole
process for every bit of this app documented so that it can be freely
inspected, modified, ported, etc.  We want this app to build a trusted Debian
install on the phone, so free software is the only way to get there.  This is
currently functional alpha software, so do not rely on it to produce a trusted
Debian install.  Please do try it out, use it, and report criticisms, bugs,
improvements, etc.


Installing Debian
=================

The process of installing Debian with Lil' Debi is self-explanatory, just run
the app and click the Install... button.  But it doesn't yet work on all
phones.  If the install process fails on your phone, you can still use Lil'
Debi by downloading a pre-built Debian image.  It should work with any
Debian/Ubuntu/Mint armel image file.  Here is a Debian image file that was
built by Lil' Debi:

https://github.com/guardianproject/lildebi/downloads

Download the file, uncompress it and rename it 'debian.img' and copy it to
your SD Card.  Launch Lil' Debi, and you should now see the button says "Start
Debian".  Click the button to start your new Debian install.


Build Setup
===========

On Debian/Ubuntu/Mint/etc.:

  sudo apt-get install autoconf automake libtool transfig wget patch \
       texinfo ant make openjdk-7-jdk faketime

On Mac OS X, you will need Fink, MacPorts, or Brew to install some of these
dependencies.  For example, GNU tar is required, OS X's tar will not work.
Also, faketime is needed to make repeatable builds of busybox.

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
  make NDK_BASE=/path/to/your/android-ndk -C external assets
  ./update-ant-build.sh
  ant debug

Once that has completed, you can install it however you would normally install
an .apk file.  You will find the .apk in the bin/ folder.  An easy way to
install it via the terminal is to run:

  adb install bin/LilDebi-debug.apk


NDK build options
-----------------

The following options can be set from the make command line to tailor the NDK
build to your setup:

 * NDK_BASE             (/path/to/your/android-ndk)
 * NDK_PLATFORM_LEVEL   (7-17 as in android-17)
 * NDK_ABI              (arm, mips, x86)
 * NDK_COMPILER_VERSION (4.4.3, 4.6, 4.7, clang3.1, clang3.2)
 * HOST                 (arm-linux-androideabi, mipsel-linux-android, x86)


Original Sources
================

cdebootstrap
-----------
http://packages.debian.org/unstable/cdebootstrap

cdebootstrap is downloaded directly from Debian, extracted, and then
tar'ed into the included tarball assets/cdebootstrap.tar. See
external/cdebootstrap/Makefile for details.


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
