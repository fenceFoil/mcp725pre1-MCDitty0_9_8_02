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

import java.util.ArrayList;

/**
 * 
 *
 */
public class DiscoKeyword extends ParsedKeyword {

	private ArrayList<Integer> voices = new ArrayList<Integer>();

	public DiscoKeyword(String wholeKeyword) {
		super(wholeKeyword);

		// By default, follow all voices
		for (int i=0;i<16;i++) {
			voices.add(i);
		}
	}
	
	public static DiscoKeyword parse (String args) {
		
		String[] argsSplit = args.split(" ");
		int numArgs = argsSplit.length;
		
		// Sets all voices to true; sets all default settings
		DiscoKeyword k = new DiscoKeyword(args);
		
		if (numArgs <= 1) {
			// No args; return a default disco keywords
			return k;
		} else {
			// There are arguments!
			// All must be numbers
			// Reset voices
			k.getVoices().clear();
			for (int i=1;i<numArgs;i++) {
				if (!argsSplit[i].matches("\\d+")) {
					k.setGoodKeyword(false);
					k.setErrorMessageType(ERROR);
					k.setErrorMessage("Follow disco with the numbers of the voices you want to beat to.");
					return k;
				} else {
					// Valid number
					Integer voice = Integer.parseInt(argsSplit[i]);
					if (voice < 0 || voice > 15) {
						// Voice out of range
						k.setGoodKeyword(false);
						k.setErrorMessageType(ERROR);
						k.setErrorMessage("Voices range from 0 to 15.");
						return k;
					} else {
						// Valid voice!
						k.getVoices().add(voice);
					}
				}
			}
		}
		
		return k;
	}

	public ArrayList<Integer> getVoices() {
		return voices;
	}

	public void setVoices(ArrayList<Integer> voices) {
		this.voices = voices;
	}

}
