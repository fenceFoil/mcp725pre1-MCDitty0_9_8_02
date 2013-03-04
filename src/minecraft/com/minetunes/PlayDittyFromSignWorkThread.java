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
package com.minetunes;

import java.util.LinkedList;

import net.minecraft.src.World;

import com.minetunes.signs.SignTuneParser;

/**
 * Parses and starts a sign ditty from a thread off of the main game loop.
 * 
 */
public class PlayDittyFromSignWorkThread extends Thread {
	private World w;
	private int x;
	private int y;
	private int z;
	private boolean one;
	private boolean silent;
	private LinkedList<Point3D> limitToSigns;

	public PlayDittyFromSignWorkThread(World world, int x, int y, int z,
			boolean oneAtATimeOn, boolean silent,
			LinkedList<Point3D> limitToSigns) {
		this.w = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.one = oneAtATimeOn;
		this.limitToSigns = limitToSigns;

		setName("MineTunes Sign Ditty Reader - " + x + ":" + y + ":" + z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		SignTuneParser.playDittyFromSignsDoWork(w, x, y, z, one, silent,
				limitToSigns);
	}

}
