
PKG_NAME:=debootstrap
PKG_VERSION:=1.0.26+squeeze1
PKG_RELEASE:=1

PKG_SOURCE:=$(PKG_NAME)_$(PKG_VERSION)_all.deb
PKG_SOURCE_URL:=http://ftp.debian.org/debian/pool/main/d/debootstrap

DL_DIR=$(PKG_NAME)-$(PKG_VERSION)

all: debootstrap.tar

TIMESTAMP=`stat $(DL_DIR) | sed -n 's,Modify: \(.*\),\1,p'`
debootstrap.tar: $(DL_DIR)/usr/sbin/debootstrap \
			     $(DL_DIR)/usr/share/debootstrap/functions
	echo "TIMESTAMP: $(TIMESTAMP)"
	for f in `find $(DL_DIR)`; do touch --date="$(TIMESTAMP)" "$$f"; done
	cd $(DL_DIR) && \
		find usr/ -type f -print0 -o -type l -print0 \
		| sort --zero-terminated \
		| tar --no-recursion --null --files-from - \
			--owner=1000 --group=1000 --numeric-owner --format=gnu \
			-cpf ../debootstrap.tar
	touch --date="$(TIMESTAMP)" debootstrap.tar

$(DL_DIR)/usr/sbin/debootstrap: $(DL_DIR)/usr/share/debootstrap/scripts \
				patches/02-remove-check_sane_mount.diff
	patch -p0 < patches/02-remove-check_sane_mount.diff

$(DL_DIR)/usr/share/debootstrap/functions: $(DL_DIR)/usr/share/debootstrap/scripts \
					   patches/00-no_dpkg_deb.diff
	patch -p0 < patches/00-no_dpkg_deb.diff

$(DL_DIR)/usr/share/debootstrap/scripts: $(DL_DIR)/$(PKG_SOURCE)
	cd $(DL_DIR) && \
		(ar -p $(PKG_SOURCE) data.tar.gz | tar -xzf -)

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
	-rm -f -- debootstrap.tar

.PHONY: patch unpack clean
