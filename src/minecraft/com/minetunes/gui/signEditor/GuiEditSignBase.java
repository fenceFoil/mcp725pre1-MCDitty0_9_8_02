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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JComboBox;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.ChatAllowedCharacters;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiEditSign;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.Packet130UpdateSign;
import net.minecraft.src.TileEntityRenderer;
import net.minecraft.src.TileEntitySign;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.minetunes.Minetunes;
import com.minetunes.RightClickCheckThread;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.GuiButtonL;
import com.minetunes.gui.MinetunesGui;
import com.minetunes.gui.MinetunesVersionGuiElement;
import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.TileEntitySignMinetunes;

public class GuiEditSignBase extends GuiEditSign {
	/**
	 * This String is just a local copy of the characters allowed in text
	 * rendering of minecraft.
	 */
	private static final String allowedCharacters = ChatAllowedCharacters.allowedCharacters;

	public static final int MODE_SIGNTUNES = 24601;
	public static final int MODE_RETRO = 0;
	public static final int MODE_DISCREET = 1;

	/** Reference to the sign object. */
	protected TileEntitySignMinetunes sign;

	/** Counts the number of screen updates. */
	protected int updateCounter;

	/** The number of the line that is being edited. */
	protected int editLine;

	/**
	 * Current index in the list of saved signs
	 */
	protected int bufferPosition = savedSigns.size();

	private boolean placingBlankSign = false;

	protected int editChar;

	private boolean overwrite = false;

	protected GuiButtonL editorModeButton;

	/**
	 * MineTunes's buffer of saved signs.
	 */
	protected static LinkedList<String[]> savedSigns = new LinkedList<String[]>();

	private static boolean bufferInitalized = false;

	protected TileEntitySign[] signsHereBefore = null;

	protected int recalledSignCount;

	private float signTranslateY;

	private String bottomMessage = null;

	private int bottomMessageColor = 0xffffff;

	private GuiScreen queuedGui = null;

	protected GuiButtonL doneButton;

	public GuiEditSignBase(TileEntitySign par1TileEntitySign) {
		super(par1TileEntitySign);

		// Change screentitle
		editLine = 0;
		editChar = 0;
		if (!(par1TileEntitySign instanceof TileEntitySignMinetunes)) {
			par1TileEntitySign = new TileEntitySignMinetunes(par1TileEntitySign);
		}
		sign = (TileEntitySignMinetunes) par1TileEntitySign;
		// sign.setEditable(true);
		sign.alwaysRender = true;

		// Add first entry to savedSigns buffer
		if (bufferInitalized == false) {
			String[] firstEntry = new String[4];
			for (int i = 0; i < 4; i++) {
				firstEntry[i] = "";
			}
			savedSigns.add(firstEntry);
			bufferInitalized = true;
		}

		// Update number of signs available to recall
		if (signsHereBefore == null) {
			signsHereBefore = Minetunes.getUniqueSignsForPos(sign.xCoord,
					sign.yCoord, sign.zCoord, true);
		}
	}

