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
package com.minetunes.jfugue.rendererEffect;

import org.jfugue.elements.JFugueElement;

/**
 * Used to store lists of jfugue elements with times attached.
 * 
 */
public class TimedJFugueElement implements Comparable<TimedJFugueElement> {
	public JFugueElement element;
	public double time;

	public TimedJFugueElement(JFugueElement e, double t) {
		time = t;
		element = e;
	}

	@Override
	public int compareTo(TimedJFugueElement o) {
		double diff = o.time - time;
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		} else {
			return 0;
		}
	}

}