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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.jfugue.elements.Note;

public class OctavesEffect extends RendererEffect {

	private HashSet<Integer> octaves = new HashSet<Integer>();

	public OctavesEffect(Double endTime, Collection<Integer> octaves) {
		super(endTime);
		addOctaves(octaves);
		
		applyMethod = ApplyEffect.COMBINE;
	}

	private void addOctaves(Collection<Integer> octs) {
		octaves.addAll(octs);
	}

	private void addOctave(int oct) {
		octaves.add(oct);
	}

	@Override
	public void apply(LinkedList<TimedJFugueElement> elements) {
		super.apply(elements);

		// Add octave-transposed copies of notes
		LinkedList<TimedJFugueElement> toAdd = new LinkedList<TimedJFugueElement>();
		for (TimedJFugueElement e : elements) {
			if (e.element instanceof Note) {
				Note note = (Note) e.element;
				int noteValue = note.getValue();

				// Make copies
				for (Integer i : octaves) {
					// Ignore "0" octaves: that's the original note duplicated!
					if (i == 0) {
						continue;
					}

					int octaveCopyValue = noteValue + 12 * i;
					if (octaveCopyValue < 0 || octaveCopyValue > 127) {
						// Just fuggedabowtit if it's out of your range, chump
						continue;
					} else {
						// In range -- add note!
						Note cloneNote = cloneNote(note);
						cloneNote.setValue(octaveCopyValue);
						TimedJFugueElement newNoteElement = new TimedJFugueElement(
								cloneNote, e.time);
						toAdd.add(newNoteElement);
					}
				}
			}
		}
		
		// Add elements to add (if done above, would cause concurrent modification exception)
		for (TimedJFugueElement e:toAdd) {
			elements.add(e);
		}
	}

	private static Note cloneNote(Note n) {
		Note result = new Note();
		result.setValue(n.getValue());

		result.setAccompanyingNotes(n.isAccompanyingNotes());
		result.setAdjustedForKey(n.isAdjustedForKey());
		result.setAttackVelocity(n.getAttackVelocity());
		result.setDecayVelocity(n.getDecayVelocity());
		result.setDecimalDuration(n.getDecimalDuration());
		result.setEndOfTie(n.isEndOfTie());
		result.setMillisDuration(n.getMillisDuration());
		result.setRest(n.isRest());
		result.setStartOfTie(n.isStartOfTie());
		result.setType(n.getType());

		return result;
	}

	@Override
	public void combineWith(RendererEffect effect) {
		super.combineWith(effect);

		if (effect instanceof OctavesEffect) {
			OctavesEffect oe = (OctavesEffect) effect;
			addOctaves(oe.getOctaves());
		}
	}

	public HashSet<Integer> getOctaves() {
		return octaves;
	}

	public void setOctaves(HashSet<Integer> octaves) {
		this.octaves = octaves;
	}

}
