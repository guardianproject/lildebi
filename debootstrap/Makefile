
PKG_NAME:=debootstrap
PKG_VERSION:=1.0.28
PKG_RELEASE:=1

PKG_SOURCE:=$(PKG_NAME)-udeb_$(PKG_VERSION)_all.udeb
PKG_SOURCE_URL:=http://ftp.debian.org/debian/pool/main/d/debootstrap
PKG_MD5SUM:=dee0dc77573d88af80b225d8810ab7a1

DL_DIR=download

$(DL_DIR):
	mkdir $(DL_DIR)

$(PKG_SOURCE): $(DL_DIR)
	cd $(DL_DIR) && \
		wget $(PKG_SOURCE_URL)/$(PKG_SOURCE)

unpack: $(PKG_SOURCE)
	cd $(DL_DIR) && \
		(ar -p $(PKG_SOURCE) data.tar.gz | tar -xzf -)

clean:
	-rm -rf -- download

.PHONY: unpack clean
