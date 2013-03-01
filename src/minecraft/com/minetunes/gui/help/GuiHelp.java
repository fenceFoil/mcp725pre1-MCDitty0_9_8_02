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
package com.minetunes.gui.help;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.GuiButtonRect;

/**
 * @author William
 * 
 */
public class GuiHelp extends GuiScreen {
	/**
	 * 
	 */
	private static final int BOTTOM_MARGIN = 45;
	/**
	 * 
	 */
	private static final int TOP_MARGIN = 25;
	private static final int SIDE_BUTTON_WIDTH = 45;
	private String topic = "";
	private Properties props;
	private boolean notDownloaded = false;
	private GuiScreen backScreen;
	private int numSlides;
	private int currSlide = -1;
	private boolean badCaptions = false;
	private LinkedList<Slide> slides = new LinkedList<Slide>();
	private GuiButtonRect fwdButton;
	private GuiButtonRect backButton;

	private File helpDir = new File(MinetunesConfig.getResourcesDir(), "help");
	private GuiButtonRect exitButton;
	private GuiButtonRect printButton;
	private boolean printableSlide = false;

	public GuiHelp(String topic, GuiScreen backScreen) {
		this.backScreen = backScreen;

		this.topic = topic;
		helpDir = new File(helpDir, topic);

		props = new Properties();
		try {
			props.load(new FileInputStream(new File(helpDir, "captions.txt")));
		} catch (FileNotFoundException e) {
			notDownloaded = true;
			e.printStackTrace();
			return;
		} catch (IOException e) {
			notDownloaded = true;
			e.printStackTrace();
			return;
		}

		String numSlidesStr = props.getProperty("numSlides");
		if (numSlidesStr != null && numSlidesStr.matches("\\d+")) {
			numSlides = Integer.parseInt(numSlidesStr);
		} else {
			badCaptions = true;
			numSlides = 0;
			return;
		}

		String lastTitle = "";
		for (int i = 0; i < numSlides; i++) {
			Integer I = Integer.valueOf(i);
			String format = "%1$03d";
			String paddedSlideNum = String.format(format, I);
			String title = props.getProperty(i + ".title");
			if (title == null) {
				title = lastTitle;
			} else {
				lastTitle = title;
			}
			int textureQuality = MinetunesConfig
					.getBoolean("slides.highQuality") ? 512 : 1024;
			Slide s = new Slide(new File(helpDir.getPath(), paddedSlideNum
					+ ".png"), title, props.getProperty(i + ".caption"),
					SIDE_BUTTON_WIDTH, TOP_MARGIN, textureQuality);
			slides.add(s);
		}
	}

	/**
	 * @param i
	 */
	private void changeSlide(int i) {
		// Bound newSlideNum
		int newSlideNum = Math.max(0, Math.min(numSlides - 1, i));

		// Change shown slide texture
		Slide slide = slides.get(newSlideNum);
		slide.prepareToShow();
		currSlide = newSlideNum;

		// Check for printable slide
		if (props.getProperty(currSlide + ".printable") != null
				&& props.getProperty(currSlide + ".printable")
						.equalsIgnoreCase("true")) {
			printableSlide = true;
		} else {
			printableSlide = false;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if (notDownloaded) {
			mc.displayGuiScreen(new GuiSimpleMessage(backScreen,
					"No help available.", 0xffffffff));
		} else if (badCaptions) {
			mc.displayGuiScreen(new GuiSimpleMessage(backScreen,
					"The captions file for this topic is unreadable.",
					0xffffffff));
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		for (Slide s : slides) {
			s.unloadImage();
		}
	}

	@Override
	public void drawScreen(int mx, int my, float par3) {
		drawDefaultBackground();

		// Draw sign background
		GL11.glColor4f(0.8f, 0.8f, 0.8f, 1.0f);
		mc.renderEngine.bindTexture(Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/signBG2.png"));
		// drawTexturedModalRect(0, height - BOTTOM_MARGIN, 0, 0, width,
		// BOTTOM_MARGIN);
		for (int i = 0; i < height; i += 128) {
			drawTexturedModalRect(0, i, 0, 0, width, height);
		}

		if (badCaptions || notDownloaded) {
			return;
		}

		if (currSlide == -1) {
			changeSlide(0);
		}

		// Render the picture
		Slide slide = slides.get(currSlide);
		slide.draw(new Rectangle(width - 2 * SIDE_BUTTON_WIDTH, height
				- TOP_MARGIN - BOTTOM_MARGIN), true);

		// Render the title and caption
		drawCenteredString(fontRenderer, slide.getTitle(), width / 2,
				(TOP_MARGIN / 2) - 5, 0xffff00);
		fontRenderer.drawSplitString(slide.getCaption(), SIDE_BUTTON_WIDTH + 2,
				height - 40, width - 2 * SIDE_BUTTON_WIDTH - 4, 0xffffff);

		// Render left and right buttons
		if (currSlide < numSlides - 1) {
			fwdButton.draw(mx, my, par3, fontRenderer);
		}
		if (currSlide > 0) {
			backButton.draw(mx, my, par3, fontRenderer);
		}

		exitButton.draw(mx, my, par3, fontRenderer);
		if (printableSlide) {
			printButton.draw(mx, my, par3, fontRenderer);
		}

		// Render count
		drawString(fontRenderer, (currSlide + 1) + " of " + numSlides,
				width - 50, (TOP_MARGIN / 2) - 5, 0xffffff);

		// Render buttons
		super.drawScreen(mx, my, par3);
	}

	@Override
	public void initGui() {
		super.initGui();

		exitButton = new GuiButtonRect(new Rectangle(0, 0, 60, 20), "Exit",
				0xbba03030);
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mc.displayGuiScreen(backScreen);
			}
		});

		printButton = new GuiButtonRect(new Rectangle(60, 0, 60, 20), "Print",
				0xbb3030a0);
		final GuiHelp thisGui = this;
		printButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported() && thisGui.printableSlide) {
					try {
						// Print the file. Filter out the "\." that gets added
						// into the path sometimes.
						Desktop.getDesktop()
								.print(new File(slides.get(currSlide).getFile()
										.getPath()
										.replace("./", "").replace(".\\", "")));
					} catch (IOException e1) {
						e1.printStackTrace();
						mc.displayGuiScreen(new GuiSimpleMessage(thisGui,
								"Could not read image to print.", 0xffffff));
					}
				}
			}
		});

		backButton = new GuiButtonRect(new Rectangle(0, TOP_MARGIN,
				SIDE_BUTTON_WIDTH, height - TOP_MARGIN - BOTTOM_MARGIN), "<--");
		backButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeSlide(currSlide - 1);
			}
		});

		fwdButton = new GuiButtonRect(new Rectangle(width - SIDE_BUTTON_WIDTH,
				TOP_MARGIN, width, height - TOP_MARGIN - BOTTOM_MARGIN), "-->");
		fwdButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				changeSlide(currSlide + 1);
			}
		});
	}

	@Override
	protected void mouseClicked(int mx, int my, int button) {
		super.mouseClicked(mx, my, button);
		fwdButton.onMousePressed(mx, my, button);
		backButton.onMousePressed(mx, my, button);
		exitButton.onMousePressed(mx, my, button);
		printButton.onMousePressed(mx, my, button);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 0) {
			mc.displayGuiScreen(backScreen);
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1) {
			mc.displayGuiScreen(backScreen);
		}
	}
}
