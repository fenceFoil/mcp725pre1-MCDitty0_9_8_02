/**
 * Copyright (c) 2012 William Karnavas 
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
package com.wikispaces.mcditty.ditty.event;

import com.wikispaces.mcditty.signs.keywords.SFXInstKeyword;

/**
 *
 */
public class SFXInstrumentEvent extends TimedDittyEvent {

	private int instrument;
	private String sfxFilename;
	private String sfxName;
	private String sfxNameIncomplete;
	private int sfxNumber;
	private int centerPitch;

	// private SFXInstKeyword keyword;
	private long createdTime = 0;
	private int sfxSource;

	/**
	 * @param emitterLocation
	 * 
	 */
	public SFXInstrumentEvent(SFXInstKeyword keyword, int createdTime,
			int dittyID) {
		super(dittyID);
		// setKeyword(keyword);
		setCreatedTime(createdTime);
		instrument = keyword.getInstrument();
		sfxFilename = keyword.getSfxFilename();
		sfxName = keyword.getSFXName();
		sfxNameIncomplete = keyword.getSFXNameIncomplete();
		sfxNumber = keyword.getSFXNumber();
		centerPitch = keyword.getCenterPitch();
		sfxSource = keyword.getSFXSource();
	}

	public SFXInstrumentEvent(int inst, String sfxFile, String sfx,
			String sfxIncomplete, int sfxNum, int source, int tuning,
			int createdTime, int dittyID) {
		super(dittyID);
		setCreatedTime(createdTime);
		instrument = inst;
		sfxFilename = sfxFile;
		sfxName = sfx;
		sfxNameIncomplete = sfxIncomplete;
		sfxNumber = sfxNum;
		sfxSource = source;
		centerPitch = tuning;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	// public SFXInstKeyword getKeyword() {
	// return keyword;
	// }
	//
	// public void setKeyword(SFXInstKeyword keyword) {
	// this.keyword = keyword;
	// }

	/**
	 * This event needs access to the synthesizer to change instruments.
	 */
	@Override
	public boolean isExecutedAtPlayerLevel() {
		return true;
	}

	public int getInstrument() {
		return instrument;
	}

	public void setInstrument(int instrument) {
		this.instrument = instrument;
	}

	public String getSfxFilename() {
		return sfxFilename;
	}

	public void setSfxFilename(String sfxFilename) {
		this.sfxFilename = sfxFilename;
	}

	public String getSfxName() {
		return sfxName;
	}

	public void setSfxName(String sfxName) {
		this.sfxName = sfxName;
	}

	public String getSfxNameIncomplete() {
		return sfxNameIncomplete;
	}

	public void setSfxNameIncomplete(String sfxNameIncomplete) {
		this.sfxNameIncomplete = sfxNameIncomplete;
	}

	public int getSfxNumber() {
		return sfxNumber;
	}

	public void setSfxNumber(int sfxNumber) {
		this.sfxNumber = sfxNumber;
	}

	public int getCenterPitch() {
		return centerPitch;
	}

	public void setCenterPitch(int centerPitch) {
		this.centerPitch = centerPitch;
	}

	public int getSfxSource() {
		return sfxSource;
	}

	public void setSfxSource(int source) {
		sfxSource = source;
	}

}
