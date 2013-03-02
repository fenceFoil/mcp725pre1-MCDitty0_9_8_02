/**
 * Copyright (c) 2012-2013 William Karnavas 
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

import net.minecraft.src.FontRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.RenderEngine;

/**
 * Utilities for dealing with FontRenderer
 * 
 */
public class FontRendererUtils {

	public static void changeCharWidth(int amount, FontRenderer target) {
		int[] charWidth = null;
		try {
			Object[] intArrays = Finder.getAllUniqueTypedFieldsFromClass(
					FontRenderer.class, int[].class, target);
			
			for (Object o:intArrays) {
				if (o instanceof int[]) {
					int[] array = (int[]) o;
					if (array.length == 256){ 
						// 1.4.6: Narrows it down to two suspects...
						// The one we want does NOT mainly consist of zeros.
						int zeroCount = 0;
						for (int i=0;i<array.length;i++) {
							if (array[i] == 0) {
								zeroCount ++;
							}
						}
						if (zeroCount<100) {
							charWidth = array;
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (charWidth != null) {
			for (int i=0;i<charWidth.length;i++) {
				charWidth[i] = amount;
			}
		}
	}

}
