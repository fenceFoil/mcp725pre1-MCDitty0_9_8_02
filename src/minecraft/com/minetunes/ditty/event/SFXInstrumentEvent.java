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
package com.minetunes.ditty.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.keywords.SFXInstKeyword;

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
		// updateTestMode();
	}

	// private static LinkedList<String> effectsToTest = new
	// LinkedList<String>();
	// private static LinkedList<Integer> effectNums = new
	// LinkedList<Integer>();
	// static {
	// Set<String> effectNames = SFXManager.getAllEffects(1).keySet();
	//
	// boolean startYet = false;
	// for (String s : effectNames) {
	// for (int i = 1; i < 9; i++) {
	// if (SFXManager.doesEffectExist(
	// SFXManager.getEffectForShorthandName(s, 1), i, 1)) {
	// System.out.println(s + ":" + i + " Added");
	// if (startYet) {
	// effectsToTest.add(s);
	// effectNums.add(i);
	// }
	// } else {
	// System.out
	// .println("XXX " + s + ":" + i + " Does not exist");
	// break;
	// }
	// }
	//
	// if (true || s.equals("thunder")) {
	// startYet = true;
	// }
	// }
	// }
	//
	// /**
	// * Used in a temporary test of all sound effects
	// */
	// private void updateTestMode() {
	// String effect = effectsToTest.poll();
	// if (effect != null) {
	// int num = effectNums.poll();
	//
	// sfxFilename = SFXManager.getEffectFilename(effect, num, 1);
	// sfxSource = 1;
	// sfxName = effect;
	// sfxNameIncomplete = sfxName;
	// sfxNumber = num;
	//
	// System.out.println(sfxName + ":" + sfxNumber);
	// } else {
	// System.exit(-500);
	// }
	// }

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
