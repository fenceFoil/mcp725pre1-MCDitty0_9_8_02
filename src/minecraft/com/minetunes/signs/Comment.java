/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
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

package com.minetunes.signs;


import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

/**
 * Represents a particular single comment line on a particular sign.
 */
public class Comment {

	private SignLine location = new SignLine();
	private String comment = "";

	public Comment(SignLine location, String comment) {
		setLocation(location);
		setComment(comment);
	}

	public Comment(String string) {
		setComment(comment);
	}

	/**
	 * Checks a world to see whether the comment described by this object still
	 * exists on a sign at the described location.
	 * 
	 * @param world
	 * @return
	 */
	public boolean stillExistsInWorld(World world) {
		// Redundant, next set of checks do same thing
		// if (BlockSign.getSignBlockType(location, world) == null) {
		// // No sign at location
		// return false;
		// }

		TileEntity tile = world.getBlockTileEntity(location.x, location.y,
				location.z);
		if (tile == null || !(tile instanceof TileEntitySign)) {
			// Problem finding tile entity for sign in the world
			return false;
		} else {
			TileEntitySign signTile = (TileEntitySign) tile;
			String commentLineText = signTile.signText[location.getLine()];
			if (commentLineText == null
					|| !commentLineText.equals(getCommentText())) {
				// If the comment line is blank or different from the comment in
				// this object
				return false;
			} else {
				return true;
			}
		}
	}

	/**
	 * Currently returns true if the first letter of the line is a #, but this
	 * is potentially subject to change.
	 * 
	 * @param line
	 * @return True if given line is a comment.
	 */
	public static boolean isLineComment(String line) {
		if (line.length() > 0) {
			if (line.charAt(0) == '#') {
				return true;
			}
		}
		return false;
	}

	public SignLine getLocation() {
		return location;
	}

	public void setLocation(SignLine location) {
		this.location = location;
	}

	public String getCommentText() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Comment) {
			Comment c = (Comment) obj;
			if (c.getLocation().equals(getLocation())
					&& c.getCommentText().equals(getCommentText())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
