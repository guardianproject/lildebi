/*
 * string.h
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

#ifndef DEBIAN_INSTALLER__STRING_H
#define DEBIAN_INSTALLER__STRING_H

#include <debian-installer/types.h>

#include <stdio.h>

typedef struct di_rstring di_rstring;

/**
 * @addtogroup di_string
 * @{
 */

/**
 * @brief raw string
 */
struct di_rstring
{
  char *string;                                         /**< pointer to a string, don't need to be 0-terminated */
  di_ksize_t size;                                      /**< size of string */
};

/**
 * cat resolved format to str
 *
 * @param str string
 * @param size len of str
 * @param format printf compatible string
 * @return append chars
 */
int di_snprintfcat (char *str, size_t size, const char *format, ...)
#ifdef __GNUC__
  __attribute__ ((format (printf, 3, 4)))
#endif
    ;

/**
 * Copies n bytes from s, without calculating the length of s itself.
 *
 * @param s source
 * @param n len of source without delimiter
 * @return malloced string
 */
char *di_stradup (const char *s, size_t n);

/** @} */
#endif
