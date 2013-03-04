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
import com.minetunes.ditty.event.SFXEvent;
import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.SignTuneParser;

/**
 * @author William
 * 
 */
public class SFXKeyword extends SignTuneKeyword {

	String effectName = "";
	String effectShorthand = "";

	/**
	 * @param wholeKeyword
	 */
	public SFXKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		String[] arguments = getWholeKeyword().split(" ");

		String effectName = "";

		if (arguments.length <= 1) {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow SFX with an effect to play. Zombie is fun.");
		} else {
			effectName = arguments[1];
			for (int i = 2; i < arguments.length; i++) {
				effectName += " " + arguments[i];
			}
		}

		setEffectShorthand(effectName);

		String soundEffect = SFXManager.getEffectForShorthandName(effectName,
				SFXManager.getLatestSource());
		if (soundEffect == null) {
			setGoodKeyword(true);
			setErrorMessageType(WARNING);
			if (!effectName.trim().equals("")) {
				setErrorMessage("No SFX named " + effectName + " was found.");
			} else {
				setErrorMessage("Add an effect name.");
			}
		}
		setEffectName(soundEffect);
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

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Add event
		int eventID = ditty.addDittyEvent(new SFXEvent(getEffectName(), -1,
				ditty.getDittyID()));
		// Add token
		ditty.addMusicStringTokens(readMusicString,
				SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);

		return null;
	}

}
