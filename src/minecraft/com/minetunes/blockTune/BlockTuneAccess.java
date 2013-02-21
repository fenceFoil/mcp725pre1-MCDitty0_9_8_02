/**
 * Copyright (c) 2012-2013 William Karnavas 
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
package com.minetunes.blockTune;

/**
 * Describes a an abstract BlockTune-type music source. An instance of this can
 * be attached to a BlockTunePlayer to play music.
 * 
 * Frame: A single moment in time: in normal BlockTunes, a single column of
 * blocks. FPM: Frames Per Minute. Meant to be similar to musical Beats Per
 * Minute, if every frame is considered 1 beat. FPS: Frames Per Second.
 */
public interface BlockTuneAccess {
	// Tell blocktuneplayer, don't have it ask you
	// public double getFPM();

	public int getFrameCount();

	public boolean isPaused();

	public boolean isLooping();

	/**
	 * Get the base volume, affecting all channels. This is the maximum volume a
	 * channel can be set to.
	 * 
	 * @return 0 to 1
	 */
	public double getMasterVolume();

	// /**
	// * Get the length of a given
	// * @param frameNum
	// * @return
	// */
	// public double getFrameNoteLength(int frameNum);

	public Frame getFrame(int frameNum);

	// public Frame[] getFrames(int len, int offset);
	
	public double getBeatsPerSecond();

	/**
	 * Called by BlockTunePlayer, signaling that a frame has just been played
	 * 
	 * @param frameNum
	 */
	public void onFramePlayed(Frame frame, int frameNum);
}
