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
package com.minetunes.gui.signEditor;

import java.util.LinkedList;

import org.jfugue.ParserListener;
import org.jfugue.elements.ChannelPressure;
import org.jfugue.elements.Controller;
import org.jfugue.elements.Instrument;
import org.jfugue.elements.JFugueElement;
import org.jfugue.elements.KeySignature;
import org.jfugue.elements.Layer;
import org.jfugue.elements.Lyric;
import org.jfugue.elements.MCDittyEvent;
import org.jfugue.elements.Measure;
import org.jfugue.elements.Note;
import org.jfugue.elements.PitchBend;
import org.jfugue.elements.PolyphonicPressure;
import org.jfugue.elements.SystemExclusive;
import org.jfugue.elements.Tempo;
import org.jfugue.elements.Time;
import org.jfugue.elements.Voice;

/**
 *
 */
public class GuiEditSignNoteHelpParserListener implements ParserListener {

	private LinkedList<JFugueElement> readNotesList;

	public GuiEditSignNoteHelpParserListener(LinkedList<JFugueElement> readNotes) {
		readNotesList = readNotes;
	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#voiceEvent(org.jfugue.elements.Voice)
	 */
	@Override
	public void voiceEvent(Voice voice) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#tempoEvent(org.jfugue.elements.Tempo)
	 */
	@Override
	public void tempoEvent(Tempo tempo) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#instrumentEvent(org.jfugue.elements.Instrument)
	 */
	@Override
	public void instrumentEvent(Instrument instrument) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#layerEvent(org.jfugue.elements.Layer)
	 */
	@Override
	public void layerEvent(Layer layer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#measureEvent(org.jfugue.elements.Measure)
	 */
	@Override
	public void measureEvent(Measure measure) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#timeEvent(org.jfugue.elements.Time)
	 */
	@Override
	public void timeEvent(Time time) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#keySignatureEvent(org.jfugue.elements.KeySignature)
	 */
	@Override
	public void keySignatureEvent(KeySignature keySig) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#systemExclusiveEvent(org.jfugue.elements.SystemExclusive)
	 */
	@Override
	public void systemExclusiveEvent(SystemExclusive sysex) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#controllerEvent(org.jfugue.elements.Controller)
	 */
	@Override
	public void controllerEvent(Controller controller) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#channelPressureEvent(org.jfugue.elements.ChannelPressure)
	 */
	@Override
	public void channelPressureEvent(ChannelPressure channelPressure) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#polyphonicPressureEvent(org.jfugue.elements.PolyphonicPressure)
	 */
	@Override
	public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#pitchBendEvent(org.jfugue.elements.PitchBend)
	 */
	@Override
	public void pitchBendEvent(PitchBend pitchBend) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#noteEvent(org.jfugue.elements.Note)
	 */
	@Override
	public void noteEvent(Note note) {
		readNotesList.add(note);
	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#sequentialNoteEvent(org.jfugue.elements.Note)
	 */
	@Override
	public void sequentialNoteEvent(Note note) {
		readNotesList.add(note);
	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#parallelNoteEvent(org.jfugue.elements.Note)
	 */
	@Override
	public void parallelNoteEvent(Note note) {
		readNotesList.add(note);
	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#lyricEvent(org.jfugue.elements.Lyric)
	 */
	@Override
	public void lyricEvent(Lyric event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jfugue.ParserListener#MineTunesEvent(org.jfugue.elements.MineTunesEvent)
	 */
	@Override
	public void mcDittyEvent(MCDittyEvent event) {
		// TODO Auto-generated method stub

	}

}
