/*
 * mem.h
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

#ifndef DEBIAN_INSTALLER__MEM_H
#define DEBIAN_INSTALLER__MEM_H

#include <debian-installer/types.h>

#include <stdio.h>

/**
 * @addtogroup di_mem
 * @{
 */


/**
 * Allocate memory
 *
 * @param n_bytes size in bytes
 *
 * @post never returns NULL
 */
void *di_malloc (size_t n_bytes) __attribute__ ((malloc));

/**
 * Allocate cleared memory
 *
 * @param n_bytes size in bytes
 *
 * @post never returns NULL
 */
void *di_malloc0 (size_t n_bytes) __attribute__ ((malloc));

/**
 * Reallocate memory
 *
 * @param mem memory
 * @param n_bytes size in bytes
 *
 * @post never returns NULL
 */
void *di_realloc (void *mem, size_t n_bytes) __attribute__ ((malloc));

/**
 * Free memory
 *
 * @param mem memory
 */
void di_free (void *mem);

/**
 * @param struct_type returned type
 * @param n_structs number of returned structs
 */
#define di_new(struct_type, n_structs) \
  ((struct_type *) di_malloc (sizeof (struct_type) * (n_structs)))
/**
 * @param struct_type returned type
 * @param n_structs number of returned structs
 */
#define di_new0(struct_type, n_structs) \
  ((struct_type *) di_malloc0 (sizeof (struct_type) * (n_structs)))
/**
 * @param struct_type returned type
 * @param mem current memory pointer
 * @param n_structs number of returned structs
 */
#define di_renew(struct_type, mem, n_structs) \
  ((struct_type *) di_realloc ((mem), sizeof (struct_type) * (n_structs)))

/** @} */
#endif
