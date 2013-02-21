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
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityFireworkRocket;
import net.minecraft.src.WorldClient;

/**
 * Handles exploding firework rockets after they reach a certain height.
 * 
 */
public class FireworkExploder implements TickListener {

	protected static final Random rand = new Random();

	private class Firework {
		EntityFireworkRocket rocket;
		double startY;
		byte flightDuration;
		double startTime = System.currentTimeMillis();
		double endTime;

		public Firework(EntityFireworkRocket rocket, double startY,
				byte flightDuration) {
			super();
			this.rocket = rocket;
			this.startY = startY;
			this.flightDuration = flightDuration;
			this.endTime = startTime + (rand.nextDouble() * 0.5 + 1.0) * 500
					* flightDuration;
		}

	}

	private LinkedList<Firework> fireworks = new LinkedList<Firework>();

	public void add(EntityFireworkRocket fireworkEntity, double y,
			byte flightDuration) {
		fireworks.add(new Firework(fireworkEntity, y, flightDuration));
	}

	private void update(WorldClient world) {
		for (int i = 0; i < fireworks.size(); i++) {
			Firework f = fireworks.get(i);
			if (f.endTime < System
					.currentTimeMillis()) {
				f.rocket.handleHealthUpdate((byte) 17);
				f.rocket.setDead();
				fireworks.remove(i);
				i--;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.wikispaces.MineTunes.TickListener#onTick(float, net.minecraft.client.Minecraft)
	 */
	@Override
	public boolean onTick(float partialTick, Minecraft minecraft) {
		update (minecraft.theWorld);
		return true;
	}

}
