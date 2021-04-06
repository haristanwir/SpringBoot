package com.esb.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ErrorHandling {
	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * Defines a custom format for the stack trace as String.
	 */
	public static String getCustomStackTrace(Throwable aThrowable, String msg) {
		// add the class name and any message passed to constructor
		final StringBuilder result = new StringBuilder(msg);
		result.append(aThrowable.toString());

		// add each element of the stack trace
		for (StackTraceElement element : aThrowable.getStackTrace()) {
			result.append(element);
		}
		return result.toString();
	}
}
