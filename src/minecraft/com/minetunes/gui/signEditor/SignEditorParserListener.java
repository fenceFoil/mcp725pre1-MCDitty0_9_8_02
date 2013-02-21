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

import net.minecraft.src.GuiEditSign;

import org.jfugue.ParserListener;
import org.jfugue.elements.ChannelPressure;
import org.jfugue.elements.Controller;
import org.jfugue.elements.Instrument;
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

public class SignEditorParserListener implements ParserListener {

	private GuiEditSign gui;
	
	private int[][] tickCount = new int[17][17];
	
	private int currVoice = 16;
	private int currLayer = 16;

	public SignEditorParserListener(GuiEditSign guiEditSign) {
		gui = guiEditSign;
	}

	@Override
	public void voiceEvent(Voice voice) {
		currVoice = voice.getVoice();
	}

	@Override
	public void tempoEvent(Tempo tempo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void instrumentEvent(Instrument instrument) {
		// TODO Auto-generated method stub

	}

	@Override
	public void layerEvent(Layer layer) {
		currLayer = layer.getLayer();
	}

	@Override
	public void measureEvent(Measure measure) {
		// TODO Auto-generated method stub

	}

	@Override
	public void timeEvent(Time time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keySignatureEvent(KeySignature keySig) {
		// TODO Auto-generated method stub

	}

	@Override
	public void systemExclusiveEvent(SystemExclusive sysex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerEvent(Controller controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelPressureEvent(ChannelPressure channelPressure) {
		// TODO Auto-generated method stub

	}

	@Override
	public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pitchBendEvent(PitchBend pitchBend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void noteEvent(Note note) {
		tickCount[currVoice][currLayer] = (int) (128d*note.getDecimalDuration());
	}

	@Override
	public void sequentialNoteEvent(Note note) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parallelNoteEvent(Note note) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lyricEvent(Lyric event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mcDittyEvent(MCDittyEvent event) {
		// TODO Auto-generated method stub

	}

	public String getResultText() {
		return "Time: "+tickCount[16][16];
	}

}
