
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
       texinfo ant

Install the Android NDK for the command line version, and the Android SDK for
the Android app version:

SDK: http://developer.android.com/sdk/
NDK: http://developer.android.com/sdk/ndk/


Original Sources
================

debootstrap
-----------

http://packages.debian.org/unstable/debootstrap

This package was extracted, the usr/sbin/debootstrap script placed into
assets/ and the usr/share/debootstrap folder tar-bzipped into the included
tarball usr-share-debootstrap.tar.bz2.


pkgdetails
----------

pkgdetails comes from OpenWRT's debootstrap, and is built using their build
system:

https://dev.openwrt.org/browser/packages/utils/debootstrap/files/pkgdetails.c


busybox
-------

The goal is to provide a minimal busybox custom built from source, but
currently, we are using the busybox for Android binaries from:

http://benno.id.au/blog/2007/11/14/android-busybox


gpgv
----

This is built from source using the gnupg-for-android build system.  The
binary ends up in external/data/ called gpgv2-static.  To build it yourself,
follow the included instructions.

https://github.com/guardianproject/gnupg-for-android

