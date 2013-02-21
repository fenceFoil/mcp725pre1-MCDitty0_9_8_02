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

import com.minetunes.signs.keywords.SFXInstOffKeyword;

/**
 *
 */
public class SFXInstrumentOffEvent extends TimedDittyEvent {

	//private SFXInstOffKeyword keyword;
	private long createdTime = 0;
	
	private int instrument = 0;

	/**
	 * @param emitterLocation
	 * 
	 */
	public SFXInstrumentOffEvent(SFXInstOffKeyword k, int createdTime,
			int dittyID) {
		super(dittyID);
		//setKeyword(k);
		setInstrument(k.getInstrument());
		setCreatedTime(createdTime);
	}
	
	public SFXInstrumentOffEvent(int instrument, int createdTime, int dittyID) {
		super(dittyID);
		setInstrument(instrument);
		setCreatedTime(createdTime);
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

//	public SFXInstOffKeyword getKeyword() {
//		return keyword;
//	}
//
//	public void setKeyword(SFXInstOffKeyword keyword) {
//		this.keyword = keyword;
//	}

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

}
