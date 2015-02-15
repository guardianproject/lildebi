/*
 * hash.h
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

#ifndef DEBIAN_INSTALLER__HASH_H
#define DEBIAN_INSTALLER__HASH_H

#include <debian-installer/types.h>

/** 
 * @addtogroup di_hash
 * @{
 */

di_equal_func di_rstring_equal;
di_hash_func di_rstring_hash;

#if 0
di_equal_func di_string_equal;
di_hash_func di_string_hash;
#endif

/** @} */

typedef struct di_hash_table di_hash_table;

/**
 * @addtogroup di_hash_table
 * @{
 */

/**
 * Creates a new di_hash_table.
 *
 * @param hash_func a function to create a hash value from a key.
 *   Hash values are used to determine where keys are stored within the
 *   di_hash_table data structure.
 * @param key_equal_func a function to check two keys for equality.  This is
 *   used when looking up keys in the di_hash_table.
 *
 * @return a new di_hash_table.
 */
di_hash_table *di_hash_table_new (di_hash_func hash_func, di_equal_func key_equal_func);

/**
 * Creates a new di_hash_table like di_hash_table_new and allows to specify
 * functions to free the memory allocated for the key and value that get
 * called when removing the entry from the di_hash_table
 *
 * @param hash_func a function to create a hash value from a key.
 *   Hash values are used to determine where keys are stored within the
 *   di_hash_table data structure.
 * @param key_equal_func a function to check two keys for equality.  This is
 *   used when looking up keys in the di_hash_table.
 * @param key_destroy_func a function to free the memory allocated for the key
 *   used when removing the entry from the di_hash_table or %NULL if you
 *   don't want to supply such a function.
 * @param value_destroy_func a function to free the memory allocated for the
 *   value used when removing the entry from the di_hash_table or %NULL if
 *   you don't want to supply such a function.
 *
 * @return a new di_hash_table.
 */
di_hash_table *di_hash_table_new_full (di_hash_func hash_func, di_equal_func key_equal_func, di_destroy_notify key_destroy_func, di_destroy_notify value_destroy_func);

/**
 * Destroys the di_hash_table. If keys and/or values are dynamically
 * allocated, you should either free them first or create the di_hash_table
 * using di_hash_table_new_full. In the latter case the destroy functions
 * you supplied will be called on all keys and values before destroying
 * the di_hash_table.
 *
 * @param hash_table a di_hash_table.
 */
void di_hash_table_destroy (di_hash_table *hash_table);

/**
 * Inserts a new key and value into a di_hash_table.
 *
 * If the key already exists in the di_hash_table its current value is replaced
 * with the new value. If you supplied a value_destroy_func when creating the
 * di_hash_table, the old value is freed using that function. If you supplied
 * a key_destroy_func when creating the di_hash_table, the passed key is freed
 * using that function.
 *
 * @param hash_table a di_hash_table.
 * @param key a key to insert.
 * @param value the value to associate with the key.
 */
void di_hash_table_insert (di_hash_table *hash_table, void *key, void *value);

/**
 * Looks up a key in a di_hash_table.
 *
 * @param hash_table a di_hash_table,
 * @param key the key to look up.
 *
 * @return the associated value, or %NULL if the key is not found.
 */
void *di_hash_table_lookup (di_hash_table *hash_table, const void *key);

/**
 * Calls the given function for each of the key/value pairs in the
 * di_hash_table. The function is passed the key and value of each
 * pair, and the given user_data parameter.
 *
 * @post The hash table may not be modified while iterating over it
 * (you can't add/remove items).
 *
 * @param hash_table a di_hash_table.
 * @param func the function to call for each key/value pair.
 * @param user_data user data to pass to the function.
 */
void di_hash_table_foreach (di_hash_table *hash_table, di_hfunc *func, void *user_data);

/**
 * Returns the number of elements contained in the di_hash_table.
 *
 * @param hash_table a di_hash_table.
 *
 * @return the number of key/value pairs.
 */
di_ksize_t di_hash_table_size (di_hash_table *hash_table);

/** @} */
#endif
