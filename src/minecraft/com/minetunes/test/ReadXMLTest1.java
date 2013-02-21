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
package com.minetunes.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author William
 * 
 */
public class ReadXMLTest1 {

	/**
	 * 
	 */
	public ReadXMLTest1() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException {
		StringBuffer bookTextBuffer = new StringBuffer();
		BufferedReader textIn = new BufferedReader(new FileReader(
				"C:\\users\\william\\dropbox\\booktext.xml"));
		while (true) {
			String in = textIn.readLine();
			if (in == null) {
				break;
			}
			bookTextBuffer.append(in).append("\n");
		}
		textIn.close();
		String bookText = bookTextBuffer.toString();

		// Parse the XML ditty data
		// Create the document builder
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		// Parse the book's text
		InputStream bookTextInputStream = new ByteArrayInputStream(
				bookText.getBytes());
		Document bookDocument = documentBuilder.parse(bookTextInputStream);
		bookDocument.getDocumentElement().normalize();

		// Output
		writeNode(bookDocument.getDocumentElement(), 0);
	}

	private static void writeNode(Node node, int indent) {
		String space = "";
		for (int i = 0; i < indent; i++) {
			space += "  ";
		}
		System.out.print(space + node.getNodeName() + ": '"
				+ node.getNodeValue() + "'");
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				System.out.print(". " + attributes.item(i).getNodeName() + "="
						+ attributes.item(i).getNodeValue());
			}
		}
		System.out.println();
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			writeNode(node.getChildNodes().item(i), indent + 1);
		}
	}

}
