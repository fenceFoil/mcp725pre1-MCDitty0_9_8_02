/*
 * JFugue - API for Music Programming
 * Copyright (C) 2003-2008  David Koelle
 *
 * http://www.jfugue.org 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 */

package org.jfugue;

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

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

import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.DittyEventReadListener;
import com.minetunes.ditty.InstrumentEventReadListener;
import com.minetunes.ditty.LyricEventReadListener;
import com.minetunes.ditty.ParticleWorthyNoteReadListener;
import com.minetunes.ditty.TempoEventReadListener;
import com.minetunes.jfugue.rendererEffect.AccelerateEffect;
import com.minetunes.jfugue.rendererEffect.ApplyEffect;
import com.minetunes.jfugue.rendererEffect.OctavesEffect;
import com.minetunes.jfugue.rendererEffect.RendererEffect;
import com.minetunes.jfugue.rendererEffect.StaccatoEffect;
import com.minetunes.jfugue.rendererEffect.TempoEffect;
import com.minetunes.jfugue.rendererEffect.TimedJFugueElement;
import com.minetunes.jfugue.rendererEffect.TransposeEffect;
import com.minetunes.signs.SignTuneParser;

/**
 * This class takes a Pattern, and turns it into wonderful music.
 * 
 * <p>
 * Playing music is only one thing that can be done by rendering a pattern. You
 * could also create your own renderer that draws sheet music based on a
 * pattern. Or, you could create a graphical light show based on the musical
 * notes in the pattern.
 * </p>
 * 
 * <p>
 * This was named Renderer in previous versions of JFugue. The name has been
 * changed to differentiate it from other types of renderers.
 * </p>
 * 
 * <p>
 * Modified heavily by MCDitty.
 * </p>
 * 
 * @author David Koelle
 * @version 2.0
 * @version 3.0 - Renderer renamed to MidiRenderer
 */
public final class MidiRenderer extends ParserListenerAdapter {
	/**
	 * Keeps track of nitty gritty details of the midi being rendered, such as
	 * track times
	 */
	private MidiEventManager eventManager;

	/**
	 * Used for parallel notes, and going back to the time of the first note to
	 * add them.
	 */
	private double initialNoteTime = 0;

	private float sequenceTiming;

	private int resolution;

	/**
	 * MCDitty: Random listeners
	 */
	private LyricEventReadListener lyricEventReadListener = null;
	private TempoEventReadListener tempoEventReadListener = null;
	private DittyEventReadListener mcdittyEventReadListener = null;
	private ParticleWorthyNoteReadListener particleWorthyNoteReadListener = null;
	private InstrumentEventReadListener instrumentEventReadListener = null;

	/**
	 * Whether to force the notes in [index] instrument to be played at full
	 * volume.
	 */
	private boolean[] forceInstrumentFullAttackDecay = new boolean[128];

	/**
	 * The track where notes are currently being added
	 */
	private byte currentTrack = 0;

	/**
	 * The layer of the current track that notes are currently being added to
	 */
	private byte currentLayer = 0;

	/**
	 * Current instrument for each track
	 */
	private byte[] currentInstrument = new byte[16];

	/**
	 * MCDitty: used for sign highlighting
	 */
	private int currentSignID = 0;

	// /**
	// * Staccato settings for each track; 0 = off, 1-8 = x/8 note length, 9 = 1
	// * tick.
	// */
	// private int[] staccato = new int[16];
	//
	// /**
	// * How many ticks of staccato are left for each track; -1 means on
	// * indefinitely
	// */
	// private Double[] staccatoEnd = new Double[16];

	private LinkedList<RendererEffect>[] effects;

	/**
	 * Instantiates a Renderer
	 */
	public MidiRenderer(float sequenceTiming, int resolution) {
		reset(sequenceTiming, resolution);
		for (int i = 0; i < forceInstrumentFullAttackDecay.length; i++) {
			forceInstrumentFullAttackDecay[i] = false;
		}
	}

	/**
	 * Creates a new MidiEventManager. If this isn't called, events from
	 * multiple calls to render() will be added to the same eventManager, which
	 * means that the second time render() is called, it will contain music left
	 * over from the first time it was called. (This wasn't a problem with Java
	 * 1.4)
	 * 
	 * @since 3.0
	 */
	public void reset(float sequenceTiming, int resolution) {
		this.sequenceTiming = sequenceTiming;
		this.resolution = resolution;
		this.eventManager = new MidiEventManager(sequenceTiming, resolution);

		resetEffects();
	}

