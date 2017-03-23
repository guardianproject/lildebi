/*
 * macros.h
 *
 * Copyright (C) 1995-1997 Peter Mattis, Spencer Kimball and Josh MacDonald
 *               2003 Bastian Blank <waldi@debian.org>
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

#ifndef DEBIAN_INSTALLER__MACROS_H
#define DEBIAN_INSTALLER__MACROS_H

#define DI_STRINGIFY(contents) DI_STRINGIFY_ARG (contents)
#define DI_STRINGIFY_ARG(contents) #contents
#define DI_STRLOC __FILE__ ":" DI_STRINGIFY(__LINE__)

/* GCC version checking borrowed from glibc. */
#if defined(__GNUC__) && defined(__GNUC_MINOR__)
#  define DI_GNUC_PREREQ(maj,min) \
        ((__GNUC__ << 16) + __GNUC_MINOR__ >= ((maj) << 16) + (min))
#else
#  define DI_GNUC_PREREQ(maj,min) 0
#endif

#endif
