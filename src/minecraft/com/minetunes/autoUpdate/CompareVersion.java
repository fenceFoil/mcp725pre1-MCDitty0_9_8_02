package com.minetunes.autoUpdate;

import com.minetunes.config.MinetunesConfig;

/**
 * Code from:
 * http://ramakrsna.wordpress.com/2008/07/05/comparing-version-numbers/ <br>
 * A suite of methods for processing basic version numbers. Formats supported:
 * ##.##.##.##, ##.##.##, ##.##, ##<br>
 * <br>
 * No license given; code modified.
 */
public class CompareVersion {

	public final static int LESSER = -1; // versionA is lesser than versionB
	public final static int EQUALS = 0; // versionA equal to versionB
	public final static int GREATER = 1; // versionA is greater then versionB

	public static final int INVALID_ARGS = 0;

	public static int compareVersions(String versionA, String versionB) {
		// Check for valid version numbers
		if (!isVersionNumber(versionA) || !isVersionNumber(versionB)) {
			return INVALID_ARGS;
		}

		try {
			String[] a = versionA.split("\\.");
			String[] b = versionB.split("\\.");
			int i, j;
			int index = 0;
			while ((index < a.length) && (index < b.length)) {
				i = Integer.parseInt(a[index]);
				j = Integer.parseInt(b[index]);
				if (i > j) {
					return CompareVersion.GREATER;
				} else if (i < j) {
					return CompareVersion.LESSER;
				}
				index++;
			}
			if ((index < a.length) && (index == b.length)) {
				return CompareVersion.GREATER;
			} else if ((index == a.length) && (index < b.length)) {
				return CompareVersion.LESSER;
			} else {
				return CompareVersion.EQUALS;
			}
		} catch (NumberFormatException e) {
			// e.printStackTrace();
			return INVALID_ARGS;
		}
	}

	/**
	 * Proper syntax: One to four parts, separated by dots: each part can be
	 * numbers or a *
	 */
	public static boolean isVersionNumber(String part) {
		return part
				.matches("^(?:(\\d+)\\.)?(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\*|\\d+)$");
	}

	public static boolean isVersionNewerThanCurrent(String version) {
		int result = compareVersions(version, MinetunesConfig.CURRENT_VERSION);
		if (result == GREATER) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isVersionOlderThanCurrent(String version) {
		int result = compareVersions(version, MinetunesConfig.CURRENT_VERSION);
		if (result == LESSER) {
			return true;
		} else {
			return false;
		}
	}
}
