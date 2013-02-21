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
import com.minetunes.signs.Comment;

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

	public static PattKeyword parse(String rawLine) {
		PattKeyword keyword = new PattKeyword(rawLine);

		String[] args = rawLine.split(" ");
		int numArgs = args.length;
		if (numArgs <= 1) {
			// No argument
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Add a comment.");
			return keyword;
		}

		int currArg = 1;

		// Try to find a repeat count first
		String repeatCountString = args[currArg];
		if (repeatCountString.matches("\\d+")) {
			// found
			keyword.setRepeatCount(Integer.parseInt(args[currArg]));
			currArg++;
		}
		
		// Read the comment
		if (numArgs <= currArg) {
			// Missing comment
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Add a comment.");
			return keyword;
		} else {
			int commentStartIndex = rawLine.indexOf("#");
			if (commentStartIndex <= 0) {
				// Missing comment
				keyword.setGoodKeyword(true);
				keyword.setErrorMessageType(INFO);
				keyword.setErrorMessage("Add a comment.");
				return keyword;
			}
			keyword.setComment(rawLine.substring(commentStartIndex));
		}

		// try {
		// keyword.setComment(rawLine.substring("patt ".length()));
		// } catch (Exception e) {
		// // In case of string bounds errors
		// e.printStackTrace();
		// }

		return keyword;
	}

	public void setRepeatCount(int count) {
		repeatCount = count;
	}

	public int getRepeatCount() {
		return repeatCount;
	}
}