	/**
	 * Creates a new MidiEventManager using the sequenceTiming and resolution
	 * already used to create this MidiRenderer. If this isn't called, events
	 * from multiple calls to render() will be added to the same eventManager,
	 * which means that the second time render() is called, it will contain
	 * music left over from the first time it was called. (This wasn't a problem
	 * with Java 1.4)
	 * 
	 * @since 3.2
	 */
	public void reset() {
		this.eventManager = new MidiEventManager(this.sequenceTiming,
				this.resolution);

		resetEffects();
	}

	/**
	 * Clears any remaining RendererEffects. Also sets up the list for the first
	 * time if it has not yet been.
	 */
	private void resetEffects() {
		effects = (LinkedList<RendererEffect>[]) new LinkedList[16];
		for (int i = 0; i < effects.length; i++) {
			effects[i] = new LinkedList<RendererEffect>();
		}

		tempoChanges.clear();
	}

	/**
	 * Returns the last sequence generated by this renderer
	 */
	public Sequence getSequence() {
		// First, post-render any effects that need to be applied once whole
		// MusicString is known
		for (QueuedTempoEffect e : tempoEffectQueue) {
			eventManager.setCurrentTrack(e.track);
			eventManager.setCurrentLayer(e.layer);
			eventManager.setTrackTimerDec(e.trackTime);
			if (e.effect instanceof TempoEffect) {
				((TempoEffect) e.effect).setStartTempo(getTempoAt(e.trackTime));
			}

			LinkedList<RendererEffect> effect = new LinkedList<RendererEffect>();
			effect.add(e.effect);
			LinkedList<TimedJFugueElement> effectResult = applyEffects(effect);

			for (TimedJFugueElement element : effectResult) {
				eventManager.setTrackTimerDec(e.trackTime + element.time);
				if (element.element instanceof Tempo) {
					tempoEvent(new Tempo(((Tempo) element.element).getTempo()));
				}
			}
		}

		// Then return sequence
		Sequence sequence = this.eventManager.getSequence();

		return sequence;
	}

	// ParserListener methods
	// //////////////////////////

	public void voiceEvent(Voice voice) {
		// MCDitty: End current sign
		mcDittyEvent(new MCDittyEvent(SignTuneParser.SIGN_END_TOKEN + currentSignID));

		// Original code (the only line left!)
		this.eventManager.setCurrentTrack(voice.getVoice());

		// MCDITTY
		currentTrack = voice.getVoice();

		// Reset current sign location
		mcDittyEvent(new MCDittyEvent(SignTuneParser.SIGN_START_TOKEN
				+ currentSignID));
	}

	/**
	 * A listing of all tempo changes in a ditty, by time
	 */
	private TreeMap<Double, Integer> tempoChanges = new TreeMap<Double, Integer>();

	public int getTempoAt(double time) {
		SortedSet<Double> keys = new TreeSet<Double>(tempoChanges.keySet());
		if (keys.size() <= 0) {
			// The default tempo for JFugue
			return 120;
		} else if (keys.first() > time) {
			// Starts at default tempo
			return 120;
		}

		// Find the first tempo whose time is <= the requested time
		double closestFound = keys.first();
		for (Double d : keys) {
			if (d <= time) {
				closestFound = d;
			} else {
				break;
			}
		}

		// return the last tempo change before given time
		return tempoChanges.get(closestFound);
	}

	public void tempoEvent(Tempo tempo) {
		byte[] threeTempoBytes = TimeFactor.convertBPMToBytes(tempo.getTempo());

		// Note tempo change for later reference
		tempoChanges.put(eventManager.getTrackTimerDec(), tempo.getTempo());

		// Add event to track 0 at the current track's time
		// (Non-track 0 tempo changes upset the synth: it ignores them)
		// Note that there is no harm in doing this even when we're at track 0
		// already.
		// note the time we want the event at
		double currTrackTime = eventManager.getTrackTimerDec();
		// switch to track 0
		eventManager.setCurrentTrack((byte) 0);
		// note the track 0 time
		double oldTrack0Time = eventManager.getTrackTimerDec();
		// move track 0 to the desired time, and add the event
		eventManager.setTrackTimerDec(currTrackTime);
		this.eventManager.addMetaMessage(0x51, threeTempoBytes);
		// move track 0 back to where it was
		eventManager.setTrackTimerDec(oldTrack0Time);
		// switch back to the current track
		eventManager.setCurrentTrack(currentTrack);

		// // MCDitty
		// // Note: For some reason, only "Txxx" tokens in the first track (any
		// // layer) are counted when played back.
		// // Therefore, MCDitty must explicitly do the same thing that JFugue
		// does
		// // implicitly.
		// if (currentTrack == 0) {
		// if (tempoEventReadListener != null) {
		// long now = (long) eventManager.getTrackTimerDec();
		// tempoEventReadListener.tempoEventRead(tempo, now);
		// }
		// }
	}

