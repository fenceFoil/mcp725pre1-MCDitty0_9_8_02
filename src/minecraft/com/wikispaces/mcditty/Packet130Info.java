/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty;

import net.minecraft.src.Packet130UpdateSign;

/**
 * Part of a bugfix for MC 1.3.1. Grr. Used in indexes of these packets: keeps
 * the packet and the time it was constructed at.
 * 
 */
public class Packet130Info {
	public Packet130UpdateSign packet = null;
	public long timeConstructed = -1;

	public Packet130Info(Packet130UpdateSign p, long t) {
		packet = p;
		timeConstructed = t;
	}
}
