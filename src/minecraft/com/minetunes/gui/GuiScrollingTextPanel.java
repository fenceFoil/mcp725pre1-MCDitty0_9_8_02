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
package com.minetunes.gui;

import java.util.Arrays;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Gui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.minetunes.Finder;

/**
 * A widget that renders a scrollable text pane (uneditable) in a Minecraft gui.
 * 
 */
public class GuiScrollingTextPanel extends Gui {
	private static final int MARGIN = 5;
	private static final int SCROLL_BAR_WIDTH = 8;
	private static final double SCROLLBAR_FRAC_HEIGHT = 4;

	private boolean scrollbarOnLeft = true;

	private int x;
	private int y;
	private int width;
	private int height;

	private String[] text = {};
	private int startLine = 0;
	private FontRenderer fontRenderer;

	private double placeInText = 0;
	private int scrollBarTopY = 0;
	private int scrollBarHeight = 0;
	private boolean draggingScrollBar = false;
	private boolean scrollbarHighlighted = false;
	private int linesShown = 0;
	private boolean visible = true;
	private boolean textureBackground = true;;

	public GuiScrollingTextPanel(int x, int y, int width, int height,
			boolean scrollBarOnLeft, FontRenderer fontRenderer,
			boolean textureBackground) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.fontRenderer = fontRenderer;
		this.scrollbarOnLeft = scrollBarOnLeft;
		this.textureBackground = textureBackground;
	}

	public void draw(int mx, int my) {
		mouseMovedOrUp(mx, my, -1);

		if (visible) {
			// Draw background

			if (textureBackground) {
				int bgTextureNumber = Minecraft.getMinecraft().renderEngine
						.getTexture("/com/minetunes/resources/textures/signBG2.png");
				int currBGTextureY = y;
				// Tile background texture vertically
				while (currBGTextureY - y < height) {
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
					Minecraft.getMinecraft().renderEngine
							.bindTexture(bgTextureNumber);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);

					int textureHeight = 128;
					// If too tall, decide how much to shorten
					if (height - (currBGTextureY - y) < 128) {
						textureHeight = height % 128;
					}

					// Draw background texture
					drawTexturedModalRect(x, currBGTextureY, 0, 0, width,
							textureHeight);

					currBGTextureY += 128;
				}
			} else {
				// drawRect(x, y, width + x, height + y, 0x44666666);
			}

			// Draw Text
			// Update lines that can be shown at current size
			// Draw as many lines as can fit
			linesShown = 0;
			if (text != null) {
				int currLine = startLine;
				for (int textY = y + MARGIN; textY < y + height - MARGIN * 2; textY += 10) {
					if (currLine < text.length) {
						int textX = x + MARGIN;
						if (scrollbarOnLeft) {
							textX += SCROLL_BAR_WIDTH;
						}
						drawString(fontRenderer, text[currLine], x + MARGIN,
								textY, 0xffffff);
						linesShown++;
						currLine++;
					} else {
						break;
					}
				}
			}

			// Draw Scrollbar
			if (linesShown < text.length) {
				placeInText = (double) startLine
						/ ((double) text.length - (double) linesShown);
				double scrollBarMovementRange = ((double) height * (1d - (1d / SCROLLBAR_FRAC_HEIGHT)));
				scrollBarTopY = (int) (scrollBarMovementRange * (placeInText) + y);
				// System.out.println((double) placeInText / ((double)
				// text.length -
				// (double) linesShown));
				scrollBarHeight = (int) ((double) height / SCROLLBAR_FRAC_HEIGHT);
				int scrollbarColor = 0xffffffff;
				if (draggingScrollBar) {
					scrollbarColor = 0xff00ff00;
				} else if (scrollbarHighlighted) {
					scrollbarColor = 0xffffffff;
				} else {
					scrollbarColor = 0xbbbbbbbb;
				}
				int scrollBarX;
				if (scrollbarOnLeft) {
					scrollBarX = 0;
				} else {
					scrollBarX = x + width - SCROLL_BAR_WIDTH;
				}
				drawRect(scrollBarX, y, scrollBarX + SCROLL_BAR_WIDTH, y
						+ height, 0x44888888);
				drawGradientRect(scrollBarX - 1, scrollBarTopY, scrollBarX
						+ SCROLL_BAR_WIDTH + 1,
						scrollBarTopY + scrollBarHeight, scrollbarColor,
						0x88888888);
			}
		}
	}

	public void setScrollLine(int line) {
		startLine = line;
	}

	public int getScrollLine() {
		return startLine;
	}

	public void setText(String string) {
		LinkedList<String> linesBuffer = new LinkedList<String>();
		String[] lines = string.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].replace("\t", "     ");
			// Break up line

			// Word-wrap
			String[] wordsOnLine = line.split(" ");
			LinkedList<String> wordList = new LinkedList<String>(
					Arrays.asList(wordsOnLine));
			// Make lines out of the words
			String currLineSoFar = "";

			while (true) {
				if (wordList.size() <= 0) {
					break;
				}
				String currLine = wordList.getFirst();
				wordList.removeFirst();
				while (true) {
					if (wordList.size() > 0
							&& fontRenderer.getStringWidth(currLine.replaceAll(
									"§.", "")
									+ " "
									+ wordList.getFirst().replaceAll("§.", "")) < width
									- MARGIN * 2 - SCROLL_BAR_WIDTH) {
						// Add word
						currLine += " " + wordList.getFirst();
						wordList.removeFirst();
					} else {
						linesBuffer.add(currLine);
						// Time to break line
						break;
					}
				}
			}

			// This old code wraps so that the smallest unit to wrap by is the
			// letter
			// String lineCandidate = "";
			// for (int l = 0; l < line.length(); l++) {
			// lineCandidate = line.substring(0, l);
			// System.out.println("? " + lineCandidate);
			// if (fontRenderer.getStringWidth(lineCandidate) >= width - MARGIN
			// * 2) {
			// // Line is perfect
			// linesBuffer.add(lineCandidate.substring(0,
			// lineCandidate.length()));
			// System.out.println("* " + lineCandidate);
			// // Take the new line out of the line being broken up
			// line = line.substring(lineCandidate.length());
			// l -= lineCandidate.length();
			// System.out.println("= " + line);
			// }
			// }
			// if (line.length() > 0) {
			// linesBuffer.add(line);
			// }
		}
		text = linesBuffer.toArray(new String[linesBuffer.size()]);

		setScrollLine(0);
	}

	// private int distanceDraggedDown = 0;
	private double scrollRatio = 0d;

	public void mouseClicked(int mx, int my, int button) {
		if (coordsInScrollbar(mx, my)) {
			draggingScrollBar = true;
			mouseMovedOrUp(mx, my, -1);
		}
	}

	private boolean coordsInScrollbar(int mx, int my) {
		if (!scrollbarOnLeft) {
			if (mx >= x + width - SCROLL_BAR_WIDTH && mx <= x + width
					&& my >= y && my <= y + height) {
				// if (mx >= x && mx <= x + width && my >= y && my <= y +
				// height) {
				return true;
			} else {
				return false;
			}
		} else {
			if (mx >= x && mx <= x + SCROLL_BAR_WIDTH && my >= y
					&& my <= y + height) {
				// if (mx >= x && mx <= x + width && my >= y && my <= y +
				// height) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * This flag prevents the phenomenon where, if the mouse wheel is rotated
	 * outside of the panel, upon reentering the panel the panel scrolls by the
	 * amount the wheel was turned outside the panel.
	 */
	private boolean ignoreWheel = true;

	public void mouseMovedOrUp(int mx, int my, int button) {
		// System.out.println(mx + ":" + my + ":" + button);

		// Update mousewheel
		if (mx >= x && mx <= x + width && my >= y && my <= y + height) {
			int wheelDelta = Mouse.getDWheel();

			if (ignoreWheel) {
				ignoreWheel = false;
			} else {
				if (wheelDelta != 0) {
					startLine = startLine - (wheelDelta / 120);

					// range check
					if (startLine < 0) {
						startLine = 0;
					} else if (startLine >= text.length - linesShown) {
						startLine = text.length - linesShown;
					}
				}
			}
		} else {
			ignoreWheel = true;
		}

		// Update scrollbar
		if (button == 0) {
			draggingScrollBar = false;
		} else {
			if (coordsInScrollbar(mx, my)) {
				scrollbarHighlighted = true;
			} else {
				scrollbarHighlighted = false;
			}

			if (draggingScrollBar) {
				scrollRatio = ((((double) my - (double) y) / (double) ((linesShown * 10))));
				// System.out.println(scrollRatio);
				if (scrollRatio > 1) {
					scrollRatio = 1;
				} else if (scrollRatio < 0) {
					scrollRatio = 0;
				}

				setScrollLine((int) (scrollRatio * ((double) text.length - (double) linesShown)));
			}
		}
	}

	public void setVisible(boolean b) {
		visible = b;
	}

	public boolean getVisible() {
		return visible;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
