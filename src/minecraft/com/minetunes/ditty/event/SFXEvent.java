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

/**
 * Requests that a sound effect be played.
 *
 */
public class SFXEvent extends TimedDittyEvent {

	private String soundEffect;
	private float pitch = 1.0f;
	
	/**
	 * Not influenced by the Minecraft volume, reflects the music volume such as
	 * that adjusted by the Volume keyword.
	 */
	private float volume = 1.0f;

	/**
	 * @return the soundEffect
	 */
	public String getSoundEffect() {
		return soundEffect;
	}

	/**
	 * @param soundEffect
	 *            the soundEffect to set
	 */
	public void setSoundEffect(String soundEffect) {
		this.soundEffect = soundEffect.toLowerCase();
	}

	public SFXEvent(String effectName, long time, int dittyID) {
		super(dittyID);
		soundEffect = effectName.toLowerCase();
		setTime(time);
	}

	public SFXEvent(String effectName, long time, float pitch,
			float volume, int dittyID) {
		super(dittyID);
		soundEffect = effectName.toLowerCase();
		setTime(time);
		setVolume(volume);
		setPitch(pitch);
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

}
