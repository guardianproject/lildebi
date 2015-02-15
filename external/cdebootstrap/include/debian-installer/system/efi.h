/*
 * efi.h
 *
 * Copyright (C) 2014 Ian Campbell <ijc@hellion.org.uk>
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

#ifndef DEBIAN_INSTALLER__SYSTEM__EFI_H
#define DEBIAN_INSTALLER__SYSTEM__EFI_H

/**
 * @addtogroup di_system_utils
 * @{
 */

/**
 * Checks if ystem is EFI based.
 */
int di_system_is_efi(void);

/** @} */
#endif
