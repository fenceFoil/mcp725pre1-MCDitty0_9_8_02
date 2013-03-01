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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Tessellator;

import org.imgscalr.Scalr;
import org.lwjgl.opengl.GL11;

/**
 * Represents a slide, as in a slideshow. To use, construct with the filename of
 * an image to show. To load the image onto a texture, call prepareToShow()
 * before the first call to draw(). Call unloadImage() when done to release the
 * texture.
 * 
 */
public class Slide {
	private File file;
	private BufferedImage image;
	private String title;
	private String caption;
	private int usedWidth = 0;
	private int usedHeight = 0;
	private int texture;
	private boolean textureLoaded = false;
	private int x;
	private int y;
	private int textureSize;

	public Slide(File f, String tit, String capt, int x, int y, int textureSize) {
		file = f;
		title = tit;
		caption = capt;
		this.x = x;
		this.y = y;
		this.textureSize = textureSize;
	}

	public BufferedImage getImage() {
		if (image == null) {
			BufferedImage rawImage = null;
			try {
				rawImage = ImageIO.read(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (rawImage == null) {
				rawImage = new BufferedImage(10, 10,
						BufferedImage.TYPE_INT_ARGB);
			}

			// Make an appropriately-sized square image for a texture
			int dimensions = textureSize;
			image = new BufferedImage(dimensions, dimensions,
					BufferedImage.TYPE_INT_ARGB);
			BufferedImage scaledImage = Scalr.resize(rawImage, dimensions);

			setUsedWidth(scaledImage.getWidth());
			setUsedHeight(scaledImage.getHeight());

			Graphics2D g2d = (Graphics2D) image.getGraphics();
			g2d.drawImage(scaledImage, 0, 0, null);
			g2d.dispose();

			return image;
		} else {
			return image;
		}
	}

	/**
	 * Load the image and put it on a texture. Can be expensive.
	 */
	public void prepareToShow() {
		if (!textureLoaded) {
			texture = GL11.glGenTextures();
			Minecraft.getMinecraft().renderEngine.setupTexture(getImage(),
					texture);
			textureLoaded = true;
		}
	}

	/**
	 * Releases texture.
	 */
	public void unloadImage() {
		if (textureLoaded) {
			Minecraft.getMinecraft().renderEngine.deleteTexture(texture);
			textureLoaded = false;
		}
	}

	// public int getNextLargestPowOf2(int numberInside) {
	// int n = numberInside;
	//
	// n--;
	// n |= n >> 1; // Divide by 2^k for consecutive doublings of k up to 32,
	// n |= n >> 2; // and then or the results.
	// n |= n >> 4;
	// n |= n >> 8;
	// n |= n >> 16;
	// n++; // The result is a number of 1 bits equal to the number
	// // of bits in the original number, plus 1. That's the
	// // next highest power of 2.
	// n=64;
	// return n;
	// }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public int getUsedWidth() {
		return usedWidth;
	}

	public void setUsedWidth(int usedWidth) {
		this.usedWidth = usedWidth;
	}

	public int getUsedHeight() {
		return usedHeight;
	}

	public void setUsedHeight(int usedHeight) {
		this.usedHeight = usedHeight;
	}

	/**
	 * Draws the slide. Ignores x and y of rectangle. Call prepareToShow()
	 * before calling this; otherwise it will be run automatically on the first
	 * call to draw(). (dontPrepare removes this behaviour)
	 */
	public void draw(Rectangle validArea, boolean dontPrepare) {
		if (image == null) {
			if (dontPrepare) {
				return;
			} else {
				prepareToShow();
			}
		}

		// Still can't load?
		if (image == null) {
			return;
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0f);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Get the width and height of the slide's image on the texture
		// Note that the way MC works, U and V in the texture will always max
		// out at 256, not 512, 1024, or whatever size it really is.
		int textureWidth = (int) (getUsedWidth()
				/ (double) getImage().getWidth() * 256d);
		int textureHeight = (int) (getUsedHeight()
				/ (double) getImage().getHeight() * 256d);

		// Decide the width and height to draw the texture at onto the screen
		// First, compare the aspect ratios of the slide's image and the valid
		// area for it
		double imageAspect = (double) textureWidth / (double) textureHeight;
		double areaAspect = (double) validArea.width
				/ (double) validArea.height;
		int drawWidth, drawHeight;
		if (imageAspect > areaAspect) {
			// If texture is wider than area, width = area's width and change
			// the drawn height
			drawWidth = validArea.width;
			drawHeight = (int) ((double) drawWidth / imageAspect);
		} else {
			// Otherwise, texture is taller than area, height = area's height
			// and change the drawn width
			drawHeight = validArea.height;
			drawWidth = (int) ((double) drawHeight * imageAspect);
		}

		// Calculate the x and y coords of each corner (upper left, lower
		// right)
		int x1 = x + (validArea.width / 2) - (drawWidth / 2);
		int y1 = y + (validArea.height / 2) - (drawHeight / 2);
		int x2 = x + (validArea.width / 2) + (drawWidth / 2);
		int y2 = y + (validArea.height / 2) + (drawHeight / 2);

		// Draw
		double uScale = 1f / 256f;
		double vScale = 1f / 256f;
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x1, y2, 0, 0, textureHeight * vScale);
		tess.addVertexWithUV(x2, y2, 0, textureWidth * uScale, textureHeight
				* vScale);
		tess.addVertexWithUV(x2, y1, 0, textureWidth * uScale, 0);
		tess.addVertexWithUV(x1, y1, 0, 0, vScale);
		tess.draw();
	}

	/**
	 * @return
	 */
	public File getFile() {
		return file;
	}

}
