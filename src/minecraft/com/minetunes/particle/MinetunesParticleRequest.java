/**
 * Copyright (c) 2012-2013 William Karnavas 
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

import java.util.HashMap;

import com.minetunes.Point3D;

/**
 * @author William
 * 
 */
public class MinetunesParticleRequest extends ParticleRequest {

	private int index = 0;

	protected static final HashMap<String, Integer> indexNames = new HashMap<String, Integer>();
	static {
		indexNames.put("save", 0);
		indexNames.put("diverge", 1);
		indexNames.put("merge", 2);
		indexNames.put("arrow", 3);
		indexNames.put("end", 4);
		indexNames.put("loop", 5);
		indexNames.put("time", 6);
		indexNames.put("star", 7);
		indexNames.put("pause", 8);
		indexNames.put("mute", 9);
		indexNames.put("text", 10);
		indexNames.put("sound", 11);

		indexNames.put("firework", 16);
	}

	protected double xzMean, xzSD, yMean, ySD;

	/**
	 * @param location
	 * @param particleType
	 */
	public MinetunesParticleRequest(Point3D location, String particleType,
			double xzMean, double xzSD, double yMean, double ySD, double locationVariance) {
		super(location, particleType);
		setLocationVariance(locationVariance);
	}

	/**
	 * @param location
	 */
	public MinetunesParticleRequest(Point3D location) {
		super(location);
	}

	public int getIndex() {
		index = indexNames.get(getParticleType());
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getXzMean() {
		return xzMean;
	}

	public void setXzMean(double xzMean) {
		this.xzMean = xzMean;
	}

	public double getXzSD() {
		return xzSD;
	}

	public void setXzSD(double xzSD) {
		this.xzSD = xzSD;
	}

	public double getyMean() {
		return yMean;
	}

	public void setyMean(double yMean) {
		this.yMean = yMean;
	}

	public double getySD() {
		return ySD;
	}

	public void setySD(double ySD) {
		this.ySD = ySD;
	}

}
