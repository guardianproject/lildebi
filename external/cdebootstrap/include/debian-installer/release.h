/*
 * release.h
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

#ifndef DEBIAN_INSTALLER__RELEASE_H
#define DEBIAN_INSTALLER__RELEASE_H

#include <debian-installer/hash.h>
#include <debian-installer/parser.h>
#include <debian-installer/slist.h>

typedef struct di_release di_release;
typedef struct di_release_file di_release_file;

/**
 * @addtogroup di_release
 * @{
 */

/**
 * @brief Release file
 */
struct di_release
{
  char *origin;                                 /**< Origin field */
  char *suite;                                  /**< Suite field */
  char *codename;                               /**< Codename field */
  di_hash_table *md5sum;                        /**< checksum fields, includes di_release_file */
  di_mem_chunk *release_file_mem_chunk;         /**< @internal */
};

/**
 * @brief Release file - file entry
 */
struct di_release_file
{
  union
  {
    char *filename;                             /**< filename */
    di_rstring key;                             /**< @internal */
  };
  unsigned int size;                            /**< size */
  char *sum[2];                                 /**< checksums, currently md5 and sha1 */
};

di_release *di_release_alloc (void);
void di_release_free (di_release *packages);

/**
 * @}
 * @addtogroup di_release_parser
 * @{
 */

di_release *di_release_read_file (const char *file);

/** @} */

di_parser_fields_function_read
  di_release_parser_read_file;

/**
 * @addtogroup di_release_parser
 * @{
 */

extern const di_parser_fieldinfo *di_release_parser_fieldinfo[];

/** @} */
#endif
