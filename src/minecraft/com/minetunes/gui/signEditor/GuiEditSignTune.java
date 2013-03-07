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
package com.minetunes.gui.signEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.Block;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySign;

import org.jfugue.JFugueException;
import org.jfugue.ParserListener;
import org.jfugue.elements.Instrument;
import org.jfugue.elements.JFugueElement;
import org.jfugue.elements.Note;
import org.jfugue.elements.Tempo;
import org.jfugue.parsers.MusicStringParser;
import org.jfugue.util.MapUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.minetunes.MidiFileFilter;
import com.minetunes.Point3D;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.GuiButtonL;
import com.minetunes.gui.GuiScrollingTextPanel;
import com.minetunes.gui.MinetunesVersionGuiElement;
import com.minetunes.resources.ResourceManager;
import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.Comment;
import com.minetunes.signs.ParsedSign;
import com.minetunes.signs.SignParser;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.TileEntitySignMinetunes;
import com.minetunes.signs.keywords.BaseMidiKeyword;
import com.minetunes.signs.keywords.ExplicitGotoKeyword;
import com.minetunes.signs.keywords.GotoKeyword;
import com.minetunes.signs.keywords.LyricKeyword;
import com.minetunes.signs.keywords.PlayMidiKeyword;
import com.minetunes.signs.keywords.ProxPadKeyword;
import com.minetunes.signs.keywords.SFXInstKeyword;
import com.minetunes.signs.keywords.SFXKeyword;
import com.minetunes.signs.keywords.SignTuneKeyword;

/**
 *
 */
public class GuiEditSignTune extends GuiEditSignBase {
	/**
	 * @param signTileEntity
	 */
	public GuiEditSignTune(TileEntitySign signTileEntity) {
		super(signTileEntity);

		if (lockedCode != null) {
			// Set up the locked color code
			if (sign.signText[2].length() <= 13) {
				sign.signText[2] += "%" + lockedCode;
			}
		}
	}

	/**
	 * @param entitySign
	 * @param recalledSignCount
	 * @param editLine
	 * @param editChar
	 */
	public GuiEditSignTune(TileEntitySignMinetunes entitySign,
			int recalledSignCount, int editLine, int editChar) {
		super(entitySign, recalledSignCount, editLine, editChar);

		if (lockedCode != null) {
			// Set up the locked color code
			if (sign.signText[2].length() <= 13) {
				sign.signText[2] += "%" + lockedCode;
			}
		}
	}

	private static final int HELP_STATE_KEYWORD = 0;
	private static final int HELP_STATE_TOKEN = 1;
	private static final int HELP_STATE_DETECT = 2;
	private static final int HELP_STATE_HIDDEN = 3;

	private static int helpState = HELP_STATE_DETECT;

	private String parseResultText;

	private String currentKeyword;

	private GuiScrollingTextPanel helpTextArea;

	private GuiButtonL testButton;

	private GuiButton selectHelpButton;

	// private GuiButton doneButton;

	private GuiButtonL recallButton;

	private GuiButtonL clearButton;

	private GuiButton lockButton;

	protected static String lockedCode = null;

	static {
		INSTRUMENT_NAMES_MAP = MapUtils
				.convertArrayToImutableMap(new String[][] {
						//
						// Instrument names
						//
						{ "PIANO", "0" }, { "GRAND", "0" },
						{ "ACOUSTIC_GRAND", "0" }, { "GRAND_PIANO", "0" },
						{ "BRIGHT_ACOUSTIC", "1" }, { "ELECTRIC_GRAND", "2" },
						{ "HONKEY_TONK", "3" }, { "HONKEYTONK", "3" },
						{ "ELECTRIC_PIANO", "4" }, { "ELECTRIC_PIANO_1", "4" },
						{ "ELECTRIC_PIANO_2", "5" }, { "HARPISCHORD", "6" },
						{ "CLAVINET", "7" }, { "CELESTA", "8" },
						{ "GLOCKENSPIEL", "9" },

						{ "MUSIC_BOX", "10" }, { "VIBRAPHONE", "11" },
						{ "MARIMBA", "12" }, { "XYLOPHONE", "13" },
						{ "BELLS", "14" }, { "TUBULAR_BELLS", "14" },
						{ "DULCIMER", "15" }, { "DRAWBAR_ORGAN", "16" },
						{ "PERCUSSIVE_ORGAN", "17" }, { "ROCK_ORGAN", "18" },
						{ "CHURCH_ORGAN", "19" }, { "ORGAN", "19" },

						{ "REED_ORGAN", "20" }, { "ACCORDIAN", "21" },
						{ "HARMONICA", "22" }, { "TANGO_ACCORDIAN", "23" },
						{ "GUITAR", "24" }, { "NYLON_STRING_GUITAR", "24" },
						{ "STEEL_STRING_GUITAR", "25" },
						{ "ELECTRIC_JAZZ_GUITAR", "26" },
						{ "ELECTRIC_CLEAN_GUITAR", "27" },
						{ "ELECTRIC_MUTED_GUITAR", "28" },
						{ "OVERDRIVEN_GUITAR", "29" },

						{ "DISTORTION_GUITAR", "30" },
						{ "GUITAR_HARMONICS", "31" },
						{ "ACOUSTIC_BASS", "32" },
						{ "ELECTRIC_BASS_FINGER", "33" },
						{ "ELECTRIC_BASS_PICK", "34" },
						{ "FRETLESS_BASS", "35" }, { "SLAP", "36" },
						{ "SLAP_BASS", "36" }, { "SLAP_BASS_1", "36" },
						{ "SLAP_BASS_2", "37" }, { "BASS", "38" },
						{ "SYNTH_BASS", "38" }, { "SYNTH_BASS_1", "38" },
						{ "SYNTH_BASS_2", "39" },

						{ "VIOLIN", "40" }, { "VIOLA", "41" },
						{ "CELLO", "42" }, { "CONTRABASS", "43" },
						{ "TREMOLO_STRINGS", "44" },
						{ "PIZZICATO_STRINGS", "45" },
						{ "ORCHESTRAL_STRINGS", "46" }, { "TIMPANI", "47" },
						{ "STRING_ENSEMBLE_1", "48" },
						{ "STRING_ENSEMBLE_2", "49" },

						{ "SYNTH", "50" }, { "SYNTHSTRINGS", "50" },
						{ "SYNTH_STRINGS", "50" }, { "SYNTH_STRINGS_1", "50" },
						{ "SYNTH_STRINGS_2", "51" }, { "CHOIR", "52" },
						{ "AAHS", "52" }, { "CHOIR_AAHS", "52" },
						{ "VOICE", "53" }, { "OOHS", "53" },
						{ "VOICE_OOHS", "53" }, { "SYNTH_VOICE", "54" },
						{ "ORCHESTRA", "55" }, { "ORCHESTRA_HIT", "55" },
						{ "TRUMPET", "56" }, { "TROMBONE", "57" },
						{ "TUBA", "58" }, { "MUTED_TRUMPET", "59" },

						{ "FRENCH_HORN", "60" }, { "BRASS", "61" },
						{ "BRASS_SECTION", "61" }, { "SYNTHBRASS_1", "62" },
						{ "SYNTH_BRASS_1", "62" }, { "SYNTHBRASS_2", "63" },
						{ "SYNTH_BRASS_2", "63" }, { "SOPRANO_SAX", "64" },
						{ "SAX", "65" }, { "SAXOPHONE", "65" },
						{ "ALTO_SAX", "65" }, { "TENOR_SAX", "66" },
						{ "BARITONE_SAX", "67" }, { "OBOE", "68" },
						{ "HORN", "69" }, { "ENGLISH_HORN", "69" },

						{ "BASSOON", "70" }, { "CLARINET", "71" },
						{ "PICCOLO", "72" }, { "FLUTE", "73" },
						{ "RECORDER", "74" }, { "PANFLUTE", "75" },
						{ "PAN_FLUTE", "75" }, { "BLOWN_BOTTLE", "76" },
						{ "SKAKUHACHI", "77" }, { "WHISTLE", "78" },
						{ "OCARINA", "79" },

						{ "LEAD_SQUARE", "80" }, { "SQUARE", "80" },
						{ "LEAD_SAWTOOTH", "81" }, { "SAWTOOTH", "81" },
						{ "LEAD_CALLIOPE", "82" }, { "CALLIOPE", "82" },
						{ "LEAD_CHIFF", "83" }, { "CHIFF", "83" },
						{ "LEAD_CHARANG", "84" }, { "CHARANG", "84" },
						{ "LEAD_VOICE", "85" }, { "VOICE", "85" },
						{ "LEAD_FIFTHS", "86" }, { "FIFTHS", "86" },
						{ "LEAD_BASSLEAD", "87" }, { "BASSLEAD", "87" },
						{ "PAD_NEW_AGE", "88" }, { "NEW_AGE", "88" },
						{ "PAD_WARM", "89" }, { "WARM", "89" },

						{ "PAD_POLYSYNTH", "90" }, { "POLYSYNTH", "90" },
						{ "PAD_CHOIR", "91" }, { "CHOIR", "91" },
						{ "PAD_BOWED", "92" }, { "BOWED", "92" },
						{ "PAD_METALLIC", "93" }, { "METALLIC", "93" },
						{ "PAD_HALO", "94" }, { "HALO", "94" },
						{ "PAD_SWEEP", "95" }, { "SWEEP", "95" },
						{ "FX_RAIN", "96" }, { "RAIN", "96" },
						{ "FX_SOUNDTRACK", "97" }, { "SOUNDTRACK", "97" },
						{ "FX_CRYSTAL", "98" }, { "CRYSTAL", "98" },
						{ "FX_ATMOSPHERE", "99" }, { "ATMOSPHERE", "99" },

						{ "FX_BRIGHTNESS", "100" }, { "BRIGHTNESS", "100" },
						{ "FX_GOBLINS", "101" }, { "GOBLINS", "101" },
						{ "FX_ECHOES", "102" }, { "ECHOES", "102" },
						{ "FX_SCI-FI", "103" }, { "SCI-FI", "103" },
						{ "SITAR", "104" }, { "BANJO", "105" },
						{ "SHAMISEN", "106" }, { "KOTO", "107" },
						{ "KALIMBA", "108" }, { "BAGPIPE", "109" },

						{ "FIDDLE", "110" }, { "SHANAI", "111" },
						{ "TINKLE_BELL", "112" }, { "AGOGO", "113" },
						{ "STEEL_DRUMS", "114" }, { "WOODBLOCK", "115" },
						{ "TAIKO", "116" }, { "TAIKO_DRUM", "116" },
						{ "MELODIC_TOM", "117" }, { "SYNTH_DRUM", "118" },
						{ "REVERSE_CYMBAL", "119" },

						{ "GUITAR_FRET_NOISE", "120" },
						{ "BREATH_NOISE", "121" }, { "SEASHORE", "122" },
						{ "BIRD_TWEET", "123" }, { "BIRD", "123" },
						{ "TWEET", "123" }, { "TELEPHONE", "124" },
						{ "TELEPHONE_RING", "124" }, { "HELICOPTER", "125" },
						{ "APPLAUSE", "126" }, { "GUNSHOT", "127" }, });
	}

