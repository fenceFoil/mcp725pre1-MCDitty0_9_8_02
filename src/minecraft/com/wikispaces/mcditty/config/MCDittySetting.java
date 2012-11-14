package com.wikispaces.mcditty.config;
///**
// * Copyright (c) 2012 William Karnavas 
// * All Rights Reserved
// */
//
///**
// * 
// * This file is part of MCDitty.
// * 
// * MCDitty is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the License, or (at your option) any
// * later version.
// * 
// * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
// * details.
// * 
// * You should have received a copy of the GNU Lesser General Public License
// * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package com.wikispaces.mcditty;
//
///**
// * Represents a saved property or value in the MCDitty config file.
// * 
// * Does not at this time perform any checking that valueClass and value are
// * compatible.
// * 
// */
//public class MCDittySetting {
//
//	private String description = "";
//	private String saveName = "";
//	private Object value = null;
//	private Class valueClass = null;
//
//	public MCDittySetting(String saveName, String description, Object value) {
//		setDescription(description);
//		setSaveName(saveName);
//		setValue(value);
//		valueClass = value.getClass();
//	}
//
//	public void setValue(Object value2) {
//		value = value2;
//	}
//
//	public void setSaveName(String saveName2) {
//		saveName = saveName2;
//	}
//
//	public void setDescription(String description2) {
//		description = description2;
//	}
//
//	public String getDescription() {
//		return description;
//	}
//
//	public String getSaveName() {
//		return saveName;
//	}
//
//	public String getValueAsString() {
//		if (value != null) {
//			if (value instanceof MCDittyColor) {
//				MCDittyColor color = (MCDittyColor) value;
//				StringBuilder buf = new StringBuilder();
//				for (float f : color.color) {
//					buf.append(Float.toString(f));
//					buf.append(":");
//				}
//				// Take off excess colon at end
//				buf.deleteCharAt(buf.length() - 1);
//				// return
//				return buf.toString();
//			} else {
//				return value.toString();
//			}
//		} else {
//			return "";
//		}
//	}
//
//	public String readValueFromString(String source) {
//		if (valueClass == MCDittyColor.class) {
//			try {
//				String[] values = source.split(":");
//				if (values.length == 4) {
//					for (int i = 0; i < 4; i++) {
//						((MCDittyColor) value).color[i] = Float
//								.parseFloat(values[i]);
//					}
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} else {
//			// TODO
//		}
//	}
//}
