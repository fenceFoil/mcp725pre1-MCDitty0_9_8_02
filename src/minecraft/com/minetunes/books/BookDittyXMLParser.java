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
package com.minetunes.books;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.minecraft.src.BlockSign;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.minetunes.DOMUtil;
import com.minetunes.ditty.Ditty;

/**
 * Accepts a writable book, and returns any elements from inside a <bookTune>
 * element, if it exists in the book.
 * 
 */
public class BookDittyXMLParser {

	/**
	 * Parses the book for valid DittyXML containers (\<bookTune\> elements) and
	 * returns a list of any found.
	 * 
	 * @param book
	 * @return a list of containers OR an empty list if none were found OR null
	 *         if there were errors parsing xml OR null if the parameter book is
	 *         null.
	 * @throws SAXException
	 *             if there are errors the XML in the book.
	 * @throws any
	 *             errors found while parsing XML
	 */
	public static LinkedList<Element> getDittyXMLContainers(BookWrapper book)
			throws SAXException {
		// Cannot parse a non-existant book!
		if (book == null) {
			return null;
		}

		// Set up the list of containers to return
		LinkedList<Element> containersList = new LinkedList<Element>();

		// Get the text of the book being parsed
		String bookText = book.getAllText(false);

		// Check that the book is a MineTunes book before trying to parse.
		if (!bookText.toLowerCase().contains("<booktune")) {
			// Book does not contain DittyXML
			// return an empty list
			return containersList;
		}

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
		InputStream bookTextInputStream = new ByteArrayInputStream(
				bookText.getBytes());
		// Parse
		Document bookDocument = null;
		try {
			bookDocument = documentBuilder.parse(bookTextInputStream);
		} catch (IOException e) {
			e.printStackTrace();
			// Ideally this should never happen streaming from a String, but
			// stranger has happened...
		}

		// Decipher the parsed XML
		if (bookDocument != null) {
			bookDocument.getDocumentElement().normalize();
			// Look for all "bookTune" elements
			LinkedList<Element> bookTuneElements = DOMUtil.findElementsIgnoreCase ("booktune", bookDocument.getChildNodes());
			// Add all of these elements to containersList
			containersList.addAll(bookTuneElements);
		}

		// Return any found containers
		return containersList;
	}
}
