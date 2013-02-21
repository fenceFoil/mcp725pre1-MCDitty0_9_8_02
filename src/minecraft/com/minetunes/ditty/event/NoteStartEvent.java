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

import org.jfugue.elements.Note;

import com.minetunes.Point3D;

public class NoteStartEvent extends TimedDittyEvent {

	private Point3D location;
	private int voice;
	private int layer;
	private Note note;

	public NoteStartEvent(Point3D signLocation, long time, int voice,
			int layer, Note note, int dittyID) {
		super(dittyID);
		location = signLocation;
		this.timeToPlay = time;
		setVoice(voice);
		setLayer(layer);
		setNote(note);
	}

	public void setNote(Note note2) {
		note = note2;
	}

	/**
	 * @return the location
	 */
	public Point3D getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Point3D location) {
		this.location = location;
	}

	/**
	 * @return the voice
	 */
	public int getVoice() {
		return voice;
	}

	/**
	 * @param voice
	 *            the voice to set
	 */
	public void setVoice(int voice) {
		this.voice = voice;
	}

	/**
	 * @return the layer
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * @param layer
	 *            the layer to set
	 */
	public void setLayer(int layer) {
		this.layer = layer;
	}

	public Note getNote() {
		return note;
	}
}
