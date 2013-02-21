/**
 * CHANGES FROM MOJANG CODE
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
package com.minetunes;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import com.minetunes.config.MinetunesConfig;
import com.minetunes.noteblocks.BlockNoteMinetunes;

import net.minecraft.src.Packet62LevelSound;

public class Packet62LevelSoundMinetunes extends Packet62LevelSound {
	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	public void readPacketData(DataInputStream par1DataInputStream)
			throws IOException {
		super.readPacketData(par1DataInputStream);

		// Handle noteblock muting
		if (MinetunesConfig.getBoolean("noteblock.mute")) {
			if (getSoundName().startsWith("note.")) {
				setNoteNameWithReflection("");
			}
		} else if (getSoundName().startsWith("note.")) {
			// Handle noteblock octave adjustment
			int adjust = BlockNoteMinetunes.getOctaveAdjust(
					(int) (getEffectX() - 0.5), (int) (getEffectY() - 0.5),
					(int) (getEffectZ() - 0.5));
			if (adjust != 0) {
				String newSoundName = getSoundName() + "_" + adjust + "o";
				setNoteNameWithReflection(newSoundName);
			}
		}
	}

	private void setNoteNameWithReflection(String value) {
		try {
			Finder.setUniqueTypedFieldFromClass(Packet62LevelSound.class,
					String.class, this, value);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
