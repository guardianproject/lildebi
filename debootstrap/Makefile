#
# Copyright (C) 2010 Gianluigi Tiesi <sherpya@netfarm.it>
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#

PKG_NAME:=debootstrap
PKG_VERSION:=1.0.28
PKG_RELEASE:=1

PKG_SOURCE:=$(PKG_NAME)-udeb_$(PKG_VERSION)_all.udeb
PKG_SOURCE_URL:=http://ftp.debian.org/debian/pool/main/d/debootstrap
PKG_MD5SUM:=dee0dc77573d88af80b225d8810ab7a1

UNPACK_CMD=ar -p "$(DL_DIR)/$(PKG_SOURCE)" data.tar.gz | ( cd $(1) && tar -xzf - )
