/**
 * Copyright (c) 2012 William Karnavas 
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
package com.minetunes.jfugue.rendererEffect;

import java.util.LinkedList;

import org.jfugue.elements.Tempo;
import org.jfugue.parsers.MusicStringParser;

public class AccelerateEffect extends TempoEffect {
	/**
	 * Number of semitones to increase or decrease note pitch
	 */
	private int bpm = 0;

	private double duration = 0;

	public AccelerateEffect(Double endTime, int tempoIncrease, double length) {
		super(endTime);
		setBPM(tempoIncrease);
		setDuration(length);

		// System.out.println(toString());
	}

	public int getBPM() {
		return bpm;
	}

	public void setBPM(int steps) {
		this.bpm = steps;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double length) {
		this.duration = length;
	}

	/**
	 * SET STARTTEMPO BEFORE APPLYING THIS EFFECT!
	 */
	@Override
	public void apply(LinkedList<TimedJFugueElement> elements) {
		// Create tempo tokens
		double spaceBetweenChanges = duration / Math.abs(bpm);
		for (int i = 0; i < Math.abs(bpm); i++) {
			double time = spaceBetweenChanges * i;
			int tempo;
			if (bpm >= 0) {
				tempo = startTempo + i;
			} else {
				tempo = startTempo - i;
			}

			if (tempo < MusicStringParser.MIN_TEMPO) {
				tempo = MusicStringParser.MIN_TEMPO;
			} else if (tempo > MusicStringParser.MAX_TEMPO) {
				tempo = MusicStringParser.MAX_TEMPO;
			}

			Tempo tempoChange = new Tempo(tempo);
			TimedJFugueElement e = new TimedJFugueElement(tempoChange, time);
			elements.add(e);
		}

		// Add the final event
		int tempo = startTempo + bpm;
		if (tempo < MusicStringParser.MIN_TEMPO) {
			tempo = MusicStringParser.MIN_TEMPO;
		} else if (tempo > MusicStringParser.MAX_TEMPO) {
			tempo = MusicStringParser.MAX_TEMPO;
		}
		elements.add(new TimedJFugueElement(new Tempo(tempo), duration));
	}

	@Override
	public String toString() {
		// Okay, I royally messed up my attempt at efficiency here. I'm a secure
		// person who is comfortable admitting his faults. Fix this if you ever
		// feel like it, dear reader.
		return new StringBuilder().append(
				"TransposeEffect-EndTime=" + getEnd() + "-Endless="
						+ getEndless() + "-steps=" + bpm + "-applyMethod="
						+ applyMethod + ";").toString();
	}

}
