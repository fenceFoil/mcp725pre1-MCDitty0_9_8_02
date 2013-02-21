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

import java.io.Serializable;
import java.util.LinkedList;

import org.jfugue.elements.Note;

public class StaccatoEffect extends RendererEffect {
	/**
	 * Eighths of a note play the note for before resting for the remainder of
	 * its specified duration. 0 means to only play note for one tick.
	 */
	private int eighths = 4;

	public StaccatoEffect(Double endTime, int eighths) {
		super(endTime);
		setEighths(eighths);

		applyMethod = ApplyEffect.DUAL_MUTEX_FINITE_INFINITE;
		//System.out.println(toString());
	}

	public int getEighths() {
		return eighths;
	}

	public void setEighths(int eighths) {
		this.eighths = eighths;
	}

	@Override
	public void apply(LinkedList<TimedJFugueElement> elements) {
		super.apply(elements);

		// Shorten each note
		for (TimedJFugueElement e : elements) {
			if (e.element instanceof Note) {
				Note note = (Note) e.element;
				note.setDecimalDuration(getStaccatoNoteLength(note
						.getDecimalDuration()));
			}
		}
	}

	private double getStaccatoNoteLength(double originalLength) {
		if (eighths == 0) {
			// Shortest possible
			return 1d / 128d;
		} else {
			// Return x eighths of the original
			return originalLength * (1d / 8d * (double) eighths);
		}
	}

	@Override
	public String toString() {
		// Okay, I royally messed up my attempt at efficiency here. I'm a secure
		// person who is comfortable admitting his faults. Fix this if you ever
		// feel like it, dear reader.
		return new StringBuilder().append(
				"StaccatoEffect-EndTime=" + getEnd() + "-Endless="
						+ getEndless() + "-eighths=" + getEighths()
						+ "-applyMethod=" + applyMethod + ";").toString();
	}

}
