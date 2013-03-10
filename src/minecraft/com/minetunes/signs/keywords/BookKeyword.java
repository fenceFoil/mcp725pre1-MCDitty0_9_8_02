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
package com.minetunes.signs.keywords;

import java.util.LinkedList;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.books.BookDittyXMLParser;
import com.minetunes.books.BookWrapper;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.event.ParticleEvent;
import com.minetunes.dittyXML.DittyXMLParser;
import com.minetunes.dittyXML.MissingContainerException;
import com.minetunes.dittyXML.UnnamedLyricsException;
import com.minetunes.dittyXML.UnnumberedLyricException;
import com.minetunes.particle.ParticleRequest;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.keywords.argparser.ArgParser;

/**
 * @author William
 * 
 */
public class BookKeyword extends SignTuneKeyword {

	/**
	 * @param wholeKeyword
	 */
	public BookKeyword(String wholeKeyword) {
		super(wholeKeyword);
		// No arguments
		argParser = new ArgParser();
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {

		LinkedList<ItemStack> nearbyBooks = Minetunes.getFramedItemsNearby(
				world, location, 2, Item.writableBook.shiftedIndex,
				Item.writtenBook.shiftedIndex);

		for (ItemStack item : nearbyBooks) {
			boolean failure = false;
			BookWrapper book = BookWrapper.wrapBook(item);
			try {
				LinkedList<Element> mcdittyelements = BookDittyXMLParser
						.getDittyXMLContainers(book);
				DittyXMLParser.parseDittyXMLContainers(mcdittyelements, ditty);
			} catch (SAXException e) {
				failure = true;
				e.printStackTrace();
			} catch (MissingContainerException e) {
				failure = true;
				e.printStackTrace();
			} catch (UnnamedLyricsException e) {
				failure = true;
				e.printStackTrace();
			} catch (UnnumberedLyricException e) {
				failure = true;
				e.printStackTrace();
			}

			if (failure) {
				for (int i = 0; i < 20; i++) {
					String type = "lava";
					if (Minetunes.rand.nextInt(3) == 0) {
						type = "smoke";
					} else if (Minetunes.rand.nextInt(2) == 0) {
						type = "explode";
					}
					int id = ditty.addDittyEvent(new ParticleEvent(
							new ParticleRequest(location, type), ditty
									.getDittyID()));
					ditty.addMusicStringTokens(readMusicString,
							SignTuneParser.TIMED_EVENT_TOKEN + id, false);
				}
			}
		}

		// System.out.println (ditty.getMusicString());
		readMusicString.append(ditty.getMusicString());

		// No change in sign flow
		return null;
	}

}
