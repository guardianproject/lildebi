# Please install the following prerequisites (instructions for each follows):
# 	Android OS SDK: http://source.android.com/download
#
# Install and prepare the Android OS SDK ( http://source.android.com/download )
# on Debian or Ubuntu

### these modify the calling shell
# point pkg-config to the .pc files generated from these builds
export PKG_CONFIG_PATH=$(LOCAL)/lib/pkgconfig
# workaround for cross-compiling bug in autoconf
export ac_cv_func_malloc_0_nonnull=yes

CWD = $(shell pwd)
PROJECT_ROOT = $(CWD)/..
EXTERNAL_ROOT = $(PROJECT_ROOT)/external
ASSETS = $(PROJECT_ROOT)/assets

# Android now has 64-bit and 32-bit versions of the NDK for GNU/Linux.  We
# assume that the build platform uses the appropriate version, otherwise the
# user building this will have to manually set NDK_PROCESSOR or NDK_TOOLCHAIN.
CPU := $(shell uname -m)
ifeq ($(CPU),x86_64)
 NDK_PROCESSOR=x86_64
else
 NDK_PROCESSOR=x86
endif

# Android NDK setup
NDK_BASE ?= /opt/android-ndk
NDK_ABI=arm
NDK_TOOLCHAIN_VERSION=4.8
# NDK platform level, aka APP_PLATFORM, is equivalent to minSdkVersion
NDK_PLATFORM_LEVEL ?= \
	$(shell sed -n 's,.*android:minSdkVersion="\([0-9][0-9]*\)".*,\1,p' $(PROJECT_ROOT)/AndroidManifest.xml)

NDK_SYSROOT=$(NDK_BASE)/platforms/android-$(NDK_PLATFORM_LEVEL)/arch-$(NDK_ABI)
NDK_UNAME := $(shell uname -s | tr '[A-Z]' '[a-z]')
NDK_TOOLCHAIN = $(NDK_ABI)-linux-androideabi-$(NDK_TOOLCHAIN_VERSION)
NDK_TOOLCHAIN_BASE=$(NDK_BASE)/toolchains/$(NDK_TOOLCHAIN)/prebuilt/$(NDK_UNAME)-$(NDK_PROCESSOR)

# to use the real HOST tag, you need the latest libtool files:
# http://stackoverflow.com/questions/4594736/configure-does-not-recognize-androideabi
#HOST := arm-none-linux-gnueabi
HOST := arm-linux-androideabi

PATH := ${PATH}:$(NDK_TOOLCHAIN_BASE)/bin

CC := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-gcc --sysroot=$(NDK_SYSROOT)
CXX := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-g++
CPP := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-cpp
LD := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-ld
AR := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-ar
RANLIB := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-ranlib
STRIP := $(NDK_TOOLCHAIN_BASE)/bin/$(HOST)-strip \
	--strip-unneeded -R .note -R .comment

# build as small as possible, mostly useful for static binaries
ALL_CFLAGS = -fdata-sections -ffunction-sections -Os
ALL_LDFLAGS = -Wl,--gc-sections

# faketime needs to have a very specific timestamp format in order to freeze
# time.  The time needs to be frozen so that the timestamps don't depend on
# the speed of the machine that the build process is running on.  See `man
# faketime` for more info on the "advanced timestamp format".  Also, force
# time to UTC so its always the same on all machines.
TIMESTAMP="$(shell faketime "`git log -n1 --format=format:%ai`" \
				date --utc '+%Y-%m-%d %H:%M:%S')"
export TZ=UTC


.PHONY: all assets clean distclean

all: $(EXTERNAL_ROOT)/busybox/busybox $(EXTERNAL_ROOT)/gnupg/g10/gpgv
	make -C $(EXTERNAL_ROOT)/cdebootstrap

assets: all
	install $(EXTERNAL_ROOT)/busybox/busybox \
		$(EXTERNAL_ROOT)/cdebootstrap/cdebootstrap.tar \
		$(EXTERNAL_ROOT)/gnupg/g10/gpgv \
		$(ASSETS)/


