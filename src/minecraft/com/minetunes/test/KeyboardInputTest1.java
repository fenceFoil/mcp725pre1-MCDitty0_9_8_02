package com.minetunes.test;

import javax.sound.midi.MidiUnavailableException;

import org.jfugue.DeviceThatWillTransmitMidi;
import org.jfugue.ParserListener;
import org.jfugue.PatternInterface;
import org.jfugue.Player;
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

public class KeyboardInputTest1 {

	/**
	 * @param args
	 * @throws MidiUnavailableException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws MidiUnavailableException, InterruptedException {
		DeviceThatWillTransmitMidi piano = new DeviceThatWillTransmitMidi();
		piano.startListening();
		piano.addParserListener(new ParserListener() {
			
			@Override
			public void voiceEvent(Voice voice) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void timeEvent(Time time) {
				System.out.println (time.getMusicString());
			}
			
			@Override
			public void tempoEvent(Tempo tempo) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void systemExclusiveEvent(SystemExclusive sysex) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void sequentialNoteEvent(Note note) {
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
			public void parallelNoteEvent(Note note) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void noteEvent(Note note) {
				System.out.println (note.getMusicString());
				
			}
			
			@Override
			public void measureEvent(Measure measure) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mcDittyEvent(MCDittyEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void lyricEvent(Lyric event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void layerEvent(Layer layer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keySignatureEvent(KeySignature keySig) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void instrumentEvent(Instrument instrument) {
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
		});
		Thread.sleep(4000);
		piano.stopListening();
		piano.close();
		
		PatternInterface p = piano.getPatternFromListening();
		System.out.println ("Final: "+p.getMusicString());
		Player player = new Player();
		player.play(p);
		player.close();
	}

}
