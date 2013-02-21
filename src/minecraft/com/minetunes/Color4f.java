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

/**
 * Wrapper for a 4 item float array. Used to represent a color.
 */
public class Color4f {
	public float[] color = { 0f, 0f, 0f, 0f };

	public Color4f(float f1, float f2, float f3, float f4) {
		color[0] = f1;
		color[1] = f2;
		color[2] = f3;
		color[3] = f4;
	}

	/**
	 * @param sineBowColor
	 *            must be 4 elements long
	 */
	public Color4f(float[] sineBowColor) {
		color = sineBowColor;
	}

	/**
	 * 
	 */
	public Color4f() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return color[0] + "," + color[1] + "," + color[2] + "," + color[3];
	}

	public static Color4f fromString(String string) {
		String[] parts = string.split(",");
		Color4f color = new Color4f();
		for (int i = 0; i < 4; i++) {
			color.color[i] = Float.parseFloat(parts[i]);
		}
		return color;
	}
}
