/*
 * parser_rfc822.h
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

#ifndef DEBIAN_INSTALLER__PARSER_RFC822_H
#define DEBIAN_INSTALLER__PARSER_RFC822_H

#include <debian-installer/hash.h>
#include <debian-installer/parser.h>

#include <stdio.h>

/**
 * @addtogroup di_parser_rfc822
 * @{
 */

/**
 * Parse a rfc822 formated file
 *
 * @param begin begin of memory segment
 * @param size size of memory segment
 * @param fieldinfo parser info
 * @param entry_new function which is called before each entry, may return the new entry or return NULL
 * @param entry_finish function which is called after each entry, return non-0 aborts the parsing
 * @param user_data user_data for parser functions
 *
 * @return number of parsed entries
 */
int di_parser_rfc822_read (char *begin, size_t size, di_parser_info *fieldinfo, di_parser_read_entry_new entry_new, di_parser_read_entry_finish entry_finish, void *user_data);

/**
 * Parse a rfc822 formated file
 *
 * @param file filename
 * @param fieldinfo parser info
 * @param entry_new function which is called before each entry, may return the new entry or return NULL
 * @param entry_finish function which is called after each entry, return non-0 aborts the parsing
 * @param user_data user_data for parser functions
 *
 * @return number of parsed entries
 */
int di_parser_rfc822_read_file (const char *file, di_parser_info *fieldinfo, di_parser_read_entry_new entry_new, di_parser_read_entry_finish entry_finish, void *user_data);

/**
 * Dump a rfc822 formated file
 *
 * @param file filename
 * @param fieldinfo parser info
 * @param entry_next function which is called to gather the next entry
 * @param user_data user_data for parser functions
 *
 * @return number of dumped entries
 */
int di_parser_rfc822_write_file (const char *file, di_parser_info *fieldinfo, di_parser_write_entry_next entry_next, void *user_data);
  
/** @} */
#endif
