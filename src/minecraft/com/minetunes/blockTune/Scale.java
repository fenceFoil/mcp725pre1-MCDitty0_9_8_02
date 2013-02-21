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
package com.minetunes.blockTune;

/**
 *
 */
public class Scale {
	public static final int[] PENTATONIC_STEPS = { 2, 2, 3, 2, 3 };
	public static final int[] DIATONIC_STEPS = { 2, 2, 1, 2, 2, 2, 1 };
	public static final int[] CHROMATIC_STEPS = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1 };

	public static final Scale PENTATONIC_MAJOR = new Scale(PENTATONIC_STEPS, 0);
	public static final Scale PENTATONIC_MINOR = new Scale(PENTATONIC_STEPS, 4);
	public static final Scale DIATONIC_MAJOR = new Scale(PENTATONIC_STEPS, 0);
	public static final Scale DIATONIC_MINOR = new Scale(PENTATONIC_STEPS, 5);
	public static final Scale CHROMATIC = new Scale(CHROMATIC_STEPS, 0);

	// public static final int MAJOR_DIATONIC = 0;
	// public static final int MAJOR_PENTA = 0;
	// public static final int MINOR_PENTA = 4;
	// public static final int MINOR_DIA = 5;

	// One octave below middle C: C4 -> 48
	private int baseNote = 48;
	private int[] steps = PENTATONIC_STEPS;
	private int mode = 0;

	public Scale(int[] steps, int mode) {
		this.steps = steps;
		setMode(mode);
	}

	public Scale(int[] steps, int mode, int baseNote) {
		this.steps = steps;
		setMode(mode);
		setBaseNote(baseNote);
	}

	public int getNoteForStep(int step) {
		int note = baseNote;
		for (int i = 0; i < step; i++) {
			note += steps[((i + mode) % steps.length)];
		}
		return note;
	}

	/**
	 * @param value
	 */
	public void setBaseNote(int value) {
		baseNote = value;
	}

	public int getBaseNote() {
		return baseNote;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int[] getSteps() {
		return steps;
	}

	public int getNumSteps() {
		if (steps == null) {
			return 0;
		} else {
			return steps.length;
		}
	}

	public void setSteps(int[] steps) {
		this.steps = steps;
	}

	@Override
	public Scale clone() {
		Scale newScale = new Scale(getSteps(), getMode(), getBaseNote());
		return newScale;
	}

	public static double getNoteFreq(double noteValue) {
		return 8.1757989156 * Math.pow(2.0, noteValue / 12.0);
	}

}
