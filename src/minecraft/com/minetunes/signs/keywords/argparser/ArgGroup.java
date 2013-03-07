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
public class ArgGroup extends Arg {
	private ArgGroupOrder ordered = ArgGroupOrder.ORDERED;

	private LinkedList<Arg> subArgs = new LinkedList<Arg>();

	@Override
	public void reset() {
		super.reset();
		for (Arg a : subArgs) {
			a.reset();
		}
	}

	/**
	 * Adds an Arg to the group
	 * 
	 * @param a
	 * @return this object, for chaining calls
	 */
	public ArgGroup addArg(Arg a) {
		subArgs.add(a);
		return this;
	}

	public ArgGroupOrder getOrdered() {
		return ordered;
	}

	public void setOrdered(ArgGroupOrder ordered) {
		this.ordered = ordered;
	}
}
