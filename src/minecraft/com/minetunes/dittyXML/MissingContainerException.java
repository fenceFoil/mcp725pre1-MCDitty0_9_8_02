/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.dittyXML;

/**
 * Thrown when a DittyXML tune cannot be read completely, due to parts being
 * missing. The <part element in the first level of a DittyXML container can say
 * that a given container is part of a set of multiple, and if a DittyXML parser
 * cannot find every container that claims to be in the set, instead of parsing
 * the partial ditty this exception will be thrown.
 * 
 */
public class MissingContainerException extends Exception {

}
