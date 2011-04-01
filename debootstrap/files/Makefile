NDK_BASE := /usr/local/android-ndk
NDK_PLATFORM_VERSION := 5
NDK_SYSROOT=$(NDK_BASE)/platforms/android-$(NDK_PLATFORM_VERSION)/arch-arm
NDK_UNAME := $(shell uname -s | tr '[A-Z]' '[a-z]')
NDK_TOOLCHAIN_BASE=$(NDK_BASE)/toolchains/arm-linux-androideabi-4.4.3/prebuilt/$(NDK_UNAME)-x86
CC := $(NDK_TOOLCHAIN_BASE)/bin/arm-linux-androideabi-gcc --sysroot=$(NDK_SYSROOT)
CFLAGS += -O6 -funroll-loops -fomit-frame-pointer
LDFLAGS += 
LIBS += -lc
STRIP := $(NDK_TOOLCHAIN_BASE)/bin/arm-linux-androideabi-strip \
	--strip-unneeded -R .note -R .comment


all: pkgdetails

%.o: %.c
	$(CC) $(CFLAGS) -o $*.o -c $*.c

pkgdetails: pkgdetails.o
	$(CC) $(LDFLAGS) -o pkgdetails pkgdetails.o  $(LIBS)
	$(STRIP) pkgdetails

install: pkgdetails
	adb shell mkdir /data/debian/
	adb push pkgdetails /data/debian/

clean:
	-rm -f -- pkgdetails
	-rm -f -- pkgdetails.o

.PHONY: clean
