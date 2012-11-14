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
package com.wikispaces.mcditty.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.wikispaces.mcditty.bot.AnimatedValue;
import com.wikispaces.mcditty.pulpcore.animation.Easing;
import com.wikispaces.mcditty.pulpcore.animation.Fixed;

public class AnimatedValueTest1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final long startTime = (System.currentTimeMillis() + 3000) / 1;
		final AnimatedValue value = new AnimatedValue(5, 0);
		
		Fixed fixed = new Fixed(0);
		value.addAnimation(fixed);
		//fixed.animate(-3, 3, 5000);
		fixed.set(-3);
		fixed.animateTo(3, 3200, new Easing(Easing.ELASTIC_IN_OUT, 1));
	
		//Fixed fixed2 = new Fixed(0);
		//value.addAnimation(fixed2);
		//fixed2.animateTo(1, 3200, Easing.ELASTIC_OUT, 3200);
		
		//Fixed fixed3 = new Fixed(0);
		//value.addAnimation(fixed3);
		//fixed3.animateTo(2, 1600, Easing.REGULAR_IN, 2400);
		
		
		JFrame frame = new JFrame();
		frame.setLocationRelativeTo(null);
		frame.setSize(700, 100);

		final JProgressBar bar = new JProgressBar(0, 100000);

		frame.add(bar);

		frame.setVisible(true);
		
		Timer t = new Timer (20, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				value.update(System.currentTimeMillis() / 1 - startTime);
				System.out.println (value.getValue());
				bar.setValue((int) (value.getValue() * 10000));
				bar.repaint();
			}
		});
		t.start();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
