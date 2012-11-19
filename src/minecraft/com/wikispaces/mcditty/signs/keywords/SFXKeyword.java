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

import com.wikispaces.mcditty.sfx.SFXManager;

/**
 * @author William
 * 
 */
public class SFXKeyword extends ParsedKeyword {

	String effectName = "";
	String effectShorthand = "";

	/**
	 * @param wholeKeyword
	 */
	public SFXKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static SFXKeyword parse(String currLine) {
		SFXKeyword k = new SFXKeyword(currLine);

		String[] arguments = currLine.split(" ");

		String effectName = "";

		if (arguments.length <= 1) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Follow SFX with an effect to play. Zombie is fun.");
		} else {
			effectName = arguments[1];
			for (int i = 2; i < arguments.length; i++) {
				effectName += " " + arguments[i];
			}
		}

		k.setEffectShorthand(effectName);

		String soundEffect = SFXManager.getEffectForShorthandName(effectName, SFXManager.getLatestSource());
		if (soundEffect == null) {
			k.setGoodKeyword(true);
			k.setErrorMessageType(WARNING);
			if (!effectName.trim().equals("")) {
				k.setErrorMessage("No SFX named " + effectName + " was found.");
			} else {
				k.setErrorMessage("Add an effect name.");
			}
		}
		k.setEffectName(soundEffect);

		return k;
	}

	/**
	 * @return the effectName
	 */
	public String getEffectName() {
		return effectName;
	}

	/**
	 * @param effectName
	 *            the effectName to set
	 */
	public void setEffectName(String effectName) {
		this.effectName = effectName;
	}

	/**
	 * @return the effectShorthand
	 */
	public String getEffectShorthand() {
		return effectShorthand;
	}

	/**
	 * @param effectShorthand
	 *            the effectShorthand to set
	 */
	public void setEffectShorthand(String effectShorthand) {
		this.effectShorthand = effectShorthand;
	}

}
