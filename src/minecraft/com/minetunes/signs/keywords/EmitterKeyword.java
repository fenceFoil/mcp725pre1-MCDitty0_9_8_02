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
import java.util.Collection;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Emitter;
import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.event.CreateEmitterEvent;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.Comment;
import com.minetunes.signs.ParsedSign;

/**
 * 
 *
 */
public class EmitterKeyword extends SignTuneKeyword {

	/**
	 * All types of particles currently supported by emitters
	 */
	public static String[] types;
	static {
		types = new String[3 + Emitter.particleHandleMap.size()];
		Collection<String> c = Emitter.particleHandleMap.keySet();
		ArrayList<String> typesList = new ArrayList<String>();
		typesList.addAll(c);
		typesList.add("note");
		typesList.add("heart");
		typesList.add("bubble");
		types = typesList.toArray(new String[0]);
	}

	/**
	 * Type of particles emitted
	 */
	private String type = "note";

	/**
	 * Voices that this emitter emits in response to
	 */
	private ArrayList<Integer> voices = new ArrayList<Integer>();

	/**
	 * A copy of an array filled with all vocies.
	 */
	private static final ArrayList<Integer> allVoicesEnabled = new ArrayList<Integer>();
	static {
		for (int i = 0; i < 16; i++) {
			allVoicesEnabled.add(i);
		}
	}

	/**
	 * -1 denotes no rate specified
	 */
	private int rate = -1;

	/**
	 * Color of particle, if applicable. Is -1 if no color is specified.
	 */
	private int color = -1;

	/**
	 * How many blocks above sign to emit particles
	 */
	private int above = 0;

	/**
	 * How many particles to emit before stopping.
	 */
	private int life = Integer.MAX_VALUE;

	/**
	 * Quarter notes before ceasing to emit particles
	 */
	private int timeQuarterNotes = Integer.MAX_VALUE / 128; // 128 is an
															// arbitrary
															// number.

	/**
	 * Value to multiply numbers of particles by
	 */
	private int particleMultiplier = 1;

	/**
	 * Stream frequency
	 */
	private String streamFreq = null;

	/**
	 * @param wholeKeyword
	 */
	public EmitterKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		EmitterKeyword keyword = new EmitterKeyword(getWholeKeyword());

		// Get number of blocks to move; default is 1 if not specified
		String[] args = getWholeKeyword().split(" ");
		int numArgs = args.length;

