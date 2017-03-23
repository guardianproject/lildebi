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

#ifndef DEBIAN_INSTALLER__SYSTEM__PACKAGES_H
#define DEBIAN_INSTALLER__SYSTEM__PACKAGES_H

#include <debian-installer/package.h>
#include <debian-installer/packages.h>

#include <stdbool.h>

typedef struct di_system_package di_system_package;

/**
 * @addtogroup di_system_packages
 * @{
 */

/**
 * @brief Package - System
 */
struct di_system_package
{
  di_package p;                                         /**< standard package */
  int installer_menu_item;                              /**< Installer-Menu-Item field */
  char *subarchitecture;                                /**< Subarchitecture field */
  char *kernel_version;                                 /**< Kernel-Version field */
};

void di_system_package_destroy (di_system_package *package);

di_packages *di_system_packages_alloc (void);
di_packages_allocator *di_system_packages_allocator_alloc (void);

bool di_system_package_check_subarchitecture (di_package *package, const char *subarchitecture);

extern const di_parser_fieldinfo *di_system_package_parser_fieldinfo[];

di_parser_info *di_system_package_parser_info (void);
di_parser_info *di_system_packages_parser_info (void);
di_parser_info *di_system_packages_status_parser_info (void);

/**
 * Read a standard package control file
 *
 * @param file file to read
 * @param allocator the allocator for the packages structure
 */
static inline di_package *di_system_package_read_file (const char *file, di_packages *packages, di_packages_allocator *allocator)
{
  return di_package_special_read_file (file, packages, allocator, di_system_package_parser_info);
}

/**
 * Read a standard Packages file
 *
 * @param file file to read
 * @param allocator the allocator for the packages structure
 */
static inline di_packages *di_system_packages_read_file (const char *file, di_packages_allocator *allocator)
{
  return di_packages_special_read_file (file, allocator, di_system_packages_parser_info);
}

/**
 * Read a standard status file
 *
 * @param file file to read
 * @param allocator the allocator for the packages structure
 */
static inline di_packages *di_system_packages_status_read_file (const char *file, di_packages_allocator *allocator)
{
  return di_packages_special_read_file (file, allocator, di_system_packages_status_parser_info);
}

/**
 * Write a standard Packages file
 *
 * @param packages the packages structure
 * @param file file to read
 */
static inline int di_system_packages_write_file (di_packages *packages, const char *file)
{
  return di_packages_special_write_file (packages, file, di_system_packages_parser_info);
}

/**
 * Write a standard status file
 *
 * @param packages the packages structure
 * @param file file to read
 */
static inline int di_system_packages_status_write_file (di_packages *packages, const char *file)
{
  return di_packages_special_write_file (packages, file, di_system_packages_status_parser_info);
}

di_slist *di_system_packages_resolve_dependencies_array_permissive (di_packages *packages, di_package **array, di_packages_allocator *allocator);
void di_system_packages_resolve_dependencies_mark_anna (di_packages *packages, const char *subarchitecture, const char *kernel);

/** @} */
#endif
