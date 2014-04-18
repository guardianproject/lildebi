
PKG_NAME:=cdebootstrap-static
PKG_VERSION:=0.5.10

PKG_SOURCE:=$(PKG_NAME)_$(PKG_VERSION)_armel.deb
PKG_SOURCE_URL:=https://mirrors.kernel.org/debian/pool/main/c/cdebootstrap

DL_DIR=$(PKG_NAME)-$(PKG_VERSION)

all: cdebootstrap.tar

TIMESTAMP="`stat $(DL_DIR) | sed -n 's,Modify: \(.*\),\1,p'`"
cdebootstrap.tar: $(DL_DIR)/usr/bin/cdebootstrap-static
	echo "TIMESTAMP: $(TIMESTAMP)"
	for f in `find $(DL_DIR)`; do touch --date=$(TIMESTAMP) "$$f"; done
	cd $(DL_DIR) && \
		tar --create --file ../cdebootstrap.tar usr/
	touch --date=$(TIMESTAMP) cdebootstrap.tar

$(DL_DIR)/usr/bin/cdebootstrap-static: $(DL_DIR)/$(PKG_SOURCE)
	cd $(DL_DIR) && \
		(ar -p $(PKG_SOURCE) data.tar.gz | \
			tar --exclude=usr/share/man --exclude=usr/share/doc \
				--preserve-permissions --extract --gunzip --file -)
# I haven't found a reliable way to install/uninstall a patch from a Makefile,
# so just always try to apply it, and ignore it if it fails. Works fine unless
# the files being patched have changed, in which cause a partial application
# could happen unnoticed.
	patch -N -p0 --reject-file=- < add-missing-releases.patch

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
	-rm -f -- cdebootstrap.tar

.PHONY: clean
