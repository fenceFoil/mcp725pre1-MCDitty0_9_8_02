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
import com.minetunes.ditty.event.PlayMidiDittyEvent;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.ParsedSign;

/**
 *
 */
public class PlayMidiKeyword extends BaseMidiKeyword {

	public PlayMidiKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Play a midi
		int eventID = ditty.addDittyEvent(new PlayMidiDittyEvent(getMidiFile(),
				ditty.getDittyID()));
		ditty.addMusicStringTokens(readMusicString,
				SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);

		return null;
	}

}
