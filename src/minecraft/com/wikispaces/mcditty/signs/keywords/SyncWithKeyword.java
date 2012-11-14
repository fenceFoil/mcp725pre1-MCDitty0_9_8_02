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
package com.wikispaces.mcditty.signs.keywords;

/**
 * @author William
 * 
 */
public class SyncWithKeyword extends ParsedKeyword {

	int voice = 0;
	// a.k.a. no layer selected; choose longest
	int layer = -1000;

	/**
	 * @param wholeKeyword
	 */
	public SyncWithKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static SyncWithKeyword parse(String currLine) {
		SyncWithKeyword k = new SyncWithKeyword(currLine);

		// Check for voice #
		int numArgs = currLine.split(" ").length;
		if (numArgs >= 2) {
			String argument = currLine.split(" ")[1];
			if (argument.matches("\\d+")) {
				k.voice = Integer.parseInt(argument);
			} else {
				k.setGoodKeyword(false);
				k.setErrorMessageType(ERROR);
				k.setErrorMessage("Follow SyncWith one or two numbers (voice and layer to sync with).");
			}
		} else {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Follow SyncWith one or two numbers (voice and layer to sync with).");
		}

		// range check
		if (k.voice < 0 || k.voice >= 16) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Voices range from 0 to 15.");
		}

		// Check for layer #
		if (numArgs >= 3) {
			String argument = currLine.split(" ")[2];
			if (argument.matches("\\d+")) {
				k.layer = Integer.parseInt(argument);
			} else {
				k.setGoodKeyword(false);
				k.setErrorMessageType(WARNING);
				k.setErrorMessage("Follow SyncWith one or two numbers (voice and layer to sync with).");
			}
		}
		
		// range check
		if (k.layer < 0 || k.layer >= 16) {
			if (k.layer != -1000) {
				k.setGoodKeyword(false);
				k.setErrorMessageType(ERROR);
				k.setErrorMessage("Layers range from 0 to 15.");
			}
		}
		
		// Check for too many args
		if (numArgs > 3) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Follow SyncWith just one or two numbers.");
		}
		
		return k;
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

}
