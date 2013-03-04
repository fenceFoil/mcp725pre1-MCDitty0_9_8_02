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
package com.minetunes.signs.keywords;

import java.io.File;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import org.jfugue.elements.Note;

import com.minetunes.Point3D;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.event.SFXInstrumentOffEvent;
import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.ParsedSign;
import com.minetunes.signs.SignParser;

/**
 * 
 *
 */
public class SFXInstOffKeyword extends SignTuneKeyword {

	/**
	 * The instrument number to replace with a sound effect
	 */
	private int instrument;

	/**
	 * @param wholeKeyword
	 */
	public SFXInstOffKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		// Parse the first (and only) line of a SFXInstOff keyword

		// Get the instrument number
		String[] args = getWholeKeyword().split(" ");
		int numArgs = args.length;

		if (numArgs > 2) {
			// Too many arguments
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Only the instrument number is needed on the first line.");
		} else if (numArgs <= 1) {
			// No instrument number
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow SFXInstOff with an instrument number.");
			return;
		} else {
			// Instrument number given
			String argument = args[1];
			if (argument.trim().matches("\\d+")) {
				setInstrument(Integer.parseInt(argument));
			} else {
				// Error: invalid agument
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Follow SFXInstOff with an instrument number.");
				return;
			}

			if (getInstrument() < 0 || getInstrument() > 127) {
				// Out of bounds
				setInstrument(0);
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Instrument numbers range from 0 to 127.");
				return;
			}
		}
	}

	public int getInstrument() {
		return instrument;
	}

	public void setInstrument(int instrument) {
		this.instrument = instrument;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Add keyword to schedule
		int eventID = ditty.addDittyEvent(new SFXInstrumentOffEvent(this, -1,
				ditty.getDittyID()));
		ditty.addMusicStringTokens(readMusicString,
				SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);
		return null;
	}

}
