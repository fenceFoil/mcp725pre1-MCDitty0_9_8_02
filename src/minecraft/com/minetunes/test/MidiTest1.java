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
package com.minetunes.test;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jfugue.Player;
import org.jfugue.elements.Note;

import com.sun.media.sound.SF2Instrument;
import com.sun.media.sound.SF2InstrumentRegion;
import com.sun.media.sound.SF2Layer;
import com.sun.media.sound.SF2LayerRegion;
import com.sun.media.sound.SF2Region;
import com.sun.media.sound.SF2Sample;
import com.sun.media.sound.SF2Soundbank;
import com.sun.media.sound.SoftSynthesizer;

/**
 * @author William
 * 
 */
public class MidiTest1 {

	/**
	 * @param args
	 * @throws MidiUnavailableException
	 * @throws UnsupportedAudioFileException
	 * @throws IOException 
	 * @throws InvalidMidiDataException 
	 */
	public static void main(String[] args) throws MidiUnavailableException,
			UnsupportedAudioFileException, IOException, InvalidMidiDataException {

		SoftSynthesizer synth = new SoftSynthesizer();
		//synth.
		synth.open();

//		System.out.println("1");
//		SF2Soundbank soundbank = new SF2Soundbank(new File(
//				"C:/timidity/sndfont/merlin_vienna.sf2"));
//		System.out.println("2");
//		System.out.println(soundbank.getName() + ":"
//				+ soundbank.getInstruments()[14].getName());
//		System.out.println(synth.isSoundbankSupported(soundbank));
//		// synth.loadAllInstruments(s);
//		// System.out.println ("3");
//		Instrument[] instruments = soundbank.getInstruments();
//
//		for (Instrument inst : instruments) {
//			System.out.println("Bank=" + inst.getPatch().getBank() + " Patch="
//					+ inst.getPatch().getProgram() + " Inst=" + inst);
//		}
//
//		System.out.println("Is supported: "
//				+ synth.isSoundbankSupported(soundbank));
//		System.out.println("Loaded inst: "
//				+ synth.loadAllInstruments(soundbank));

//		// // Add note block bass
//		File file = new File(
//				"C:/Users/William/Dropbox/mcp72pre-MCDitty0_9_6_01/src/minecraft/com/wikispaces/mcditty/sounds/harp.wav");
//		// Get AudioInputStream from given file.
//		AudioInputStream in = AudioSystem.getAudioInputStream(file);
//		AudioInputStream din = null;
//		AudioFormat baseFormat = null;
//		if (in != null) {
//			baseFormat = in.getFormat();
//			AudioFormat decodedFormat = new AudioFormat(
//					AudioFormat.Encoding.PCM_SIGNED,
//					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
//					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
//					false);
//			// Get AudioInputStream that will be decoded by underlying
//			// VorbisSPI
//			din = AudioSystem.getAudioInputStream(decodedFormat, in);
//		}
//		
//		AudioFileSoundbankReader r = new AudioFileSoundbankReader();
//		Soundbank sndbnk = r.getSoundbank(din);
//		synth.loadAllInstruments(sndbnk);

		/*
		 * Create new SoundFont2 soundbank
		 */
		SF2Soundbank sf2 = new SF2Soundbank();

		/*
		 * Select audio file.
		 */
		File audiofile = new File(
				"C:/Users/William/Dropbox/mcp72pre-MCDitty0_9_6_01/src/minecraft/com/wikispaces/mcditty/sounds/harp.wav");
		AudioInputStream audiosream = AudioSystem
				.getAudioInputStream(audiofile);

		/*
		 * Make sure the audio stream is in correct format for soundfonts e.g.16
		 * bit signed, little endian
		 */
		AudioFormat format = new AudioFormat(audiosream.getFormat()
				.getSampleRate(), 16, 1, true, false);
//		format = new AudioFormat(
//				AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
//				16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
//				baseFormat.getSampleRate(), false);
		AudioInputStream convaudiosream = AudioSystem.getAudioInputStream(
				format, audiosream);

		/*
		 * Read the content of the file into a byte array.
		 */
		System.out.println (convaudiosream.getFrameLength()+","+format.getFrameSize());
		int datalength = (int) convaudiosream.getFrameLength()
				* format.getFrameSize();
		byte[] data = new byte[datalength];
		convaudiosream.read(data, 0, data.length);
		audiosream.close();

		/*
		 * Create SoundFont2 sample.
		 */
		SF2Sample sample = new SF2Sample(sf2);
		sample.setName("Ding Sample");
		sample.setData(data);
		sample.setSampleRate((long) format.getSampleRate());
		sample.setOriginalPitch(Note.createNote("F#4").getValue());
		sf2.addResource(sample);

		/*
		 * Create SoundFont2 layer.
		 */
		SF2Layer layer = new SF2Layer(sf2);
		layer.setName("Ding Layer");
		sf2.addResource(layer);

		/*
		 * Create region for layer.
		 */
		SF2LayerRegion region = new SF2LayerRegion();
		region.putInteger(SF2Region.GENERATOR_RELEASEVOLENV, 12000);
		region.setSample(sample);
		layer.getRegions().add(region);

		/*
		 * Create SoundFont2 instrument.
		 */
		SF2Instrument ins = new SF2Instrument(sf2);
		ins.setName("Ding Instrument");
		ins.setPatch(new Patch(0, 22));
		sf2.addInstrument(ins);

		/*
		 * Create region for instrument.
		 */
		SF2InstrumentRegion insregion = new SF2InstrumentRegion();
		insregion.setLayer(layer);
		ins.getRegions().add(insregion);

		synth.loadInstrument(sf2.getInstruments()[0]);
		System.out.println(sf2.getInstruments()[0].getName());

		int c = 0;
		for (SF2Instrument i : sf2.getInstruments()) {
			System.out.println(i.getPatch() + ": " + i.getName() + ",");
		}

		System.out.println(".");

		
		Player p = new Player(synth);
		String maids = "T80 C4ia127d127 C4sa127d127 C4s C4s B3ia4 B3ia4 D4i G3qa127d127 E4ia127d127 E4sa127d127 E4sa127d127 E4i D4i D4i G4i G3qa0";
		String chords = "T100 Cmaj F#maj Fmaj Cmajwa127d127";
		
		//p.play(maids);
		p.playMidiDirectly(new File("C:/Users/William/AppData/Roaming/.minecraft/MCDitty/midi/redandblack1.mid"));
	}
}
