package edu.ca.ualberta.code.nuggets.utils;

/**
 * Utility class to perform string operations.
 */
public class StringUtils {

	public static String join(String[] array, String glue) {
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < array.length; i++) {
			result.append(array[i]);

			if (i < array.length - 1) {
				result.append(glue);
			}
		}

		return result.toString();
	}
}
