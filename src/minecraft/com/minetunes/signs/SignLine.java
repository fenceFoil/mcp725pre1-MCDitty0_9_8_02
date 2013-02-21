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
 * Represents a point in 3d space where a sign resides, and a particular line on that sign.
 *
 */
public class SignLine extends Point3D {
	private int line = 0;
	
	public SignLine() {
		super (0, 0, 0);
		this.line = 0;
	}
	
	public SignLine(int x, int y, int z, int line) {
		super(x, y, z);
		this.line = line;
	}
	
	public SignLine(Point3D linePoint, int line) {
		super(linePoint);
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	/**
	 * @return true if the argument is a SignLine with the same 3d point and line number.
	 */
	public boolean equals (Object obj) {
		if (obj instanceof SignLine) {
			SignLine otherLine = (SignLine) obj;
			if (super.equals(otherLine)) {
				// Points are equal
				if (otherLine.getLine() == getLine()) {
					// Lines are equal
					return true;
				} else {
					return false;
				}
			} else {
				// Points differ
				return false;
			}
		} else {
			// Arg is not a SignLine
			return false;
		}
	}

}
