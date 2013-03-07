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
package com.minetunes.signs.keywords.argparser;

import java.util.LinkedList;

/**
 * @author William
 * 
 */
public abstract class Arg {

	private boolean optional = true;

	private String name = null;

	private LinkedList<ArgParseError> parseErrors = new LinkedList<ArgParseError>();
	
	private boolean parsed = false;
	
	public Arg (String name, boolean optional) {
		setName(name);
		setOptional(optional);
	}
	
	public Arg () {
		
	}

	/**
	 * 
	 */
	public void reset() {
		parseErrors.clear();
		setParsed(false);
	}

	/**
	 * Parses the given tokens. Expected not to alter "tokens," instead
	 * returning number of tokens to remove.
	 * 
	 * @param tokens
	 * @return number of tokens read from beginning of list
	 */
	public int parse(LinkedList<String> tokens) {
		return 0;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkedList<ArgParseError> getParseErrors() {
		return parseErrors;
	}

	protected void addParseError(ArgParseError e) {
		parseErrors.add(e);
	}

	protected void addMissingError() {
		parseErrors.add(new ArgParseError(this, " required", true));
	}

	public boolean hasFatalErrors() {
		for (ArgParseError e : parseErrors) {
			if (e.isFatal()) {
				return true;
			}
		}
		return false;
	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

}
