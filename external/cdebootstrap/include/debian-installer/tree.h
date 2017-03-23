/*
 * tree.h
 *
 * Copyright (C) 2006 Bastian Blank <waldi@debian.org>
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

#ifndef DEBIAN_INSTALLER__TREE_H
#define DEBIAN_INSTALLER__TREE_H

#include <debian-installer/types.h>

typedef struct di_tree di_tree;

/**
 * @addtogroup di_tree
 * @{
 */

/**
 * Creates a new di_tree.
 *
 * @param key_compare_func a function to compare two keys.  This is used when
 *   looking up keys in the di_tree.
 *
 * @return a new di_tree.
 */
di_tree *di_tree_new (di_compare_func key_compare_func);

/**
 * Creates a new di_tree like di_tree_new and allows to specify functions to
 * free the memory allocated for the key and value that get called when
 * removing the entry from the di_tree
 *
 * @param key_compare_func a function to check two keys for equality.  This is
 *   used when looking up keys in the di_tree.
 * @param key_destroy_func a function to free the memory allocated for the key
 *   used when removing the entry from the di_tree or %NULL if you don't want
 *   to supply such a function.
 * @param value_destroy_func a function to free the memory allocated for the
 *   value used when removing the entry from the di_tree or %NULL if you don't
 *   want to supply such a function.
 *
 * @return a new di_tree.
 */
di_tree *di_tree_new_full (di_compare_func key_compare_func, di_destroy_notify key_destroy_func, di_destroy_notify value_destroy_func);

/**
 * Destroys the di_tree. If keys and/or values are dynamically allocated, you
 * should either free them first or create the di_tree using di_tree_new_full.
 * In the latter case the destroy functions you supplied will be called on all
 * keys and values before destroying the di_hash_table.
 *
 * @param tree a di_tree.
 */
void di_tree_destroy (di_tree *tree);

/**
 * Inserts a new key and value into a di_tree.
 *
 * If the key already exists in the di_tree its current value is replaced with
 * the new value. If you supplied a value_destroy_func when creating the
 * di_tree, the old value is freed using that function. If you supplied a
 * key_destroy_func when creating the di_tree, the passed key is freed using
 * that function.
 *
 * @param tree a di_tree.
 * @param key a key to insert.
 * @param value the value to associate with the key.
 */
void di_tree_insert (di_tree *tree, void *key, void *value);

/**
 * Looks up a key in a di_tree.
 *
 * @param tree a di_tree.,
 * @param key the key to look up.
 *
 * @return the associated value, or %NULL if the key is not found.
 */
void *di_tree_lookup (di_tree *tree, const void *key);

/**
 * Calls the given function for each of the key/value pairs in the di_tree. The
 * function is passed the key and value of each pair, and the given user_data
 * parameter.
 *
 * @post The hash table may not be modified while iterating over it
 * (you can't add/remove items).
 *
 * @param tree a di_tree.
 * @param func the function to call for each key/value pair.
 * @param user_data user data to pass to the function.
 */
void di_tree_foreach (di_tree *tree, di_hfunc *func, void *user_data);

/**
 * Returns the number of elements contained in the di_tree.
 *
 * @param hash_table a di_tree.
 *
 * @return the number of key/value pairs.
 */
di_ksize_t di_tree_size (di_tree *tree);

/** @} */
#endif
