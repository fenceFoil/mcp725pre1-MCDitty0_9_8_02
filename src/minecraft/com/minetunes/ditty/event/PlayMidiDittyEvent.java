/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.ditty.event;

import java.io.File;

/**
 * Requests a midi to be played from a file.
 */
public class PlayMidiDittyEvent extends TimedDittyEvent {
	
	private File midiFile = null;

	public PlayMidiDittyEvent (File midiFile, int dittyID) {
		super(dittyID);
		setMidiFile(midiFile);
	}

	public File getMidiFile() {
		return midiFile;
	}

	public void setMidiFile(File midiFile) {
		this.midiFile = midiFile;
	}
}
