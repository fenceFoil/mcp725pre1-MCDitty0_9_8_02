/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.signs.keywords;

import java.util.ArrayList;
import java.util.LinkedList;

import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.signs.Comment;

import net.minecraft.src.World;

/**
 * @author William
 * 
 */
public class GotoKeyword extends ParsedKeyword {
	private String destinationComment = "";

	public GotoKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static GotoKeyword parse(String rawLine) {
		GotoKeyword keyword = new GotoKeyword(rawLine);

		int numArgs = rawLine.split(" ").length;
		if (numArgs <= 1) {
			// No argument
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Add a comment to jump to.");
		} else {
			try {
				keyword.setComment(rawLine.substring("goto ".length()));
			} catch (Exception e) {
				// In case of string bounds errors
				e.printStackTrace();
			}
		}

		return keyword;
	}

	public static LinkedList<Comment> matchingCommentsNearby(Point3D signPos,
			World world, String comment) {
		LinkedList<Comment> matchingComments = new LinkedList<Comment>();
		// TODO: Optimise this line to be called less often?
		MCDitty.optimizeCommentList(world);

		LinkedList<Comment> allComments = MCDitty
				.getCommentsSortedByDistFrom(signPos);
		for (Comment c : allComments) {
			if (c.getCommentText().toLowerCase()
					.startsWith(comment.toLowerCase())) {
				// comment matches
				matchingComments.add(c);
			}
		}
		
		return matchingComments;
	}

	/**
	 * Todo: optimise
	 * @param signPos
	 * @param world
	 * @return
	 */
	public static Comment getNearestMatchingComment(Point3D signPos, World world, String comment) {
		LinkedList<Comment> matchingComments = new LinkedList<Comment>();
		// TODO: Optimise this line to be called less often?
		MCDitty.optimizeCommentList(world);

		LinkedList<Comment> allComments = MCDitty
				.getCommentsSortedByDistFrom(signPos);
		Comment bestMatch = null;
		for (Comment c : allComments) {
			if (c.getCommentText().toLowerCase()
					.startsWith(comment.toLowerCase())) {
				// comment matches
				matchingComments.add(c);
				if (bestMatch == null
						&& c.getCommentText().equalsIgnoreCase(comment)) {
					// Matches exactly!
					bestMatch = c;
				}
			}
		}

		// If no exact match was found, yet other matches were found, find the
		// best one
		if (bestMatch == null && matchingComments.size() >= 1) {
			bestMatch = matchingComments.get(0);
		}
		
		return bestMatch;
	}

	public String getComment() {
		return destinationComment;
	}

	public void setComment(String destinationComment) {
		this.destinationComment = destinationComment;
	}
}
