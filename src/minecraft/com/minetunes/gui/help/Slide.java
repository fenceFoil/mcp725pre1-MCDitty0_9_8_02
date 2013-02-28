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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.minecraft.client.Minecraft;

import org.imgscalr.Scalr;

/**
 * @author William
 * 
 */
public class Slide {
	private File file;
	private BufferedImage image;
	private String title;
	private String caption;
	private int usedWidth = 0;
	private int usedHeight = 0;

	public Slide(File f, String tit, String capt) {
		file = f;
		title = tit;
		caption = capt;
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
			int dimensions = 512;
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

//	public int getNextLargestPowOf2(int numberInside) {
//		int n = numberInside;
//
//		n--;
//		n |= n >> 1; // Divide by 2^k for consecutive doublings of k up to 32,
//		n |= n >> 2; // and then or the results.
//		n |= n >> 4;
//		n |= n >> 8;
//		n |= n >> 16;
//		n++; // The result is a number of 1 bits equal to the number
//				// of bits in the original number, plus 1. That's the
//				// next highest power of 2.
//		n=64;
//		return n;
//	}

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

}
