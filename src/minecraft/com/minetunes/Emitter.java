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
package com.minetunes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.minetunes.ditty.event.CreateEmitterEvent;
import com.minetunes.ditty.event.NoteStartEvent;
import com.minetunes.particle.BubbleParticleRequest;
import com.minetunes.particle.HeartParticleRequest;
import com.minetunes.particle.NoteParticleRequest;
import com.minetunes.particle.ParticleRequest;
import com.minetunes.signs.keywords.EmitterKeyword;

/**
 * Represents a single particle emitter. Can be created during a ditty, and
 * requests that particles be created.
 */
public class Emitter {
	private EmitterKeyword sourceKeyword;
	private int dittyID;
	private long timeCreated;

	private long lastParticleEmitTime = 0;
	private int particlesEmittedAtOnceSoFar = 0;
	private Point3D location;

	public static HashMap<String, String> particleHandleMap = new HashMap<String, String>();
	static {
		particleHandleMap.put("chaos", "hugeexplosion");
		particleHandleMap.put("dust", "largeexplode");
		particleHandleMap.put("spore", "townaura");
		particleHandleMap.put("crit", "crit");
		particleHandleMap.put("critmag", "magicCrit");
		particleHandleMap.put("smoke", "smoke");
		// Todo: specify colors? how?
//		particleHandleMap.put("t1", "mobSpell");
//		particleHandleMap.put("t2", "mobSpellAmbient");
//		particleHandleMap.put("t3", "spell");
//		particleHandleMap.put("t4", "instantSpell");
//		particleHandleMap.put("t5", "witchMagic");
		//particleHandleMap.put("portal", "portal");
		//particleHandleMap.put("runes", "enchantmenttable");
		particleHandleMap.put("poof", "explode");
		// it flies right now: particleHandleMap.put("fire", "flame");
		particleHandleMap.put("spark", "lava");
		particleHandleMap.put("splash", "splash");
		// footstep
		particleHandleMap.put("smoke2", "largesmoke");
		particleHandleMap.put("redfume", "reddust");
		// flies too much particleHandleMap.put("cloud", "cloud");
		particleHandleMap.put("snowhit", "snowballpoof");
		particleHandleMap.put("slime", "slime");
		// invsible particleHandleMap.put("lava", "driplava");
		//particleHandleMap.put("water", "dripwater");
		particleHandleMap.put("flake", "snowshovel");
		particleHandleMap.put("angry", "angryVillager");
		particleHandleMap.put("happy", "happyVillager");
		// Iconcrack, tilecrack
		particleHandleMap.put("mad", "villagerFace");
		particleHandleMap.put("face", "villagerFace");
	}

	public Emitter(CreateEmitterEvent sourceEvent) {
		sourceKeyword = sourceEvent.getEmitterKeyword();
		dittyID = sourceEvent.getDittyID();
		timeCreated = sourceEvent.getTime();
		location = sourceEvent.getEmitterLocation();
		location.y += sourceKeyword.getAbove();
	}

	/**
	 * Update the emitter. Returns all particles that it wants to emit in
	 * response to a note being sounded.
	 * 
	 * @param event
	 * @return
	 */
	public List<ParticleRequest> processNoteEvent(NoteStartEvent event) {
		LinkedList<ParticleRequest> requests = new LinkedList<ParticleRequest>();

		if (lastParticleEmitTime != event.getTime()) {
			// Time has advanced. Clear rate counters.
			particlesEmittedAtOnceSoFar = 0;
		}

		if (sourceKeyword.getVoices().contains(event.getVoice())) {
			// If voice is valid
			if ((sourceKeyword.getRate() < 0 || particlesEmittedAtOnceSoFar < sourceKeyword
					.getRate())
					&& sourceKeyword.getLife() > 0
					&& event.getTime() - timeCreated < (sourceKeyword
							.getTimeQuarterNotes() * 32)) {
				// Note above: multiplication in time remaining is to convert
				// from quarter notes to 128th notes
				// If still under the cap of particles per instant, and a rate
				// is in fact defined
				// Find the right kind
				// TODO: Make a request for multiple particles, not multiple
				// requests!

				// First, handle streams of particles
				String streamFreq = sourceKeyword.getStreamFreq();
				int numToStream = 1;
				int streamPeriodInPulses = 0;
				if (streamFreq != null) {
					// Need to stream detected.
					// Get period of each particle in pulses (period = 1/freq)
					int powerOfTwo = "whqistxo".indexOf(streamFreq);
					streamPeriodInPulses = (int) (128 / (int) Math.pow(2,
							powerOfTwo));

					// Get length of stream
					numToStream = (int) ((double) event.getNote()
							.getMillisDuration() / (double) streamPeriodInPulses);
				}

				for (int streamNum = 0; streamNum < numToStream; streamNum++) {
					int streamTimeOffset = streamNum * streamPeriodInPulses;
					// System.out.println
					// ("streamTimeOffset = "+streamTimeOffset);

					for (int i = 0; i < sourceKeyword.getParticleMultiplier(); i++) {
						ParticleRequest request = null;
						if (sourceKeyword.getType().equalsIgnoreCase("note")) {
							// Request a note particle
							double noteColor = 0;
							boolean randomColor = true;
							if (sourceKeyword.getColor() >= 0) {
								// Color is specified
								randomColor = false;
								noteColor = (float) sourceKeyword.getColor() / 24f;
							}
							request = new NoteParticleRequest(location,
									noteColor, randomColor);
						} else if (sourceKeyword.getType().equalsIgnoreCase(
								"heart")) {
							request = new HeartParticleRequest(location);
						} else if (sourceKeyword.getType().equalsIgnoreCase(
								"bubble")) {
							request = new BubbleParticleRequest(location);
						} else {
							String realParticleName = particleHandleMap
									.get(sourceKeyword.getType());
							if (realParticleName == null) {
								// System.out.println("UNKNOWN PARTICLE TYPE: "
								// + sourceKeyword.getType());
							} else {
								request = new ParticleRequest(location,
										realParticleName);
							}
						}

						if (request != null) {
							if (streamNum != 0) {
								// Must schedule to happen in the future
								request.setTime(event.getTime()
										+ streamTimeOffset);
								request.setDittyID(event.getDittyID());
							}
							requests.add(request);
						}
					}
				}

				// Decrement life for each particle (or batch, in the case of
				// multi)
				sourceKeyword.setLife(sourceKeyword.getLife() - 1);
			}
		}

		// Keep track of times and particle counts
		if (lastParticleEmitTime == event.getTime()) {
			particlesEmittedAtOnceSoFar++;
		} else {
			particlesEmittedAtOnceSoFar = 0;
		}
		lastParticleEmitTime = event.getTime();

		return requests;
	}

	/**
	 * The ditty that this emitter emits particles in response to.
	 * @return
	 */
	public int getDittyID() {
		return dittyID;
	}

	/**
	 * Set the ditty that this emitter emits particles in response to.
	 * @param dittyID
	 */
	public void setDittyID(int dittyID) {
		this.dittyID = dittyID;
	}

}
