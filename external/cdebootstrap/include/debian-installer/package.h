/*
 * package.h
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

#ifndef DEBIAN_INSTALLER__PACKAGE_H
#define DEBIAN_INSTALLER__PACKAGE_H

#include <debian-installer/mem.h>
#include <debian-installer/parser.h>
#include <debian-installer/slist.h>
#include <debian-installer/string.h>

typedef struct di_package di_package;
typedef struct di_package_dependency di_package_dependency;
typedef struct di_package_version di_package_version;

typedef enum di_package_dependency_type di_package_dependency_type;
typedef enum di_package_priority di_package_priority;
typedef enum di_package_status di_package_status;
typedef enum di_package_status_want di_package_status_want;
typedef enum di_package_type di_package_type;

#include <debian-installer/packages.h>

/**
 * @addtogroup di_package
 * @{
 */

/**
 * Priority field
 */
enum di_package_priority
{
  di_package_priority_extra = 1,
  di_package_priority_optional,
  di_package_priority_standard,
  di_package_priority_important,
  di_package_priority_required,
};

/**
 * Status field, third part
 */
enum di_package_status
{
  di_package_status_undefined = 0,
  di_package_status_not_installed,
  di_package_status_unpacked,
  di_package_status_installed,
  di_package_status_half_configured,
  di_package_status_config_files,
};

/**
 * Status field, first part
 */
enum di_package_status_want
{
  di_package_status_want_unknown = 0,
  di_package_status_want_install,
  di_package_status_want_hold,
  di_package_status_want_deinstall,
  di_package_status_want_purge,
};

/**
 * type of package
 */
enum di_package_type
{
  di_package_type_non_existent = 0,                     /**< @internal Non existing package */
  di_package_type_virtual_package,                      /**< Virtual package */
  di_package_type_real_package,                         /**< Real package */
};

/**
 * @brief Package
 */
struct di_package
{
  union
  {
    char *package;                                      /**< Package field */
    di_rstring key;                                     /**< @internal hash key */
  };
  di_package_type type;                                 /**< Type of package */
  di_package_status_want status_want;                   /**< Status field, first part */
  di_package_status status;                             /**< Status field, third part */
  int essential;                                        /**< Essential field */
  di_package_priority priority;                         /**< Priority field */
  char *section;                                        /**< Section field */
  int installed_size;                                   /**< Installed-Size field */
  char *maintainer;                                     /**< Maintainer field */
  char *architecture;                                   /**< Architecture field */
  char *version;                                        /**< Version field */
  di_slist depends;                                     /**< Any different dependency types */
  char *filename;                                       /**< Filename field */
  size_t size;                                          /**< Size field */
  char *md5sum;                                         /**< MD5Sum field */
  char *short_description;                              /**< Description field, first part*/
  char *description;                                    /**< Description field, second part */
  unsigned int resolver;                                /**< @internal */
};

/**
 * Type of dependency
 */
enum di_package_dependency_type
{
  di_package_dependency_type_replaces = 1,              /**< Replaces field */
  di_package_dependency_type_provides,                  /**< Provides field */
  di_package_dependency_type_depends,                   /**< Depends field */
  di_package_dependency_type_pre_depends,               /**< Pre-Depends field */
  di_package_dependency_type_recommends,                /**< Recommends field */
  di_package_dependency_type_suggests,                  /**< Suggests field */
  di_package_dependency_type_conflicts,                 /**< Conflicts field */
  di_package_dependency_type_enhances,                  /**< Enhances field */
  di_package_dependency_type_reverse_provides = 0x100,  /**< @internal */
  di_package_dependency_type_reverse_enhances,          /**< @internal */
};

/**
 * @brief Package dependency
 */
struct di_package_dependency
{
  di_package_dependency_type type;                      /**< type of dependency */
  di_package *ptr;                                      /**< the package, may be NULL */
};

/**
 * @brief Package version
 */
struct di_package_version
{
  unsigned long epoch;                                  /**< epoch */
  char *upstream;                                       /**< upstream */
  char *debian_revision;                                /**< debian revision */
};

void di_package_destroy (di_package *package);

static inline di_package *di_package_alloc (di_packages_allocator *allocator)
{
  return di_mem_chunk_alloc0 (allocator->package_mem_chunk);
}

static inline di_package_dependency *di_package_dependency_alloc (di_packages_allocator *allocator)
{
  return di_mem_chunk_alloc0 (allocator->package_dependency_mem_chunk);
}

void di_package_version_free (di_package_version *version);

int di_package_version_compare (const di_package_version *a, const di_package_version *b);
di_package_version *di_package_version_parse (di_package *package);

extern const char *const di_package_priority_text[];
extern const char *const di_package_status_want_text[];
extern const char *const di_package_status_text[];

int di_package_array_text_from (const char *const *array, const char *text);

static inline di_package_priority di_package_priority_text_from (const char *text)
{
  return di_package_array_text_from (di_package_priority_text, text);
}

static inline di_package_status_want di_package_status_want_text_from (const char *text)
{
  return di_package_array_text_from (di_package_status_want_text, text);
}

static inline di_package_status di_package_status_text_from (const char *text)
{
  return di_package_array_text_from (di_package_status_text, text);
}

static inline const char *di_package_priority_text_to (const di_package_priority priority)
{
  return di_package_priority_text[priority];
}

static inline const char *di_package_status_want_text_to (const di_package_status_want status)
{
  return di_package_status_want_text[status];
}

static inline const char *di_package_status_text_to (const di_package_status status)
{
  return di_package_status_text[status];
}

/** @} */

/**
 * @addtogroup di_package_parser
 * @{
 */

di_parser_fields_function_read
  /**
   * Read function for Dependency field
   */
  di_package_parser_read_dependency,
  /**
   * Read function for Description field
   */
  di_package_parser_read_description,
  /**
   * Read function for Priority field
   */
  di_package_parser_read_priority,
  /**
   * Read function for Status field
   */
  di_package_parser_read_status;

di_parser_fields_function_write
  /**
   * Write function for Dependency field
   */
  di_package_parser_write_dependency,
  /**
   * Write function for Description field
   */
  di_package_parser_write_description,
  /**
   * Write function for Priority field
   */
  di_package_parser_write_priority,
  /**
   * Write function for Status field
   */
  di_package_parser_write_status;

/**
 * Standard package control file
 */
extern const di_parser_fieldinfo *di_package_parser_fieldinfo[];

/**
 * @internal
 * Get parser info for standard control file
 */
di_parser_info *di_package_parser_info (void);

/**
 * Read a special package control file
 *
 * @param file file to read
 * @param packages di_packages which the package is add to
 * @param allocator the corresponding allocator
 */
di_package *di_package_special_read_file (const char *file, di_packages *packages, di_packages_allocator *allocator, di_parser_info *(info) (void));

/**
 * Read a package control file
 *
 * @param file file to read
 * @param packages di_packages which the package is add to
 * @param allocator the corresponding allocator
 */
static inline di_package *di_package_read_file (const char *file, di_packages *packages, di_packages_allocator *allocator)
{
  return di_package_special_read_file (file, packages, allocator, di_package_parser_info);
}

/** @} */
#endif