$(EXTERNAL_ROOT)/busybox/.config: $(EXTERNAL_ROOT)/busybox_config
	cp $(EXTERNAL_ROOT)/busybox_config $(EXTERNAL_ROOT)/busybox/.config
	sed -i "s|^CONFIG_SYSROOT=.*|CONFIG_SYSROOT=\"$(NDK_SYSROOT)\"|" \
		$(EXTERNAL_ROOT)/busybox/.config

$(EXTERNAL_ROOT)/busybox/busybox: $(EXTERNAL_ROOT)/busybox/.config
# I haven't found a reliable way to install/uninstall a patch from a Makefile,
# so just always try to apply it, and ignore it if it fails. Works fine unless
# the files being patched have changed, in which cause a partial application
# could happen unnoticed.
	-cd $(EXTERNAL_ROOT)/busybox/ && \
		patch -N -p1 --reject-file=- < ../003-mount-umount-fsck-df.patch
	echo "TIMESTAMP: $(TIMESTAMP)"
	faketime -f $(TIMESTAMP) make -C $(EXTERNAL_ROOT)/busybox/


$(EXTERNAL_ROOT)/gnupg/configure: $(EXTERNAL_ROOT)/gnupg/configure.ac
	cd $(EXTERNAL_ROOT)/gnupg && \
		./autogen.sh --force

$(EXTERNAL_ROOT)/gnupg/Makefile: $(EXTERNAL_ROOT)/gnupg/configure
	cd $(EXTERNAL_ROOT)/gnupg && \
		SYSROOT=$(NDK_SYSROOT) \
		AR="$(AR)" \
		CC="$(CC)" \
		LD="$(LD)" \
		CFLAGS="$(ALL_CFLAGS)" \
		LDFLAGS="$(ALL_LDFLAGS) -static" \
			./configure \
				--host=$(HOST) \
				--disable-agent-support \
				--disable-bzip2 \
				--disable-card-support \
				--disable-exec \
				--disable-keyserver-helpers \
				--disable-hkp \
				--disable-ldap \
				--disable-nls \
				--disable-photo-viewers

$(EXTERNAL_ROOT)/gnupg/g10/gpgv: $(EXTERNAL_ROOT)/gnupg/Makefile
	make -C $(EXTERNAL_ROOT)/gnupg/cipher
	make -C $(EXTERNAL_ROOT)/gnupg/mpi
	make -C $(EXTERNAL_ROOT)/gnupg/util
	make -C $(EXTERNAL_ROOT)/gnupg/zlib
	make -C $(EXTERNAL_ROOT)/gnupg/g10
	$(STRIP) $(EXTERNAL_ROOT)/gnupg/g10/gpgv

# to re-download and verify that the .debs included in git match the official
# Debian ones
verify-debian-packages: clean
	make -C $(EXTERNAL_ROOT)/debian-archive-keyring
	-rm $(EXTERNAL_ROOT)/cdebootstrap/cdebootstrap-static*.deb
	make -C $(EXTERNAL_ROOT)/cdebootstrap

clean:
	rm -f -- $(EXTERNAL_ROOT)/busybox/.config
	rm -f -- $(EXTERNAL_ROOT)/busybox/busybox
	-cd $(EXTERNAL_ROOT)/busybox/ && \
		patch -N -R -p1 --reject-file=- < ../003-mount-umount-fsck-df.patch
	-make -C $(EXTERNAL_ROOT)/gnupg/ clean
	make -C $(EXTERNAL_ROOT)/busybox/ clean
	make -C $(EXTERNAL_ROOT)/cdebootstrap/ clean
	make -C $(EXTERNAL_ROOT)/debian-archive-keyring/ clean

distclean: clean
	rm -f -- $(ASSETS)/busybox
	rm -f -- $(ASSETS)/cdebootstrap.tar
