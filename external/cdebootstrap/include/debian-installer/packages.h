/*
 * packages.h
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

#ifndef DEBIAN_INSTALLER__PACKAGES_H
#define DEBIAN_INSTALLER__PACKAGES_H

#include <debian-installer/hash.h>
#include <debian-installer/parser.h>
#include <debian-installer/slist.h>

#include <stddef.h>
#include <string.h>

typedef struct di_packages di_packages;
typedef struct di_packages_allocator di_packages_allocator;

/**
 * @addtogroup di_packages
 * @{
 */

/**
 * @brief Packages file
 */
struct di_packages
{
  di_hash_table *table;                                 /**< includes di_package */
  di_slist list;                                        /**< includes di_package */
  unsigned int resolver;                                /**< @internal */
};

/**
 * @internal
 * @brief Packages file - Allocator
 */
struct di_packages_allocator
{
  di_mem_chunk *package_mem_chunk;                      /**< @internal */
  di_mem_chunk *package_dependency_mem_chunk;           /**< @internal */
  di_mem_chunk *slist_node_mem_chunk;                   /**< @internal */
};

#include <debian-installer/package.h>

di_packages *di_packages_alloc (void);
void di_packages_free (di_packages *packages);

di_packages_allocator *di_packages_allocator_alloc (void);
void di_packages_allocator_free (di_packages_allocator *packages);

void di_packages_append_package (di_packages *packages, di_package *package, di_packages_allocator *allocator);
di_package *di_packages_get_package (di_packages *packages, const char *name, size_t n);
di_package *di_packages_get_package_new (di_packages *packages, di_packages_allocator *allocator, char *name, size_t n);

di_slist *di_packages_resolve_dependencies (di_packages *packages, di_slist *list, di_packages_allocator *allocator);
di_slist *di_packages_resolve_dependencies_array (di_packages *packages, di_package **array, di_packages_allocator *allocator);
void di_packages_resolve_dependencies_mark (di_packages *packages);

/** @} */

di_parser_fields_function_read
  di_packages_parser_read_name;

/**
 * @addtogroup di_packages_parser
 * @{
 */

extern const di_parser_fieldinfo *di_packages_parser_fieldinfo[];
extern const di_parser_fieldinfo *di_packages_status_parser_fieldinfo[];
extern const di_parser_fieldinfo *di_packages_minimal_parser_fieldinfo[];

di_parser_info *di_packages_parser_info (void);
di_parser_info *di_packages_minimal_parser_info (void);
di_parser_info *di_packages_status_parser_info (void);

/**
 * Read a special Packages file
 *
 * @param file file to read
 */
di_packages *di_packages_special_read_file (const char *file, di_packages_allocator *allocator, di_parser_info *(info) (void));

/**
 * Write a special Packages file
 *
 * @param packages the packages structure
 * @param file file to write
 *
 * @return number of written entries
 */
int di_packages_special_write_file (di_packages *packages, const char *file, di_parser_info *(info) (void));

/**
 * Read a standard Packages file
 *
 * @param file file to read
 * @param allocator the allocator for the packages structure
 */
static inline di_packages *di_packages_read_file (const char *file, di_packages_allocator *allocator)
{
  return di_packages_special_read_file (file, allocator, di_packages_parser_info);
}

/**
 * Read a minimal Packages file
 *
 * @param file file to read
 * @param allocator the allocator for the packages structure
 */
static inline di_packages *di_packages_minimal_read_file (const char *file, di_packages_allocator *allocator)
{
  return di_packages_special_read_file (file, allocator, di_packages_minimal_parser_info);
}

/**
 * Read a standard status file
 *
 * @param file file to read
 * @param allocator the allocator for the packages structure
 */
static inline di_packages *di_packages_status_read_file (const char *file, di_packages_allocator *allocator)
{
  return di_packages_special_read_file (file, allocator, di_packages_status_parser_info);
}

/**
 * Write a standard Packages file
 *
 * @param packages the packages structure
 * @param file file to write
 *
 * @return number of written entries
 */
static inline int di_packages_write_file (di_packages *packages, const char *file)
{
  return di_packages_special_write_file (packages, file, di_packages_parser_info);
}

/**
 * Write a standard status file
 *
 * @param packages the packages structure
 * @param file file to write
 *
 * @return number of written entries
 */
static inline int di_packages_status_write_file (di_packages *packages, const char *file)
{
  return di_packages_special_write_file (packages, file, di_packages_status_parser_info);
}

/** @} */
#endif
