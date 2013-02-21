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
package com.minetunes.noteblocks;

import net.minecraft.src.TileEntityNote;

/**
 * An enhanced version of TileEntityNote.
 * 
 */
public class TileEntityNoteMinetunes extends TileEntityNote {
	/**
	 * Its value is known when it is first played or tuned: the server sends the
	 * pitch to play the note, but not before. Before, it is set to the default
	 * 0 on the client side, but since note blocks can actually be tuned to 0,
	 * there's no way of knowing whether the note value is set. Until now.
	 */
	public boolean noteValueKnown = false;
}
