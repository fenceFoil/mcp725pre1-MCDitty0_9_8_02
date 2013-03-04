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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.signs.SignTuneParser;

/**
 * Measures a disco floor's size, scanning for wool blocks connected to the
 * disco floor sign. Takes time, so it is done concurrently.
 * 
 */
public class MeasureDiscoFloorThread extends Thread {

	private LinkedList<DiscoFloorDoneListener> doneListeners = new LinkedList<DiscoFloorDoneListener>();
	private DiscoFloor floor;
	private World world;

	public MeasureDiscoFloorThread(DiscoFloor d, World theWorld) {
		floor = d;
		world = theWorld;
		setName("MineTunes Disco Floor Measurer");
	}

	/**
	 * Measures the disco floor and pings all listeners when finished.
	 */
	public void run() {
		TileEntitySign anchor = floor.getAnchorEntity();
		if (anchor == null) {
			// Cannot measure sign. Ping as done
			fireDoneMeasuringEvent();
			return;
		}

		Point3D startBlock = new Point3D(anchor.xCoord, anchor.yCoord,
				anchor.zCoord);

		// Determing where to start measuring from: find the wool block this
		// sign is mounted on
		startBlock = SignTuneParser.getBlockAttachedTo(anchor);

		// Flood fill wool sheet
		// TODO: Fill a wool AREA, then remove any blocks that aren't visible

		// A temp queue of all blocks to be checked
		LinkedList<Point3D> floodFillQueue = new LinkedList<Point3D>();
		floodFillQueue.add(startBlock);

		// A result list of all found wool blocks
		LinkedList<DiscoFloorBlock> foundBlocks = new LinkedList<DiscoFloorBlock>();

		while (floodFillQueue.size() > 0) {
			Point3D currBlock = floodFillQueue.pollLast();
			// Only add if it isn't already added and the new block is wool
			if (!foundBlocks.contains(currBlock)
					&& world.getBlockId(currBlock.x, currBlock.y, currBlock.z) == Block.cloth.blockID) {
				foundBlocks.add(new DiscoFloorBlock(currBlock,
						world.getBlockMetadata(currBlock.x, currBlock.y,
								currBlock.z)));

				// Add adjacent blocks to queue to look at next
				floodFillQueue.add(new Point3D(currBlock.x + 1, currBlock.y,
						currBlock.z));
				floodFillQueue.add(new Point3D(currBlock.x - 1, currBlock.y,
						currBlock.z));
				floodFillQueue.add(new Point3D(currBlock.x, currBlock.y,
						currBlock.z + 1));
				floodFillQueue.add(new Point3D(currBlock.x, currBlock.y,
						currBlock.z - 1));
			}
		}

		ArrayList discoFloorBlockList = new ArrayList<DiscoFloorBlock>();
		discoFloorBlockList.addAll(Arrays.asList(foundBlocks.toArray()));
		floor.setBlockList(discoFloorBlockList);

		fireDoneMeasuringEvent();
	}

	private void fireDoneMeasuringEvent() {
		for (DiscoFloorDoneListener l : doneListeners) {
			l.discoFloorDoneMeasuring();
		}
	}

	public void addDiscoFloorDoneListener(
			DiscoFloorDoneListener discoFloorDoneListener) {
		doneListeners.add(discoFloorDoneListener);
	}
}
