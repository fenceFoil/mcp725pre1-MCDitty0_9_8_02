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

public class TransposeEffect extends RendererEffect {
	/**
	 * Number of semitones to increase or decrease note pitch
	 */
	private int steps = 0;

	public TransposeEffect(Double endTime, int steps) {
		super(endTime);
		setSteps(steps);

		applyMethod = ApplyEffect.STACK;
		// System.out.println(toString());
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	@Override
	public void apply(LinkedList<TimedJFugueElement> elements) {
		super.apply(elements);

		// Adjust each note
		for (TimedJFugueElement e : elements) {
			if (e.element instanceof Note) {
				Note note = (Note) e.element;
				
				// Adjust note pitch
				int notePitch = note.getValue();
				notePitch += steps;
				
				// Range check
				if (notePitch < 0) {
					notePitch = 0;
				} else if (notePitch > 127) {
					notePitch = 127;
				}
				
				// Apply new pitch
				note.setValue((byte)notePitch);
			}
		}
	}

	@Override
	public String toString() {
		// Okay, I royally messed up my attempt at efficiency here. I'm a secure
		// person who is comfortable admitting his faults. Fix this if you ever
		// feel like it, dear reader.
		return new StringBuilder().append(
				"TransposeEffect-EndTime=" + getEnd() + "-Endless="
						+ getEndless() + "-steps=" + steps + "-applyMethod="
						+ applyMethod + ";").toString();
	}

}