		if (numArgs > 2) {
			// Too many arguments
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Only one type is needed.");
		} else if (numArgs <= 1) {
			// No type
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow Emitter with a type of particle to emit.");
		} else {
			// Type supplied
			String type = args[1].toLowerCase().trim();
			// Make sure it's a valid type
			boolean valid = false;
			for (String s : types) {
				if (s.equals(type)) {
					valid = true;
					break;
				}
			}

			if (!valid) {
				setGoodKeyword(true);
				setErrorMessageType(WARNING);
				setErrorMessage("Cannot emit this type of particle.");
			}

			setType(type);
		}
	}

	@Override
	public boolean isFirstLineOnly() {
		return true;
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public <T extends SignTuneKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);

		// Mark first line as keyword
		parsedSign.getLines()[0] = this;

		// Parse options
		String[] rawLines = parsedSign.getSignText();

		// Make options into one string
		// Ignore comment lines
		StringBuilder optionsString = new StringBuilder();
		for (int i = 1; i < rawLines.length; i++) {
			if (!Comment.isLineComment(rawLines[i])) {
				optionsString.append(rawLines[i]);
				optionsString.append(" ");
				// Mark option line as a part of this keyword
				parsedSign.getLines()[i] = this;
			}
		}

		String[] optionTokens = optionsString.toString().toLowerCase()
				.split("\\s\\s*");

		for (int i = 0; i < optionTokens.length; i++) {
			if (optionTokens[i].equals("v") || optionTokens[i].equals("voice")) {
				// Voice option
				// Read any following numbers
				while ((i + 1) < optionTokens.length
						&& optionTokens[i + 1].matches("\\d\\d?")) {
					voices.add(Integer.parseInt(optionTokens[i + 1]));
					i++;
				}
			} else if (optionTokens[i].equals("r")
					|| optionTokens[i].equals("rate")) {
				// Rate option
				if (i + 1 < optionTokens.length) {
					// Read the following number
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.matches("\\d\\d?")) {
						int rateArg = Integer.parseInt(argument);
						if (rateArg < 1 || rateArg > 100) {
							// Out of range
							setErrorMessageType(WARNING);
							// setGoodKeyword(true);
							setErrorMessage("Follow the emitter option 'Rate' with a number from 1 to 99.");
						} else {
							// System.out.println ("Setting rate: "+rateArg);
							setRate(rateArg);
						}

						// Increment current token only if valid.
						// This way the options reader attempts to recognize if
						// the
						// invalid token is the start of another option.
						i++;
					} else {
						// System.out.println ("Rate arg invalid");
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow the emitter option 'Rate' with a number.");
					}
				} else {
					// System.out.println
					// ("Not enought tokens for there to be a rate arg");
					// Invalid argument. do not increment current token
					setErrorMessageType(WARNING);
					// setGoodKeyword(true);
					setErrorMessage("Follow the emitter option 'Rate' with a number.");
				}
			} else if (optionTokens[i].equals("c")
					|| optionTokens[i].equals("color")) {
				// Color option
				// Read the following number
				if (i + 1 < optionTokens.length) {
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.matches("\\d\\d?")) {
						int colorArg = Integer.parseInt(argument);
						if (colorArg < 0 || colorArg > 23) {
							// Out of range
							setErrorMessageType(WARNING);
							// setGoodKeyword(true);
							setErrorMessage("Follow the emitter option 'Color' with a number from 1 to 99.");
						}
						setColor(colorArg);

						// Increment current token only if valid.
						// This way the options reader attempts to recognize if
						// the
						// invalid token is actually the start of another
						// option.
						i++;
					} else if (optionTokens[i].trim().length() <= 0) {
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow the emitter option 'Color' with a number.");
					}
				} else {
					// Invalid argument. do not increment current token
					setErrorMessageType(WARNING);
					// setGoodKeyword(true);
					setErrorMessage("Follow the emitter option 'Color' with a number.");
				}
			} else if (optionTokens[i].equals("u")
					|| optionTokens[i].equals("up")) {
				// Above option
				// Read the following number
				if (i + 1 < optionTokens.length) {
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.matches("-?\\d\\d?")) {
						int aboveArg = Integer.parseInt(argument);
						if (aboveArg < -99 || aboveArg > 99) {
							// Out of range
							setErrorMessageType(WARNING);
							// setGoodKeyword(true);
							setErrorMessage("Follow the emitter option 'Up' with a number from -99 to 99.");
						}
						setAbove(aboveArg);

						// Increment current token only if valid.
						// This way the options reader attempts to recognize if
						// the
						// invalid token is actually the start of another
						// option.
						i++;
					} else if (optionTokens[i].trim().length() <= 0) {
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow the emitter option 'Up' with a number.");
					}
				} else {
					// Invalid argument. do not increment current token
					setErrorMessageType(WARNING);
					// setGoodKeyword(true);
					setErrorMessage("Follow the emitter option 'Up' with a number.");
				}

				// TODO:
				/*
				 * Life Time Muliply / Multi
				 */
			} else if (optionTokens[i].equals("l")
					|| optionTokens[i].equals("life")) {
				// Life option
				if (i + 1 < optionTokens.length) {
					// Read the following number
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.matches("\\d\\d?")) {
						int rateArg = Integer.parseInt(argument);
						if (rateArg < 1 || rateArg > 100) {
							// Out of range
							setErrorMessageType(WARNING);
							// setGoodKeyword(true);
							setErrorMessage("Follow the emitter option 'Life' with a number from 1 to 99.");
						} else {
							// System.out.println ("Setting rate: "+rateArg);
							setLife(rateArg);
						}

						// Increment current token only if valid.
						// This way the options reader attempts to recognize if
						// the
						// invalid token is the start of another option.
						i++;
					} else {
						// System.out.println ("Rate arg invalid");
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow the emitter option 'Life' with a number.");
					}
				} else {
					// System.out.println
					// ("Not enought tokens for there to be a rate arg");
					// Invalid argument. do not increment current token
					setErrorMessageType(WARNING);
					// setGoodKeyword(true);
					setErrorMessage("Follow the emitter option 'Life' with a number.");
				}
			} else if (optionTokens[i].equals("t")
					|| optionTokens[i].equals("time")) {
				// Time option
				if (i + 1 < optionTokens.length) {
					// Read the following number
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.matches("\\d\\d?")) {
						int rateArg = Integer.parseInt(argument);
						if (rateArg < 1 || rateArg > 100) {
							// Out of range
							setErrorMessageType(WARNING);
							// setGoodKeyword(true);
							setErrorMessage("Follow the emitter option 'Time' with a number from 1 to 99.");
						} else {
							// System.out.println ("Setting rate: "+rateArg);
							setTimeQuarterNotes(rateArg);
						}

						// Increment current token only if valid.
						// This way the options reader attempts to recognize if
						// the
						// invalid token is the start of another option.
						i++;
					} else {
						// System.out.println ("Rate arg invalid");
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow the emitter option 'Time' with a number.");
					}
				} else {
					// System.out.println
					// ("Not enought tokens for there to be a rate arg");
					// Invalid argument. do not increment current token
					setErrorMessageType(WARNING);
					// setGoodKeyword(true);
					setErrorMessage("Follow the emitter option 'Time' with a number.");
				}
			} else if (optionTokens[i].equals("m")
					|| optionTokens[i].equals("multi")) {
				// Time option
				if (i + 1 < optionTokens.length) {
					// Read the following number
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.matches("\\d")) {
						int multiArg = Integer.parseInt(argument);
						if (multiArg < 0 || multiArg > 5) {
							// Out of range
							setErrorMessageType(WARNING);
							// setGoodKeyword(true);
							setErrorMessage("Follow the emitter option 'Multi' with a number from 0 to 5.");
						} else {
							// System.out.println ("Setting rate: "+rateArg);
							setParticleMultiplier(multiArg);
						}

						// Increment current token only if valid.
						// This way the options reader attempts to recognize if
						// the
						// invalid token is the start of another option.
						i++;
					} else {
						// System.out.println ("Rate arg invalid");
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow the emitter option 'Multi' with a number.");
					}
				}
			} else if (optionTokens[i].equals("s")
					|| optionTokens[i].equals("stream")) {
				// Stream of particles option
				if (i + 1 < optionTokens.length) {
					// Read the following number
					String argument = optionTokens[i + 1];
					// Check that number is valid, then read if possible
					if (argument.toLowerCase().matches("[whqistxo]")) {
						setStreamFreq(argument.toLowerCase());

						// Increment current token only if valid.
						// This way the options reader attempts to recognize
						// if
						// the
						// invalid token is the start of another option.
						i++;
					} else {
						// System.out.println ("Rate arg invalid");
						// Invalid argument. do not increment current token
						setErrorMessageType(WARNING);
						// setGoodKeyword(true);
						setErrorMessage("Follow option 'Stream' with a duration: w, h, q, i, s, t, x, or o.");
					}

				} else {
					// System.out.println
					// ("Not enough tokens for there to be a rate arg");
					// Invalid argument. do not increment current token
					setErrorMessageType(WARNING);
					// setGoodKeyword(true);
					setErrorMessage("Follow the emitter option 'Stream' with a letter.");
				}
			} else {
				// Invalid option token
				setErrorMessageType(WARNING);
				// setGoodKeyword(true);
				setErrorMessage("The option '" + optionTokens[i]
						+ "' does not exist.");

				// Move on to next token
				i++;
			}
		}

		return;
	}

	/**
	 * The public method for getting voices enabled in a Emitter. If no voices
	 * are set internally, returns a list of all voices instead.
	 * 
	 * @return
	 */
	public ArrayList<Integer> getVoices() {
		if (voices.size() > 0) {
			return voices;
		} else {
			return (ArrayList<Integer>) allVoicesEnabled.clone();
		}
	}

	public void setVoices(ArrayList<Integer> voices) {
		this.voices = voices;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getAbove() {
		return above;
	}

	public void setAbove(int above) {
		this.above = above;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public int getTimeQuarterNotes() {
		return timeQuarterNotes;
	}

	public void setTimeQuarterNotes(int timeQuarterNotes) {
		this.timeQuarterNotes = timeQuarterNotes;
	}

	public int getParticleMultiplier() {
		return particleMultiplier;
	}

	public void setParticleMultiplier(int particleMultiplier) {
		this.particleMultiplier = particleMultiplier;
	}

	public String getStreamFreq() {
		return streamFreq;
	}

	public void setStreamFreq(String streamFreq) {
		this.streamFreq = streamFreq;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Creates a create emitter event in the ditty, with
		// corresponding token
		int eventID = ditty.addDittyEvent(new CreateEmitterEvent(this, -1, ditty
				.getDittyID(), location.clone()));
		ditty.addMusicStringTokens(readMusicString,
				SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);

		return null;
	}

	@Override
	public boolean isAllBelow() {
		return true;
	}

}
