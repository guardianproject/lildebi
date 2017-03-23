/*
 * list.h
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

#ifndef DEBIAN_INSTALLER__LIST_H
#define DEBIAN_INSTALLER__LIST_H

#include <debian-installer/mem_chunk.h>

typedef struct di_list di_list;
typedef struct di_list_node di_list_node;

/**
 * @addtogroup di_list
 * @{
 */

/**
 * @brief Double-linked list
 */
struct di_list
{
  di_list_node *head;                                   /**< head of list */
  di_list_node *bottom;                                 /**< bottom of list */
};

/**
 * @brief Node of a double-linked list
 */
struct di_list_node
{
  di_list_node *next;                                   /**< next node */
  di_list_node *prev;                                   /**< previsous node */
  void *data;                                           /**< data */
};

/**
 * Allocate a double-linked list
 *
 * @return a di_list
 */
di_list *di_list_alloc (void);

/**
 * Destroy the contents of a double-linked list
 *
 * @warning never use this function with a list which makes use of the chunk allocator
 *
 * @param list a di_list
 */
void di_list_destroy (di_list *list, di_destroy_notify destroy_func) __attribute__ ((nonnull(1)));

/**
 * Free a double-linked list
 *
 * @param list a di_list
 */
void di_list_free (di_list *list);

/**
 * Append to a double-linked list
 *
 * @warning don't mix with di_list_append_chunk
 *
 * @param list a di_list
 * @param data the data
 */
void di_list_append (di_list *list, void *data) __attribute__ ((nonnull(1)));

/**
 * Append to a double-linked list
 *
 * @warning don't mix with di_list_append_chunk
 *
 * @param list a di_list
 * @param data the data
 */
void di_list_append_chunk (di_list *list, void *data, di_mem_chunk *mem_chunk) __attribute__ ((nonnull(1,3)));

/**
 * Prepend to a double-linked list
 *
 * @warning don't mix with di_list_prepend_chunk
 *
 * @param list a di_list
 * @param data the data
 */
void di_list_prepend (di_list *list, void *data) __attribute__ ((nonnull(1)));

/**
 * Prepend to a double-linked list
 *
 * @warning don't mix with di_list_prepend
 *
 * @param list a di_list
 * @param data the data
 * @param mem_chunk a di_mem_chunk for allocation of new nodes
 *
 * @pre the di_mem_chunk must return chunks with at least the size of di_list_node
 */
void di_list_prepend_chunk (di_list *list, void *data, di_mem_chunk *mem_chunk) __attribute__ ((nonnull(1,3)));

/** @} */
#endif
