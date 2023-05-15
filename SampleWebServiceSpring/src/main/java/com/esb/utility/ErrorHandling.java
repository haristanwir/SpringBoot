package com.esb.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ErrorHandling {
	public static String getMessage(Throwable aThrowable) {
		return aThrowable.getClass().getName() + " " + aThrowable.getMessage();
	}

	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}
}
