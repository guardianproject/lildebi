/*
 * prebaseconfig.h
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

#ifndef DEBIAN_INSTALLER__SYSTEM__PREBASECONFIG_H
#define DEBIAN_INSTALLER__SYSTEM__PREBASECONFIG_H

/**
 * @addtogroup di_system_prebaseconfig
 * @{
 */

/**
 * Directory for prebaseconfig files
 */
#define DI_SYSTEM_PREBASECONFIG_DIR "/usr/lib/prebaseconfig.d"

/**
 * Append resolved format to the prebaseconfig file for udeb
 */
int di_system_prebaseconfig_append (const char *udeb, const char *format, ...) __attribute__ ((format(printf,2,3)));

/**
 * @deprecated
 * Alias of di_system_prebaseconfig_append
 */
int di_prebaseconfig_append (const char *udeb, const char *format, ...) __attribute__ ((format(printf,2,3),deprecated));

/** @} */
#endif
