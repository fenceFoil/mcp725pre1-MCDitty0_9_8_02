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

import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.midi.Patch;

/**
 * Represents a single moment where notes are struck in a BlockTune. In a normal
 * BlockTune, represents a column of blocks.
 * 
 */
public class Frame {
	/**
	 * Length of this frame, where one beat = 1 and instant = 0
	 */
	private double length;

	/**
	 * Notes started for a given instrument
	 */
	private HashMap<Integer, LinkedList<Byte>> channelNotesStarted = new HashMap<Integer, LinkedList<Byte>>();
	
	public Frame(double length) {
		setLength(length);
	}

	public void addNoteStart(int channel, byte note) {
		LinkedList<Byte> notes = channelNotesStarted.get(channel);
		if (notes == null) {
			notes = new LinkedList<Byte>();
		}
		notes.add(note);
		channelNotesStarted.put(channel, notes);
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public HashMap<Integer, LinkedList<Byte>> getChannelNotesStarted() {
		return channelNotesStarted;
	}

	public void setChannelNotesStarted(
			HashMap<Integer, LinkedList<Byte>> channelNotesStarted) {
		this.channelNotesStarted = channelNotesStarted;
	}
}
