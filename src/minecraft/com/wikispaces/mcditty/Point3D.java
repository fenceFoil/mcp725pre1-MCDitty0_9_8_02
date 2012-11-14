/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.wikispaces.mcditty;

public class Point3D {

	public int x = 0;
	public int y = 0;
	public int z = 0;

	public Point3D() {

	}

	public Point3D(int x, int y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D(Point3D point) {
		super();
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point3D) {
			Point3D otherPoint = (Point3D) obj;
			if ((x == otherPoint.x) && (y == otherPoint.y)
					&& (z == otherPoint.z)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns the distance between this point and a given point. <br>
	 * <br>
	 * WARNING: Uses square roots; for high speed when you simply need to
	 * compare relative distances between Point3Ds, use distanceToRel(Point3D)
	 * 
	 * @param otherPoint
	 * @return
	 */
	public double distanceTo(Point3D otherPoint) {
		int xd = x - otherPoint.x;
		int yd = y - otherPoint.y;
		int zd = z - otherPoint.z;
		return Math.sqrt(xd*xd + yd*yd + zd*zd);
	}

	/**
	 * Returns a distance value between this point and another point, which is
	 * NOT an exact distance. Square-root the result of this method for an exact
	 * distance.<br>
	 * <br>
	 * Use this method instead of distanceTo() for higher speed, and when you
	 * only need to compare distances between two Point3Ds.
	 * 
	 * @param otherPoint
	 * @return
	 */
	public double distanceToRel(Point3D otherPoint) {
		int xd = x - otherPoint.x;
		int yd = y - otherPoint.y;
		int zd = z - otherPoint.z;
		return xd*xd + yd*yd + zd*zd;
	}

	public Point3D clone() {
		return new Point3D(x, y, z);
	}

	public String toString() {
		return "Point3D:" + x + ":" + y + ":" + z;
	}

}
