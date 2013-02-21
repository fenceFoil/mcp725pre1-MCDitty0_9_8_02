/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes;

import net.minecraft.src.AxisAlignedBB;

/**
 * Represents the collision box of a proxpad.
 * 
 */
public class ProxPadBoundingBox {
	private int padX = 0;
	private int padY = 0;
	private int padZ = 0;

	private int width = 0;
	private int height = 0;
	private int depth = 0;

	private AxisAlignedBB box = null;

	// TRUE, because if false the prox pad will activate if the player is in it
	// at time of creation (e.g. when putting up a sign)
	/**
	 * This ensures that a proxpad is only activated once: when the player
	 * enters it. This keeps it from activating continuously.
	 */
	private boolean lockout = true;

	/**
	 * @return the padX
	 */
	public int getPadX() {
		return padX;
	}

	/**
	 * @param padX
	 *            the padX to set
	 */
	public void setPadX(int padX) {
		this.padX = padX;
	}

	/**
	 * @return the padY
	 */
	public int getPadY() {
		return padY;
	}

	/**
	 * @param padY
	 *            the padY to set
	 */
	public void setPadY(int padY) {
		this.padY = padY;
	}

	/**
	 * @return the padZ
	 */
	public int getPadZ() {
		return padZ;
	}

	/**
	 * @param padZ
	 *            the padZ to set
	 */
	public void setPadZ(int padZ) {
		this.padZ = padZ;
	}

	/**
	 * @return the box
	 */
	public AxisAlignedBB getBox() {
		return box;
	}

	/**
	 * @param box
	 *            the box to set
	 */
	public void setBox(AxisAlignedBB box) {
		this.box = box;
	}

	// public static ProxPadBoundingBox getProxBoundingBox(double x1, double y1,
	// double z1, double x2, double y2, double z2) {
	// ProxPadBoundingBox bb = new ProxPadBoundingBox();
	// bb.setBox(AxisAlignedBB.getBoundingBox(x1, y1, z1, x2, y2, z2));
	// return bb;
	// }

	public void setLockout(boolean b) {
		lockout = b;
	}

	public boolean getLockout() {
		return lockout;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Returns true if all fields are equal. Handles null values for box.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ProxPadBoundingBox) {
			ProxPadBoundingBox bb = (ProxPadBoundingBox) o;
			if (bb.padX == padX && bb.padY == padY && bb.padZ == padZ) {
				if (bb.box == null) {
					if (box == null) {
						return true;
					} else {
						return false;
					}
				} else if (box == null) {
					if (bb.box == null) {
						return true;
					} else {
						return false;
					}
				} else {
					if (bb.box.maxX == box.maxX && bb.box.maxY == box.maxY
							&& bb.box.maxZ == box.maxZ) {
						if (bb.box.minX == box.minX && bb.box.minY == box.minY
								&& bb.box.minZ == box.minZ) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