	public void instrumentEvent(Instrument instrument) {
		this.eventManager.addEvent(ShortMessage.PROGRAM_CHANGE,
				instrument.getInstrument(), 0);

		// MCDitty: note instrument change
		currentInstrument[currentTrack] = instrument.getInstrument();
		// Relay to listeners
		if (instrumentEventReadListener != null) {
			instrumentEventReadListener.instrumentUsed(instrument
					.getInstrument());
		}
	}

	public void layerEvent(Layer layer) {
		// MCDitty: End current sign
		mcDittyEvent(new MCDittyEvent(SignTuneParser.SIGN_END_TOKEN + currentSignID));

		this.eventManager.setCurrentLayer(layer.getLayer());

		// MCDITTY
		currentLayer = layer.getLayer();

		// Reset current sign location
		mcDittyEvent(new MCDittyEvent(SignTuneParser.SIGN_START_TOKEN
				+ currentSignID));
	}

	public void timeEvent(Time time) {
		this.eventManager.setTrackTimerDec(time.getTime());
	}

	public void measureEvent(Measure measure) {
		// No MIDI is generated when a measure indicator is identified.
	}

	public void keySignatureEvent(KeySignature keySig) {
		this.eventManager.addMetaMessage(0x59, new byte[] { keySig.getKeySig(),
				keySig.getScale() });
	}

	public void systemExclusiveEvent(SystemExclusive systemExclusiveEvent) {
		this.eventManager.addSystemExclusiveEvent(systemExclusiveEvent
				.getBytes());
	}

	public void controllerEvent(Controller controller) {
		this.eventManager.addEvent(ShortMessage.CONTROL_CHANGE,
				controller.getIndex(), controller.getValue());
	}

	public void channelPressureEvent(ChannelPressure channelPressure) {
		this.eventManager.addEvent(ShortMessage.CHANNEL_PRESSURE,
				channelPressure.getPressure());
	}

