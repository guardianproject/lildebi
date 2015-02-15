/*
 * devfs.h
 *
 * Copyright (C) 2003 Bastian Blank <waldi@debian.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef DEBIAN_INSTALLER__SYSTEM__DEVFS_H
#define DEBIAN_INSTALLER__SYSTEM__DEVFS_H

#include <sys/types.h>

/**
 * @addtogroup di_system_devfs
 * @{
 */

/**
 * Maps a devfs path to the corresponding standard dev path
 *
 * @param path an existing device
 * @param ret device
 * @param len len of ret
 */
ssize_t di_system_devfs_map_from (const char *path, char *ret, size_t len);

/**
 * @deprecated
 * Alias of di_system_devfs_map_from
 */
ssize_t di_mapdevfs (const char *path, char *ret, size_t len) __attribute__ ((deprecated));

/** @} */
#endif
