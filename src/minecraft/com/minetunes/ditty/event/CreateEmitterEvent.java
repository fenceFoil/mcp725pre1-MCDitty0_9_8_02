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
package com.minetunes.ditty.event;

import com.minetunes.Point3D;
import com.minetunes.signs.keywords.EmitterKeyword;

/**
 * Signals that an emitter should be created.
 */
public class CreateEmitterEvent extends TimedDittyEvent {
	
	private EmitterKeyword emitterKeyword;
	private long createdTime = 0;
	private Point3D emitterLocation = new Point3D();

	/**
	 * @param emitterLocation 
	 * 
	 */
	public CreateEmitterEvent(EmitterKeyword emitterKeyword, int createdTime, int dittyID, Point3D emitterLocation) {
		super(dittyID);
		setEmitterKeyword(emitterKeyword);
		setCreatedTime(createdTime);
		setEmitterLocation(emitterLocation);
	}

	public EmitterKeyword getEmitterKeyword() {
		return emitterKeyword;
	}

	public void setEmitterKeyword(EmitterKeyword emitterKeyword) {
		this.emitterKeyword = emitterKeyword;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public Point3D getEmitterLocation() {
		return emitterLocation;
	}

	public void setEmitterLocation(Point3D emitterLocation) {
		this.emitterLocation = emitterLocation;
	}

}
