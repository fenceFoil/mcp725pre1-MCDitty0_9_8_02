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

/**
 * An effect that requires the current tempo before applying. The current tempo
 * for given time in a MusicString can only be discovered reliably after the
 * entire string has been parsed, so TempoEffects have to be added after
 * parsing: post-effects.
 * 
 */
public class TempoEffect extends RendererEffect {

	protected int startTempo = 120;

	public TempoEffect(Double endTime) {
		super(endTime);
		applyMethod = ApplyEffect.POST_TEMPO_RQD;
	}

	public int getStartTempo() {
		return startTempo;
	}

	public void setStartTempo(int startTempo) {
		this.startTempo = startTempo;
	}

}
