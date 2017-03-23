/*
 * subarch.h
 *
 * Copyright (C) 2003 Bastian Blank <waldi@debian.org>
 * Copyright (C) 2005 Colin Watson <cjwatson@debian.org>
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

#ifndef DEBIAN_INSTALLER__SYSTEM__SUBARCH_H
#define DEBIAN_INSTALLER__SYSTEM__SUBARCH_H

#if defined(__ARMEL__)
#define DI_SYSTEM_SUBARCH_CAN_GUESS 1
#endif

/**
 * @addtogroup di_system_subarch
 * @{
 */

/**
 * Returns a string describing the current subarchitecture, e.g.
 * "powermac_newworld".
 */
const char *di_system_subarch_analyze (void);

/**
 * Return a string with a best-guess of the current subarchitecture
 *
 * Only present on armel currently, and is a stub on all other architectures
 */

const char *di_system_subarch_analyze_guess (void);

/** @} */
#endif
