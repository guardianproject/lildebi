
# Lil' Debi #

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


### Build Setup ###

-------------------

On Debian/Ubuntu/Mint/etc.:

    sudo apt-get install autoconf automake libtool transfig wget patch \
      texinfo ant make openjdk-6-jdk

Both the Android SDK and the Android NDK are needed:

    SDK: http://developer.android.com/sdk/
    NDK: http://developer.android.com/sdk/ndk/

Follow the installation instructions provided by developer.android.com for
each.

*The NDK should be installed in /usr/local/android-ndk, otherwise you'll need
to specify its location when running the externals Makefile below.*

Export the SDK_BASE environment variable:

    export SDK_BASE=/path/to/sdk/tools

It's also advisable to add the tools/platform-tools directories to your PATH,
for easy access to CLI commands:

    export PATH=$PATH:/path/to/sdk/tools:/path/to/sdk/platform-tools

### Build ###

-------------

*From the root of the checkout...*

Pull in the busybox external:

    git submodule init
    git submodule update

Build the externals:

    cd external
    make
    cd ..

Copy the externals into place in the assets directory:

    cp external/busybox/busybox external/pkgdetails/pkgdetails \
       external/debootstrap/debootstrap-1.0.26+squeeze1/usr/sbin/debootstrap \
       external/debootstrap/usr-share-debootstrap.tar.bz2 assets/

Run the update script to generate the ant build file:

    ./update-ant-build.sh

Build the Android package:

    ant debug

### Installation ###

--------------------

    adb install bin/LilDebi-debug.apk


## Original Sources ##

### debootstrap ###

-------------------

http://packages.debian.org/unstable/debootstrap

This package was extracted, the usr/sbin/debootstrap script placed into
assets/ and the usr/share/debootstrap folder tar-bzipped into the included
tarball usr-share-debootstrap.tar.bz2.


### pkgdetails ###

------------------

pkgdetails comes from OpenWRT's debootstrap, and is built using their build
system:

https://dev.openwrt.org/browser/packages/utils/debootstrap/files/pkgdetails.c


### busybox ###

---------------

The goal is to provide a minimal busybox custom built from source, but
currently, we are using the busybox for Android binaries from:

http://benno.id.au/blog/2007/11/14/android-busybox


### gpgv ###

------------

This is built from source using the gnupg-for-android build system.  The
binary ends up in external/data/ called gpgv2-static.  To build it yourself,
follow the included instructions.

https://github.com/guardianproject/gnupg-for-android

