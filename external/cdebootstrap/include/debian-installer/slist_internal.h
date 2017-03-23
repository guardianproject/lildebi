/*
 * slist_internal.h
 *
 * Copyright (C) 2004 Bastian Blank <waldi@debian.org>
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

#ifndef DEBIAN_INSTALLER__SLIST_INTERNAL_H
#define DEBIAN_INSTALLER__SLIST_INTERNAL_H

#include <debian-installer/slist.h>

/**
 * @addtogroup di_slist
 * @{
 */

/**
 * @internal
 */
void internal_di_slist_append_list (di_slist *slist, di_slist *slist_append);

/** @} */
#endif
