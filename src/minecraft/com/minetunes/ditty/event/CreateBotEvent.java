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

public class CreateBotEvent extends TimedDittyEvent {

	/**
	 * Location to spawn bot at.
	 */
	private float locX = 0;
	private float locY = 0;
	private float locZ = 0;
	
	/**
	 * 
	 */
	private boolean searchForYLocation = false;
	
	/**
	 * Type of bot to spawn
	 */
	private String type = "";
	
	private String name = "";
	
	/**
	 * Direction bot will spawn looking in
	 */
	private float rotation = 0;

	public CreateBotEvent(float x, float y, float z, String type, float rot, boolean searchUpwards, String name, int dittyID) {
		super(dittyID);
		locX = x;
		locY = y;
		locZ = z;
		this.type = type;
		rotation = rot;
		searchForYLocation = searchUpwards;
		setName(name);
	}

	/**
	 * @return the locX
	 */
	public float getLocX() {
		return locX;
	}

	/**
	 * @param locX the locX to set
	 */
	public void setLocX(float locX) {
		this.locX = locX;
	}

	/**
	 * @return the locY
	 */
	public float getLocY() {
		return locY;
	}

	/**
	 * @param locY the locY to set
	 */
	public void setLocY(float locY) {
		this.locY = locY;
	}

	/**
	 * @return the locZ
	 */
	public float getLocZ() {
		return locZ;
	}

	/**
	 * @param locZ the locZ to set
	 */
	public void setLocZ(float locZ) {
		this.locZ = locZ;
	}

	/**
	 * @return the searchForYLocation
	 */
	public boolean isSearchForYLocation() {
		return searchForYLocation;
	}

	/**
	 * @param searchForYLocation the searchForYLocation to set
	 */
	public void setSearchForYLocation(boolean searchForYLocation) {
		this.searchForYLocation = searchForYLocation;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the rotation
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
