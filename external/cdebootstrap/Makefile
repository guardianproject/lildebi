
PKG_NAME:=cdebootstrap-static
PKG_VERSION:=0.5.10

PKG_SOURCE:=$(PKG_NAME)_$(PKG_VERSION)_armel.deb
PKG_SOURCE_URL:=http://ftp.debian.org/debian/pool/main/c/cdebootstrap

DL_DIR=$(PKG_NAME)-$(PKG_VERSION)

all: cdebootstrap.tar.bz2


cdebootstrap.tar.bz2: $(DL_DIR)/usr/bin/cdebootstrap-static
	cd $(DL_DIR) && \
		tar cjpf ../cdebootstrap.tar.bz2 usr/

$(DL_DIR)/usr/bin/cdebootstrap-static: $(DL_DIR)/$(PKG_SOURCE)
	cd $(DL_DIR) && \
		(ar -p $(PKG_SOURCE) data.tar.gz | \
			tar --exclude=usr/share/man --exclude=usr/share/doc -xzf -)

$(DL_DIR)/$(PKG_SOURCE): $(DL_DIR)
	cd $(DL_DIR) && \
		wget --continue $(PKG_SOURCE_URL)/$(PKG_SOURCE)
	md5sum -c $(PKG_SOURCE).md5
	sha1sum -c $(PKG_SOURCE).sha1
	sha256sum -c $(PKG_SOURCE).sha256

$(DL_DIR):
	mkdir $(DL_DIR)


clean:
	-rm -rf -- $(DL_DIR)
	-rm -f -- cdebootstrap.tar.bz2

.PHONY: clean
