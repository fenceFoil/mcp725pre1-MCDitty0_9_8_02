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

package com.minetunes;

import java.util.LinkedList;

import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

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

	/**
	 * Returns the 6 points directly adjacent to a block in 3D-space. If a block
	 * is at y=0 or y=255, the returned list of points will only be 5 elements
	 * long.
	 * 
	 * @param point
	 *            if null, method returns null
	 * @return a 5 or 6 element array of Point3D
	 */
	public static Point3D[] getAdjacentBlocks(Point3D point) {
		if (point == null) {
			return null;
		}
	
		LinkedList<Point3D> returns = new LinkedList<Point3D>();
	
		// Find the x/z adjacent blocks
		returns.add(new Point3D(point.x, point.y, point.z - 1));
		returns.add(new Point3D(point.x, point.y, point.z + 1));
		returns.add(new Point3D(point.x - 1, point.y, point.z));
		returns.add(new Point3D(point.x + 1, point.y, point.z));
	
		// Find the y adjacent blocks
		if (point.y > 0) {
			returns.add(new Point3D(point.x, point.y - 1, point.z));
		}
	
		if (point.y < 255) {
			returns.add(new Point3D(point.x, point.y + 1, point.z));
		}
	
		return returns.toArray(new Point3D[returns.size()]);
	}

	/**
	 * Returns the 4 points directly adjacent to a block in 2D space, on the x
	 * and z coordinates.
	 * 
	 * @param point
	 *            if null, method returns null
	 * @return a 4 element array of Point3D
	 */
	public static Point3D[] getAdjacentBlocksXZ(Point3D point) {
		if (point == null) {
			return null;
		}
	
		LinkedList<Point3D> returns = new LinkedList<Point3D>();
	
		// Find the x/z adjacent blocks
		returns.add(new Point3D(point.x, point.y, point.z - 1));
		returns.add(new Point3D(point.x, point.y, point.z + 1));
		returns.add(new Point3D(point.x - 1, point.y, point.z));
		returns.add(new Point3D(point.x + 1, point.y, point.z));
	
		return returns.toArray(new Point3D[returns.size()]);
	}

	public static int getNumAdjacent(World world, int blockID,
			Point3D blockPoint) {
		int found = 0;
		for (Point3D p : getAdjacentBlocks(blockPoint)) {
			int id = world.getBlockId(p.x, p.y, p.z);
			if (id == blockID) {
				found++;
			}
		}
		return found;
	}

	/**
	 * Returns a point3d representing the tile entity's position.
	 * Convenience method for Point3D (tile.xCoord, tile.yCoord, tile.zCoord);
	 * @param tile
	 * @return
	 */
	public static Point3D getTileEntityPos(TileEntity tile) {
		return new Point3D (tile.xCoord, tile.yCoord, tile.zCoord);
	}

}
