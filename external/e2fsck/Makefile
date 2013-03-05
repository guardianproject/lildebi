
PKG_NAME:=e2fsck-static
PKG_VERSION:=1.41.12
PKG_RELEASE:=4stable1

PKG_SOURCE:=$(PKG_NAME)_$(PKG_VERSION)-$(PKG_RELEASE)_armel.deb
PKG_SOURCE_URL:=http://ftp.debian.org/debian/pool/main/e/e2fsprogs/
DL_DIR=$(PKG_NAME)-$(PKG_VERSION)

$(DL_DIR)/sbin/e2fsck.static: $(DL_DIR)/$(PKG_SOURCE)
	dpkg -x $(DL_DIR)/$(PKG_SOURCE) $(DL_DIR)
	cp $(DL_DIR)/sbin/e2fsck.static .

$(DL_DIR)/$(PKG_SOURCE): $(DL_DIR)
	cd $(DL_DIR) && \
		wget $(PKG_SOURCE_URL)/$(PKG_SOURCE)
	md5sum -c $(PKG_SOURCE).md5
	sha1sum -c $(PKG_SOURCE).sha1
	sha512sum -c $(PKG_SOURCE).sha512

$(DL_DIR):
	mkdir $(DL_DIR)

clean:
	-rm -rf -- $(DL_DIR)
	-rm -f -- e2fsck.static

.PHONY: patch unpack clean
