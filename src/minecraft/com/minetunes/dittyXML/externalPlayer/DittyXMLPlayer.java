/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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
package com.minetunes.dittyXML.externalPlayer;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.minetunes.DOMUtil;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.DittyPlayerThread;
import com.minetunes.dittyXML.DittyXMLParser;

/**
 * An attempt to play ditties without a Minecraft running directly from a
 * DittyXML file. Currently fails because the "world" is null without starting a
 * game, and all sorts of junk for particles and whatnot become grumpy.
 */
public class DittyXMLPlayer extends JFrame {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws Exception {
		FileDialog chooser = new FileDialog((Frame) null);
		chooser.show();
		String file = chooser.getDirectory() + chooser.getFile();
		BufferedReader in = new BufferedReader(new FileReader(new File(file)));
		StringBuilder readFile = new StringBuilder();
		while (true) {
			String lineIn = in.readLine();
			if (lineIn == null) {
				break;
			} else {
				readFile.append(lineIn);
			}
		}
		in.close();

		// Parse the XML ditty data
		// Create the document builder
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		// Parse the book's text
		// Convert the book's text to an InputStream
		InputStream bookTextInputStream = new ByteArrayInputStream(readFile
				.toString().getBytes());
		// Parse
		Document bookDocument = null;
		try {
			bookDocument = documentBuilder.parse(bookTextInputStream);
		} catch (IOException e) {
			e.printStackTrace();
			// Ideally this should never happen streaming from a String, but
			// stranger has happened...
		}

		LinkedList<Element> containersList = new LinkedList<Element>();

		// Decipher the parsed XML
		if (bookDocument != null) {
			bookDocument.getDocumentElement().normalize();
			// Look for all "MineTunes" elements
			LinkedList<Element> mcdittyElements = DOMUtil
					.findElementsIgnoreCase("mcditty",
							bookDocument.getChildNodes());
			// Add all of these elements to containersList
			containersList.addAll(mcdittyElements);
		}

		Ditty d = DittyXMLParser.parseDittyXMLContainers(containersList, null);
		if (d != null) {
			DittyPlayerThread t = new DittyPlayerThread(d);
			t.start();
		}
	}

}