	public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) {
		this.eventManager.addEvent(ShortMessage.POLY_PRESSURE,
				polyphonicPressure.getKey(), polyphonicPressure.getPressure());
	}

	public void pitchBendEvent(PitchBend pitchBend) {
		this.eventManager.addEvent(ShortMessage.PITCH_BEND,
				pitchBend.getBend()[0], pitchBend.getBend()[1]);
	}

	public void noteEvent(Note note) {
		// Remember the current track time, so we can flip back to it
		// if there are other notes to play in parallel
		this.initialNoteTime = this.eventManager.getTrackTimerDec();
		// long duration = note.getMillisDuration();
		double duration = note.getDecimalDuration() * (double) resolution;

		// If there is no duration, don't add this note to the event manager
		// TODO: This is a special case as of v4.0.3 that should be re-thought
		// if a new noteEvent callback is created in v5.0
		if (duration == 0) {
			return;
		}

		addNoteToMIDI(note, duration, true);
	}

	public void sequentialNoteEvent(Note note) {
		// long duration = note.getMillisDuration();
		double duration = note.getDecimalDuration() * (double) resolution;

		addNoteToMIDI(note, duration, false);
	}

	public void parallelNoteEvent(Note note) {
		// long duration = note.getMillisDuration();
		double duration = note.getDecimalDuration() * (double) resolution;

		// Go back to the note this is parallel to
		this.eventManager.setTrackTimerDec(this.initialNoteTime);

		addNoteToMIDI(note, duration, false);
	}

	private void emitParticleForNote(Note note) {
		long now = (long) eventManager.getTrackTimerDec();
		if (!note.isRest() && !note.isEndOfTie()
				&& particleWorthyNoteReadListener != null) {
			particleWorthyNoteReadListener.fireParticleWorthyNoteEvent(now,
					currentTrack, currentLayer, note, currentSignID);
		}
	}

	private void addNoteToMIDI(Note note, double duration, boolean setChordStart) {
		emitParticleForNote(note);

		// Add messages to the track
		if (note.isRest()) {
			this.eventManager.addRest(duration);
		} else {
			if (setChordStart) {
				initialNoteTime = eventManager.getTrackTimerDec();
			}

			byte attackVelocity = note.getAttackVelocity();
			byte decayVelocity = note.getDecayVelocity();
			if (forceInstrumentFullAttackDecay[currentInstrument[currentTrack]]) {
				attackVelocity = 127;
				decayVelocity = 127;
			}

			// Get the notes etc. that need to be added after applying all
			// active effects for this track to the note
			LinkedList<TimedJFugueElement> elements = applyEffects(
					effects[currentTrack], note);

			// Add those elements to the midi being rendered

			// Calculate the start and end times for the original note
			double noteStart = eventManager.getTrackTimerDec();
			double noteEnd = eventManager.getTrackTimerDec() + duration;

			// Add the events
			for (TimedJFugueElement e : elements) {
				eventManager.setTrackTimerDec(noteStart + e.time);

				// By type
				if (e.element instanceof Note) {
					eventManager.addNoteEvent(((Note) e.element).getValue(),
							attackVelocity, decayVelocity,
							((Note) e.element).getDecimalDuration()
									* (double) resolution,
							!((Note) e.element).isEndOfTie(),
							!((Note) e.element).isStartOfTie());
				}
			}

			// Set the track timer to the end of the original note
			eventManager.setTrackTimerDec(noteEnd);
		}
	}

	/**
	 * Applies renderer effects to a note, returning the result. Example: a note
	 * is hit on track 0. Call this with the note and the effects currently on
	 * track 0, and this will run it through all the track's effects and return
	 * the resulting note(s) and other elements.
	 * 
	 * Note that for MUTEX or DUAL_MUTEX effects, this method assumes that there
	 * is only one (or two for DUAL) effects in the list: it will not obey the
	 * mutex command properly if the effects list has an incorrect number of the
	 * mutex-ed effect.
	 * 
	 * @param note
	 *            all should start at the same time to be handled correctly
	 * @param effects
	 * @return
	 */
	private LinkedList<TimedJFugueElement> applyEffects(
			LinkedList<RendererEffect> effects, Note... note) {

		// Create a set of effects to store the resusts of the effects in
		LinkedList<TimedJFugueElement> elements = new LinkedList<TimedJFugueElement>();

		// Add the original note to begin
		for (Note n : note) {
			elements.add(new TimedJFugueElement(n, 0));
		}

		// Apply effects to the resulting elements
		for (RendererEffect e : effects) {
			// If effect is infinite or a finite and still in effect
			if (e.getEndless()
					|| (!e.getEndless() && eventManager.getTrackTimerDec() < e
							.getEnd())) {
				if (e.getApplyMethod() == ApplyEffect.DUAL_MUTEX_FINITE_INFINITE) {
					if (!e.getEndless()) {
						// Apply finite one for sure
						e.apply(elements);
					} else {
						// only apply infinite ones if there is not finite one
						if (!containsMatchingEffects(effects, e.getClass(),
								true)) {
							// No finite one; apply
							e.apply(elements);
						} // Otherwise don't
					}
				} else {
					e.apply(elements);
				}
			}
		}

		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfugue.ParserListenerAdapter#mcDittyEvent(org.jfugue.parsers.MCDittyEvent
	 * )
	 */
	@Override
	public void mcDittyEvent(MCDittyEvent event) {
		if (MinetunesConfig.DEBUG) {
			System.out.println("MidiRenderer: MCDittyEvent hit: "
					+ event.getVerifyString());
		}

		// Actually do something
		String token = event.getToken();
		if (token.equalsIgnoreCase(SignTuneParser.SYNC_VOICES_TOKEN)) {
			eventManager.alignChannelTimes();
		} else if (token.toLowerCase().startsWith(
				SignTuneParser.SYNC_WITH_TOKEN.toLowerCase())) {
			// Parse token
			String arguments = token.substring(SignTuneParser.SYNC_WITH_TOKEN
					.length());

			// Get voice
			String voiceNum = arguments.substring(1, 3);
			int voice = 0;
			if (voiceNum.matches("\\d\\d")) {
				voice = Integer.parseInt(voiceNum);
			} else if (voiceNum.substring(0, 1).matches("\\d")) {
				voice = Integer.parseInt(voiceNum.substring(0, 1));
			} else {
				// !?!?!
				System.err.println("Bad SyncWith token (invalid voice): "
						+ token);
			}

			// Get Layer
			arguments = arguments.substring(arguments.lastIndexOf("L") + 1);
			int layer = 0;
			if (arguments.matches("\\d\\d")) {
				layer = Integer.parseInt(arguments);
			} else if (arguments.matches("\\d")) {
				layer = Integer.parseInt(arguments);
			} else {
				// !?!?!
				if (arguments.equalsIgnoreCase("u")) {
					// THIS IS A SPECIAL CASE.
					// Sync with longest layer in that voice
					layer = -1000;
				} else {
					System.err.println("Bad SyncWith token (invalid layer): "
							+ token);
				}
			}
			eventManager.syncCurrVoiceAndLayerWith(voice, layer);
		} else if (token.toLowerCase().startsWith(
				SignTuneParser.SIGN_START_TOKEN.toLowerCase())) {
			// Note current sign id
			currentSignID = Integer.parseInt(token.substring(2));
		} else if (SignTuneParser.isNoteEffectToken(token)) {
			// Get useful info from token
			boolean isOffToken = SignTuneParser.getNoteEffectTokenOff(token);
			String type = SignTuneParser.getNoteEffectTokenType(token).toLowerCase();
			String[] args = SignTuneParser.getNoteEffectTokenArgs(token);

			// Execute according to type

			// Effect to add
			RendererEffect effect = null;

			if (type.equals(SignTuneParser.NOTE_EFFECT_STACCATO)) {
				if (isOffToken) {
					// Turn off all staccato effects for curr track
					removeAllEffects(currentTrack, new StaccatoEffect(0d, 0));
				} else {
					// Turn on staccato for current track
					int eighths = Integer.parseInt(args[0]);
					double duration = Double.parseDouble(args[1]);

					// Create the staccato event to add to the current track
					// from the
					// token's info
					Double endTime = null;
					if (duration != -1) {
						// There is a finite end to the staccato
						endTime = eventManager.getTrackTimerDec() + duration;
					}

					effect = new StaccatoEffect(endTime, eighths);
				}
			} else if (type.equals(SignTuneParser.NOTE_EFFECT_TRANSPOSE)) {
				if (isOffToken) {
					// Turn off all transposition on current track
					removeAllEffects(currentTrack, new TransposeEffect(0d, 0));
				} else {
					// Turn on transpose for current track
					int tones = Integer.parseInt(args[0]);
					double duration = Double.parseDouble(args[1]);

					// Create the event to add to the current track from the
					// token's info
					Double endTime = null;
					if (duration != -1) {
						// There is a finite end to the staccato
						endTime = eventManager.getTrackTimerDec() + duration;
					}

					effect = new TransposeEffect(endTime, tones);
				}
			} else if (type.equals(SignTuneParser.NOTE_EFFECT_ACCELERATE)) {
				// Create an acceleration in the current track
				int bpm = Integer.parseInt(args[0]);
				double duration = Double.parseDouble(args[1]);

				// Create the event to add to the current track from the
				// token's info
				Double endTime = eventManager.getTrackTimerDec() + duration;

				effect = new AccelerateEffect(endTime, bpm, duration);
			} else if (type.equals(SignTuneParser.NOTE_EFFECT_OCTAVES)) {
				// Read the affected octaves
				LinkedList<Integer> octaves = new LinkedList<Integer>();
				for (String s : args) {
					octaves.add(Integer.parseInt(s));
				}

				if (isOffToken) {
					// Turn off signaled octaves
					if (octaves.size() <= 0) {
						// None specified: remove all
						removeAllEffects(currentTrack, new OctavesEffect(null,
								new LinkedList<Integer>()));
					} else {
						// Remove specified if possible
						OctavesEffect octavesEffect = (OctavesEffect) getFirstEffectOfType(
								currentTrack, new OctavesEffect(null,
										new LinkedList<Integer>()));
						if (octavesEffect != null) {
							octavesEffect.getOctaves().removeAll(octaves);
						}
					}
				} else {
					// Turn on signaled octaves
					OctavesEffect octavesEffect = new OctavesEffect(null,
							octaves);

					// Signal to add this effect
					effect = octavesEffect;
				}
			}

			// Add effect according to type
			if (effect != null) {
				addEffect(currentTrack, effect);
			}
		}

		// Report to listsners
		if (mcdittyEventReadListener != null) {
			long now = (long) eventManager.getTrackTimerDec();
			if (MinetunesConfig.DEBUG) {
				System.out.println("mcDittyEvent: time=" + now);
			}
			mcdittyEventReadListener.mcDittyEventRead(event, now, currentTrack,
					currentLayer);
		}
	}

	private LinkedList<QueuedTempoEffect> tempoEffectQueue = new LinkedList<QueuedTempoEffect>();

	/**
	 * Adds an effect to the given track.
	 * 
	 * @param track
	 * @param effect
	 */
	private void addEffect(byte track, RendererEffect effect) {
		if (track < 0 || track >= 16) {
			return;
		}

		LinkedList<RendererEffect> trackEffects = effects[track];

		// Handle other effects of the same type first, based on the type of
		// effect
		if (effect.getApplyMethod() == ApplyEffect.MUTEX) {
			removeAllEffects(track, effect);
			// Add the new effect
			trackEffects.add(effect);
		} else if (effect.getApplyMethod() == ApplyEffect.DUAL_MUTEX_FINITE_INFINITE) {
			removeMatchingEffects(track, effect, effect.getEndless());
			// Add the new effect
			trackEffects.add(effect);
		} else if (effect.getApplyMethod() == ApplyEffect.STACK) {
			// No need to change other effects -- they stack, after all!
			// Add the new effect
			trackEffects.add(effect);
		} else if (effect.getApplyMethod() == ApplyEffect.COMBINE) {
			// Combine with any existing effects, if possible
			RendererEffect combineEffect = getFirstEffectOfType(currentTrack,
					effect);
			if (combineEffect == null) {
				// Just add as the first combine effect
				trackEffects.add(effect);
			} else {
				// Mix two effects together: don't add new effect to track
				// effects
				combineEffect.combineWith(effect);
			}
			// } else if (effect.getApplyMethod() == ApplyEffect.IMMEDIATE) {
			// // Apply effect to current track
			// LinkedList<RendererEffect> effectInAList = new
			// LinkedList<RendererEffect>();
			// effectInAList.add(effect);
			// applyEffects(effectInAList);
			// /\ this line does nothing by itself
		} else if (effect.getApplyMethod() == ApplyEffect.POST_TEMPO_RQD) {
			// Add to queue to process after all tempos are known
			tempoEffectQueue.add(new QueuedTempoEffect(eventManager
					.getTrackTimerDec(), currentTrack, currentLayer, effect));
		}

	}

	private class QueuedTempoEffect {
		public QueuedTempoEffect(double trackTime, byte track, byte layer,
				RendererEffect effect) {
			this.trackTime = trackTime;
			this.track = track;
			this.layer = layer;
			this.effect = effect;
		}

		public double trackTime;
		public byte track;
		public byte layer;
		public RendererEffect effect;
	}

	private RendererEffect getFirstEffectOfType(byte track,
			RendererEffect effectOfType) {
		if (track < 0 || track >= 16) {
			return null;
		}

		// System.out.println("getFirstEffectOfType called on valid track.");
		LinkedList<RendererEffect> trackEffects = effects[track];
		for (int i = 0; i < trackEffects.size(); i++) {
			// System.out.println("Checking " + trackEffects.get(i).toString());
			RendererEffect eff = trackEffects.get(i);
			if (eff.getClass().isInstance(effectOfType)) {
				return eff;
			}
		}

		// If loop fell through, return none found
		return null;
	}

	/**
	 * Removes all instances of a given class from the effects list of a given
	 * track, if they are either infinite or finite in length (as specified in
	 * finiteLength)
	 * 
	 * @param currentTrack2
	 * @param class1
	 */
	private void removeMatchingEffects(byte track,
			RendererEffect objectOfTypeToRemove, boolean isEndless) {
		if (track < 0 || track >= 16) {
			return;
		}

		// System.out.println ("RemoveMatchingEffects called on valid track.");
		LinkedList<RendererEffect> trackEffects = effects[track];
		for (int i = 0; i < trackEffects.size(); i++) {
			// System.out.println ("Checking "+trackEffects.get(i).toString());
			RendererEffect eff = trackEffects.get(i);
			if (eff.getClass().isInstance(objectOfTypeToRemove)) {
				if (eff.getEndless() == isEndless) {
					// System.out.println
					// ("Removing effect. Is endless? "+eff.getEndless());
					trackEffects.remove(i);
					i--;
				}
			}
		}
	}

	private boolean containsMatchingEffects(LinkedList<RendererEffect> list,
			Class<? extends RendererEffect> type, boolean finiteLength) {
		for (int i = 0; i < list.size(); i++) {
			if (type.getClass().isInstance(list.get(i))) {
				RendererEffect eff = list.get(i);
				if (eff.getEndless() == finiteLength) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes all instances of a given class from the effects list of a given
	 * track
	 * 
	 * @param currentTrack2
	 * @param class1
	 */
	private void removeAllEffects(byte track,
			RendererEffect objectOfTypeToRemove) {
		if (track < 0 || track >= 16) {
			return;
		}

		LinkedList<RendererEffect> trackEffects = effects[track];
		for (int i = 0; i < trackEffects.size(); i++) {
			RendererEffect eff = trackEffects.get(i);
			if (eff.getClass().isInstance(objectOfTypeToRemove)) {
				trackEffects.remove(i);
				i--;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfugue.ParserListenerAdapter#lyricEvent(org.jfugue.elements.Lyric)
	 */
	/**
	 * MCDitty: obviously, since lyric events are added by MCDitty. This
	 * forwards the event onwards to something else.
	 */
	@Override
	public void lyricEvent(Lyric event) {
		if (MinetunesConfig.DEBUG) {
			System.out.println("MidiRenderer: lyricEvent hit: "
					+ event.getVerifyString());
			System.out.println(resolution + ":" + sequenceTiming + ":"
					+ initialNoteTime);
		}
		if (lyricEventReadListener != null) {
			// long now = channelTimesMS[currentTrack];
			long now = (long) eventManager.getTrackTimerDec();
			if (MinetunesConfig.DEBUG) {
				System.out.println("lyricEvent: time=" + now);
			}
			lyricEventReadListener.lyricEventRead(event, now);
		}
	}

	/**
	 * MCDitty
	 * 
	 * @return the lyricEventReadListener
	 */
	public LyricEventReadListener getLyricEventReadListener() {
		return lyricEventReadListener;
	}

	/**
	 * MCDitty
	 * 
	 * @param lyricEventReadListener
	 *            the lyricEventReadListener to set
	 */
	public void setLyricEventReadListener(
			LyricEventReadListener lyricEventReadListener) {
		this.lyricEventReadListener = lyricEventReadListener;
	}

	/**
	 * MCDitty
	 * 
	 * @return the tempoEventReadListener
	 */
	public TempoEventReadListener getTempoEventReadListener() {
		return tempoEventReadListener;
	}

	/**
	 * MCDitty
	 * 
	 * @param tempoEventReadListener
	 *            the tempoEventReadListener to set
	 */
	public void setTempoEventReadListener(
			TempoEventReadListener tempoEventReadListener) {
		this.tempoEventReadListener = tempoEventReadListener;
	}

	/**
	 * @return the mcdittyEventReadListener
	 */
	public DittyEventReadListener getMcdittyEventReadListener() {
		return mcdittyEventReadListener;
	}

	/**
	 * @param mcdittyEventReadListener
	 *            the mcdittyEventReadListener to set
	 */
	public void setMcdittyEventReadListener(
			DittyEventReadListener mcdittyEventReadListener) {
		this.mcdittyEventReadListener = mcdittyEventReadListener;
	}

	public ParticleWorthyNoteReadListener getParticleWorthyNoteReadListener() {
		return particleWorthyNoteReadListener;
	}

	public void setParticleWorthyNoteReadListener(
			ParticleWorthyNoteReadListener particleWorthyNoteReadListener) {
		this.particleWorthyNoteReadListener = particleWorthyNoteReadListener;
	}

	public void setForceFullAttackDecay(int instrument, boolean force) {
		forceInstrumentFullAttackDecay[instrument] = force;
	}

	public InstrumentEventReadListener getInstrumentEventReadListener() {
		return instrumentEventReadListener;
	}

	public void setInstrumentEventReadListener(
			InstrumentEventReadListener instrumentEventReadListener) {
		this.instrumentEventReadListener = instrumentEventReadListener;
	}

}
