/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.bot;

import java.util.ArrayList;

import com.wikispaces.mcditty.pulpcore.animation.Fixed;
import com.wikispaces.mcditty.pulpcore.animation.Tween;

/**
 * Represents a single numerical value, which changes through time as directed
 * by various 'Animation's (taken from the PulpCore applet game engine,
 * com.wikispaces.mcditty.pulpcore.*). Animations may be added to the
 * AnimatedValue arbitrarily, and the results of all animations are all added
 * together in the current value of the AnimatedValue.
 * 
 */
public class AnimatedValue {

	private double startValue = 0;
	
	private double lowerLimit = Double.MIN_VALUE;
	private double upperLimit = Double.MAX_VALUE;
	
	private long lastTick = 0;
	
	private ArrayList<Fixed> animations = new ArrayList<Fixed>();
	
	public AnimatedValue (double startValue, long startTick) {
		setStartValue(startValue);
		lastTick = startTick;
	}
	
	public AnimatedValue (double startValue, long startTick, double lowerLimit, double upperLimit) {
		this(startValue, startTick);
		
		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);
	}
	
	public void addAnimation (Fixed a) {
		animations.add(a);
	}
	
	public void update (long currTick) {
		long delta = currTick - lastTick;
		
		for (Fixed f:animations) {
			f.update((int) delta);
		}
		
		lastTick = currTick;
	}
	
	public double getValue () {
		double currValue = startValue;
		for (Fixed f:animations) {
			currValue += f.get();
		}
		
		// Range check
		currValue = constrainToLimits(currValue);
		
		return currValue;
	}
	
	private double constrainToLimits (double in) {
		if (in < lowerLimit) {
			return lowerLimit;
		} else if (in > upperLimit) {
			return upperLimit;
		} else {
			return in;
		}
	}

	public double getStartValue() {
		return startValue;
	}

	public void setStartValue(double startValue) {
		this.startValue = startValue;
	}

	public double getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public double getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(double upperLimit) {
		this.upperLimit = upperLimit;
	}
	
}
