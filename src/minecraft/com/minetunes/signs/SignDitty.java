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
package com.minetunes.signs;

import java.util.HashMap;
import java.util.LinkedList;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;

/**
 * Contains data about a ditty read off of signs.
 * 
 */
public class SignDitty extends Ditty {

	/**
	 * Information about errors and their locations in the sign ditty.
	 */
	private LinkedList<SignLine> highlightedErrorLines = new LinkedList<SignLine>();

	private Point3D midiSavePoint = null;

	/**
	 * The sign clicked to start the ditty.
	 */
	private Point3D startPoint = new Point3D();

	/**
	 * Whether to try to play the ditty, too many errors or not.
	 */
	private boolean forceGoodDittyDetect = false;

	/**
	 * In contrast to forceGoodDittyDetect, implies that a ditty should not be
	 * played at all.
	 */
	private boolean containsNoPlayTokens = false;

	/**
	 * The next unique Sign ID number
	 */
	private int nextSignID = 0;

	/**
	 * All sign IDs used in this ditty mapped to their locations in the world
	 */
	private HashMap<Integer, Point3D> signIDs = new HashMap<Integer, Point3D>();

	/**
	 * All lines that should be highlighted to denote keywords etc.
	 */
	private LinkedList<SignLineHighlight> highlightedLines = new LinkedList<SignLineHighlight>();

	private boolean oneAtATime;

	private LinkedList<MaxPlaysLockPoint> maxPlayLockPoints = new LinkedList<MaxPlaysLockPoint>();

	/**
	 * 
	 */
	public SignDitty() {
		super();
	}

	public void addErrorHighlight(Point3D currSignPoint, int line) {
		highlightedErrorLines.add(new SignLine(currSignPoint, line));
	}

	public void addHighlight(Point3D sign, int line, String code) {
		highlightedLines.add(new SignLineHighlight(sign, line, code));
	}

	public LinkedList<SignLine> getHighlightedErrorLines() {
		return highlightedErrorLines;
	}

	public LinkedList<SignLineHighlight> getHighlightedLines() {
		return highlightedLines;
	}

	/**
	 * @return the startPoint
	 */
	public Point3D getStartPoint() {
		return startPoint;
	}

	/**
	 * @param startPoint
	 *            the startPoint to set
	 */
	public void setStartPoint(Point3D startPoint) {
		this.startPoint = startPoint;
	}

	public void setForceGoodDittyDetect(boolean b) {
		forceGoodDittyDetect = b;
	}

	/**
	 * @return the forceGoodDittyDetect
	 */
	public boolean isForceGoodDittyDetect() {
		return forceGoodDittyDetect;
	}

	/**
	 * Registers a sign to a Map of signs, and gives it an ID.
	 * 
	 * @param signPoint
	 * @return the ID for the given point.
	 */
	public int registerSignForID(Point3D signPoint) {
		int id = nextSignID++;
		signIDs.put(id, signPoint);
		return id;
	}

	public Point3D getPointForID(int id) {
		return signIDs.get((Integer) id);
	}

	public boolean isContainsNoPlayTokens() {
		return containsNoPlayTokens;
	}

	public void setContainsNoPlayTokens(boolean containsNoPlayTokens) {
		this.containsNoPlayTokens = containsNoPlayTokens;
	}

	public void setOneAtATime(boolean b) {
		oneAtATime = b;
	}

	/**
	 * @return the oneAtATime
	 */
	public boolean isOneAtATime() {
		return oneAtATime;
	}

	public void addMaxPlayLockPoint(Point3D currSignPoint, int maxPlays) {
		maxPlayLockPoints.add(new MaxPlaysLockPoint(currSignPoint, maxPlays));
	}

	public LinkedList<MaxPlaysLockPoint> getMaxPlayLockPoints() {
		return maxPlayLockPoints;
	}

	public Point3D getMidiSavePoint() {
		return midiSavePoint;
	}

	public void setMidiSavePoint(Point3D midiSavePoint) {
		this.midiSavePoint = midiSavePoint;
	}

}
