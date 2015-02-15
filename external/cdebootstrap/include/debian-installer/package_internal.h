/*
 * package_internal.h
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

#ifndef DEBIAN_INSTALLER__PACKAGE_INTERNAL_H
#define DEBIAN_INSTALLER__PACKAGE_INTERNAL_H

#include <debian-installer/package.h>

typedef struct internal_di_package_parser_data internal_di_package_parser_data;

/**
 * @addtogroup di_package_parser
 * @{
 */

/**
 * @internal
 * parser info
 */
const di_parser_fieldinfo
  internal_di_package_parser_field_status,
  internal_di_package_parser_field_essential,
  internal_di_package_parser_field_priority,
  internal_di_package_parser_field_section,
  internal_di_package_parser_field_installed_size,
  internal_di_package_parser_field_maintainer,
  internal_di_package_parser_field_architecture,
  internal_di_package_parser_field_version,
  internal_di_package_parser_field_replaces,
  internal_di_package_parser_field_provides,
  internal_di_package_parser_field_depends,
  internal_di_package_parser_field_pre_depends,
  internal_di_package_parser_field_recommends,
  internal_di_package_parser_field_suggests,
  internal_di_package_parser_field_conflicts,
  internal_di_package_parser_field_enhances,
  internal_di_package_parser_field_filename,
  internal_di_package_parser_field_size,
  internal_di_package_parser_field_md5sum,
  internal_di_package_parser_field_description;

/**
 * @internal
 * Holds data for the Package parser
 */
struct internal_di_package_parser_data
{
  di_packages_allocator *allocator;                     /**< the used allocator */
  di_packages *packages;                                /**< the used packages struct */
  di_package *package;                                  /**< only used in the control file parser */
};

/** @} */

/**
 * @addtogroup di_package
 * @{
 */

/**
 * Destroys a di_package struct
 */
di_destroy_notify
  internal_di_package_destroy_func;

int internal_di_package_array_text_from_rstring (const char *const *array, const di_rstring *text);

static inline di_package_priority internal_di_package_priority_text_from_rstring (const di_rstring *text)
{
  return internal_di_package_array_text_from_rstring (di_package_priority_text, text);
}

static inline di_package_status_want internal_di_package_status_want_text_from_rstring (const di_rstring *text)
{
  return internal_di_package_array_text_from_rstring (di_package_status_want_text, text);
}

static inline di_package_status internal_di_package_status_text_from_rstring (const di_rstring *text)
{
  return internal_di_package_array_text_from_rstring (di_package_status_text, text);
}

/** @} */
#endif