	/**
	 * @param entitySign2
	 * @param recalledSignCount2
	 * @param editLine2
	 * @param editChar2
	 */
	public GuiEditSignBase(TileEntitySignMinetunes entitySign,
			int recalledSignCount, int editLine, int editChar) {
		this(entitySign);
		this.recalledSignCount = recalledSignCount;
		this.editLine = editLine;
		this.editChar = editChar;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		// Avoid flicker
		if (placingBlankSign) {
			return;
		}

		if (MinetunesConfig.getInt("signeditor.mode") != MODE_DISCREET) {
			drawDefaultBackground();

			// drawCenteredString(fontRenderer, "Edit Sign Text", width / 2, 10,
			// 0xffffff);
			// drawCenteredString(fontRenderer,
			// "(PgUp and PgDown Scroll Saved Signs)", width / 2, 30, 0xcccccc);

			// Draw chars left display
			int charsLeftColor = 0x00cc00;
			if (editLine >= 0 && editLine < 4) {
				int charsLeft = (15 - sign.signText[editLine].length());

				boolean signColorCodePresent = false;
				if (editLine == 2 && sign.signColorCode != null) {
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
					drawString += " + %" + sign.signColorCode;
				}

				drawCenteredString(fontRenderer, drawString, width / 2, 25,
						charsLeftColor);
			}

			// Draw message
			if (bottomMessage != null) {
				drawCenteredString(fontRenderer, bottomMessage, width / 2,
						height - 10, bottomMessageColor);
			}

			// Draw colorcode info
			drawCenteredString(fontRenderer,
					TileEntitySignMinetunes
							.getNameForSignColorCode(sign.signColorCode),
					width - 45, height - 44, 0xffffff);

			// Draw insert/overwrite display
			// drawRect (20, 20, 20+fontRenderer.getStringWidth("Overwrite"),
			// 20+10,
			// 0x44888888);
			if (overwrite) {
				drawString(fontRenderer, "Overwrite", 20, 12, 0xff8800);
			} else {
				// drawString(fontRenderer, "Insert", 20, 20, 0xffff00);
			}

			// if (bufferPosition > 0 && bufferPosition <= savedSigns.size() -
			// 1) {
			// drawString(fontRenderer, "Loaded Sign #" + bufferPosition, 20,
			// 22, 0x88ff00);
			// } else {
			// //drawString(fontRenderer, "New Sign", 20, 40, 0x0000ff);
			// }
			// Need to reset color after this?
			GL11.glColor4f(0Xff, 0Xff, 0Xff, 0xff);

			// Decide the size to draw the sign at
			float signScaling = 93.75F; // The default minecraft value
			signTranslateY = 0f; // Default minecraft value
			float signTranslateX = width / 2;

			GL11.glPushMatrix();
			GL11.glTranslatef(signTranslateX, signTranslateY, 50f);
			GL11.glScalef(-signScaling, -signScaling, -signScaling);
			GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
			Block block = sign.getBlockType();

			if (block == Block.signPost) {
				float f1 = (float) (sign.getBlockMetadata() * 360) / 16F;
				GL11.glRotatef(f1, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, -1.0625F, 0.0F);
			} else {
				int i = sign.getBlockMetadata();
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

			sign.charBeingEdited = editChar;

			if ((updateCounter / 6) % 2 == 0) {
				sign.lineBeingEdited = editLine;
			}

			TileEntityRenderer.instance.renderTileEntityAt(sign, -0.5D, -0.75D,
					-0.5D, 0f);
			sign.lineBeingEdited = -1;
			GL11.glPopMatrix();
		}
		updateButtons();

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
		Keyboard.enableRepeatEvents(true);

		// Check for ctrl key pressed
		if (isCtrlKeyDown()) {
			// Place blank sign
			placingBlankSign = true;
			sendSignAndCloseSignGui();
		}

		controlList.clear();

		int iconTexture = mc.renderEngine
				.getTexture("/com/minetunes/resources/textures/signEditor1.png");
		// editorModeButton = new GuiButton(1000, width - 100, 10, 80, 20, "");
		editorModeButton = new GuiButtonL("editorMode", width - 30, 10, 20, 20,
				iconTexture, 13);
		editorModeButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Switch gui mode
				if (MinetunesConfig.getInt("signeditor.mode") == MODE_RETRO) {
					MinetunesConfig.setInt("signeditor.mode", MODE_SIGNTUNES);
					setQueuedGUI(new GuiEditSignTune(sign, recalledSignCount,
							editLine, editChar));
				} else if (MinetunesConfig.getInt("signeditor.mode") == MODE_SIGNTUNES) {
					MinetunesConfig.setInt("signeditor.mode", MODE_DISCREET);
					setQueuedGUI(queuedGui = new GuiEditSignBase(sign,
							recalledSignCount, editLine, editChar));
				} else {
					MinetunesConfig.setInt("signeditor.mode", MODE_RETRO);
					setQueuedGUI(queuedGui = new GuiEditSignBase(sign,
							recalledSignCount, editLine, editChar));
				}
				try {
					// Save mode change
					MinetunesConfig.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		controlList.add(editorModeButton);

		doneButton = new GuiButtonL("done", width / 2 - 100, height - 50, 200,
				20, "Done");
		controlList.add(doneButton);
		doneButton.addListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendSignAndCloseSignGui();
			}
		});

	}

	protected void updateButtons() {
		// Switch modes button
		// if (MinetunesConfig.getInt("signeditor.mode") == MODE_SIGNTUNES) {
		// editorModeButton.displayString = "SignTune";
		// } else if (MinetunesConfig.getInt("signeditor.mode") == MODE_RETRO) {
		// editorModeButton.displayString = "";
		// } else {
		// editorModeButton.displayString = "Invisible";
		// }
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat
	 * events
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);

		// Moved the stuff that sends a MP packet to a method where it
		// is not unintentionally fired when viewing a SignEditor "sub-gui" such
		// as the guide

		// Update face state
		sign.isFace(true);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		if (placingBlankSign) {
			return;
		}

		// Check for queued up gui to display
		if (queuedGui != null) {
			GuiScreen t = queuedGui;
			queuedGui = null;
			mc.displayGuiScreen(t);
		}

		// Update the current tick
		updateCounter++;

		// Check whether the SignTunes gui should be opened instead
		if (MinetunesConfig.getInt("signeditor.mode") == MODE_SIGNTUNES
				&& !(mc.currentScreen instanceof GuiEditSignTune)) {
			mc.displayGuiScreen(new GuiEditSignTune(sign, recalledSignCount,
					editLine, editChar));
		}

		// // Give things some time to work in the background?
		// try {
		// Thread.sleep(1);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == -100) {
			setQueuedGUI(new MinetunesGui(this));
		} else if (par1GuiButton.id == 0) {
			sendSignAndCloseSignGui();
		}
	}

	/**
	 * Call when closing the sign editor for the final time. Do not call when
	 * switching between different sign editors: the sign finished packet can
	 * only be sent once.
	 */
	protected void sendSignAndCloseSignGui() {
		// Strip the sign of color codes
		for (int i = 0; i < sign.signText.length; i++) {
			sign.signText[i] = sign.signText[i].replaceAll("§.", "");
			sign.highlightLine[i] = "";
		}

		// Alert MineTunes to presence of new sign
		Minetunes.onSignLoaded(sign);

		// entitySign.setEditable(true);

		// Disable the always-render flag, turned on to ensure the sign is
		// always visible in the editor
		sign.alwaysRender = false;

		// Send the packet!
		sendSignPacket(sign);

		// Save sign's text to the buffer
		addTextToSavedSigns(sign.signText);

		sign.onInventoryChanged();
		mc.displayGuiScreen(null);

		// Prevent clicking done button from activating sign
		SignTuneParser.clickHeld = true;
		RightClickCheckThread t = new RightClickCheckThread();
		t.start();
	}

	/**
	 * Sends a Packet130UpdateSign with the text on the given sign over the
	 * network
	 * 
	 * @param entitySign
	 */
	public static void sendSignPacket(TileEntitySign entitySign) {
		NetClientHandler sender = Minecraft.getMinecraft().getSendQueue();
		if (sender != null) {
			sender.addToSendQueue(new Packet130UpdateSign(entitySign.xCoord,
					entitySign.yCoord, entitySign.zCoord, entitySign.signText));
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char par1, int keyCode) {
		// Do a quick range check on the infamous editChar-of-many-exceptions
		// especially handle the situation where a sign color code disappears
		// Update sign to make sure everything that will vanish has vanished
		String[] signText = sign.signText;
		String[] filteredSignText = sign.getSignTextNoCodes();

		// Allow color codes to be processed
		sign.updateEntity();

		// Check for out-of-bounds editChar
		if (editChar >= filteredSignText[editLine].length()) {
			// Move the edit char back to end of line
			editChar = filteredSignText[editLine].length();
		}

		// Up arrow key
		if (keyCode == Keyboard.KEY_UP) {
			if (isCtrlKeyDown()) {
				// Swap lines down
				int newEditLine = editLine - 1 & 3;

				// First check that the line being moved will fit on the next
				// line
				// If the next line has a sign color code, the new line might
				// not fit
				if (newEditLine == 2 && sign.signColorCode != null
						&& sign.signText[editLine].length() > 13) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.wood");
				} else if (editLine == 2 && sign.signColorCode != null
						&& sign.signText[newEditLine].length() > 13) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.wood");
				} else {
					String code = sign.signColorCode;

					String buffer = sign.getSignTextNoCodes()[newEditLine];
					sign.signText[newEditLine] = sign.getSignTextNoCodes()[editLine];
					sign.signText[editLine] = buffer;

					// Add code back in, if necessary
					if ((newEditLine == 2 || editLine == 2) && code != null) {
						sign.signText[2] += "%" + code;
					}

					editLine = newEditLine;
				}

				editLine = newEditLine;
			} else {
				editLine = editLine - 1 & 3;
				editChar = sign.getSignTextNoCodes()[editLine].length();
			}
		}

		// Down arrow key (or enter key)
		if (keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_RETURN) {
			if (isCtrlKeyDown()) {
				// Swap lines up
				int newEditLine = editLine + 1 & 3;

				// First check that the line being moved will fit on the next
				// line
				// If the next line has a sign color code, the new line might
				// not fit
				if (newEditLine == 2 && sign.signColorCode != null
						&& (sign.signText[editLine].length() > 13)) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.stone");
				} else if (editLine == 2 && sign.signColorCode != null
						&& sign.signText[newEditLine].length() > 13) {
					// Does not fit
					// Tell user
					SFXManager.playEffect("step.wood");
				} else {
					String code = sign.signColorCode;

					String buffer = sign.getSignTextNoCodes()[newEditLine];
					sign.signText[newEditLine] = sign.getSignTextNoCodes()[editLine];
					sign.signText[editLine] = buffer;

					// Add code back in, if necessary
					if ((newEditLine == 2 || editLine == 2) && code != null) {
						sign.signText[2] += "%" + code;
					}

					editLine = newEditLine;
				}
			} else {
				editLine = editLine + 1 & 3;
				editChar = sign.getSignTextNoCodes()[editLine].length();
			}
		}

		if (keyCode == Keyboard.KEY_M) {
			if (isCtrlKeyDown()) {
				// XXX: Broken
				actionPerformed(editorModeButton);
			}
		}

		if (keyCode == Keyboard.KEY_R) {
			if (isCtrlKeyDown()) {
				recallSignHereBefore();
			}
		}

		// DONE? (should never matter, since editchar is before code) TODO:
		// Handle color codes
		if (keyCode == Keyboard.KEY_BACK
				&& sign.signText[editLine].length() > 0 && editChar > 0) {
			if (isCtrlKeyDown()) {
				// Backspace a whole word
				StringBuilder b = new StringBuilder(sign.signText[editLine]);
				while (editChar > 0) {
					char currChar = b.charAt(editChar - 1);
					b.replace(editChar - 1, editChar, "");
					editChar--;
					if (currChar == ' ') {
						break;
					}
				}
				sign.signText[editLine] = b.toString();
			} else {
				sign.signText[editLine] = new StringBuilder(
						sign.signText[editLine]).replace(editChar - 1,
						editChar, "").toString();
				editChar--;
			}
		}

		// Delete key
		if (keyCode == Keyboard.KEY_DELETE
				&& sign.getSignTextNoCodes()[editLine].length() > 0
				&& editChar < sign.getSignTextNoCodes()[editLine].length()) {
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
			sign.signText[editLine] = new StringBuilder(sign.signText[editLine])
					.replace(editChar, editChar + 1, "").toString();
			// }
		}

		// Home key
		if (keyCode == Keyboard.KEY_HOME) {
			if (isCtrlKeyDown()) {
				// Move to top of document
				editChar = 0;
				editLine = 0;
			} else {
				editChar = 0;
			}
		}

		// Insert key
		if (keyCode == Keyboard.KEY_INSERT) {
			overwrite = !overwrite;
		}

		// End key
		if (keyCode == Keyboard.KEY_END) {
			if (isCtrlKeyDown()) {
				// Move to end of document
				int lineToMoveTo = 3;
				for (lineToMoveTo = 3; lineToMoveTo >= 0; lineToMoveTo--) {
					if (sign.getSignTextNoCodes()[lineToMoveTo].length() > 0) {
						break;
					}
				}
				editLine = lineToMoveTo;
			}
			editChar = sign.getSignTextNoCodes()[editLine].length();
		}

		// Left key
		if (keyCode == Keyboard.KEY_LEFT && editChar > 0) {
			if (isCtrlKeyDown()) {
				// Move a whole word back
				int newPos;
				for (newPos = editChar - 1; newPos >= 0; newPos--) {
					if (sign.signText[editLine].charAt(newPos) == ' '
							|| sign.signText[editLine].charAt(newPos) == '+') {
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

		// Right key
		if (keyCode == Keyboard.KEY_RIGHT
				&& editChar < sign.getSignTextNoCodes()[editLine].length()) {
			if (isCtrlKeyDown()) {
				// Move a whole word forward
				int newPos;
				for (newPos = editChar; newPos < sign.getSignTextNoCodes()[editLine]
						.length(); newPos++) {
					if (sign.signText[editLine].charAt(newPos) == ' '
							|| sign.signText[editLine].charAt(newPos) == '+'
							|| sign.signText[editLine].charAt(newPos) == '_') {
						if (newPos < sign.getSignTextNoCodes()[editLine]
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
				if (editChar > sign.getSignTextNoCodes()[editLine].length()) {
					editChar = sign.getSignTextNoCodes()[editLine].length();
				}
			} else {
				editChar++;
			}
		}

		// Handle typing onto sign with letter, numeral, and symbol keys
		if (allowedCharacters.indexOf(par1) >= 0) {
			int maxLineLength = 15;

			// This morass of code deals with checking for the end of a line,
			// accounting for sign color codes
			if ((!overwrite
					|| (overwrite && editChar >= sign.signText[editLine]
							.length()) || (overwrite && editLine == 2
					&& sign.signColorCode != null && editChar < maxLineLength - 2))
					&& (!(sign.signText[editLine].length() >= maxLineLength))) {
				// insert / add character
				sign.signText[editLine] = new StringBuffer(
						sign.signText[editLine]).insert(editChar, par1)
						.toString();
				editChar++;

				// Play end of line sound; at 15 chars for most lines and 13 for
				// second line
				// TODO: Handle no color code on line 2
				if ((editLine != 2 && sign.signText[editLine].length() >= 15)
						|| (editLine == 2 && sign.signText[editLine].length() >= 13)) {
					SFXManager.playEffect("step.wood");
				}
			} else if (overwrite && editChar < maxLineLength) {
				// Overwrite character, but not a color code
				if (!(editLine == 2 && sign.signColorCode != null && editChar >= maxLineLength - 2)) {
					sign.signText[editLine] = new StringBuffer(
							sign.signText[editLine])
							.replace(editChar, editChar + 1, "")
							.insert(editChar, par1).toString();
					editChar++;
				}
			}
		}

		// Check for page up and page down keys to scroll saved signs
		// into the editor
		if (keyCode == 201) {
			scrollSavedBufferDown();
		}

		if (keyCode == 209) {
			scrollSavedBufferUp();
		}

		// Exit gui
		if (keyCode == Keyboard.KEY_ESCAPE) {
			sendSignAndCloseSignGui();
		}

		// Before doing anything with sign's text, update color code state
		sign.updateEntity();
		// Then update editChar for range
		if (editChar > sign.getSignTextNoCodes()[editLine].length()) {
			editChar = sign.getSignTextNoCodes()[editLine].length();
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

	protected void copyTextToEditor(String[] strings) {
		sign.signText = TileEntitySignMinetunes.copyOfSignText(strings);
		editChar = sign.getSignTextNoCodes()[editLine].length();
	}

	public void recallSignHereBefore() {
		// Load signs here before, if not already done
		if (signsHereBefore == null) {
			signsHereBefore = Minetunes.getUniqueSignsForPos(sign.xCoord,
					sign.yCoord, sign.zCoord, true);
			recalledSignCount = signsHereBefore.length - 1; // Assumes this will
															// be
			// decremented below
			// immediately
			// -1 acconts for the fact the latest sign is being edited this very
			// moment; recall would be redundant.
		}

		// Go back a sign
		recalledSignCount--;
		if (recalledSignCount < 0) {
			recalledSignCount = signsHereBefore.length - 1;
			// If doing this, backcount is still less than zero, make it zero
			if (recalledSignCount < 0) {
				recalledSignCount = 0;
			}
		}

		// Recall sign's text at backcount, if possible
		if (signsHereBefore.length > recalledSignCount) {
			copyTextToEditor(signsHereBefore[recalledSignCount].signText);
		}
	}

	/**
	 * Add a sign's text to the buffer of saved texts, if it is not already
	 * there. If it is, move that text to the end of the buffer and remove the
	 * duplicate from earlier.
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
	 * Compares two sign texts for equality.
	 * 
	 * @param text1
	 * @param text2
	 * @return true if equal
	 */
	public static boolean compareSignTexts(String[] text1, String[] text2) {
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
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		super.mouseMovedOrUp(par1, par2, par3);
	}

	/**
	 * Shows a message on the bottom of the screen in the given color
	 * 
	 * @param message
	 * @param color
	 */
	public void setBottomMessage(String message, int color) {
		bottomMessage = message;
		bottomMessageColor = color;
	}

	/**
	 * Clears the message on the bottom of the screen, if any is currently being
	 * shown
	 */
	public void clearBottomMessage() {
		bottomMessage = null;
	}

	/**
	 * queues up a new GuiScreen to display on the next call to updateScreen().
	 * 
	 * @param gui
	 */
	public void setQueuedGUI(GuiScreen gui) {
		queuedGui = gui;
	}

}
