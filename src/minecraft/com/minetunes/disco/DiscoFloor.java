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
package com.minetunes.disco;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import com.minetunes.ditty.event.NoteStartEvent;
import com.minetunes.resources.ResourceManager;
import com.minetunes.sfx.SFXManager;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

/**
 * Represents a disco floor: the blocks that compose it, the attached ditty,
 * etc.
 */
public class DiscoFloor {

	private static ArrayList<Integer[]> pulsePalettes = new ArrayList<Integer[]>();
	static {
		try {
			// System.out.println("MineTunes: Loading Disco Floor Pallets");
			// Read in the palettes file, a string, line by line with a Reader
			// (for that ever-handy readLine method)
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(ResourceManager
							.loadCached("disco/discoFloorPalettes.txt")
							.getBytes())));
			while (true) {
				String inLine = reader.readLine();
				if (inLine == null || inLine.toLowerCase().startsWith("end")) {
					break;
				}
				String[] readColors = inLine.split(":");
				// System.out.println (readColors[0]);
				Integer[] readValues = new Integer[readColors.length];
				for (int i = 0; i < readColors.length; i++) {
					readValues[i] = Integer.parseInt(readColors[i]);
				}
				pulsePalettes.add(readValues);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int dittyID = -1;
	private ArrayList<Integer> voices = new ArrayList<Integer>();
	private ArrayList<DiscoFloorBlock> blockList = new ArrayList<DiscoFloorBlock>();
	private TileEntitySign anchorEntity;

	// public DiscoFloor () {
	//
	// }

	public DiscoFloor(TileEntitySign entityOfStartSign,
			ArrayList<Integer> voices) {
		anchorEntity = entityOfStartSign;
		setVoices(voices);
	}

	/**
	 * @return the blockList
	 */
	public ArrayList<DiscoFloorBlock> getBlockList() {
		return blockList;
	}

	/**
	 * @param blockList
	 *            the blockList to set
	 */
	public void setBlockList(ArrayList<DiscoFloorBlock> blockList) {
		this.blockList = blockList;
	}

	/**
	 * @return the dittyID
	 */
	public int getDittyID() {
		return dittyID;
	}

	/**
	 * @param dittyID
	 *            the dittyID to set
	 */
	public void setDittyID(int dittyID) {
		this.dittyID = dittyID;
	}

	/**
	 * @return the voices
	 */
	public ArrayList<Integer> getVoices() {
		return voices;
	}

	/**
	 * @param voices
	 *            the voices to set
	 */
	public void setVoices(ArrayList<Integer> voices) {
		this.voices = voices;
	}

	/**
	 * @return the anchorEntity
	 */
	public TileEntitySign getAnchorEntity() {
		return anchorEntity;
	}

	/**
	 * @param anchorEntity
	 *            the anchorEntity to set
	 */
	public void setAnchorEntity(TileEntitySign anchorEntity) {
		this.anchorEntity = anchorEntity;
	}

	public void turnOff(World world) {
		for (DiscoFloorBlock b : blockList) {
			if (world.getBlockId(b.x, b.y, b.z) == Block.cloth.blockID) {
				world.setBlockMetadataWithNotify(b.x, b.y, b.z,
						b.originalBlockMeta);
			}
		}
	}

	private Random rand = new Random();

	private long lastPulseTime = -1;

	public void pulse(World world, NoteStartEvent noteEvent) {
		// System.out.println("Pulsing disco floor");
		if (noteEvent.getTime() > lastPulseTime) {
			lastPulseTime = noteEvent.getTime();

			Integer[] pulsePallet = pulsePalettes.get(rand
					.nextInt(pulsePalettes.size()));
			for (DiscoFloorBlock b : blockList) {
				if (world.getBlockId(b.x, b.y, b.z) == Block.cloth.blockID) {
					world.setBlockMetadataWithNotify(b.x, b.y, b.z,
							pulsePallet[rand.nextInt(pulsePallet.length)]);
				}
			}
		}
	}
}
