/**
 * CHANGES FROM MOJANG CODE
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
package com.wikispaces.mcditty;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.noteblocks.BlockNoteMCDitty;

import net.minecraft.src.Packet62LevelSound;

public class Packet62LevelSoundMCDitty extends Packet62LevelSound {
	// /** e.g. step.grass */
	// private String soundName;
	//
	// /** Effect X multiplied by 8 */
	// private int effectX;
	//
	// /** Effect Y multiplied by 8 */
	// private int effectY = Integer.MAX_VALUE;
	//
	// /** Effect Z multiplied by 8 */
	// private int effectZ;
	//
	// /** 1 is 100%. Can be more. */
	// private float volume;
	//
	// /** 63 is 100%. Can be more. */
	// private int pitch;
	//
	// public Packet62LevelSoundMCDitty() {}
	//
	// public Packet62LevelSoundMCDitty(String par1Str, double par2, double
	// par4, double par6, float par8, float par9)
	// {
	// this.soundName = par1Str;
	// this.effectX = (int)(par2 * 8.0D);
	// this.effectY = (int)(par4 * 8.0D);
	// this.effectZ = (int)(par6 * 8.0D);
	// this.volume = par8;
	// this.pitch = (int)(par9 * 63.0F);
	//
	// if (this.pitch < 0)
	// {
	// this.pitch = 0;
	// }
	//
	// if (this.pitch > 255)
	// {
	// this.pitch = 255;
	// }
	// }
	//
	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	public void readPacketData(DataInputStream par1DataInputStream)
			throws IOException {
		super.readPacketData(par1DataInputStream);

		// Handle noteblock muting
		if (MCDittyConfig.getBoolean("noteblock.mute")) {
			if (getSoundName().startsWith("note.")) {
				setNoteNameWithReflection("");
			}
		} else if (getSoundName().startsWith("note.")) {
			// Handle noteblock octave adjustment
			int adjust = BlockNoteMCDitty.getOctaveAdjust(
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
	//
	// /**
	// * Abstract. Writes the raw packet data to the data stream.
	// */
	// public void writePacketData(DataOutputStream par1DataOutputStream) throws
	// IOException
	// {
	// writeString(this.soundName, par1DataOutputStream);
	// par1DataOutputStream.writeInt(this.effectX);
	// par1DataOutputStream.writeInt(this.effectY);
	// par1DataOutputStream.writeInt(this.effectZ);
	// par1DataOutputStream.writeFloat(this.volume);
	// par1DataOutputStream.writeByte(this.pitch);
	// }
	//
	// public String getSoundName()
	// {
	// return this.soundName;
	// }
	//
	// public double getEffectX()
	// {
	// return (double)((float)this.effectX / 8.0F);
	// }
	//
	// public double getEffectY()
	// {
	// return (double)((float)this.effectY / 8.0F);
	// }
	//
	// public double getEffectZ()
	// {
	// return (double)((float)this.effectZ / 8.0F);
	// }
	//
	// public float getVolume()
	// {
	// return this.volume;
	// }
	//
	// /**
	// * Gets the pitch divided by 63 (63 is 100%)
	// */
	// public float getPitch()
	// {
	// return (float)this.pitch / 63.0F;
	// }
	//
	// /**
	// * Passes this Packet on to the NetHandler for processing.
	// */
	// public void processPacket(NetHandler par1NetHandler)
	// {
	// par1NetHandler.handleLevelSound(this);
	// }
	//
	// /**
	// * Abstract. Return the size of the packet (not counting the header).
	// */
	// public int getPacketSize()
	// {
	// return 24;
	// }
}