	@Override
	public void drawScreen(int mx, int my, float par3) {
		super.drawScreen(mx, my, par3);

		// Draw keyword messages display
		drawCenteredString(fontRenderer, parseResultText, width / 2,
				height - 15, 0xffff00);

		// Draw the help text area
		helpTextArea.draw(mx, my);

		// Draw shovel icon
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iconTexture);
		int iconIndex = 17;
		drawTexturedModalRect(shovelButtonDown.xPosition - 16,
				shovelButtonDown.yPosition + 2, (iconIndex % 16) * 16,
				(iconIndex / 16) * 16, 16, 16);
		if (savedSigns.size() > 1) {
			int posToShow = bufferPosition;
			
			if (bufferPosition >= savedSigns.size()) {
				posToShow = 0;
			}
			
			drawCenteredString(fontRenderer, posToShow + " / "
					+ (savedSigns.size() - 1), shovelButtonDown.xPosition - 7,
					shovelButtonDown.yPosition - 10, 0xffffff);
		}
	}

	@Override
	public void initGui() {
		super.initGui();

		iconTexture = mc.renderEngine
				.getTexture("/com/minetunes/resources/textures/signEditor1.png");

		// Changed button label
		// X is defined in the draw method
		controlList.remove(doneButton);
		doneButton = new GuiButtonL("done", width / 2 - 60, Math.min(
				height / 4 + 120, height - 40), 120, 20, "Done & Save");
		controlList.add(doneButton);
		doneButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendSignAndCloseSignGui();
			}
		});

		controlList.add(new MinetunesVersionGuiElement(-100));

		// // Added new buttons
		// controlList.add(new GuiButton(400, 5, height - 150, 80, 20,
		// "Clear Signs"));
		// controlList.add(new GuiButton(100, 5, height - 120, 80, 20,
		// "Import"));
		// controlList.add(new GuiButton(200, 5, height - 90, 80, 20,
		// "Export"));
		// controlList.add(new GuiButton(300, 5, height - 60, 80, 20,
		// "Open Folder"));

		// recallButton = new GuiButton(1400, width - 100, 60, 80, 20,
		// "Recall");
		recallButton = new GuiButtonL("recall", width - 50, 110, 40, 20,
				iconTexture, 2);
		recallButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				recallSignHereBefore();
			}
		});
		controlList.add(recallButton);

		// clearButton = new GuiButton(1500, width - 100, 85, 80, 20, "Clear");
		clearButton = new GuiButtonL("clear", width - 30, 60, 20, 20,
				iconTexture, 14);
		controlList.add(clearButton);
		clearButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				copyTextToEditor(savedSigns.get(0));
			}
		});

		GuiButtonL keysButton = new GuiButtonL("keys", width - 30, 35, 20, 20,
				iconTexture, 1);
		controlList.add(keysButton);
		final GuiScreen thisGui = this;
		keysButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SignEditorGuideGui guideGui = new SignEditorGuideGui(thisGui);
				mc.displayGuiScreen(guideGui);
			}
		});

		shovelButtonDown = new GuiButtonL("shovelDown", width - 25, 145, 20,
				20, iconTexture, 4);
		shovelButtonDown.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollSavedBufferUp();
			}
		});
		controlList.add(shovelButtonDown);

		shovelButtonUp = new GuiButtonL("shovelUp",
				shovelButtonDown.xPosition - 16 - 20,
				shovelButtonDown.yPosition, 20, 20, iconTexture, 5);
		shovelButtonUp.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				scrollSavedBufferDown();
			}
		});
		controlList.add(shovelButtonUp);

		// Set up the sign color code controls
		controlList.add(new GuiButton(2000, width - 85, height - 50, 20, 20,
				"+"));
		controlList.add(new GuiButton(2100, width - 25, height - 50, 20, 20,
				"-"));
		lockButton = new GuiButton(2200, width - 65, height - 70, 40, 20,
				"Lock");
		controlList.add(lockButton);
		controlList.add(new GuiButton(2300, width - 65, height - 30, 40, 20,
				"Clear"));

		// Add the test sign button
		// testButton = new GuiButton(1200, width - 100, 110, 80, 20,
		// "Test Sign");
		testButton = new GuiButtonL("test", width - 30, 85, 20, 20,
				iconTexture, 0);
		testButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Play the sign
				LinkedList<Point3D> signsToPlay = new LinkedList<Point3D>();
				signsToPlay.add(new Point3D(sign.xCoord, sign.yCoord,
						sign.zCoord));
				LinkedList<String> testErrors = SignTuneParser
						.playDittyFromSignsQuietly(mc.theWorld, sign.xCoord,
								sign.yCoord, sign.zCoord, true, signsToPlay);

				// Display any errors, or success if there aren't any
				// Strip all color codes as well from the error message
				StringBuilder testErrorsFormatted = new StringBuilder();
				testErrorsFormatted.append("Test Result:\n\n");
				if (testErrors != null && testErrors.size() >= 1) {
					testErrorsFormatted.append(testErrors.get(0).replaceAll(
							"§.", ""));
					helpTextArea.setText(testErrorsFormatted.toString());
				} else if (testErrors == null || testErrors.size() <= 0) {
					testErrorsFormatted.append("§aSuccess!");
					// NOTE: DO NOT DISPLAY THIS STRING: IT IS ANNOYING
					// ONLY DISPLAY FAILURES
				}
			}
		});
		controlList.add(testButton);

		// Set up the keyword text area
		helpTextArea = new GuiScrollingTextPanel(10, 32, 125, height - 60,
				false, fontRenderer, true);

		// Position the change suggestion mode button below the keywordtextarea
		selectHelpButton = new GuiButton(1300, helpTextArea.getX(),
				helpTextArea.getY() + helpTextArea.getHeight() + 5, 70, 20, "");
		controlList.add(selectHelpButton);

		// Set up minetunes elements
		updateMineTunesElements();
	}

	@Override
	protected void updateButtons() {
		super.updateButtons();

		doneButton.xPosition = width / 2 - 60;

		if (signsHereBefore.length <= 0) {
			recallButton.enabled = false;
			recallButton.drawButton = false;
			recallButton.displayString = "(0)";
		} else {
			recallButton.enabled = true;
			recallButton.displayString = "(" + signsHereBefore.length + ")";
		}

		// Update sign color code buttons
		if (lockedCode == null) {
			lockButton.displayString = "Lock";
		} else {
			lockButton.displayString = TileEntitySignMinetunes
					.getNameForSignColorCode(lockedCode);
		}

		// Update helpstate button
		if (helpState == HELP_STATE_KEYWORD) {
			selectHelpButton.displayString = "Keyword";
		} else if (helpState == HELP_STATE_TOKEN) {
			selectHelpButton.displayString = "MusicString";
		} else if (helpState == HELP_STATE_DETECT) {
			selectHelpButton.displayString = "";
		} else if (helpState == HELP_STATE_HIDDEN) {
			selectHelpButton.displayString = "Hide";
		}

		// Update shovel buttons
		if (bufferPosition <= 0 || savedSigns.size() <= 1) {
			shovelButtonUp.enabled = false;
		} else {
			shovelButtonUp.enabled = true;
		}

		if (bufferPosition >= savedSigns.size() - 1 || savedSigns.size() <= 1) {
			shovelButtonDown.enabled = false;
		} else {
			shovelButtonDown.enabled = true;
		}
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		super.actionPerformed(par1GuiButton);

		if (par1GuiButton.id == 1300) {
			// Help pane button -- until detect is working right
			if (helpState == HELP_STATE_DETECT) {
				helpState = HELP_STATE_KEYWORD;
			} else if (helpState == HELP_STATE_KEYWORD) {
				helpState = HELP_STATE_TOKEN;
			} else if (helpState == HELP_STATE_TOKEN) {
				helpState = HELP_STATE_HIDDEN;
			} else {
				// If helpState == HELP_STATE_HIDDEN or if the value is funky
				// if (BlockSignMinetunes.debug) {
				helpState = HELP_STATE_DETECT;
				// } else {
				// helpState = HELP_STATE_KEYWORD;
				// }
			}

			// Acknowledge changes
			updateMineTunesElements();
		} else if (par1GuiButton.id == 2000) {
			// Inc color

			// Clear old code
			String currCode = sign.signColorCode;
			TileEntitySignMinetunes.removeSignColorCodes(sign.signText);

			if (sign.signText[2].length() <= 13) {
				String newCode = "f";
				// Decide on new code
				if (currCode == null) {
					newCode = "1";
				} else {
					newCode = TileEntitySignMinetunes.nextCodeInCycle(currCode);
				}

				// add new code
				sign.signText[2] += "%" + newCode;
			} else {
				if (sign.signText[2].length() == 14) {
					setBottomMessage("Take a letter off of Line 3 first.",
							0xffffff);
				} else {
					setBottomMessage("Take 2 letters out of Line 3.", 0xffffff);
				}
			}
			sign.updateEntity();
		} else if (par1GuiButton.id == 2100) {
			// Dec color

			// Clear old code
			String currCode = sign.signColorCode;
			TileEntitySignMinetunes.removeSignColorCodes(sign.signText);

			if (sign.signText[2].length() <= 13) {
				String newCode = "f";
				// Decide on new code
				if (currCode == null) {
					newCode = "1";
				} else {
					newCode = TileEntitySignMinetunes.prevCodeInCycle(currCode);
				}

				// add new code
				sign.signText[2] += "%" + newCode;
			} else {
				if (sign.signText[2].length() == 14) {
					setBottomMessage("Take a letter off of Line 3.", 0xffffff);
				} else {
					setBottomMessage("Take 2 letters out of Line 3.", 0xffffff);
				}
			}
			sign.updateEntity();
		} else if (par1GuiButton.id == 2200) {
			// Lock color
			if (lockedCode == null) {
				lockedCode = sign.signColorCode;
			} else {
				lockedCode = null;
			}
			updateButtons();
		} else if (par1GuiButton.id == 2300) {
			// Clear color
			TileEntitySignMinetunes.removeSignColorCodes(sign.signText);
			sign.updateEntity();
		} else if (par1GuiButton.id == 400) {
			// Clear signs buffer; leave only the empty sign at the top of the
			// buffer
			String[] emptySign = savedSigns.get(0);
			savedSigns.clear();
			savedSigns.add(emptySign);
			editChar = sign.signText[editLine].length();
			bufferPosition = 0;
		}
	}

	@Override
	protected void keyTyped(char par1, int keyCode) {
		super.keyTyped(par1, keyCode);

		if (keyCode == Keyboard.KEY_T) {
			if (isCtrlKeyDown()) {
				// XXX: Broken
				actionPerformed(testButton);
			}
		}

		if (keyCode == Keyboard.KEY_K) {
			if (isCtrlKeyDown()) {
				// Click select help button
				// XXX: Broken
				actionPerformed(selectHelpButton);
			}
		}

		// Handle autocorrect for SFXInst
		if (keyCode == Keyboard.KEY_T
				&& sign.signText[editLine].equalsIgnoreCase("sfxinst")) {
			sign.signText[editLine] = "SFXInst2";
			editChar = sign.signText[editLine].length();
		}

		updateMineTunesElements();
	}

	@Override
	public void copyTextToEditor(String[] strings) {
		super.copyTextToEditor(strings);

		// Add any locked color code
		if (lockedCode != null) {
			TileEntitySignMinetunes.removeSignColorCodes(sign.signText);
			if (sign.signText[2].length() <= 13) {
				sign.signText[2] += "%" + lockedCode;
			}
		}

		updateMineTunesElements();
	}

	/**
	 * Updates all buttons, readouts, and text displays shown in MineTunes mode.
	 * 
	 * TODO: Note that it now updates EVERY LINE of a sign at once, instead of
	 * just one.
	 */
	private void updateMineTunesElements() {
		// The keyword text area should only be invisible if something in here
		// explicitly chooses to turn it off.
		helpTextArea.setVisible(true);

		// Parse the current sign.
		ParsedSign parsedSign = SignParser.parseSign(sign.getSignTextNoCodes());

		// Update highlighting
		updateKeywordHighlightingAndMessage(editLine, parsedSign);

		// Update stuff according to the help mode and what it is
		if (parsedSign.getLine(editLine) instanceof Comment) {
			showCommentHelp((Comment) parsedSign.getLine(editLine));
		}

		if (helpState == HELP_STATE_DETECT) {
			// Decide between keyword and token help
			// If nothing is on line, show token help
			// If a keyword is on line, show keyword help
			// Otherwise, show token help
			if (parsedSign.getLine(editLine) == null) {
				showGenericTokenHelp();
			} else if (parsedSign.getLine(editLine) instanceof SignTuneKeyword) {
				showKeywordHelp(editLine, parsedSign);
			} else if (parsedSign.getLine(editLine) instanceof String) {
				// Random text or tokens. I hope it's tokens.
				showTokenHelp((String) parsedSign.getLine(editLine));
			}
		} else if (helpState == HELP_STATE_KEYWORD) {
			// Show keyword help
			if (parsedSign.getLine(editLine) instanceof SignTuneKeyword) {
				showKeywordHelp(editLine, parsedSign);
			} else {
				showGenericKeywordHelp();
			}
		} else if (helpState == HELP_STATE_TOKEN) {
			// Show token help
			if (parsedSign.getLine(editLine) instanceof SignTuneKeyword) {
				showKeywordHelp(editLine, parsedSign);
			} else if (parsedSign.getLine(editLine) == null) {
				showGenericTokenHelp();
			} else if (parsedSign.getLine(editLine) instanceof String) {
				showTokenHelp((String) parsedSign.getLine(editLine));
			}
		} else if (helpState == HELP_STATE_HIDDEN) {
			// Hide help area
			helpTextArea.setVisible(false);
		}

	}

	/**
	 * Shows a short help guide on comments.
	 * 
	 * @param comment
	 */
	private void showCommentHelp(Comment comment) {
		// Show generic comment help
		helpTextArea.setText(SignTuneParser.COMMENT_HIGHLIGHT_CODE
				+ "Comment:§r\n" + "\n" + "Text that isn't read as music.\n"
				+ "\n"
				+ "Goto and Patt keywords can jump to signs with comments.");
		return;
	}

	private void showTokenHelp(String musicString) {
		// Decide what help to display
		if (musicString.trim().length() <= 0) {
			// Show a default help
			showGenericTokenHelp();
		} else {
			// Get token currently being edited
			StringBuffer currTokenBuffer = new StringBuffer();
			// Look for the end of the current token.
			int tokenEndIndex = editChar - 1;
			for (int i = Math.max(0, editChar - 1); i < musicString.length(); i++) {
				if (musicString.charAt(i) == ' ') {
					break;
				} else {
					tokenEndIndex = i;
				}
			}
			// Start at the end of the token and work backwards, reading it in
			for (int i = tokenEndIndex; i >= 0; i--) {
				if (musicString.charAt(i) != ' ') {
					currTokenBuffer.insert(0, musicString.charAt(i));
				} else {
					// Beginning of token found; stop reading
					break;
				}
			}
			String currToken = currTokenBuffer.toString();

			if (currToken.length() > 0) {

				// Identify token type TODO
				switch (currToken.toUpperCase().charAt(0)) {
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'R':
					showNoteTokenHelp(currToken);
					break;
				case 'V':
					showVoiceHelp(currToken);
					break;
				case 'L':
					showLayerHelp(currToken);
					break;
				case 'T':
					showTempoHelp(currToken);
					break;
				case 'I':
					showInstrumentHelp(currToken);
					break;
				case 'Y':
					showLyricHelp(currToken);
					break;
				case 'K':
					showKeyHelp(currToken);
					break;
				default:
					showGenericTokenHelp();
					break;
				}

			} else {
				// If there is no token here? (WHY)? Display generic guide.
				showGenericTokenHelp();
			}
		}
	}

	private void showKeyHelp(String currToken) {
		String colorCode = "§d";
		String errorMessage = "";

		// Try to trim off the first letter
		if (currToken.length() > 1) {
			String arguments = currToken.substring(1);

			if (arguments.length() > 0) {
				// Look for the note
				int endOfNote = recognizeLetterNote(arguments, 0);
				if (endOfNote == -1) {
					// Illegal note
					colorCode = "§c";
					errorMessage = "Follow the key token with a note, then a scale: for example, KGbMaj or KCMin";
				} else {
					// Legal note
					String scaleArgument = arguments.substring(endOfNote);
					if (scaleArgument.toUpperCase().equals("MAJ")
							|| scaleArgument.toUpperCase().equals("MIN")) {
						// Legal scale
						colorCode = "§a";
						errorMessage = "Good key token.";
					} else if (scaleArgument.length() <= 0) {
						// No scale given yet
						colorCode = "§c";
						errorMessage = "Add a scale: either 'Maj' or 'Min' for Major or Minor, respectively.";
					} else {
						// Bad scale
						colorCode = "§c";
						errorMessage = scaleArgument
								+ " is not a valid scale. Try either 'Maj' or 'Min' instead.";
					}
				}
			}
		} else {
			// Only first letter has been written
			// Make first line red
			colorCode = "§c";
			errorMessage = "Add a note name and a scale. Example tokens: KA#Maj or KBbMin. (Sets the key to A Major, and Bb Minor, respectively).\n\nThis token sets the 'key' of the ditty on all voices and layers after this time in the ditty, like a key signature on sheet music (e.g. If the key is GMaj, all F notes are turned into F#)";
		}

		StringBuilder help = new StringBuilder();
		help.append(colorCode);
		help.append("Key Signature: ");
		help.append(currToken);
		help.append("\n\n");
		help.append(errorMessage);
		helpTextArea.setText(help.toString());
	}

	/**
	 * Finds the end index of a letter note, as defined at
	 * http://code.google.com/p/jfugue/wiki/MusicString for a note starting at
	 * the given offset of the given source string.
	 * 
	 * @param sourceString
	 * @param offset
	 * @return first index after the end of the note, or -1 if none is found, or
	 *         if the offset is after the end of the string
	 */
	private int recognizeLetterNote(String sourceString, int offset) {
		// Range check
		if (offset >= sourceString.length()) {
			return -1;
		}

		// Look for the letter A, B, C, D, E, F, or G
		boolean noteStartFound = false;
		switch (sourceString.toUpperCase().charAt(offset)) {
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
			noteStartFound = true;
			break;
		}
		if (!noteStartFound) {
			// No legal note letter found.
			return -1;
		} else {
			// Start letter was found
			// Seek a note modifier
			if (offset + 1 >= sourceString.length()) {
				// String too short for a modifier to fit. Return the end
				// position of just the first letter.
				return offset + 1;
			} else {
				// Look for first letter of modifier
				boolean modifierFirstLetterFound = false;
				switch (sourceString.toUpperCase().charAt(offset + 1)) {
				case '#':
				case 'B':
				case 'N':
					modifierFirstLetterFound = true;
					break;
				}

				if (modifierFirstLetterFound) {
					// A modifier's first letter was found

					// Check length of string again, before looking for a second
					// letter
					if (offset + 2 >= sourceString.length()) {
						// String too short for a second modifier letter to fit.
						// Return the end position of just the first two
						// letters.
						return offset + 2;
					} else {
						// Look for final letter of modifier
						boolean modifierSecondLetterFound = false;
						switch (sourceString.toUpperCase().charAt(offset + 2)) {
						case '#':
						case 'B':
							modifierSecondLetterFound = true;
							break;
						}
						if (modifierSecondLetterFound) {
							// Only ## and bb are valid modifiers: #b and b# are
							// invalid. Check that here.
							if (sourceString.toUpperCase().charAt(offset + 1) != sourceString
									.toUpperCase().charAt(offset + 2)) {
								// Not really a valid second letter
								return offset + 2;
							} else {
								// A modifier's second letter was found
								return offset + 3;
							}
						} else {
							// Not found. 1 letter modifier
							return offset + 2;
						}
					}
				} else {
					// Just return the first letter of the note
					return offset + 1;
				}
			}
		}
	}

	private void showLayerHelp(String currToken) {
		String colorCode = "§d";
		String errorMessage = "";

		// Try to trim off the first letter
		String voiceNumString = currToken;
		if (voiceNumString.length() > 1) {
			voiceNumString = currToken.substring(1);

			int voiceNum = 0;
			// Try to parse second part
			if (voiceNumString.matches("\\d+")) {
				// Is at least one or any number of digits
				voiceNum = Integer.parseInt(voiceNumString);
				// Check range
				if (voiceNum < 0 || voiceNum > 15) {
					// Out of range
					colorCode = "§c";
					errorMessage = "Layers run from 0 to 15.";
				} else {
					// In range
					colorCode = "§a";
					errorMessage = "Good layer.";
				}
			} else {
				// Is a constant
				colorCode = "§e";
				errorMessage = "Constant: §e"
						+ voiceNumString
						+ "\n\n§eRemember to define this constant somewhere with the $ token, otherwise this token won't do anything.";
			}
		} else {
			// Only first letter has been written
			// Make first line red
			colorCode = "§c";

			errorMessage = "Follow with a number from 0 to 15 or a constant.\n\nNotes after this token are played in the given layer of the current voice. Layers are like voices inside of voices: they let you play multiple melodies with one voice. Every voice has 16 layers; each layer is played with the voice's instrument. The SyncWith keyword is very handy when using layers. You could play a chord with layers in voice 0 like this:\n\n§dC L1 E L2 G";
		}

		StringBuilder help = new StringBuilder();
		help.append(colorCode);
		help.append("Layer: ");
		help.append(currToken);
		help.append("\n\n");
		help.append(errorMessage);
		helpTextArea.setText(help.toString());
	}

	private void showVoiceHelp(String currToken) {
		String colorCode = "§d";
		String errorMessage = "";

		// Try to trim off the first letter
		String voiceNumString = currToken;
		if (voiceNumString.length() > 1) {
			voiceNumString = currToken.substring(1);

			int voiceNum = 0;
			// Try to parse second part
			if (voiceNumString.matches("\\d+")) {
				// Is at least one or any number of digits
				voiceNum = Integer.parseInt(voiceNumString);
				// Check range
				if (voiceNum < 0 || voiceNum > 15) {
					// Out of range
					colorCode = "§c";
					errorMessage = "Voices run from 0 to 15.";
				} else {
					// In range
					colorCode = "§a";
					if (voiceNum == 9) {
						errorMessage = "9 is the Percussion Voice -- any notes after this token become drum beats and other sounds.";
					}
				}
			} else {
				// Is a constant
				colorCode = "§e";
				errorMessage = "Constant: §e"
						+ voiceNumString
						+ "\n\n§eRemember to define this constant somewhere with the $ token, otherwise this token won't play.";
			}
		} else {
			// Only first letter has been written
			// Make first line red
			colorCode = "§c";

			errorMessage = "Follow with a number from 0 to 15 or a constant.\n\n9 is the percussion voice.\n\nNotes after this token are played in the given voice. Voices are like individual musicians in a band: each can use one instrument, and all play at the same time. You can synchronize voices if you loose track how many notes each has played with the §bReset§r, §bSyncWith§r, and §bSyncVoices§r keywords.";
		}

		StringBuilder help = new StringBuilder();
		help.append(colorCode);
		help.append("Voice: ");
		help.append(currToken);
		help.append("\n\n");
		help.append(errorMessage);
		helpTextArea.setText(help.toString());
	}

	private void showLyricHelp(String currToken) {
		String colorCode = "§d";
		String errorMessage = "";

		// Try to trim off the first letter
		String lyricLabel = currToken;
		if (lyricLabel.length() > 1) {
			lyricLabel = currToken.substring(1);
			// Is a constant
			if (LyricKeyword.isValidCueLabel(lyricLabel)) {
				colorCode = "§a";
				errorMessage = "Lyric Name: §a" + lyricLabel
						+ "\n\n§eWill show lyric:\n§e" + lyricLabel;
			} else {
				colorCode = "§c";
				errorMessage = "Lyric names only have letters, numbers, and underscores in them.";
			}
		} else {
			// Only first letter has been written
			// Make first line red
			colorCode = "§c";

			errorMessage = "Follow with a lyric name.\n\n"
					+ "Lyric tokens trigger lyrics set with the §bLyric§r keyword.";
		}

		StringBuilder help = new StringBuilder();
		help.append(colorCode);
		help.append("Lyric: ");
		help.append(currToken);
		help.append("\n\n");
		help.append(errorMessage);
		helpTextArea.setText(help.toString());
	}

	private void showTempoHelp(String currToken) {
		String colorCode = "§d";
		String errorMessage = "";

		// Get the tempo constants
		Map<String, String> allTempoConstants = Tempo.DICT_MAP;
		Set<String> allTempoKeys = allTempoConstants.keySet();

		// Try to trim off the first letter
		String tempoNumString = currToken;
		if (tempoNumString.length() > 1) {
			tempoNumString = currToken.substring(1);

			int tempoNum = 0;
			// Try to parse second part
			if (tempoNumString.matches("\\d+")) {
				// Is at least one or any number of digits
				tempoNum = Integer.parseInt(tempoNumString);
				// Check range
				if (tempoNum < 20 || tempoNum > 300) {
					// Out of range
					colorCode = "§c";
					errorMessage = "Tempos can range between 20 and 300 BPM.";
				} else {
					// In range
					colorCode = "§a";
					errorMessage = "Tempo: " + tempoNum + " BPM";
				}
			} else {
				// Is a constant
				if (allTempoKeys.contains(tempoNumString.toUpperCase())) {
					// Matches a constant
					colorCode = "§a";
					errorMessage = "§aConstant:§r "
							+ tempoNumString
							+ " ("
							+ allTempoConstants.get(tempoNumString
									.toUpperCase()) + " BPM)";
				} else {
					// Does not match a default tempo constant
					colorCode = "§e";
					errorMessage = "§eConstant: "
							+ tempoNumString
							+ "\n\n§eRemember to define this constant somewhere with the $ token, otherwise the tempo won't change.";
				}

				StringBuilder matchingConstantsList = new StringBuilder();
				// Get the matching default tokens
				for (String s : allTempoKeys) {
					if (s.toUpperCase()
							.startsWith(tempoNumString.toUpperCase())) {
						matchingConstantsList.append(s);
						matchingConstantsList.append(" (");
						matchingConstantsList.append(allTempoConstants.get(s));
						matchingConstantsList.append(")\n");
					}
				}

				errorMessage = "\n\n§dConstants:\n"
						+ matchingConstantsList.toString();
			}
		} else {
			// Only first letter has been written
			// Make first line red
			colorCode = "§c";

			StringBuilder errorMessageBuilder = new StringBuilder();
			errorMessageBuilder
					.append("Follow with a number between 20 and 300 or a constant.\n\nChanges the tempo at this time in the ditty.\n\n§dConstants:§r\n");
			for (String s : allTempoKeys) {
				errorMessageBuilder.append(s).append(" (")
						.append(allTempoConstants.get(s)).append(" BPM)\n");
			}
			errorMessageBuilder
					.append("\n Works simultaneously across all voices and layers.");
			errorMessage = errorMessageBuilder.toString();
		}

		StringBuilder help = new StringBuilder();
		help.append(colorCode);
		help.append("Tempo: ");
		help.append(currToken);
		help.append("\n\n");
		help.append(errorMessage);
		helpTextArea.setText(help.toString());
	}

	private void showInstrumentHelp(String currToken) {
		String colorCode = "§d";
		String errorMessage = "";

		// Get the instrument constants
		Map<String, String> allInstrumentConstants = INSTRUMENT_NAMES_MAP;
		LinkedList<String> allInstrumentNames = new LinkedList<String>(
				allInstrumentConstants.keySet());
		Collections.sort(allInstrumentNames);
		String[] instruments = Instrument.INSTRUMENT_NAME;

		// Try to trim off the first letter
		String instrumentNumString = currToken;
		if (instrumentNumString.length() > 1) {
			instrumentNumString = currToken.substring(1);

			int instrumentNum = 0;
			// Try to parse second part
			if (instrumentNumString.matches("\\d+")) {
				// Is at least one or any number of digits
				instrumentNum = Integer.parseInt(instrumentNumString);
				// Check range
				if (instrumentNum < 0 || instrumentNum > 127) {
					// Out of range
					colorCode = "§c";
					errorMessage = "Instruments go from 0 to 127.";
				} else {
					// In range
					colorCode = "§a";
					errorMessage = "Instrument: " + instruments[instrumentNum];
				}
			} else {
				// Is a instrument
				if (allInstrumentNames.contains(instrumentNumString
						.toUpperCase())) {
					// Matches a constant
					colorCode = "§a";
					errorMessage = "§aConstant:§r "
							+ instrumentNumString
							+ " ("
							+ allInstrumentConstants.get(instrumentNumString
									.toUpperCase()) + ")";
				} else {
					// Does not match a default instrument constant
					colorCode = "§e";
					errorMessage = "§eConstant: "
							+ instrumentNumString
							+ "\n\n§eRemember to define this constant somewhere with the $ token, otherwise this token won't play.";
				}

				StringBuilder matchingConstantsList = new StringBuilder();
				// Get the matching default tokens
				for (String s : allInstrumentNames) {
					if (s.toUpperCase().startsWith(
							instrumentNumString.toUpperCase())) {
						matchingConstantsList.append(s);
						matchingConstantsList.append(" (");
						matchingConstantsList.append(allInstrumentConstants
								.get(s));
						matchingConstantsList.append(")\n");
					}
				}

				errorMessage = "\n\n§dConstants:\n"
						+ matchingConstantsList.toString();
			}
		} else {
			// Only first letter has been written
			// Make first line red
			colorCode = "§c";

			StringBuilder errorMessageBuilder = new StringBuilder();
			errorMessageBuilder
					.append("Follow with a number between 0 and 127 or a constant.\n\nSets the instrument for the current voice. Notes after this token will be played in the given instrument.\n\n§dConstants:§r\n");
			for (String s : allInstrumentNames) {
				errorMessageBuilder.append(s).append(" (")
						.append(allInstrumentConstants.get(s)).append(")\n");
			}
			errorMessage = errorMessageBuilder.toString();
		}

		StringBuilder help = new StringBuilder();
		help.append(colorCode);
		help.append("Instrument: ");
		help.append(currToken);
		help.append("\n\n");
		help.append(errorMessage);
		helpTextArea.setText(help.toString());
	}

	// private String listMatchingJFugueConstants(String matchWith, String
	// colorCode, ) {
	// StringBuilder list = new StringBuilder();
	//
	//
	//
	// // Find and format a list of all default JFugue constants that start
	// // with the given string
	// // Ignore case
	// String matchString = "";
	// if (matchWith != null) {
	// matchString = matchWith.toUpperCase();
	// }
	//
	// Set<String> constantNames = JFugueDefinitions.DICT_MAP.keySet();
	// for (String s:constantNames) {
	// if (s.toUpperCase().startsWith(matchString)) {
	// list.append(s);
	// list.append("\n");
	// }
	// }
	//
	// return list.toString();
	// }
	private void showNoteTokenHelp(String currToken) {
		StringBuilder help = new StringBuilder();
		String colorCode = "§a";
		String errorMessage = null;

		// Break note into parts
		LinkedList<JFugueElement> readNotes = new LinkedList<JFugueElement>();
		ParserListener parserListener = new GuiEditSignNoteHelpParserListener(
				readNotes);
		try {
			musicStringParser = new MusicStringParser();
			musicStringParser.addParserListener(parserListener);
			musicStringParser.parseTokenStrict(currToken);
		} catch (JFugueException e) {
			// TODO Auto-generated catch block
			colorCode = "§c";
			errorMessage = e.getMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			colorCode = "§c";
			errorMessage = "MineTunes can't make head nor tail of this token.";
		} finally {
			musicStringParser.removeParserListener(parserListener);
		}

		help.append(colorCode);
		help.append("Note Token: " + currToken);
		if (readNotes.size() > 0) {
			help.append("\n\nEquivalent to:\n");
			for (JFugueElement e : readNotes) {
				help.append("§a").append(e.getMusicString());
			}
		}
		if (errorMessage != null) {
			help.append("\n\n§cCan't Read Token:\n");
			help.append(errorMessage);
		}
		help.append("\n\n"
				+ "§bExample notes:\n\n"
				+ "§bC#h§r C sharp for a quarter note\n"
				+ "§bEb6i§r E flat (6th octave) for an eighth note\n"
				+ "§bAqi§r lasts for a quarter note + an eighth note.\n"
				+ "\n"
				+ "§bDuration letters:\n\n"
				+ "* W -- whole note\n"
				+ "* H -- half note\n"
				+ "* Q -- quarter note\n"
				+ "* I -- eighth note\n"
				+ "* S -- 16th note\n"
				+ "* T -- 32th note\n"
				+ "* X -- 64th note\n"
				+ "* O -- 128th note\n"
				+ "\n"
				+ "Rests are silent. They consist of the letter R followed by a duration.\n"
				+ "\n"
				+ "For more info on notes and every other MusicString token, see this guide:\n\njfugue.org/howto.html");
		helpTextArea.setText(help.toString());
	}

	// String lastTokenHelpShown = "";
	// StringBuilder lastTokenHelp = new StringBuilder();

	private static MusicStringParser musicStringParser = new MusicStringParser();

	/**
	 * Load and display a generic musicstring help guide (only lists tokens)
	 */
	private void showGenericTokenHelp() {
		helpTextArea.setText(ResourceManager
				.loadCached("help/genericTokenHelp.txt"));
	}

	/**
	 * Adjusts the highlighting on all lines, and shows any errors for the
	 * keyword or line currently being edited.
	 * 
	 * @param editLine
	 *            Line currently being edited
	 * @param parsedSign
	 */
	private void updateKeywordHighlightingAndMessage(int editLine,
			ParsedSign parsedSign) {
		for (int i = 0; i < parsedSign.getSignText().length; i++) {
			Object signLineContents = parsedSign.getLine(i);

			if (signLineContents instanceof SignTuneKeyword) {
				SignTuneKeyword keyword = (SignTuneKeyword) signLineContents;
				int bottomMessageColor;
				switch (keyword.getErrorMessageType()) {
				case SignTuneKeyword.ERROR:
					bottomMessageColor = 0xff0000;
					sign.highlightLine[i] = "§4";
					break;
				case SignTuneKeyword.WARNING:
					bottomMessageColor = 0xffff00;
					sign.highlightLine[i] = "§e";
					break;
				case SignTuneKeyword.INFO:
					bottomMessageColor = 0x0000ff;
					sign.highlightLine[i] = "§1";
					break;
				default:
					bottomMessageColor = 0xffffff;
					sign.highlightLine[i] = SignTuneParser.KEYWORD_HIGHLIGHT_CODE;
					break;
				}

				setBottomMessage(keyword.getErrorMessage(), bottomMessageColor);
			} else if (signLineContents instanceof Comment) {
				// Line is a comment
				sign.highlightLine[i] = "§b";

				if (editLine == i) {
					clearBottomMessage();
				}
			} else {
				// Is not a keyword. By default, make line normal-colored:
				if (editLine == i) {
					clearBottomMessage();
				}

				sign.highlightLine[i] = "";

				// But it might be an errored musicstring also
				// Check tokens for errors
				for (String token : sign.getSignTextNoCodes()[i].split(" ")) {
					if (token.trim().length() > 0) {
						try {
							musicStringParser.parseTokenStrict(token.trim());
						} catch (JFugueException e) {
							// Token is not a valid token
							sign.highlightLine[i] = "§4";
						} catch (Exception e) {
							// Token is a really screwed up token!
							sign.highlightLine[i] = "§4";
						}
					}
				}
			}
		}
	}

	private void showKeywordHelp(int line, ParsedSign parsedSign) {

		SignTuneKeyword keyword = null;
		Object keywordObject = parsedSign.getLine(line);

		// Handle null keywords
		if (keywordObject == null) {
			showGenericKeywordHelp();
			return;
		} else if (keywordObject instanceof SignTuneKeyword) {
			keyword = (SignTuneKeyword) keywordObject;
		}

		// Handle different types of keywords
		if (keyword instanceof ProxPadKeyword) {
			// XXX: Note: there is a hacky solution here to showing
			// proximity help
			if (sign.signText[line].trim().toLowerCase()
					.startsWith("proximity")) {
				showDefaultKeywordHelp(keyword);
			} else {
				// If the keyword is not the special case of a proximity
				// keyword
				showDefaultKeywordHelp(keyword);
			}
		} else if (keyword instanceof ExplicitGotoKeyword) {
			// Show what gotos are pointing at right now

			// "Run" all gotos on sign to find the block they point at
			Point3D startBlock = new Point3D(sign.xCoord, sign.yCoord,
					sign.zCoord);
			Point3D pointedAtBlock = startBlock.clone();
			int currSignFacing = SignTuneParser.getSignFacing(
					sign.getBlockMetadata(), sign.getBlockType());
			for (int i = 0; i < sign.signText.length; i++) {
				// On each line of the sign
				SignTuneKeyword gotoCandidate = SignParser.parseKeyword(sign
						.getSignTextNoCodes()[i]);
				// ... if there's a goto keyword ...
				if (gotoCandidate instanceof ExplicitGotoKeyword) {
					ExplicitGotoKeyword g = (ExplicitGotoKeyword) gotoCandidate;
					int amount = g.getAmountMove();

					// Decide the direction to move
					if (g.getKeyword().equalsIgnoreCase("right")
							|| g.getKeyword().equalsIgnoreCase("left")) {
						// Handle moving left or right
						if (g.getKeyword().equalsIgnoreCase("left")) {
							amount = -amount;
						}

						// Adjust next sign position based on the amount to
						// move and the current sign's facing
						pointedAtBlock = SignTuneParser
								.getCoordsRelativeToSign(pointedAtBlock,
										currSignFacing, amount, 0, 0);
					}

					if (g.getKeyword().equalsIgnoreCase("in")
							|| g.getKeyword().equalsIgnoreCase("out")) {
						// Handle moving up or down
						if (g.getKeyword().equalsIgnoreCase("in")) {
							amount = -amount;
						}

						// Adjust next sign position based on the amount to
						// move and the current sign's facing
						pointedAtBlock = SignTuneParser
								.getCoordsRelativeToSign(pointedAtBlock,
										currSignFacing, 0, 0, amount);
					}

					if (g.getKeyword().equalsIgnoreCase("up")
							|| g.getKeyword().equalsIgnoreCase("down")) {
						// Handle moving up or down
						if (g.getKeyword().equalsIgnoreCase("down")) {
							amount = -amount;
						}

						// Adjust next sign position based on the amount to
						// move and the current sign's facing
						pointedAtBlock = SignTuneParser
								.getCoordsRelativeToSign(pointedAtBlock,
										currSignFacing, 0, amount, 0);
					}
				}
			}

			// Tell user what that block is/says
			// Check that the end block isn't the same block we started at
			if (pointedAtBlock.equals(startBlock)) {
				helpTextArea.setText("§cThis sign points at itself!");
			} else {
				// Not pointing at itself
				int pointedBlockID = mc.theWorld.getBlockId(pointedAtBlock.x,
						pointedAtBlock.y, pointedAtBlock.z);
				Block pointedBlock = null;
				for (Block b : Block.blocksList) {
					if (b != null && b.blockID == pointedBlockID) {
						// Found the block!
						pointedBlock = b;
					}
				}
				if (pointedBlockID == 0) {
					// Gotos point at air
					helpTextArea.setText("§eGotos on sign point at thin air.");
				} else if (pointedBlock == null) {
					helpTextArea
							.setText("§eGotos on sign point at something that isn't a sign.");
				} else if (SignTuneParser.getSignBlockType(pointedAtBlock,
						mc.theWorld) != null) {
					// Get sign's text
					TileEntity pointedEntity = mc.theWorld.getBlockTileEntity(
							pointedAtBlock.x, pointedAtBlock.y,
							pointedAtBlock.z);
					if (pointedEntity instanceof TileEntitySign) {
						// Show text
						StringBuilder t = new StringBuilder(
								"§aGotos point at a sign that says:\n\n");
						TileEntitySign pointedSignEntity = (TileEntitySign) pointedEntity;
						for (String s : pointedSignEntity.signText) {
							t.append("§b");
							t.append(s);
							t.append("§r\n");
						}
						helpTextArea.setText(t.toString());
					}
				} else {
					// Points at known block type that isn't a sign
					// if (mc.theWorld.isRemote) {
					// If multiplayer, do not tell user what kind of
					// block they're pointing at: could be used to spy
					helpTextArea.setText("§eGotos on sign point at a block.");
					// } else {
					// //Revised for MC 1.3.1: Always show block type.
					// keywordTextArea.setText("§eGotos on sign point at a "
					// +
					// StringTranslate.getInstance().translateNamedKey(pointedBlock.getBlockName())
					// + " block");
					// }
				}
			}
		} else if (keyword instanceof SFXKeyword) {
			// Auto-suggest sounds

			if (keyword.getWholeKeyword().equalsIgnoreCase("sfx")) {
				showDefaultKeywordHelp(keyword);
			} else {
				// Fill sounds auto-suggest
				ArrayList<String> matchingEffects = new ArrayList<String>();
				HashMap<String, String> allEffectKeys = SFXManager
						.getAllEffects(SFXManager.getLatestSource());
				Set<String> allEffectHandles = allEffectKeys.keySet();
				String currHandle = ((SFXKeyword) keyword).getEffectShorthand();
				String exactMatch = null;
				for (String handle : allEffectHandles) {
					if (handle.toLowerCase().startsWith(
							currHandle.toLowerCase())) {
						if (handle.equalsIgnoreCase(currHandle)) {
							exactMatch = handle;
						} else {
							matchingEffects.add(handle);
						}
					}
				}
				Collections.sort(matchingEffects);

				// Compile matching handles into a string
				StringBuilder b = new StringBuilder();
				if (exactMatch != null) {
					b.append("§aCurrent SFX: ");
					b.append(exactMatch);
				} else {
					b.append("§cNo Match Yet");
				}
				if (matchingEffects.size() > 0) {
					b.append("§r\n\n");
					b.append("§eOther SFX:\n");
					for (String handle : matchingEffects) {
						b.append(handle);
						b.append("\n");
					}
				}
				helpTextArea.setText(b.toString());
			}
		} else if (keyword instanceof GotoKeyword) {
			// Show matching comments in immediate area

			if (keyword.getWholeKeyword().equalsIgnoreCase("goto")
					|| keyword.getWholeKeyword().equalsIgnoreCase("patt")) {
				showDefaultKeywordHelp(keyword);
			} else {
				GotoKeyword gotoKeyword = (GotoKeyword) keyword;

				// Fill auto-suggest
				Comment bestMatch = GotoKeyword.getNearestMatchingComment(
						new Point3D(sign.xCoord, sign.yCoord, sign.zCoord),
						mc.theWorld, gotoKeyword.getComment());
				LinkedList<Comment> matchingComments = gotoKeyword
						.matchingCommentsNearby(new Point3D(sign.xCoord,
								sign.yCoord, sign.zCoord), mc.theWorld,
								gotoKeyword.getComment());

				// Compile matching comments into a string
				StringBuilder b = new StringBuilder();
				if (bestMatch != null) {
					b.append("§aTargeted Comment: §b");
					b.append(bestMatch.getCommentText());
					b.append(" §r\n(");
					b.append(Math
							.round(bestMatch.getLocation().distanceTo(
									new Point3D(sign.xCoord, sign.yCoord,
											sign.zCoord))));
					b.append(" blocks away)");
				} else {
					b.append("§cNo Match");
				}
				if (matchingComments.size() > 0) {
					b.append("§r\n\n");
					b.append("§eNearby Comments:\n");
					for (Comment c : matchingComments) {
						b.append("§a(");
						b.append(Math.round(c.getLocation().distanceTo(
								new Point3D(sign.xCoord, sign.yCoord,
										sign.zCoord))));
						b.append(")§r");
						b.append(c.getCommentText());
						b.append("\n");
					}
				}
				helpTextArea.setText(b.toString());
			}
		} else if (keyword instanceof PlayMidiKeyword) {
			if (keyword.getWholeKeyword().equalsIgnoreCase("playmidi")
					&& (line == 0 || !(parsedSign.getLine(line - 1) instanceof PlayMidiKeyword))) {
				// If first line and just a bare keyword
				showDefaultKeywordHelp(keyword);
			} else if (line == 0
					|| !(parsedSign.getLine(line - 1) instanceof PlayMidiKeyword)) {
				// If first line
				helpTextArea
						.setText("§6Put the MIDI to play §6on the next line.");
			} else {
				//
				File[] midiFileList = new File(
						MinetunesConfig.getMinetunesDir(), "midi")
						.listFiles(new MidiFileFilter());

				LinkedList<File> matchingMidis = new LinkedList<File>();
				File exactMatch = null;

				String filenameFromKeyword = ((BaseMidiKeyword) parsedSign
						.getLine(line)).getMidiFile().getPath();
				if (filenameFromKeyword == null) {
					// Replace null filenames with empty strings
					filenameFromKeyword = "";
				}

				for (File f : midiFileList) {
					if (f.getName()
							.toLowerCase()
							.startsWith(
									stripFilenameExtension(filenameFromKeyword)
											.toLowerCase())) {
						matchingMidis.add(f);
					}

					if (f.getName().equalsIgnoreCase(filenameFromKeyword)) {
						exactMatch = f;
					}
				}

				StringBuilder midiMatchListText = new StringBuilder();

				if (exactMatch != null) {
					midiMatchListText.append("§aWill Play:\n\n§a");
					midiMatchListText.append(stripFilenameExtension(exactMatch
							.getName()));
					midiMatchListText.append("\n\n");
				}

				if (matchingMidis.size() > 0) {
					midiMatchListText.append("§6Matching MIDIs:\n");
					for (File f : matchingMidis) {
						midiMatchListText.append(stripFilenameExtension(f
								.getName()));
						midiMatchListText.append("\n");
					}
				} else {
					// No matches
					midiMatchListText
							.append("§6No matches in your midi folder.§r\n"
									+ "If somebody else has this file, it will play for them.");
				}
				helpTextArea.setText(midiMatchListText.toString());
			}
		} else if (keyword instanceof SFXInstKeyword) {
			SFXInstKeyword sfxInstKeyword = (SFXInstKeyword) keyword;
			boolean showDefaultHelp = false;
			StringBuilder additionalText = new StringBuilder();
			if ((keyword.getWholeKeyword().equalsIgnoreCase("sfxinst") || (keyword
					.getWholeKeyword().equalsIgnoreCase("sfxinst2")))
					&& (line == 0 || !(parsedSign.getLine(line - 1) == keyword))) {
				// If first line and just a bare keyword
				showDefaultHelp = true;
			} else if (line == 0 || !(parsedSign.getLine(line - 1) == keyword)) {
				// If first line

				// Show list of matching instruments for number
				int currentInstrument = sfxInstKeyword.getInstrument();
				boolean showAllInstruments = false;
				if (currentInstrument == 0) {
					// Could be undefined. Let all instruments be shown
					showAllInstruments = true;
				}

				// Find list of matching instruments
				LinkedList<String> matchingInstruments = new LinkedList<String>();

				// Get map of instrument names to numbers
				String[] instruments = Instrument.INSTRUMENT_NAME;

				// Select matching instruments
				String instrumentString = Integer.toString(currentInstrument);
				for (int i = 0; i < instruments.length; i++) {
					String iString = Integer.toString(i);
					if (iString.startsWith(instrumentString)
							|| showAllInstruments) {
						matchingInstruments.add("§a" + i + "§r: §7"
								+ instruments[i]);
					}
				}

				// Assemble text to display
				additionalText.append("Instruments:\n");

				for (String s : matchingInstruments) {
					additionalText.append(s).append("\n");
				}
			} else {
				// Second line, by deduction

				// Show SFX options
				// COPIED FROM SFX KEYWORD HELP

				// Show warning if using old sounds
				additionalText.append("§bFrom Minecraft:\n");
				if (sfxInstKeyword.getSFXSource() != SFXManager
						.getLatestSource()) {
					additionalText.append("§6");
				} else {
					additionalText.append("§a");
				}
				additionalText
						.append(SFXManager.getSourceName(sfxInstKeyword
								.getSFXSource())).append("\n\n");

				// Fill sounds auto-suggest
				ArrayList<String> matchingEffects = new ArrayList<String>();
				HashMap<String, String> allEffectKeys = SFXManager
						.getAllEffects(sfxInstKeyword.getSFXSource());
				Set<String> allEffectHandles = allEffectKeys.keySet();
				String currHandle = sfxInstKeyword.getSFXNameIncomplete();
				if (currHandle == null) {
					currHandle = "";
				}
				String exactMatch = null;
				for (String handle : allEffectHandles) {
					if (handle.toLowerCase().startsWith(
							currHandle.toLowerCase())) {
						if (handle.equalsIgnoreCase(currHandle)) {
							exactMatch = handle;
						} else {
							matchingEffects.add(handle);
						}
					}
				}
				Collections.sort(matchingEffects);

				// Compile matching handles into a string
				if (exactMatch != null) {
					if (SFXManager.isShorthandOnSFXInstBlacklist(exactMatch,
							sfxInstKeyword.getSFXSource())) {
						// Bad SFX; on blacklist for SFXInst
						additionalText.append("§cOut Of Order:\n§6");
					} else {
						// Good SFX
						additionalText.append("§aMatching SFX: ");
					}
					additionalText.append(exactMatch);
				} else {
					additionalText.append("§cNo Match Yet");
				}
				if (matchingEffects.size() > 0) {
					additionalText.append("§r\n\n");
					for (String handle : matchingEffects) {
						// Check for blacklist
						if (SFXManager.isShorthandOnSFXInstBlacklist(handle,
								sfxInstKeyword.getSFXSource())) {
							additionalText.append("§c");
						}
						additionalText.append(handle);
						// System.out.println(handle + ":"
						// +
						// SFXManager.getDefaultTuningInt(SFXManager.getEffectForShorthandName(handle),
						// 1));
						String sfxHandleEffect = SFXManager
								.getEffectForShorthandName(handle,
										sfxInstKeyword.getSFXSource());
						int numAlts = SFXManager
								.getNumberOfAlternativesForEffect(
										sfxHandleEffect,
										sfxInstKeyword.getSFXSource());
						additionalText.append(" §7(");
						for (int i = 0; i < numAlts; i++) {
							if (SFXManager.getDefaultTuningString(SFXManager
									.getEffectForShorthandName(handle,
											sfxInstKeyword.getSFXSource()),
									i + 1, sfxInstKeyword.getSFXSource()) != null) {
								additionalText
										.append(SFXManager.getDefaultTuningString(
												SFXManager
														.getEffectForShorthandName(
																handle,
																sfxInstKeyword
																		.getSFXSource()),
												i + 1, sfxInstKeyword
														.getSFXSource()));

							} else {
								additionalText.append(Integer.toString(i + 1));
							}
							additionalText.append(", §7");
						}
						// Remove last comma
						for (int i = 0; i < 4; i++) {
							additionalText
									.deleteCharAt(additionalText.length() - 1);
						}
						additionalText.append(")\n§r");
					}
				}
			}

			if (showDefaultHelp) {
				showDefaultKeywordHelp(keyword);
			} else {
				// Assemble multiple parts to create the final contextual help
				StringBuilder allText = new StringBuilder();

				// Show some standard readouts for sfxinst
				String[] instruments = Instrument.INSTRUMENT_NAME;
				allText.append("§bReplace:§a ")
						.append(instruments[sfxInstKeyword.getInstrument()])
						.append("\n");
				// For SFX, show either "to be selected" or the chosen one
				if (sfxInstKeyword.getSFXName() == null) {
					allText.append("§bSFX:§6 None Chosen\n");
				} else {
					allText.append("§bSFX:§a ").append(
							sfxInstKeyword.getSFXName());
					if (SFXManager.getNumberOfAlternativesForEffect(
							sfxInstKeyword.getSFXName(),
							sfxInstKeyword.getSFXSource()) > 1) {
						allText.append(" #").append(
								sfxInstKeyword.getSFXNumber());
					}
					allText.append("\n");
				}
				// For tuning, either show "unspecifed (C5)" or the tuning
				allText.append("§bTuning:§a ");

				if (sfxInstKeyword.getCenterPitch() >= 0) {
					String tuningNote = new Note(
							(byte) sfxInstKeyword.getCenterPitch())
							.getMusicString();
					if (tuningNote.length() > 2) {
						// Trim letter (duration = q) off of end
						tuningNote = tuningNote.substring(0,
								tuningNote.length() - 1);
					}
					allText.append(tuningNote);
				} else {
					// Show alternate
					allText.append("§6C5 (Default)");
				}

				allText.append("\n\n");

				// Show additional help depending on argument being worked on
				allText.append(additionalText);
				helpTextArea.setText(allText.toString());
			}
		} else {
			// ??? what did this do back in the day?: helpTextArea.setText("");
			showDefaultKeywordHelp(keyword);
		}
	}

	/**
	 * Loads a short writeup on a keyword with no dynamic stuff into the keyword
	 * text area.
	 * 
	 * @param keyword
	 */
	private String lastHelpShown = "";
	// /**
	// * Attempts to strip the extension off of a filename, if there is an
	// * extension.
	// *
	// * I.e. removes everything after the last period.
	// *
	// * @param filename
	// * @return
	// */
	// private String stripFilenameExtension(String filename) {
	// if (filename.contains(".")) {
	// return filename.substring(0, filename.lastIndexOf("."));
	// } else {
	// return filename;
	// }
	// }

	/**
	 * The proper jfugue map (Instrument.DICT_MAP) features the names of
	 * precussion instruments -- inappropriate for the I token's help.
	 */
	public static final Map<String, String> INSTRUMENT_NAMES_MAP;

	private void showDefaultKeywordHelp(SignTuneKeyword keyword) {
		if (keyword == null) {
			showGenericKeywordHelp();
			return;
		}

		if (keyword.getKeyword() != null
				&& !keyword.getKeyword().equalsIgnoreCase(lastHelpShown)) {
			String helpText = ResourceManager.loadCached("help/"
					+ keyword.getKeyword().toLowerCase() + "ShortHelp.txt");

			helpText = "§b" + helpText;
			helpText = helpText.replaceAll("\n", "§r\n");

			lastHelpShown = helpText;
		}
		helpTextArea.setText(lastHelpShown.toString());
	}

	private static String genericKeywordHelpText = null;
	private int iconTexture;
	private GuiButtonL shovelButtonUp;
	private GuiButtonL shovelButtonDown;

	// private void showGenericKeywordHelp() {
	// // Set text area to a list of all available keywords
	//
	// StringBuilder b = new StringBuilder();
	// // Add title
	// b.append("§bAll Keywords§r\n\n");
	// // Get the list of keywords
	// LinkedList<String> keywords = new LinkedList<String>();
	// for (String s : SignParser.keywords) {
	// if (!ParsedKeyword.isKeywordDeprecated(s)) {
	// keywords.add(s);
	// }
	// }
	// Collections.sort(keywords);
	// // Add the comment symbol
	// keywords.push("#");
	// // Add the keywords to the buffer, formatting them
	// for (String s : keywords) {
	// b.append(BlockSignMinetunes.KEYWORD_HIGHLIGHT_CODE);
	// b.append(s);
	// b.append("§r, ");
	// }
	// helpTextArea.setText(b.toString());
	// }

	private void showGenericKeywordHelp() {
		// Set text area to a list of all available keywords
		helpTextArea.setText(ResourceManager
				.loadCached("help/keywordsHelp.txt"));
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);

		helpTextArea.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		super.mouseMovedOrUp(par1, par2, par3);

		helpTextArea.mouseMovedOrUp(par1, par2, par3);
	}

	/**
	 * Attempts to strip the extension off of a filename, if there is an
	 * extension.
	 * 
	 * I.e. removes everything after the last period.
	 * 
	 * @param filename
	 * @return
	 */
	private String stripFilenameExtension(String filename) {
		if (filename.contains(".")) {
			return filename.substring(0, filename.lastIndexOf("."));
		} else {
			return filename;
		}
	}

}
