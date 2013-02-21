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

import java.util.LinkedList;

/**
 * Represents an effect applied to music as its being rendered in MIDIRenderer.
 * Effectively takes a musicString and, not changing it, changes what is going
 * to be played.
 * 
 */
public class RendererEffect {

	/**
	 * Whether to limit how long the effect lasts
	 */
	private boolean endless = false;

	/**
	 * The length of time to apply the effect, where 1 whole note = 1.
	 * NOTE: Changed to the time when the effect ends.
	 */
	private double endTime = 0;
	
	protected static ApplyEffect applyMethod = ApplyEffect.MUTEX;

	/**
	 * Creates a new RendererEffect.
	 * 
	 * @param endTime
	 *            null if there is no length limit, otherwise the number of
	 *            whole notes to apply effect for.
	 */
	public RendererEffect(Double endTime) {
		if (endTime == null) {
			endless = true;
		} else {
			setEnd(endTime);
		}
	}

	public boolean getEndless() {
		return endless;
	}

	public void setEndless(boolean isEndless) {
		this.endless = isEndless;
	}

	public double getEnd() {
		return endTime;
	}

	public void setEnd(double length) {
		this.endTime = length;
	}

	public static ApplyEffect getApplyMethod() {
		return applyMethod;
	}

	/**
	 * Applies the effect to a set of elements. Extended by subclasses of RendererEffect to do work.
	 * 
	 * @param elements the elements to apply the effect to
	 */
	public void apply(LinkedList<TimedJFugueElement> elements) {
		// By default, do nothing.
	}
	
	public void combineWith (RendererEffect effect) {
		// By default, do nothing
	}
}
