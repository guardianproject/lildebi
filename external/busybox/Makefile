
PKG_NAME:=busybox
PKG_VERSION:=1.18.4
PKG_DIR:=$(PKG_NAME)-$(PKG_VERSION)

PKG_SOURCE:=$(PKG_DIR).tar.bz2
PKG_SOURCE_URL:= http://www.busybox.net/downloads
PKG_MD5SUM:=b03c5b46ced732679e525a920a1a62f5

NDK_BASE := /usr/local/android-ndk
NDK_PLATFORM_VERSION := 8
NDK_SYSROOT=$(NDK_BASE)/platforms/android-$(NDK_PLATFORM_VERSION)/arch-arm
NDK_UNAME := $(shell uname -s | tr '[A-Z]' '[a-z]')
NDK_TOOLCHAIN_BASE=$(NDK_BASE)/toolchains/arm-linux-androideabi-4.4.3/prebuilt/$(NDK_UNAME)-x86
CROSS_COMPILE := arm-linux-androideabi-
CC := $(NDK_TOOLCHAIN_BASE)/bin/$(CROSS_COMPILE)gcc --sysroot=$(NDK_SYSROOT) \
	-U__linux__ -DHAVE_SYS_STATFS_H -DHAVE_MNTENT_H
# -include $(NDK_SYSROOT)/usr/include/linux/user.h
LIBS += -lc


all: $(PKG_DIR)/busybox

$(PKG_SOURCE):
	wget $(PKG_SOURCE_URL)/$(PKG_SOURCE)

$(PKG_DIR): $(PKG_SOURCE)
	tar -xjf $(PKG_SOURCE)

$(PKG_DIR)/.config: $(PKG_DIR)
	cp lildebi-busybox.config $(PKG_DIR)/.config

$(PKG_DIR)/busybox: $(PKG_DIR)/.config
	make -C $(PKG_DIR) CROSS_COMPILE=$(CROSS_COMPILE) CC="$(CC)" LDFLAGS="--static" V=1

clean:
	make -C $(PKG_DIR) clean

distclean: clean
	-rm -rf -- $(PKG_DIR)
	-rm -f -- $(PKG_SOURCE)

.PHONY: unpack clean
