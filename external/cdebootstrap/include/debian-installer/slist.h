/*
 * slist.h
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

#ifndef DEBIAN_INSTALLER__SLIST_H
#define DEBIAN_INSTALLER__SLIST_H

#include <debian-installer/mem_chunk.h>

typedef struct di_slist di_slist;
typedef struct di_slist_node di_slist_node;

/**
 * @addtogroup di_slist
 * @{
 */
 
/**
 * @brief Single-linked list
 */
struct di_slist
{
  di_slist_node *head;                                  /**< head of list */
  di_slist_node *bottom;                                /**< bottom of list */
};

/**
 * @brief Node of a single-linked list
 */
struct di_slist_node
{
  di_slist_node *next;                                  /**< next node */
  void *data;                                           /**< data */
};

/**
 * Allocate a single-linked list
 *
 * @return a di_slist
 */
di_slist *di_slist_alloc (void);

/**
 * Destroy the contents of a single-linked list
 *
 * @warning never use this function with a list which makes use of the chunk allocator
 *
 * @param slist a di_slist
 */
void di_slist_destroy (di_slist *slist, di_destroy_notify destroy_func) __attribute__ ((nonnull(1)));

/**
 * Free a single-linked list
 *
 * @param slist a di_slist
 */
void di_slist_free (di_slist *slist);

/**
 * Append to a single-linked list
 *
 * @warning don't mix with di_slist_append_chunk
 *
 * @param slist a di_slist
 * @param data the data
 */
void di_slist_append (di_slist *slist, void *data) __attribute__ ((nonnull(1)));

/**
 * Append to a single-linked list
 *
 * @warning don't mix with di_slist_append
 *
 * @param slist a di_slist
 * @param data the data
 * @param mem_chunk a di_mem_chunk for allocation of new nodes
 *
 * @pre the di_mem_chunk must return chunks with at least the size of di_slist_node
 */
void di_slist_append_chunk (di_slist *slist, void *data, di_mem_chunk *mem_chunk) __attribute__ ((nonnull(1,3)));

/**
 * Prepend to a single-linked list
 *
 * @warning don't mix with di_slist_prepend_chunk
 *
 * @param slist a di_slist
 * @param data the data
 */
void di_slist_prepend (di_slist *slist, void *data) __attribute__ ((nonnull(1)));

/**
 * Prepend to a single-linked list
 *
 * @warning don't mix with di_slist_prepend
 *
 * @param slist a di_slist
 * @param data the data
 * @param mem_chunk a di_mem_chunk for allocation of new nodes
 *
 * @pre the di_mem_chunk must return chunks with at least the size of di_slist_node
 */
void di_slist_prepend_chunk (di_slist *slist, void *data, di_mem_chunk *mem_chunk) __attribute__ ((nonnull(1,3)));

/** @} */
#endif
