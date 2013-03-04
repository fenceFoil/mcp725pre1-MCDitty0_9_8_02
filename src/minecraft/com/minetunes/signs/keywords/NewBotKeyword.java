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
package com.minetunes.signs.keywords;

import java.util.LinkedList;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.event.CreateBotEvent;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.ParsedSign;

public class NewBotKeyword extends SignTuneKeyword {

	private String name = "";

	private Integer position = null;

	private String type = "";

	private String[] validTypes = { "villager" };

	public NewBotKeyword(String wholeKeyword) {
		super(wholeKeyword);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parse() {
		// Get number of blocks to move; default is 1 if not specified
		String[] args = getWholeKeyword().split(" ");
		int numArgs = args.length;

		if (numArgs > 3) {
			// Too many arguments
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Only the bot's name and position are needed on this line.");
		} else if (numArgs <= 1) {
			// No type
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow NewBot with the name of the new bot.");
			return;
		}

		if (numArgs == 2) {
			// Name supplied
			String name = args[1].toLowerCase().trim();

			// Make sure it's a valid type: starts with letter, star, ?, or _
			// and subsequent chars can be numbers as well
			boolean valid = name.matches("[a-zA-Z_][a-zA-Z\\d_]*");

			if (!valid) {
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Bot names should contain only letters, numbers, and underscores.");
				return;
			}

			setType(name);
		}

		if (numArgs == 3) {
			// Position upwards supplied
			String pos = args[2].trim();

			// Make sure it's a valid type: matches + or - digits
			boolean valid = pos.matches("[+-]*\\d+");

			if (!valid) {
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Bot position above a sign should either be blank or a number.");
				return;
			}

			setPosition(Integer.parseInt(pos));
		}

		return;
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public <T extends SignTuneKeyword> void parseWithMultiline(
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

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// If the position is null, search upwards for a
		// space for the bot
		boolean searchUp = (getPosition() == null);

		int yOffset = 0;
		if (getPosition() != null) {
			yOffset = getPosition();
		}

		// Create the bot event
		CreateBotEvent botEvent = new CreateBotEvent(
				location.x + 0.5f,
				location.y + yOffset,
				location.z + 0.5f,
				getType(),
				SignTuneParser.getSignFacingDegrees(
						signTileEntity.blockMetadata, signTileEntity.blockType),
				searchUp, getName(), ditty.getDittyID());

		// Add the event to the ditty
		int eventID = ditty.addDittyEvent(botEvent);
		ditty.addMusicStringTokens(readMusicString,
				SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);

		return null;
	}

	@Override
	public int getLineCount() {
		return 2;
	}

}
