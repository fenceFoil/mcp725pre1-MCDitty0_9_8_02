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
package com.minetunes.gui;

import net.minecraft.src.GuiButton;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;

/**
 * @author William
 * 
 */
public class GuiButtonTweenAccessor implements TweenAccessor<GuiButton> {

	public static final int TWEEN_TYPE_Y = 1;
	//public static final int TWEEN_TYPE_BASE_NOTE = 2;

	/*
	 * (non-Javadoc)
	 * 
	 * @see aurelienribon.tweenengine.TweenAccessor#getValues(java.lang.Object,
	 * int, float[])
	 */
	@Override
	public int getValues(GuiButton target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case 1:
			returnValues[0] = (float) target.yPosition;
			return 1;
//		case 2:
//			returnValues[0] = target.getScale().getBaseNote();
//			return 1;
		default:
			assert false;
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see aurelienribon.tweenengine.TweenAccessor#setValues(java.lang.Object,
	 * int, float[])
	 */
	@Override
	public void setValues(GuiButton target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case 1:
			target.yPosition = ((int) newValues[0]);
			break;
//		case 2:
//			target.getScale().setBaseNote((int) newValues[0]);
//			break;
		default:
			break;
		}
	}

}
