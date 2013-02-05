/**
 * Copyright (c) 2012-2013 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.blockTune;

import org.jfugue.elements.Note;

/**
 *
 */
public class Scale {
	private int baseNote = Note.createNote("C5").getValue();
	private int[] steps = { 2, 2, 3, 2, 3 };

	public Scale() {

	}

	public int getNoteForStep(int step) {
		int note = baseNote;
		for (int i = 0; i < step; i++) {
			note += steps[(i % steps.length)];
		}
		return note;
	}
}
