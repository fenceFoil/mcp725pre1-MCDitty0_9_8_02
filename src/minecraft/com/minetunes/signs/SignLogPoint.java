/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
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
package com.minetunes.signs;

import com.minetunes.Point3D;


/**
 * Used in detecting infinite loops; notes the subpattern level and id of a sign
 * read.
 * 
 */
public class SignLogPoint extends Point3D {
	private int level;
	private int id;

	public SignLogPoint(Point3D point, int subpatternLevel, int subpatternID) {
		x = point.x;
		y = point.y;
		z = point.z;
		level = subpatternLevel;
		id = subpatternID;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
