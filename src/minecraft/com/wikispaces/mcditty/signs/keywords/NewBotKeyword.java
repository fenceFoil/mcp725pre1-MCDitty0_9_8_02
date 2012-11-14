/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.signs.keywords;

import java.util.LinkedList;

import com.wikispaces.mcditty.signs.ParsedSign;

public class NewBotKeyword extends ParsedKeyword {

	private String name = "";

	private Integer position = null;

	private String type = "";

	private String[] validTypes = { "villager" };

	public NewBotKeyword(String wholeKeyword) {
		super(wholeKeyword);
		// TODO Auto-generated constructor stub
	}

	public static NewBotKeyword parse(String rawArgs) {
		NewBotKeyword keyword = new NewBotKeyword(rawArgs);

		// Get number of blocks to move; default is 1 if not specified
		String[] args = rawArgs.split(" ");
		int numArgs = args.length;

		if (numArgs > 3) {
			// Too many arguments
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Only the bot's name and position are needed on this line.");
		} else if (numArgs <= 1) {
			// No type
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ERROR);
			keyword.setErrorMessage("Follow NewBot with the name of the new bot.");
			return keyword;
		}

		if (numArgs == 2) {
			// Name supplied
			String name = args[1].toLowerCase().trim();

			// Make sure it's a valid type: starts with letter, star, ?, or _
			// and subsequent chars can be numbers as well
			boolean valid = name.matches("[a-zA-Z_][a-zA-Z\\d_]*");

			if (!valid) {
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Bot names should contain only letters, numbers, and underscores.");
				return keyword;
			}

			keyword.setType(name);
		}

		if (numArgs == 3) {
			// Position upwards supplied
			String pos = args[2].trim();

			// Make sure it's a valid type: matches + or - digits
			boolean valid = pos.matches("[+-]*\\d+");

			if (!valid) {
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Bot position above a sign should either be blank or a number.");
				return keyword;
			}

			keyword.setPosition(Integer.parseInt(pos));
		}

		return keyword;
	}

	@Override
	public boolean isFirstLineOnly() {
		return false;
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public <T extends ParsedKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);

		// Mark first line as keyword
		parsedSign.getLines()[keywordLine] = this;

		// Parse type of bot
		String[] rawLines = parsedSign.getSignText();

		if (keywordLine + 1 >= parsedSign.getLines().length) {
			// Too close to bottom of sign
			k.setErrorMessageType(ERROR);
			k.setGoodKeyword(false);
			k.setErrorMessage("The NewBot keyword should be on the third line of a sign or higher.");
			return;
		}

		// Mark subsequent line as keyword
		parsedSign.getLines()[keywordLine + 1] = this;

		// Now parse type for reals
		String typeLine = rawLines[keywordLine + 1].trim();

		boolean validType = false;
		for (String s : validTypes) {
			if (s.equalsIgnoreCase(typeLine)) {
				validType = true;
				break;
			}
		}

		if (validType) {
			setType(typeLine);
		} else {
			k.setErrorMessage(validType + " is not a known type of bot.");
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			return;
		}

		return;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the position
	 */
	public Integer getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Integer position) {
		this.position = position;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
