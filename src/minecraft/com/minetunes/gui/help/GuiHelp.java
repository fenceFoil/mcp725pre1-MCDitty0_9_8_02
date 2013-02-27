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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

import com.minetunes.config.MinetunesConfig;

/**
 * @author William
 * 
 */
public class GuiHelp extends GuiScreen {
	private String topic = "";
	private Properties props;
	private boolean notDownloaded = false;
	private GuiScreen backScreen;
	private int numSlides;
	private int currSlide = -1;
	private boolean badCaptions = false;
	private LinkedList<Slide> slides = new LinkedList<Slide>();
	private int textureIndex = -1;

	private File helpDir = new File(MinetunesConfig.getResourcesDir(), "help");

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

		for (int i = 0; i < numSlides; i++) {
			Integer I = Integer.valueOf(i);
			String format = "%1$03d";
			String paddedSlideNum = String.format(format, I);
			Slide s = new Slide(new File(helpDir.getPath(), paddedSlideNum
					+ ".png"), props.getProperty(i + ".title"),
					props.getProperty(i + ".caption"));
			slides.add(s);
		}

		textureIndex = GL11.glGenTextures();
	}

	/**
	 * @param i
	 */
	private void changeSlide(int i) {
		Slide slide = slides.get(i);
		mc.renderEngine.setupTexture(slide.getImage(), textureIndex);
		currSlide = i;
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

		mc.renderEngine.deleteTexture(textureIndex);
	}

	@Override
	public void drawScreen(int mx, int my, float par3) {
		super.drawScreen(mx, my, par3);

		drawDefaultBackground();

		if (badCaptions || notDownloaded) {
			return;
		}

		if (currSlide == -1) {
			changeSlide(0);
		}

		Slide slide = slides.get(currSlide);

		// Render the picture
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0f);
		mc.renderEngine.bindTexture(textureIndex);
		int pixelWidth = (int) (slide.getUsedWidth() / ((double) slide
				.getImage().getWidth() / 256d));
		int pixelHeight = (int) (slide.getUsedHeight() / ((double) slide
				.getImage().getHeight() / 256d));

		int drawHeight = height - 45 - 20;
		int drawWidth = (int) (((double) slide.getUsedWidth() / (double) slide
				.getUsedHeight()) * (double) drawHeight);

		int x1 = width / 2 - drawWidth / 2;
		int y1 = 20;
		int x2 = width / 2 + drawWidth / 2;
		int y2 = height - 45;

		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV((double) (x1), (double) (y2),
				(double) this.zLevel, (double) ((float) (0) * var7),
				(double) ((float) (pixelHeight) * var8));
		var9.addVertexWithUV((double) (x2), (double) (y2),
				(double) this.zLevel, (double) ((float) (pixelWidth) * var7),
				(double) ((float) (pixelHeight) * var8));
		var9.addVertexWithUV((double) (x2), (double) (y1),
				(double) this.zLevel, (double) ((float) (pixelWidth) * var7),
				(double) ((float) (0) * var8));
		var9.addVertexWithUV((double) (x1), (double) (y1),
				(double) this.zLevel, (double) ((float) (0) * var7),
				(double) ((float) (0) * var8));
		var9.draw();

		// Render the title and caption
		drawCenteredString(fontRenderer, slide.getTitle(), width / 2, 5,
				0xffffff);

		GL11.glColor4f(1, 1, 1, 1);
		mc.renderEngine.bindTexture(Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/signBG2.png"));
		drawTexturedModalRect(18, height - 42, 0, 0, width - 18 * 2, 40);
		fontRenderer.drawSplitString(slide.getCaption(), 20, height - 40,
				width - 20, 0xffffff);
	}
}
