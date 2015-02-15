/*
 * parser.h
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

#ifndef DEBIAN_INSTALLER__PARSER_H
#define DEBIAN_INSTALLER__PARSER_H

#include <debian-installer/hash.h>
#include <debian-installer/slist.h>
#include <debian-installer/string.h>

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct di_parser_info di_parser_info;
typedef struct di_parser_fieldinfo di_parser_fieldinfo;

/**
 * @addtogroup di_parser
 * @{
 */

/**
 * Read a single field
 *
 * @param data the actual data
 * @param fip info of the actual field
 * @param value the actual value
 * @param value_size size of the actual value
 * @param user_data data supplied to the parser
 */
typedef void di_parser_fields_function_read (void **data, const di_parser_fieldinfo *fip, di_rstring *field_modifier, di_rstring *value, void *user_data);

/**
 * Write a single field - callback
 *
 * @param field the field
 * @param value the value of the field
 * @param data the callback_data
 */
typedef void di_parser_fields_function_write_callback (const di_rstring *field, const di_rstring *value, void *data);

/**
 * Write a single field
 *
 * @param data the actual data
 * @param fip info of the actual field
 * @param output static buffer for output
 * @param user_data data supplied to the parser
 *
 * @return written bytes
 */
typedef void di_parser_fields_function_write (void **data, const di_parser_fieldinfo *fip, di_parser_fields_function_write_callback callback, void *callback_data, void *user_data);

/**
 * @param user_data data supplied to di_parse
 * @return new data
 */
typedef void *di_parser_read_entry_new (void *user_data);

/**
 * @param data the actual data
 * @param user_data data supplied to di_parse
 */
typedef int di_parser_read_entry_finish (void *data, void *user_data);

/**
 * @param data the actual data
 * @param user_data data supplied to di_parse
 */
typedef void *di_parser_write_entry_next (void **state_data, void *user_data);

/**
 * @brief Parse info
 */
struct di_parser_info
{
  di_hash_table *table;                                 /**< table of di_parser_fieldinfo */
  di_slist list;                                        /**< list of di_parser_fieldinfo */
  bool modifier;                                        /**< use modifier */
  bool wildcard;                                        /**< use wildcard (entry with key "") */
};

/**
 * @brief Info about a parser field
 */
struct di_parser_fieldinfo
{
  di_rstring key;                                       /**< field name */
  di_parser_fields_function_read *read;                 /**< function for reading a field */
  di_parser_fields_function_write *write;               /**< function for writing a field */
  unsigned int integer;                                 /**< Simple value, usage is defined by the read and write functions.
                                                         *   Most used with an offset of the field in the structure. */
};

/**
 * generates a di_parser_fieldinfo
 */
#define DI_PARSER_FIELDINFO(name, read, write, integer) \
  { { name, sizeof (name) - 1 }, read, write, integer }

di_parser_fields_function_read
  /**
   * Read function for a boolean (true == "Yes")
   */
  di_parser_read_boolean,
  /**
   * Read function for an int
   */
  di_parser_read_int,
  /**
   * Read function for a di_rstring
   */
  di_parser_read_rstring,
  /**
   * Read function for a string
   */
  di_parser_read_string;

di_parser_fields_function_write
  /**
   * Write function for a boolean ("Yes" == true)
   */
  di_parser_write_boolean,
  /**
   * Write function for an int
   */
  di_parser_write_int,
  /**
   * Write function for a di_string
   */
  di_parser_write_rstring,
  /**
   * Write function for a string
   */
  di_parser_write_string;

di_parser_info *di_parser_info_alloc (void);
void di_parser_info_free (di_parser_info *info);

void di_parser_info_add (di_parser_info *info, const di_parser_fieldinfo *fieldinfo[]);

/** @} */
#endif
