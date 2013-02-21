/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
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
package com.minetunes.test;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.minetunes.sfx.SFXManager;

/**
 * Testing for SFXManager.
 * 
 */
public class TestSFXManager {

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, SAXException {
		System.out.println(
				SFXManager.getEffectFilename("mob.creeperdeath", 1,
						SFXManager.getLatestSource()));
		System.out.println("Testing all effects.");

		SFXManager.load();
		for (String sfx : SFXManager
				.getAllEffects(SFXManager.getLatestSource()).keySet()) {
			System.out.println(sfx
					+ " ("
					+ SFXManager.getEffectForShorthandName(sfx,
							SFXManager.getLatestSource())
					+ "): Exists? "
					+ SFXManager.doesEffectExist(SFXManager
							.getEffectForShorthandName(sfx,
									SFXManager.getLatestSource()), 1,
							SFXManager.getLatestSource()));
		}
	}

}
