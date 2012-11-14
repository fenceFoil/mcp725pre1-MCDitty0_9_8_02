/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Timer;

/**
 * Methods to get the current Minecraft instance and other private Minecraft
 * fields and methods using reflection and good old-fashioned deduction.
 * Necessary because fields and methods cannot be easily accessed by reflection;
 * when reobfuscated, their names are reduced to junk, so you need to use logic
 * like "get the only boolean field in this class" instead of "get isLit", which
 * this class provides.
 */
public class GetMinecraft {
	private static Minecraft lastReturn = null;

	/**
	 * Gets the current Minecraft instance.
	 * 
	 * @return
	 */
	public static Minecraft instance() {
		if (lastReturn == null) {
			Minecraft mc = null;
			try {
				Field[] minecraftFields = Minecraft.class.getDeclaredFields();
				Field minecraftField = null;
				for (Field f : minecraftFields) {
					if (f.getType() == Minecraft.class) {
						minecraftField = f;
						break;
					}
				}
				minecraftField.setAccessible(true);
				mc = (Minecraft) minecraftField.get(null);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			lastReturn = mc;
			return mc;
		} else {
			return lastReturn;
		}
	}

	private static Timer lastTimer = null;

	/**
	 * Gets the current Minecraft Timer.
	 * 
	 * @return null if it cannot be gotten for some reason.
	 */
	public static Timer timer() {
		if (lastTimer == null) {
			Object timerObject = null;
			try {
				timerObject = getUniqueTypedFieldFromClass(Minecraft.class,
						Timer.class, instance());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (timerObject instanceof Timer) {
				lastTimer = (Timer) timerObject;
			}
		}

		return lastTimer;
	}

	/**
	 * Attempts to find a private field in class c of type fieldType in the
	 * instance classInstance and returns it. There must be only one field of
	 * type fieldType for this to work reliably. Can narrow search down to
	 * static fields only by making classInstance null.
	 * 
	 * @param c
	 * @param fieldType
	 * @param classInstance
	 *            null for static fields
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object getUniqueTypedFieldFromClass(Class c, Class fieldType,
			Object classInstance) throws IllegalArgumentException,
			IllegalAccessException {
		Field[] minecraftFields = c.getDeclaredFields();
		Field minecraftField = null;
		for (Field f : minecraftFields) {
			if (f.getType() == fieldType) {
				minecraftField = f;
				break;
			}
		}
		if (minecraftField == null) {
			return null;
		} else {
			minecraftField.setAccessible(true);
			return minecraftField.get(classInstance);
		}
	}

	/**
	 * Attempts to find a method in class c based on its parameters. There must
	 * be only one method with these parameters for this to work reliably.
	 * 
	 * @param c
	 * @param classInstance
	 * @param paramTypes
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Method getUniqueParameterMethodFromClass(Class c,
			Class... paramTypes) throws IllegalArgumentException,
			IllegalAccessException {
		Method[] allClassMethods = c.getDeclaredMethods();
		Method foundMethod = null;
		for (Method m : allClassMethods) {
			Class[] mParams = m.getParameterTypes();
			if (mParams.length == paramTypes.length) {
				// Method with same number of params found
				boolean foundMismatchingParam = false;
				for (int i = 0; i < mParams.length; i++) {
					if (!mParams[i].getName().equals(paramTypes[i].getName())) {
						foundMismatchingParam = true;
						break;
					}
				}
				if (foundMismatchingParam) {
					return null;
				} else {
					// Found the method to return!
					foundMethod = m;
					foundMethod.setAccessible(true);
					return foundMethod;
				}
			}
		}
		return null;
	}

	// public static void forceCheckForNewMinecraft() {
	// lastReturn = null;
	// }
}
