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

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.SignTuneParser;

/**
 * @author William
 * 
 */
public class SyncWithKeyword extends SignTuneKeyword {

	int voice = 0;
	// a.k.a. no layer selected; choose longest
	int layer = -1000;

	/**
	 * @param wholeKeyword
	 */
	public SyncWithKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		// Check for voice #
		int numArgs = getWholeKeyword().split(" ").length;
		if (numArgs >= 2) {
			String argument = getWholeKeyword().split(" ")[1];
			if (argument.matches("\\d+")) {
				voice = Integer.parseInt(argument);
			} else {
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Follow SyncWith one or two numbers (voice and layer to sync with).");
			}
		} else {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow SyncWith one or two numbers (voice and layer to sync with).");
		}

		// range check
		if (voice < 0 || voice >= 16) {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Voices range from 0 to 15.");
		}

		// Check for layer #
		if (numArgs >= 3) {
			String argument = getWholeKeyword().split(" ")[2];
			if (argument.matches("\\d+")) {
				layer = Integer.parseInt(argument);
			} else {
				setGoodKeyword(false);
				setErrorMessageType(WARNING);
				setErrorMessage("Follow SyncWith one or two numbers (voice and layer to sync with).");
			}
		}
		
		// range check
		if (layer < 0 || layer >= 16) {
			if (layer != -1000) {
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Layers range from 0 to 15.");
			}
		}
		
		// Check for too many args
		if (numArgs > 3) {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow SyncWith just one or two numbers.");
		}
	}

	/**
	 * @return the voice
	 */
	public int getVoice() {
		return voice;
	}

	/**
	 * @param voice the voice to set
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
	 * @param layer the layer to set
	 */
	public void setLayer(int layer) {
		this.layer = layer;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTilEntity, Point3D nextSign, World world, StringBuilder readMusicString) {

		// Finally, add token
		if (getLayer() != -1000) {
			ditty.addMusicStringTokens(readMusicString,
					SignTuneParser.SYNC_WITH_TOKEN + "V" + getVoice() + "L"
							+ getLayer(), false);
		} else {
			ditty.addMusicStringTokens(
					readMusicString,
					SignTuneParser.SYNC_WITH_TOKEN + "V" + getVoice() + "Lu",
					false);
		}
		
		return null;
	}

}
