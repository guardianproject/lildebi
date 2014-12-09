
PKG_NAME:=cdebootstrap-static
PKG_VERSION:=0.6.4

PKG_SOURCE:=$(PKG_NAME)_$(PKG_VERSION)_armel.deb
# the archive, so the build stays working as long as possible:
PKG_SOURCE_URL:=http://snapshot.debian.org/archive/debian/20141130T041023Z/pool/main/c/cdebootstrap
# the canonical:
#PKG_SOURCE_URL:=https://mirrors.kernel.org/debian/pool/main/c/cdebootstrap

DL_DIR=$(PKG_NAME)-$(PKG_VERSION)

all: cdebootstrap.tar

TIMESTAMP="`stat $(DL_DIR) | sed -n 's,Modify: \(.*\),\1,p'`"
cdebootstrap.tar: $(DL_DIR)/usr/bin/cdebootstrap-static
	echo "TIMESTAMP: $(TIMESTAMP)"
	for f in `find $(DL_DIR)`; do touch --date=$(TIMESTAMP) "$$f"; done
	cd $(DL_DIR) && \
		find usr/ -type f -print0 -o -type l -print0 \
		| sort --zero-terminated \
		| tar --no-recursion --null --files-from - \
			--owner=1000 --group=1000 --numeric-owner --format=gnu \
			--create --file ../cdebootstrap.tar
	touch --date=$(TIMESTAMP) cdebootstrap.tar

$(DL_DIR)/usr/bin/cdebootstrap-static: $(DL_DIR)/$(PKG_SOURCE)
	cd $(DL_DIR) && \
		(ar -p $(PKG_SOURCE) data.tar.xz | \
			tar --exclude=usr/share/man --exclude=usr/share/doc \
				--preserve-permissions --extract --xz --file -)
# I haven't found a reliable way to install/uninstall a patch from a Makefile,
# so just always try to apply it, and ignore it if it fails. Works fine unless
# the files being patched have changed, in which cause a partial application
# could happen unnoticed.
	patch -N -p0 --reject-file=- < add-missing-ubuntu-releases.patch

$(DL_DIR)/$(PKG_SOURCE): $(DL_DIR)
	test -e $(PKG_SOURCE) || \
		wget --continue $(PKG_SOURCE_URL)/$(PKG_SOURCE)
	md5sum -c $(PKG_SOURCE).md5
	sha1sum -c $(PKG_SOURCE).sha1
	sha256sum -c $(PKG_SOURCE).sha256
	cp $(PKG_SOURCE) $(DL_DIR)/

$(DL_DIR):
	mkdir $(DL_DIR)


clean:
	-rm -rf -- $(DL_DIR)
	-rm -f -- cdebootstrap.tar

.PHONY: clean
