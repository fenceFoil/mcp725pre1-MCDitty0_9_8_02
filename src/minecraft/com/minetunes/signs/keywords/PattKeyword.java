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
package com.minetunes.signs.keywords;

import java.util.ArrayList;
import java.util.LinkedList;

import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.Comment;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

/**
 * Keyword format:
 * 
 * Patt [optional repeat count] #CommentToGoTo(Required)
 */
public class PattKeyword extends GotoKeyword {

	private int repeatCount = 1;

	public PattKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		String[] args = getWholeKeyword().split(" ");
		int numArgs = args.length;
		if (numArgs <= 1) {
			// No argument
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Add a comment.");
			return;
		}

		int currArg = 1;

		// Try to find a repeat count first
		String repeatCountString = args[currArg];
		if (repeatCountString.matches("\\d+")) {
			// found
			setRepeatCount(Integer.parseInt(args[currArg]));
			currArg++;
		}

		// Read the comment
		if (numArgs <= currArg) {
			// Missing comment
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Add a comment.");
			return;
		} else {
			int commentStartIndex = getWholeKeyword().indexOf("#");
			if (commentStartIndex <= 0) {
				// Missing comment
				setGoodKeyword(true);
				setErrorMessageType(INFO);
				setErrorMessage("Add a comment.");
				return;
			}
			setComment(getWholeKeyword().substring(commentStartIndex));
		}

		// try {
		// setComment(getWholeKeyword().substring("patt ".length()));
		// } catch (Exception e) {
		// // In case of string bounds errors
		// e.printStackTrace();
		// }

		return;
	}

	public void setRepeatCount(int count) {
		repeatCount = count;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	@Override
	public boolean hasSpecialExecution() {
		return true;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location, TileEntitySign sign,
			Point3D r, World world, StringBuilder readMusicString) {
		// Unlike parent class, this one does not change the next sign's
		// location
		return null;
	}
}
