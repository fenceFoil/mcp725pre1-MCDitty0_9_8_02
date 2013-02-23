/**
 * 
 * Changes from Mojang AB Code
 * 
 * Copyright (c) 2012-2013 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
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
package com.minetunes.gui.signEditor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.ChatAllowedCharacters;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiEditSign;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.Packet130UpdateSign;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityRenderer;
import net.minecraft.src.TileEntitySign;

import org.jfugue.JFugueException;
import org.jfugue.ParserListener;
import org.jfugue.elements.Instrument;
import org.jfugue.elements.JFugueElement;
import org.jfugue.elements.Note;
import org.jfugue.elements.Tempo;
import org.jfugue.parsers.MusicStringParser;
import org.jfugue.util.MapUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.minetunes.MidiFileFilter;
import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.RightClickCheckThread;
import com.minetunes.SignFilenameFilter;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.FileSaverGui;
import com.minetunes.gui.FileSaverGuiListener;
import com.minetunes.gui.FileSelectorGui;
import com.minetunes.gui.FileSelectorGuiListener;
import com.minetunes.gui.GuiScrollingTextPanel;
import com.minetunes.gui.MinetunesGui;
import com.minetunes.gui.MinetunesVersionGuiElement;
import com.minetunes.resources.ResourceManager;
import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.BlockSignMinetunes;
import com.minetunes.signs.Comment;
import com.minetunes.signs.ParsedSign;
import com.minetunes.signs.SignParser;
import com.minetunes.signs.TileEntitySignMinetunes;
import com.minetunes.signs.TileEntitySignRendererMinetunes;
import com.minetunes.signs.keywords.ExplicitGotoKeyword;
import com.minetunes.signs.keywords.GotoKeyword;
import com.minetunes.signs.keywords.LyricKeyword;
import com.minetunes.signs.keywords.ParsedKeyword;
import com.minetunes.signs.keywords.PlayMidiKeyword;
import com.minetunes.signs.keywords.ProxPadKeyword;
import com.minetunes.signs.keywords.SFXInstKeyword;
import com.minetunes.signs.keywords.SFXKeyword;

public class GuiEditSignMinetunes extends GuiEditSign implements
		FileSaverGuiListener, FileSelectorGuiListener {
	/**
	 * This String is just a local copy of the characters allowed in text
	 * rendering of minecraft.
	 */
	private static final String allowedCharacters;

	public static final int SIGN_EDITOR_MODE_MINETUNES = 24601;
	public static final int SIGN_EDITOR_MODE_NORMAL = 0;
	public static final int SIGN_EDITOR_MODE_INVISIBLE = 1;

	private static final int HELP_STATE_KEYWORD = 0;
	private static final int HELP_STATE_TOKEN = 1;
	private static final int HELP_STATE_DETECT = 2;
	private static final int HELP_STATE_HIDDEN = 3;

	/** Reference to the sign object. */
	private TileEntitySignMinetunes entitySign;

	/** Counts the number of screen updates. */
	private int updateCounter;

	/** The number of the line that is being edited. */
	private int editLine;

	private static String signsDirLocation = "";

	private static int helpState = HELP_STATE_DETECT;

	/**
	 * MineTunes: Current index in the list of saved signs
	 */
	private int bufferPosition = savedSigns.size();

	/**
	 * MineTunes: Last filename returned by GuiMineTunesFileSaver
	 */
	private String fileSaverFilename;

	private boolean closeAutomatically = false;

	private String parseResultText;

	private String currentKeyword;

	private String keywordMessage = null;

	private int keywordMessageColor = 0xffffff;

	private int editChar;

	private boolean overwrite = false;

	private GuiButton editorModeButton;

	private GuiScrollingTextPanel helpTextArea;

	private GuiButton testButton;

	private GuiButton doneButton;

	private GuiButton selectHelpButton;

	private GuiButton recallButton;

	private GuiButton clearButton;

	private GuiButton lockButton;

	/**
	 * MineTunes's buffer of saved signs.
	 */
	private static LinkedList<String[]> savedSigns = new LinkedList<String[]>();

	private static boolean bufferInitalized = false;

	private static String lockedCode = null;

	public GuiEditSignMinetunes(TileEntitySign par1TileEntitySign) {
		super(par1TileEntitySign);

		// MineTunes: Change screentitle
		editLine = 0;
		editChar = 0;
		if (!(par1TileEntitySign instanceof TileEntitySignMinetunes)) {
			par1TileEntitySign = new TileEntitySignMinetunes(par1TileEntitySign);
		}
		entitySign = (TileEntitySignMinetunes) par1TileEntitySign;
		entitySign.setEditable(true);
		entitySign.alwaysRender = true;

		// IF YOU ARE READING THIS, DELETE THIS SOURCE
		// // 1.3 SSP signs blank out bug: indicate that this sign is SUPPOSED
		// to
		// // start blank
		// entitySign.bug1_3InvalidText = false;

		// MineTunes: Add first entry to savedSigns buffer
		if (bufferInitalized == false) {
			String[] firstEntry = new String[4];
			for (int i = 0; i < 4; i++) {
				firstEntry[i] = "";
			}
			savedSigns.add(firstEntry);
			bufferInitalized = true;
		}

		if (lockedCode != null) {
			// Set up the locked color code
			if (entitySign.signText[2].length() <= 13) {
				entitySign.signText[2] += "%" + lockedCode;
			}
		}

		// Set the location of the signs directory
		signsDirLocation = (new File(Minecraft.getMinecraftDir(), "signs"))
				.getAbsolutePath();
		// End MineTunes
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		if (closeAutomatically) {
			return;
		}

		if (MinetunesConfig.getInt("signeditor.mode") != SIGN_EDITOR_MODE_INVISIBLE) {
			drawDefaultBackground();
			// drawCenteredString(fontRenderer, screenTitle, width / 2, 40,
			// 0xffffff);
			// drawString(fontRenderer, "MineTunes Version "
			// + BlockSign.CURRENT_VERSION, 0, 0, 0x444444);

			drawCenteredString(fontRenderer, "Edit Sign Text", width / 2, 10,
					0xffffff);
			// drawCenteredString(fontRenderer,
			// "(PgUp and PgDown Scroll Saved Signs)", width / 2, 30, 0xcccccc);

			// Draw chars left display
			int charsLeftColor = 0x00cc00;
			if (editLine >= 0 && editLine < 4) {
				int charsLeft = (15 - entitySign.signText[editLine].length());

				boolean signColorCodePresent = false;
				if (editLine == 2 && entitySign.signColorCode != null) {
					// Color code special case
					// charsLeft -= 2;
					signColorCodePresent = true;
				}

				if (charsLeft <= 2) {
					charsLeftColor = 0xcc0000;
				} else if (charsLeft <= 5) {
					charsLeftColor = 0xcccc00;
				}

				String drawString = Integer.toString(charsLeft);
				if (signColorCodePresent) {
					drawString += " + %" + entitySign.signColorCode;
				}

				drawCenteredString(fontRenderer, drawString, width / 2, 40,
						charsLeftColor);
			}

			// Draw keyword messages display
			if (keywordMessage != null) {
				drawCenteredString(fontRenderer, keywordMessage, width / 2,
						height - 10, keywordMessageColor);
			}

			// Draw colorcode info
			drawCenteredString(fontRenderer,
					TileEntitySignMinetunes
							.getNameForSignColorCode(entitySign.signColorCode),
					width - 60, height - 44, 0xffffff);

			// Draw insert/overwrite display
			// drawRect (20, 20, 20+fontRenderer.getStringWidth("Overwrite"),
			// 20+10,
			// 0x44888888);
			if (overwrite) {
				drawString(fontRenderer, "Overwrite", 20, 20, 0xff8800);
			} else {
				drawString(fontRenderer, "Insert", 20, 20, 0xffff00);
			}

			drawCenteredString(fontRenderer, parseResultText, width / 2,
					height - 15, 0xffff00);

			// drawRect (20, 40,
			// 20+fontRenderer.getStringWidth("Loaded Sign #10"),
			// 40+10, 0x44888888);
			if (bufferPosition > 0 && bufferPosition <= savedSigns.size() - 1) {
				drawString(fontRenderer, "Loaded Sign #" + bufferPosition, 20,
						40, 0x88ff00);
			} else {
				drawString(fontRenderer, "New Sign", 20, 40, 0x0000ff);
			}
			// Need to reset color after this?
			GL11.glColor4f(0XFF, 0XFF, 0XFF, 0xff);

			// Decide the size to draw the sign at
			float signScaling = 93.75F; // The default minecraft value
			signTranslateY = 0f; // Default minecraft value
			// if (BlockSignMinetunes.signEditorMode == SIGN_EDITOR_MODE_NORMAL)
			// {
			// // Make sign much bigger
			// signScaling *= 1.34;
			// signTranslateY = -70f;
			// System.out.println (signTranslateY);
			// // Shift up
			// //signTranslateY = updateCounter;
			// //System.out.println(updateCounter);
			// }

			float signTranslateX = width / 2;
			// if (BlockSignMinetunes.signEditorMode ==
			// SIGN_EDITOR_MODE_MINETUNES) {
			// // Shove sign off to the left
			// signTranslateX = 75;
			// }

			GL11.glPushMatrix();
			GL11.glTranslatef(signTranslateX, signTranslateY, 50f);
			GL11.glScalef(-signScaling, -signScaling, -signScaling);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			Block block = entitySign.getBlockType();

			if (block == Block.signPost) {
				float f1 = (float) (entitySign.getBlockMetadata() * 360) / 16F;
				GL11.glRotatef(f1, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, -1.0625F, 0.0F);
			} else {
				int i = entitySign.getBlockMetadata();
				float f2 = 0.0F;

				if (i == 2) {
					f2 = 180F;
				}

				if (i == 4) {
					f2 = 90F;
				}

				if (i == 5) {
					f2 = -90F;
				}

				GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, -1.0625F, 0.0F);
			}

			entitySign.charBeingEdited = editChar;

			if ((updateCounter / 6) % 2 == 0) {
				entitySign.lineBeingEdited = editLine;
			}

			TileEntityRenderer.instance.renderTileEntityAt(entitySign, -0.5D,
					-0.75D, -0.5D, 0.0F);
			entitySign.lineBeingEdited = -1;
			GL11.glPopMatrix();

			if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
				helpTextArea.draw(par1, par2);
			}
		}
		updateButtons();
		
		if (entitySign.isFace(true)) {
			keywordMessage = "Dude, that is creepy.";
			keywordMessageColor = 0xffffff;
		}

		// super.drawScreen(par1, par2, par3);
		// This now calls GuiSign.drawScreen, drawing both vanilla and the
		// Minetunes gui at once!
		// Luckily, GuiScreen.drawScreen is no great shakes. Copied below.
		for (int var4 = 0; var4 < this.controlList.size(); ++var4) {
			GuiButton var5 = (GuiButton) this.controlList.get(var4);
			var5.drawButton(this.mc, par1, par2);
		}
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		// MineTunes: Check for ctrl key pressed
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
				|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
			// Place blank sign
			closeAutomatically = true;
			exitGUIScreen();
		}

		controlList.clear();
		Keyboard.enableRepeatEvents(true);
		// MineTunes: Changed button label
		// X is defined in the draw method
		doneButton = new GuiButton(0, 0,
				Math.min(height / 4 + 120, height - 40), 120, 20, "Done & Save");
		controlList.add(doneButton);

		controlList.add(new MinetunesVersionGuiElement(-100));

		// // MineTunes: Added new buttons
		// controlList.add(new GuiButton(400, 5, height - 150, 80, 20,
		// "Clear Signs"));
		// controlList.add(new GuiButton(100, 5, height - 120, 80, 20,
		// "Import"));
		// controlList.add(new GuiButton(200, 5, height - 90, 80, 20,
		// "Export"));
		// controlList.add(new GuiButton(300, 5, height - 60, 80, 20,
		// "Open Folder"));

		editorModeButton = new GuiButton(1000, width - 100, 10, 80, 20, "");
		controlList.add(new GuiButton(1100, width - 100, 35, 80, 20, "Keys"));
		controlList.add(editorModeButton);
		recallButton = new GuiButton(1400, width - 100, 60, 80, 20, "Recall");
		controlList.add(recallButton);
		clearButton = new GuiButton(1500, width - 100, 85, 80, 20, "Clear");
		controlList.add(clearButton);
		testButton = new GuiButton(1200, width - 100, 110, 80, 20, "Test Sign");
		controlList.add(testButton);

		// Set up the sign color code controls
		controlList.add(new GuiButton(2000, width - 100, height - 50, 20, 20,
				"+"));
		controlList.add(new GuiButton(2100, width - 40, height - 50, 20, 20,
				"-"));
		lockButton = new GuiButton(2200, width - 80, height - 70, 40, 20,
				"Lock");
		controlList.add(lockButton);
		controlList.add(new GuiButton(2300, width - 80, height - 30, 40, 20,
				"Clear"));

		// Set up the keyword text area
		helpTextArea = new GuiScrollingTextPanel(10, 65, 125, height - 60 - 40,
				false, fontRenderer, true);

		// Position the change suggestion mode button below the keywordtextarea
		selectHelpButton = new GuiButton(1300, helpTextArea.getX(),
				helpTextArea.getY() + helpTextArea.getHeight() + 5,
				helpTextArea.getWidth(), 20, "");
		controlList.add(selectHelpButton);

		// Update buttons for the first time to set text ect.
		updateButtons();

		// Set the sign to be editable
		entitySign.setEditable(false);

		// If in MineTunes mode, set up MineTunes elements
		if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
			updateMineTunesElements();
		}
	}

	private void updateButtons() {
		// Double color codes for symmetry
		if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
			editorModeButton.displayString = "MineTunes Mode";
		} else if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_NORMAL) {
			editorModeButton.displayString = "Normal Mode";
		} else {
			editorModeButton.displayString = "InvisiMode";
		}

		// Test button
		if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
			testButton.drawButton = true;
		} else {
			testButton.drawButton = false;
		}

		doneButton.xPosition = width / 2 - 60;

		// Change keyword mode keyword
		if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
			selectHelpButton.drawButton = true;
			// Update button text as well
			if (helpState == HELP_STATE_KEYWORD) {
				selectHelpButton.displayString = "Keywords";
			} else if (helpState == HELP_STATE_TOKEN) {
				selectHelpButton.displayString = "MusicStrings";
			} else if (helpState == HELP_STATE_DETECT) {
				selectHelpButton.displayString = "Detect";
			} else if (helpState == HELP_STATE_HIDDEN) {
				selectHelpButton.displayString = "Hidden";
			}
		} else {
			selectHelpButton.drawButton = false;
		}

		// Update number of signs available to recall
		if (signsHereBefore == null) {
			signsHereBefore = Minetunes.getUniqueSignsForPos(entitySign.xCoord,
					entitySign.yCoord, entitySign.zCoord, true);
		}
		if (signsHereBefore.length <= 0) {
			recallButton.enabled = false;
			recallButton.drawButton = false;
			recallButton.displayString = "Recall (0)";
		} else {
			recallButton.enabled = true;
			recallButton.displayString = "Recall (" + signsHereBefore.length
					+ ")";
		}

		// Update sign color code buttons
		if (lockedCode == null) {
			lockButton.displayString = "Lock";
		} else {
			lockButton.displayString = TileEntitySignMinetunes
					.getNameForSignColorCode(lockedCode);
		}
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat
	 * events
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);

		// MineTunes: Moved the stuff that sends a MP packet to a method where
		// is
		// is not unintentionally fired when views a SignEditor "sub-gui" such
		// as the guide

		entitySign.setEditable(true);
		
		// Update face state
		entitySign.isFace(true);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		if (closeAutomatically) {
			return;
		}

		updateCounter++;
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled) {
			return;
		}
		if (par1GuiButton.id == -100) {
			exitGUIScreen();
			mc.displayGuiScreen(new MinetunesGui());
		} else if (par1GuiButton.id == 0) {
			exitGUIScreen();
		} else if (par1GuiButton.id == 100) {
			// TODO: import signs from a file
			File signsDir = new File(mc.getMinecraftDir().getPath()
					+ File.separator + "signs" + File.separator);
			if (!signsDir.exists()) {
				signsDir.mkdirs();
			}
			FileSelectorGui fileChooser = new FileSelectorGui(this, signsDir,
					new SignFilenameFilter(), "Select Signs File To Import:");
			mc.displayGuiScreen(fileChooser);
		} else if (par1GuiButton.id == 200) {
			// Export signs to a file
			File signsDir = new File(mc.getMinecraftDir().getPath()
					+ File.separator + "signs" + File.separator);
			if (!signsDir.exists()) {
				signsDir.mkdirs();
			}

			FileSaverGui fileSaver = new FileSaverGui(this, signsDir, ".txt",
					"Save Signs:");
			mc.displayGuiScreen(fileSaver);
		} else if (par1GuiButton.id == 300) {
			// Open signs folder
			openExportedSignsFolder();
		} else if (par1GuiButton.id == 400) {
			// Clear signs buffer; leave only the empty sign at the top of the
			// buffer
			String[] emptySign = savedSigns.get(0);
			savedSigns.clear();
			savedSigns.add(emptySign);
			editChar = entitySign.signText[editLine].length();
			bufferPosition = 0;
		} else if (par1GuiButton.id == 1000) {
			// Switch gui mode
			if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_NORMAL) {
				MinetunesConfig.setInt("signeditor.mode",
						SIGN_EDITOR_MODE_MINETUNES);
				updateMineTunesElements();
			} else if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
				MinetunesConfig.setInt("signeditor.mode",
						SIGN_EDITOR_MODE_INVISIBLE);
			} else {
				MinetunesConfig.setInt("signeditor.mode",
						SIGN_EDITOR_MODE_NORMAL);
			}
			try {
				// Save mode change
				MinetunesConfig.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			updateMineTunesElements();
		} else if (par1GuiButton.id == 1100) {
			// Show guide
			SignEditorGuideGui guideGui = new SignEditorGuideGui(this);
			mc.displayGuiScreen(guideGui);
		} else if (par1GuiButton.id == 1200) {
			// Play the sign
			LinkedList<Point3D> signsToPlay = new LinkedList<Point3D>();
			signsToPlay.add(new Point3D(entitySign.xCoord, entitySign.yCoord,
					entitySign.zCoord));
			LinkedList<String> testErrors = BlockSignMinetunes
					.playDittyFromSignsQuietly(mc.theWorld, entitySign.xCoord,
							entitySign.yCoord, entitySign.zCoord, true,
							signsToPlay);

			// Display any errors, or success if there aren't any
			// Strip all color codes as well from the error message
			StringBuilder testErrorsFormatted = new StringBuilder();
			testErrorsFormatted.append("Test Result:\n\n");
			if (testErrors != null && testErrors.size() >= 1) {
				testErrorsFormatted.append(testErrors.get(0).replaceAll("§.",
						""));
				helpTextArea.setText(testErrorsFormatted.toString());
			} else if (testErrors == null || testErrors.size() <= 0) {
				testErrorsFormatted.append("§aSuccess!");
				// NOTE: DO NOT DISPLAY THIS STRING: IT IS ANNOYING
				// ONLY DISPLAY FAILURES
			}
		} else if (par1GuiButton.id == 1300) {
			// Help pane button
			// if (helpState == HELP_STATE_DETECT) {
			// helpState = HELP_STATE_KEYWORD;
			// } else if (helpState == HELP_STATE_KEYWORD) {
			// helpState = HELP_STATE_TOKEN;
			// } else if (helpState == HELP_STATE_TOKEN) {
			// helpState = HELP_STATE_HIDDEN;
			// } else {
			// // If helpState == HELP_STATE_HIDDEN or if the value is funky
			// helpState = HELP_STATE_DETECT;
			// }

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
		} else if (par1GuiButton.id == 1400) {
			// Recall sign button
			recallSignHereBefore();
		} else if (par1GuiButton.id == 1500) {
			// Clear sign button
			copyTextToEditor(savedSigns.get(0));
		} else if (par1GuiButton.id == 2000) {
			// Inc color

			// Clear old code
			String currCode = entitySign.signColorCode;
			TileEntitySignMinetunes.removeSignColorCodes(entitySign.signText);

			if (entitySign.signText[2].length() <= 13) {
				String newCode = "f";
				// Decide on new code
				if (currCode == null) {
					newCode = "1";
				} else {
					newCode = TileEntitySignMinetunes.nextCodeInCycle(currCode);
				}

				// add new code
				entitySign.signText[2] += "%" + newCode;
			} else {
				if (entitySign.signText[2].length() == 14) {
					keywordMessage = "Take a letter off of Line 3.";
				} else {
					keywordMessage = "Take 2 letters out of Line 3.";
				}
				keywordMessageColor = 0xffffff;
			}
			entitySign.updateEntity();
		} else if (par1GuiButton.id == 2100) {
			// Dec color

			// Clear old code
			String currCode = entitySign.signColorCode;
			TileEntitySignMinetunes.removeSignColorCodes(entitySign.signText);

			if (entitySign.signText[2].length() <= 13) {
				String newCode = "f";
				// Decide on new code
				if (currCode == null) {
					newCode = "1";
				} else {
					newCode = TileEntitySignMinetunes.prevCodeInCycle(currCode);
				}

				// add new code
				entitySign.signText[2] += "%" + newCode;
			} else {
				if (entitySign.signText[2].length() == 14) {
					keywordMessage = "Take a letter off of Line 3.";
				} else {
					keywordMessage = "Take 2 letters out of Line 3.";
				}
				keywordMessageColor = 0xffffff;
			}
			entitySign.updateEntity();
		} else if (par1GuiButton.id == 2200) {
			// Lock color
			if (lockedCode == null) {
				lockedCode = entitySign.signColorCode;
			} else {
				lockedCode = null;
			}
			updateButtons();
		} else if (par1GuiButton.id == 2300) {
			// Clear color
			TileEntitySignMinetunes.removeSignColorCodes(entitySign.signText);
			entitySign.updateEntity();
		}
	}

	public static void openExportedSignsFolder() {
		// (Code from GuiTexturePacks)
		boolean useSys = false;
		try {
			Class class1 = Class.forName("java.awt.Desktop");
			Object obj = class1.getMethod("getDesktop", new Class[0]).invoke(
					null, new Object[0]);
			class1.getMethod("browse", new Class[] { java.net.URI.class })
					.invoke(obj,
							new Object[] { (new File(Minecraft
									.getMinecraftDir(), "signs")).toURI() });
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			useSys = true;
		}

		if (useSys) {
			System.out.println("Opening via Sys class!");
			Sys.openURL((new StringBuilder()).append("file://")
					.append(signsDirLocation).toString());
		}
	}

	// MineTunes
	private void exitGUIScreen() {
		// Strip the sign of color codes
		for (int i = 0; i < entitySign.signText.length; i++) {
			entitySign.signText[i] = entitySign.signText[i]
					.replaceAll("§.", "");
			entitySign.highlightLine[i] = "";
		}

		Minetunes.onSignLoaded(entitySign);

		// entitySign.setEditable(true);
		entitySign.alwaysRender = false;

		NetClientHandler var1 = this.mc.getSendQueue();

		if (var1 != null) {
			var1.addToSendQueue(new Packet130UpdateSign(this.entitySign.xCoord,
					this.entitySign.yCoord, this.entitySign.zCoord,
					this.entitySign.signText));
		}

		// MineTunes: Save sign's text to the buffer
		addTextToSavedSigns(entitySign.signText);

		entitySign.onInventoryChanged();
		mc.displayGuiScreen(null);

		// MC 1.3.1: Prevent clicking done button from activating sign
		BlockSignMinetunes.clickHeld = true;
		RightClickCheckThread t = new RightClickCheckThread();
		t.start();
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char par1, int par2) {
		// Do a quick range check on the infamous editChar of many exceptions
		// especially handle the situation where a sign color code disappears
		// Update sign to make sure everything that will vanish has vanished
		String[] signText = entitySign.signText;
		String[] filteredSignText = entitySign.getSignTextNoCodes();

		entitySign.updateEntity();
		if (editChar >= filteredSignText[editLine].length()) {
			// Move the edit char back to end of line
			editChar = filteredSignText[editLine].length();
		}

		// TODO: Handle color codes
		if (par2 == Keyboard.KEY_UP) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Swap lines down
				int newEditLine = editLine - 1 & 3;

				// First check that the line being moved will fit on the next
				// line
				// If the next line has a sign color code, the new line might
				// not fit
				if (newEditLine == 2 && entitySign.signColorCode != null
						&& entitySign.signText[editLine].length() > 13) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.wood");
				} else if (editLine == 2 && entitySign.signColorCode != null
						&& entitySign.signText[newEditLine].length() > 13) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.wood");
				} else {
					String code = entitySign.signColorCode;

					String buffer = entitySign.getSignTextNoCodes()[newEditLine];
					entitySign.signText[newEditLine] = entitySign
							.getSignTextNoCodes()[editLine];
					entitySign.signText[editLine] = buffer;

					// Add code back in, if necessary
					if ((newEditLine == 2 || editLine == 2) && code != null) {
						entitySign.signText[2] += "%" + code;
					}

					editLine = newEditLine;
				}

				editLine = newEditLine;
			} else {
				editLine = editLine - 1 & 3;
				editChar = entitySign.getSignTextNoCodes()[editLine].length();
			}
		}

		// TODO: Handle color codes
		if (par2 == Keyboard.KEY_DOWN || par2 == Keyboard.KEY_RETURN) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Swap lines up
				int newEditLine = editLine + 1 & 3;

				// First check that the line being moved will fit on the next
				// line
				// If the next line has a sign color code, the new line might
				// not fit
				if (newEditLine == 2 && entitySign.signColorCode != null
						&& (entitySign.signText[editLine].length() > 13)) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.stone");
				} else if (editLine == 2 && entitySign.signColorCode != null
						&& entitySign.signText[newEditLine].length() > 13) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.wood");
				} else {
					String code = entitySign.signColorCode;

					String buffer = entitySign.getSignTextNoCodes()[newEditLine];
					entitySign.signText[newEditLine] = entitySign
							.getSignTextNoCodes()[editLine];
					entitySign.signText[editLine] = buffer;

					// Add code back in, if necessary
					if ((newEditLine == 2 || editLine == 2) && code != null) {
						entitySign.signText[2] += "%" + code;
					}

					editLine = newEditLine;
				}
			} else {
				editLine = editLine + 1 & 3;
				editChar = entitySign.getSignTextNoCodes()[editLine].length();
			}
		}

		if (par2 == Keyboard.KEY_M) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				actionPerformed(editorModeButton);
			}
		}

		if (par2 == Keyboard.KEY_R) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				recallSignHereBefore();
			}
		}

		if (par2 == Keyboard.KEY_T) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				actionPerformed(testButton);
			}
		}

		if (par2 == Keyboard.KEY_K) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Click select help button
				actionPerformed(selectHelpButton);
			}
		}

		// DONE? (should never matter, since editchar is before code) TODO:
		// Handle color codes
		if (par2 == Keyboard.KEY_BACK
				&& entitySign.signText[editLine].length() > 0 && editChar > 0) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Backspace a whole word
				StringBuilder b = new StringBuilder(
						entitySign.signText[editLine]);
				while (editChar > 0) {
					char currChar = b.charAt(editChar - 1);
					b.replace(editChar - 1, editChar, "");
					editChar--;
					if (currChar == ' ') {
						break;
					}
				}
				entitySign.signText[editLine] = b.toString();
			} else {
				entitySign.signText[editLine] = new StringBuilder(
						entitySign.signText[editLine]).replace(editChar - 1,
						editChar, "").toString();
				editChar--;
			}
		}

		// DONE? // TODO: Handle color codes
		if (par2 == Keyboard.KEY_DELETE
				&& entitySign.getSignTextNoCodes()[editLine].length() > 0
				&& editChar < entitySign.getSignTextNoCodes()[editLine]
						.length()) {
			// if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
			// Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
			// // Delete a whole word
			// StringBuilder b = new StringBuilder
			// (entitySign.signText[editLine]);
			// while (editChar < b.length()) {
			// char currChar = b.charAt(editChar);
			// if (currChar == ' ' && editChar < b.length()) {
			// // Remove space
			// b.replace(editChar, editChar+1, "");
			// break;
			// } else {
			// b.replace(editChar, editChar+1, "");
			// }
			// }
			// entitySign.signText[editLine] = b.toString();
			// } else {
			entitySign.signText[editLine] = new StringBuilder(
					entitySign.signText[editLine]).replace(editChar,
					editChar + 1, "").toString();
			// }
		}

		if (par2 == Keyboard.KEY_HOME) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Move to top of document
				editChar = 0;
				editLine = 0;
			} else {
				editChar = 0;
			}
		}

		if (par2 == Keyboard.KEY_INSERT) {
			overwrite = !overwrite;
		}

		// DONE? //TODO: Handle color codes
		if (par2 == Keyboard.KEY_END) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Move to end of document
				int lineToMoveTo = 3;
				for (lineToMoveTo = 3; lineToMoveTo >= 0; lineToMoveTo--) {
					if (entitySign.getSignTextNoCodes()[lineToMoveTo].length() > 0) {
						break;
					}
				}
				editLine = lineToMoveTo;
				editChar = entitySign.getSignTextNoCodes()[editLine].length();
			} else {
				editChar = entitySign.getSignTextNoCodes()[editLine].length();
			}
		}

		// Done? Going back isn't the problem... TODO: Handle color codes
		if (par2 == Keyboard.KEY_LEFT && editChar > 0) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Move a whole word back
				int newPos;
				for (newPos = editChar - 1; newPos >= 0; newPos--) {
					if (entitySign.signText[editLine].charAt(newPos) == ' '
							|| entitySign.signText[editLine].charAt(newPos) == '+') {
						break;
					}
				}
				editChar = newPos;

				// range check
				if (editChar < 0) {
					editChar = 0;
				}
			} else {
				editChar--;
			}
		}

		// Done? TODO: Handle color codes
		if (par2 == Keyboard.KEY_RIGHT
				&& editChar < entitySign.getSignTextNoCodes()[editLine]
						.length()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
					|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
				// Move a whole word forward
				int newPos;
				for (newPos = editChar; newPos < entitySign
						.getSignTextNoCodes()[editLine].length(); newPos++) {
					if (entitySign.signText[editLine].charAt(newPos) == ' '
							|| entitySign.signText[editLine].charAt(newPos) == '+'
							|| entitySign.signText[editLine].charAt(newPos) == '_') {
						if (newPos < entitySign.getSignTextNoCodes()[editLine]
								.length()) {
							// Try to advance to beginning of next word, if it
							// exists
							newPos++;
						}
						break;
					}
				}
				editChar = newPos;

				// range check
				if (editChar > entitySign.getSignTextNoCodes()[editLine]
						.length()) {
					editChar = entitySign.getSignTextNoCodes()[editLine]
							.length();
				}
			} else {
				editChar++;
			}
		}

		// Done? // TODO: Handle color codes
		if (!(allowedCharacters.indexOf(par1) < 0)) {
			int maxLineLength = 15;

			if ((!overwrite
					|| (overwrite && editChar >= entitySign.signText[editLine]
							.length()) || (overwrite && editLine == 2
					&& entitySign.signColorCode != null && editChar < maxLineLength - 2))
					&& (!(entitySign.signText[editLine].length() >= maxLineLength))) {
				// insert / add character
				entitySign.signText[editLine] = new StringBuffer(
						entitySign.signText[editLine]).insert(editChar, par1)
						.toString();
				editChar++;
			} else if (overwrite && editChar < maxLineLength) {
				// Overwrite character, but not a color code
				if (!(editLine == 2 && entitySign.signColorCode != null && editChar >= maxLineLength - 2)) {
					entitySign.signText[editLine] = new StringBuffer(
							entitySign.signText[editLine])
							.replace(editChar, editChar + 1, "")
							.insert(editChar, par1).toString();
					editChar++;
				}
			}
		}

		// Handle autocorrect for SFXInst
		if (par2 == Keyboard.KEY_T
				&& entitySign.signText[editLine].equalsIgnoreCase("sfxinst")) {
			entitySign.signText[editLine] = "SFXInst2";
			editChar = entitySign.signText[editLine].length();
		}

		// MineTunes: Alert someone if they type too far on the current line
		// TODO: Handle color codes
		if (allowedCharacters.indexOf(par1) >= 0
				&& entitySign.signText[editLine].length() >= 15) {
			// playTunesThread.play(BlockSignMinetunes.getEndOfLineTune());
			SFXManager.playEffect("step.wood");
		}

		// MineTunes: Check for page up and page down keys to scroll saved signs
		// into the editor
		// System.out.println ("KeyTyped: Par2="+par2);
		if (par2 == 201) {
			scrollSavedBufferDown();
		}

		if (par2 == 209) {
			scrollSavedBufferUp();
		}

		// MineTunes
		if (par2 == Keyboard.KEY_ESCAPE) {
			exitGUIScreen();
		}

		// Before doing anything with sign's text, update color code state
		entitySign.updateEntity();

		// Then update editChar for range
		if (editChar > entitySign.getSignTextNoCodes()[editLine].length()) {
			editChar = entitySign.getSignTextNoCodes()[editLine].length();
		}

		if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
			updateMineTunesElements();
		}
	}

	public void scrollSavedBufferDown() {
		bufferPosition--;
		if (bufferPosition < 0) {
			bufferPosition = 0;
		}
		copyTextToEditor(savedSigns.get(bufferPosition));
	}

	public void scrollSavedBufferUp() {
		bufferPosition++;
		if (bufferPosition >= savedSigns.size()) {
			bufferPosition = savedSigns.size() - 1;
		}

		// Set sign text
		copyTextToEditor(savedSigns.get(bufferPosition));
	}

	private void copyTextToEditor(String[] strings) {
		entitySign.signText = TileEntitySignMinetunes.copyOfSignText(strings);
		editChar = entitySign.getSignTextNoCodes()[editLine].length();

		// Add any locked color code
		if (lockedCode != null) {
			TileEntitySignMinetunes.removeSignColorCodes(entitySign.signText);
			if (entitySign.signText[2].length() <= 13) {
				entitySign.signText[2] += "%" + lockedCode;
			}
		}

		updateMineTunesElements();
	}

	private TileEntitySign[] signsHereBefore = null;
	private int backCount;

	private void recallSignHereBefore() {
		// Load signs here before, if not already done
		if (signsHereBefore == null) {
			signsHereBefore = Minetunes.getUniqueSignsForPos(entitySign.xCoord,
					entitySign.yCoord, entitySign.zCoord, true);
			backCount = signsHereBefore.length - 1; // Assumes this will be
													// decremented below
													// immediately
			// -1 acconts for the fact the latest sign is being edited this very
			// moment; recall would be redundant.
		}

		// Go back a sign
		backCount--;
		if (backCount < 0) {
			backCount = signsHereBefore.length - 1;
			// If doing this, backcount is still less than zero, make it zero
			if (backCount < 0) {
				backCount = 0;
			}
		}

		// Recall sign's text at backcount, if possible
		if (signsHereBefore.length > backCount) {
			copyTextToEditor(signsHereBefore[backCount].signText);
		}
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

		// Update only if in MineTunes mode
		if (MinetunesConfig.getInt("signeditor.mode") == SIGN_EDITOR_MODE_MINETUNES) {
			// Parse the current sign.
			ParsedSign parsedSign = SignParser.parseSign(entitySign
					.getSignTextNoCodes());

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
				} else if (parsedSign.getLine(editLine) instanceof ParsedKeyword) {
					showKeywordHelp(editLine, parsedSign);
				} else if (parsedSign.getLine(editLine) instanceof String) {
					// Random text or tokens. I hope it's tokens.
					showTokenHelp((String) parsedSign.getLine(editLine));
				}
			} else if (helpState == HELP_STATE_KEYWORD) {
				// Show keyword help
				if (parsedSign.getLine(editLine) instanceof ParsedKeyword) {
					showKeywordHelp(editLine, parsedSign);
				} else {
					showGenericKeywordHelp();
				}
			} else if (helpState == HELP_STATE_TOKEN) {
				// Show token help
				if (parsedSign.getLine(editLine) instanceof ParsedKeyword) {
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
		} else {
			// If MineTunes mode is off, clear color coding and keyword message
			keywordMessage = null;
			entitySign.clearHighlightedLines();
		}
	}

	/**
	 * Shows a short help guide on comments.
	 * 
	 * @param comment
	 */
	private void showCommentHelp(Comment comment) {
		// Show generic comment help
		helpTextArea.setText(BlockSignMinetunes.COMMENT_HIGHLIGHT_CODE
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

			if (signLineContents instanceof ParsedKeyword) {
				ParsedKeyword keyword = (ParsedKeyword) signLineContents;
				switch (keyword.getErrorMessageType()) {
				case ParsedKeyword.ERROR:
					keywordMessageColor = 0xff0000;
					entitySign.highlightLine[i] = "§4";
					break;
				case ParsedKeyword.WARNING:
					keywordMessageColor = 0xffff00;
					entitySign.highlightLine[i] = "§e";
					break;
				case ParsedKeyword.INFO:
					keywordMessageColor = 0x0000ff;
					entitySign.highlightLine[i] = "§1";
					break;
				default:
					keywordMessageColor = 0xffffff;
					entitySign.highlightLine[i] = BlockSignMinetunes.KEYWORD_HIGHLIGHT_CODE;
					break;
				}

				if (editLine == i) {
					keywordMessage = keyword.getErrorMessage();
				}
			} else if (signLineContents instanceof Comment) {
				// Line is a comment
				entitySign.highlightLine[i] = "§b";

				if (editLine == i) {
					keywordMessage = null;
				}
			} else {
				// Is not a keyword. By default, make line normal-colored:
				if (editLine == i) {
					keywordMessage = null;
				}

				entitySign.highlightLine[i] = "";

				// But it might be an errored musicstring also
				// Check tokens for errors
				for (String token : entitySign.getSignTextNoCodes()[i]
						.split(" ")) {
					if (token.trim().length() > 0) {
						try {
							musicStringParser.parseTokenStrict(token.trim());
						} catch (JFugueException e) {
							// Token is not a valid token
							entitySign.highlightLine[i] = "§4";
						} catch (Exception e) {
							// Token is a really screwed up token!
							entitySign.highlightLine[i] = "§4";
						}
					}
				}
			}
		}
	}

	private void showKeywordHelp(int line, ParsedSign parsedSign) {

		ParsedKeyword keyword = null;
		Object keywordObject = parsedSign.getLine(line);

		// Handle null keywords
		if (keywordObject == null) {
			showGenericKeywordHelp();
			return;
		} else if (keywordObject instanceof ParsedKeyword) {
			keyword = (ParsedKeyword) keywordObject;
		}

		// Handle different types of keywords
		if (keyword instanceof ProxPadKeyword) {
			// XXX: Note: there is a hacky solution here to showing
			// proximity help
			if (entitySign.signText[line].trim().toLowerCase()
					.startsWith("proximity")) {
				showDefaultKeywordHelp(new ParsedKeyword("proximity"));
			} else {
				// If the keyword is not the special case of a proximity
				// keyword
				showDefaultKeywordHelp(keyword);
			}
		} else if (keyword instanceof ExplicitGotoKeyword) {
			// Show what gotos are pointing at right now

			// "Run" all gotos on sign to find the block they point at
			Point3D startBlock = new Point3D(entitySign.xCoord,
					entitySign.yCoord, entitySign.zCoord);
			Point3D pointedAtBlock = startBlock.clone();
			int currSignFacing = BlockSignMinetunes.getSignFacing(
					entitySign.getBlockMetadata(), entitySign.getBlockType());
			for (int i = 0; i < entitySign.signText.length; i++) {
				// On each line of the sign
				ParsedKeyword gotoCandidate = SignParser
						.parseKeyword(entitySign.getSignTextNoCodes()[i]);
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
						pointedAtBlock = BlockSignMinetunes
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
						pointedAtBlock = BlockSignMinetunes
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
						pointedAtBlock = BlockSignMinetunes
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
				} else if (BlockSignMinetunes.getSignBlockType(pointedAtBlock,
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
						new Point3D(entitySign.xCoord, entitySign.yCoord,
								entitySign.zCoord), mc.theWorld, gotoKeyword
								.getComment());
				LinkedList<Comment> matchingComments = gotoKeyword
						.matchingCommentsNearby(new Point3D(entitySign.xCoord,
								entitySign.yCoord, entitySign.zCoord),
								mc.theWorld, gotoKeyword.getComment());

				// Compile matching comments into a string
				StringBuilder b = new StringBuilder();
				if (bestMatch != null) {
					b.append("§aTargeted Comment: §b");
					b.append(bestMatch.getCommentText());
					b.append(" §r\n(");
					b.append(Math.round(bestMatch.getLocation().distanceTo(
							new Point3D(entitySign.xCoord, entitySign.yCoord,
									entitySign.zCoord))));
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
								new Point3D(entitySign.xCoord,
										entitySign.yCoord, entitySign.zCoord))));
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
				File[] midiFileList = new File(MinetunesConfig
						.getMinetunesDir().getPath(), "midi")
						.listFiles(new MidiFileFilter());

				LinkedList<File> matchingMidis = new LinkedList<File>();
				File exactMatch = null;

				String filenameFromKeyword = ((PlayMidiKeyword) parsedSign
						.getLine(line)).getMidiFilename();
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
	// private StringBuilder lastHelp = new StringBuilder();

	private float signTranslateY;

	private void showDefaultKeywordHelp(ParsedKeyword keyword) {
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

	static {
		allowedCharacters = ChatAllowedCharacters.allowedCharacters;
	}

	/**
	 * MineTunes: Add a sign's text to the buffer of saved texts, if it is not
	 * already there. If it is, move that text to the end of the buffer and
	 * remove the duplicate from earlier.
	 */
	public static void addTextToSavedSigns(String[] newText) {
		// System.out.println("Adding text to saved signs buffer... bufferlengthbefore="
		// + savedSigns.size());

		// Initialize savedSigns buffer, if it is not already (this method may
		// be called before the first SignEditGui is shown)
		if (bufferInitalized == false) {
			String[] firstEntry = new String[4];
			for (int i = 0; i < 4; i++) {
				firstEntry[i] = "";
			}
			savedSigns.add(firstEntry);
			bufferInitalized = true;
		}

		// Empty texts are special: there is one at the head of the buffer, and
		// it shall not be removed.
		// Ignore empty sign texts here.
		int emptyLines = 0;
		for (int i = 0; i < newText.length; i++) {
			if (newText[i] != null) {
				if (newText[i].trim().equals("")) {
					emptyLines++;
				}
			} else {
				// Null line; treat as empty
				emptyLines++;
			}
		}
		if (emptyLines == 4) {
			// System.out.println("New text is empty -- not adding");
			return;
		}

		boolean textAlreadyInBuffer = false;
		int duplicatePosition = 0;

		for (int i = 0; i < savedSigns.size(); i++) {
			if (compareSignTexts(newText, savedSigns.get(i))) {
				textAlreadyInBuffer = true;
				duplicatePosition = i;
				break;
			}
		}

		if (textAlreadyInBuffer) {
			savedSigns.remove(duplicatePosition);
			savedSigns.add(newText.clone());
		} else {
			savedSigns.add(newText.clone());
		}
	}

	/**
	 * MineTunes: Compares two sign texts for equality.
	 * 
	 * @param text1
	 * @param text2
	 * @return true if equal
	 */
	private static boolean compareSignTexts(String[] text1, String[] text2) {
		boolean areSame = true;

		if (text1.length != 4 || text2.length != 4) {
			// Not sign texts; catch now to prevent exceptions later.
			return false;
		}

		for (int i = 0; i < text1.length; i++) {
			if (!text1[i].equals(text2[i])) {
				areSame = false;
				break;
			}
		}

		return areSame;
	}

	@Override
	public void setSavedFile(String filename) {
		fileSaverFilename = filename;

		if (filename != null) {
			// Export signs to filename
			try {
				BlockSignMinetunes.exportSignsToFile(savedSigns, new File(
						filename));
			} catch (IOException e) {
				e.printStackTrace();
				// TODO: Tell user something!
			}
		}
	}

	@Override
	public void fileSelected(File f) {
		if (f != null) {
			try {
				ArrayList<String[]> signsImported = BlockSignMinetunes
						.importSignsFromFile(f);
				for (String[] sign : signsImported) {
					addTextToSavedSigns(sign);
				}
			} catch (IOException e) {
				// TODO tell user something!
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int)
	 */
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		helpTextArea.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		helpTextArea.mouseMovedOrUp(par1, par2, par3);
		super.mouseMovedOrUp(par1, par2, par3);
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

	/**
	 * The proper jfugue map (Instrument.DICT_MAP) features the names of
	 * precussion instruments -- inappropriate for the I token's help.
	 */
	public static final Map<String, String> INSTRUMENT_NAMES_MAP;
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

}
