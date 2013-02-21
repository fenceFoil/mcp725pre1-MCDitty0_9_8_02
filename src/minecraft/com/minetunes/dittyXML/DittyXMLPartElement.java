/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.dittyXML;

/**
 * Part and Of are range-limited from one to infinity.
 * 
 */
public class DittyXMLPartElement {

	// private String partString;
	private int part;
	// private String ofString;
	private int of;
	private String set;
	// private String versionString;
	private int version;

	public int getPart() {
		return part;
	}

	public void setPart(int part) {
		// Range checking
		if (part < 1) {
			part = 1;
		}

		this.part = part;
	}

	public int getOf() {
		return of;
	}

	public void setOf(int of) {
		// Range checking
		if (of < 1) {
			of = 1;
		}
		
		this.of = of;
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
