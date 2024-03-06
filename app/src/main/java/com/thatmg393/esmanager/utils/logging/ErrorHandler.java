package com.thatmg393.esmanager.utils.logging;

import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.utils.io.FileUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ErrorHandler {
	public static final void writeError(Throwable err) {
		FileUtils.appendToFile(GlobalConstants.getInstance().getESMRootFolder() + "/errors.txt", getFullStacktrace(err));
	}
	
	public static final String getFullStacktrace(Throwable err) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		
		try {
			int loopTimes = 0;
			
			Throwable cause = err;
			while (cause != null && loopTimes <= 45) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
				loopTimes++;
			}
		
			printWriter.close();
			result.close();
		} catch (IOException e) { }
		
		return result.toString();
	}
}
