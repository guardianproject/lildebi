/*
 * log.h
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

#ifndef DEBIAN_INSTALLER__LOG_H
#define DEBIAN_INSTALLER__LOG_H

#include <stdarg.h>

/**
 * @addtogroup di_log
 * @{
 */

/**
 * @brief Log levels and other flags
 */
typedef enum
{
  DI_LOG_FLAG_FATAL             = 1 << 1,       /**< flag as fatal */

  DI_LOG_LEVEL_ERROR            = 1 << 2,       /**< error level, always fatal */
  DI_LOG_LEVEL_CRITICAL         = 1 << 3,       /**< critical level */
  DI_LOG_LEVEL_WARNING          = 1 << 4,       /**< warning level */
  DI_LOG_LEVEL_MESSAGE          = 1 << 5,       /**< message level */
  DI_LOG_LEVEL_INFO             = 1 << 6,       /**< information level */
  DI_LOG_LEVEL_DEBUG            = 1 << 7,       /**< debug level */
  DI_LOG_LEVEL_OUTPUT           = 1 << 8,       /**< command output */

  DI_LOG_LEVEL_MASK             = ~DI_LOG_FLAG_FATAL,   /**< defines mask for levels */
  DI_LOG_FATAL_MASK             = DI_LOG_LEVEL_ERROR,   /**< defines always fatal levels */
}
di_log_level_flags;

typedef void di_log_handler (di_log_level_flags log_level, const char *message, void *user_data);

/**
 * logs an error
 */
#define di_error(format...) di_log (DI_LOG_LEVEL_ERROR, format)
/**
 * logs a warning
 */
#define di_warning(format...) di_log (DI_LOG_LEVEL_WARNING, format)
/**
 * logs information
 */
#define di_info(format...) di_log (DI_LOG_LEVEL_INFO, format)
/**
 * logs debug info
 */
#define di_debug(format...) di_log (DI_LOG_LEVEL_DEBUG, format)

/**
 * Logs the resolved formatstring with log_level
 *
 * @param log_level the level of the message
 * @param format printf compatible format
 */
void di_log (di_log_level_flags log_level, const char *format, ...) __attribute__ ((format(printf,2,3)));
/**
 * Logs the resolved formatstring with log_level
 *
 * @param log_level the level of the message
 * @param format printf compatible format
 * @param args variable arguments list
 */
void di_vlog (di_log_level_flags log_level, const char *format, va_list args);

/**
 * Sets a log handler
 *
 * @param log_levels levels
 * @param log_func the log handler
 * @param user_data data for log_func
 */
unsigned int di_log_set_handler (di_log_level_flags log_levels, di_log_handler *log_func, void *user_data);

di_log_handler
  /**
   * Default log handler.
   * Logs to STDOUT and STDERR.
   */
  di_log_handler_default,
  /**
   * SYSLOG log handler.
   * Logs to SYSLOG.
   */
  di_log_handler_syslog;

/** @} */
#endif
