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
package com.minetunes.particle;

import com.minetunes.Point3D;

/**
 * Requests that a particle be created in the world at a given time in a ditty.
 * Particles cannot be created outside of the main game loop, so things like
 * ditty players make these requests and file them with MineTunes, which sorts
 * through them every tick.
 */
public class ParticleRequest {
	private Point3D location;
	private String particleType;
	private long time;
	private boolean instant = true;
	private int dittyID = -1;

	private double locationVariance = 0.3d;

	public void setLocationVariance(double locationVariance) {
		this.locationVariance = locationVariance;
	}

	public int getDittyID() {
		return dittyID;
	}

	public void setDittyID(int dittyID) {
		this.dittyID = dittyID;
	}

	public ParticleRequest(Point3D location, String particleType) {
		setLocation(location);
		setParticleType(particleType);
	}

	public long getTime() {
		return time;
	}

	/**
	 * Automatically turns off the "instant" flag.
	 * 
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
		instant = false;
	}

	public boolean isInstant() {
		return instant;
	}

	public void setInstant(boolean instant) {
		this.instant = instant;
	}

	public ParticleRequest(Point3D location) {
		setLocation(location);
	}

	public Point3D getLocation() {
		return location;
	}

	public void setLocation(Point3D location) {
		this.location = location;
	}

	public String getParticleType() {
		return particleType;
	}

	public void setParticleType(String particleType) {
		this.particleType = particleType;
	}

	public double getLocationVariance() {
		return locationVariance;
	}

}
