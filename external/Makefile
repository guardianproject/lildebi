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

# Android NDK setup
NDK_BASE ?=  /usr/local/android-ndk
NDK_PLATFORM_LEVEL ?= 9
NDK_SYSROOT=$(NDK_BASE)/platforms/android-$(NDK_PLATFORM_LEVEL)/arch-arm
NDK_UNAME := $(shell uname -s | tr '[A-Z]' '[a-z]')
NDK_TOOLCHAIN=$(NDK_BASE)/toolchains/arm-linux-androideabi-4.4.3/prebuilt/$(NDK_UNAME)-x86

# to use the real HOST tag, you need the latest libtool files:
# http://stackoverflow.com/questions/4594736/configure-does-not-recognize-androideabi
#HOST := arm-none-linux-gnueabi
HOST := arm-linux-androideabi

# install root for built files
DESTDIR = $(CWD)
# TODO try adding the Android-style /data/app.name here
prefix = /data/data/info.guardianproject.lildebi/app_opt
LOCAL := $(DESTDIR)$(prefix)

PATH := ${PATH}:$(NDK_TOOLCHAIN)/bin:$(LOCAL)/bin

CC := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-gcc --sysroot=$(NDK_SYSROOT)
CXX := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-g++
CPP := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-cpp
LD := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-ld
AR := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-ar
RANLIB := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-ranlib
STRIP := $(NDK_TOOLCHAIN)/bin/arm-linux-androideabi-strip \
	--strip-unneeded -R .note -R .comment

ALL_CFLAGS = -DANDROID -I$(LOCAL)/include
ALL_LDFLAGS = -L$(LOCAL)/lib -Wl,--rpath,$(LOCAL)/lib

# build as small as possible, mostly useful for static binaries
ALL_CFLAGS += -fdata-sections -ffunction-sections -Os
ALL_LDFLAGS += -Wl,--gc-sections


.PHONY = clean distclean assets

all: $(EXTERNAL_ROOT)/pkgdetails/pkgdetails

assets: all
	install $(EXTERNAL_ROOT)/pkgdetails/pkgdetails $(ASSETS)/pkgdetails
	$(STRIP) $(ASSETS)/pkgdetails


$(EXTERNAL_ROOT)/pkgdetails/pkgdetails: $(EXTERNAL_ROOT)/pkgdetails/pkgdetails.c
	$(CC) $(ALL_CFLAGS) $(CFLAGS) $(ALL_LDFLAGS) $(LDFLAGS) $< -o $@


clean: 
	rm -f -- $(EXTERNAL_ROOT)/pkgdetails/pkgdetails

distclean: clean
	rm -f -- $(ASSETS)/pkgdetails
