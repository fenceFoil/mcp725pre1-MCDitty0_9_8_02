/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Timer;

/**
 * Methods to get private Minecraft fields and methods using reflection and good
 * old-fashioned deduction. Necessary because fields and methods cannot be
 * easily accessed by reflection; when reobfuscated, their names are reduced to
 * junk, so you need to use logic like
 * "get the only boolean field in this class" instead of "get isLit".
 */
public class Finder {

	private static Timer lastTimer = null;

	/**
	 * Gets the current Minecraft Timer.
	 * 
	 * @return null if it cannot be gotten for some reason.
	 */
	public static Timer getMCTimer() {
		if (lastTimer == null) {
			Object timerObject = null;
			try {
				timerObject = getUniqueTypedFieldFromClass(Minecraft.class,
						Timer.class, Minecraft.getMinecraft());
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
	public static Object[] getAllUniqueTypedFieldsFromClass(Class c,
			Class fieldType, Object classInstance)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] minecraftFields = c.getDeclaredFields();
		LinkedList<Field> foundFields = new LinkedList<Field>();
		for (Field f : minecraftFields) {
			if (f.getType() == fieldType) {
				foundFields.add(f);
			}
		}
		if (foundFields.size() <= 0) {
			return null;
		} else {
			LinkedList<Object> returnValues = new LinkedList<Object>();
			for (Field f : foundFields) {
				f.setAccessible(true);
				returnValues.add(f.get(classInstance));
			}
			return returnValues.toArray();
		}
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
	public static boolean setUniqueTypedFieldFromClass(Class c,
			Class fieldType, Object classInstance, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] minecraftFields = c.getDeclaredFields();
		Field minecraftField = null;
		for (Field f : minecraftFields) {
			if (f.getType() == fieldType) {
				minecraftField = f;
				break;
			}
		}
		if (minecraftField == null) {
			return false;
		} else {
			minecraftField.setAccessible(true);
			minecraftField.set(classInstance, value);
			return true;
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
