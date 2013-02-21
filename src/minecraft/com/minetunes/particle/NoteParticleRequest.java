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
package com.minetunes.particle;

import com.minetunes.Point3D;

public class NoteParticleRequest extends ParticleRequest {
	private double noteColor = 0d;
	private boolean randomColor = true;
	
	public NoteParticleRequest (Point3D location, double noteColor, boolean randomColor) {
		super (location);
		setNoteColor(noteColor);
		setRandomColor(randomColor);
	}

	public double getNoteColor() {
		return noteColor;
	}

	public void setNoteColor(double noteColor) {
		this.noteColor = noteColor;
	}

	public boolean isRandomColor() {
		return randomColor;
	}

	public void setRandomColor(boolean randomColor) {
		this.randomColor = randomColor;
	}
}
