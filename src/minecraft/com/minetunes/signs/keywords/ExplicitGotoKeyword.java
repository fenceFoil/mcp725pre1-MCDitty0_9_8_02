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

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.keywords.argparser.ArgParser;
import com.minetunes.signs.keywords.argparser.IntArg;

/**
 * An Explicit Goto keyword is one of 'up, down, in, out, left, or right.' Not
 * to be confused with the keyword "GoTo".
 * 
 */
public class ExplicitGotoKeyword extends SignTuneKeyword {

	private int amountMove = 1;
	private IntArg amountArg = new IntArg("Blocks to Move", true, 1);

	public ExplicitGotoKeyword(String wholeKeyword) {
		super(wholeKeyword);
		
		argParser = new ArgParser().addLine(amountArg);
	}

	@Override
	public void parse() {
		super.parse();
		setAmountMove(amountArg.getParsedInt());
		
//		// Get number of blocks to move; default is 1 if not specified
//		int numArgs = getWholeKeyword().split(" ").length;
//		if (numArgs == 2) {
//			String argument = getWholeKeyword().split(" ")[1];
//			if (argument.trim().matches("\\d+")) {
//				setAmountMove(Integer.parseInt(argument.trim()));
//			} else {
//				// Error: invalid agument
//				setGoodKeyword(false);
//				setErrorMessageType(ERROR);
//				setErrorMessage("Follow this go-to with a number of blocks to move.");
//			}
//		} else if (numArgs > 2) {
//			// Warning: Too Many Arguments
//			setGoodKeyword(true);
//			setErrorMessageType(INFO);
//			setErrorMessage("Only one number is needed.");
//		}
	}

	/**
	 * @return the amountMove
	 */
	public int getAmountMove() {
		return amountMove;
	}

	/**
	 * @param amountMove
	 *            the amountMove to set
	 */
	public void setAmountMove(int amountMove) {
		this.amountMove = amountMove;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder b) {
		Point3D pointedAtSign = nextSign.clone();
		int amount = getAmountMove();
		
		String ks = getKeyword().toLowerCase();

		// Decide the direction to move
		if (ks.equals("right") || ks.equals("left")) {
			// Handle moving left or right
			if (ks.equals("left")) {
				amount = -amount;
			}

			// Adjust next sign position based on the amount to
			// move and the current sign's facing
			pointedAtSign = SignTuneParser.getCoordsRelativeToSign(
					nextSign, SignTuneParser.getSignFacing(
							signTileEntity.blockMetadata,
							signTileEntity.blockType), amount, 0, 0);
		}

		if (ks.equals("in") || ks.equals("out")) {
			// Handle moving up or down
			if (ks.equals("in")) {
				amount = -amount;
			}

			// Adjust next sign position based on the amount to
			// move and the current sign's facing
			pointedAtSign = SignTuneParser.getCoordsRelativeToSign(
					nextSign, SignTuneParser.getSignFacing(
							signTileEntity.blockMetadata,
							signTileEntity.blockType), 0, 0, amount);
		}

		if (ks.equals("up") || ks.equals("down")) {
			// Handle moving up or down
			if (ks.equals("down")) {
				amount = -amount;
			}

			// Adjust next sign position based on the amount to
			// move and the current sign's facing
			pointedAtSign = SignTuneParser.getCoordsRelativeToSign(
					nextSign, SignTuneParser.getSignFacing(
							signTileEntity.blockMetadata,
							signTileEntity.blockType), 0, amount, 0);
		}
		// Only say next point moved if it really has
		if (!pointedAtSign.equals(nextSign)) {
			return pointedAtSign;
		} else {
			return null;
		}
	}

}
