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

/**
 *
 */
public class ArgParseError {
	private Arg source = null;
	private String message = null;
	private boolean fatal = false;

	public ArgParseError(Arg source, String message, boolean fatal) {
		super();
		this.source = source;
		this.message = message;
		if (source instanceof Arg && source.getName() != null) {
			this.message = source.getName() + " " + this.getMessage().trim();
		}
		this.fatal = fatal;
	}

	public Arg getSource() {
		return source;
	}

	public void setSource(Arg source) {
		this.source = source;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isFatal() {
		return fatal;
	}

	public void setFatal(boolean fatal) {
		this.fatal = fatal;
	}
}
