package com.itsaky.utils.logsender;

import android.app.Application;
import android.util.Log;
import com.thatmg393.esmanager.utils.Logger;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.Intent;
import android.content.Context;

/**
 * LogSender starts a new logcat process using {@code ProcessBuilder}
 * The output is sent to AndroidIDE which is then parsed and shown
 * in the logcat.
 */
public class LogSender extends Thread {
	private static final Logger LOG = new Logger("AndroidIDE/LogSender");
    
	private static LogSender instance;
    private BufferedReader output;
    private Process process;
	private Application app;
	
	private static final String PACKAGE_NAME = "com.itsaky.androidide";
	private static final String APPEND_LOG = PACKAGE_NAME + ".logs.APPEND_LOG";
	private static final String EXTRA_LINE = "log_line";
	
	private Callback CALLBACK = new Callback() {
		@Override
		public void output(CharSequence c) {
			if(c != null && c.toString().trim().length() > 0) {
				Intent i = new Intent();
				i.setAction(APPEND_LOG);
				i.putExtra(EXTRA_LINE, c.toString());
				i.setPackage(PACKAGE_NAME);
				app.getApplicationContext().sendBroadcast(i);
			}
		}
	};
	
    private LogSender(Application app) {
		this.app = app;
        final String shell = "sh";
		final String dirPath = app.getApplicationContext().getFilesDir().getAbsolutePath();
        
        ProcessBuilder processBuilder = new ProcessBuilder(shell);
        processBuilder.directory(new File(dirPath));
        processBuilder.redirectErrorStream(true);
        try {
            this.process = processBuilder.start();
            this.output = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
			
            final String clrCmd = "logcat -c"; // Clears the previous logcat
            this.process.getOutputStream().write(clrCmd.concat("\n").getBytes());
            
            final String logCmd = "logcat -v threadtime";
			this.process.getOutputStream().write(logCmd.concat("\n").getBytes());
            
			this.process.getOutputStream().flush();
        } catch (Throwable th) { }
    }
	
	public static void startLogging(Application app) {
		if (instance == null) instance = new LogSender(app);
		if (instance.isAlive()) return; // If you try to start an already started thread, It will result in an IllegalThreadStateException
		instance.start();
        LOG.d("Logging started...");
	}
	
    @Override
    public void run() {
        while (!interrupted()) {
            try {
                String readLine = this.output.readLine();
                if (readLine == null) break;
                CALLBACK.output(readLine.concat("\n"));
            } catch (Throwable th) { }
        }
		
        try {
            this.output.close();
            this.process.getInputStream().close();
            this.process.getErrorStream().close();
            this.process.getOutputStream().close();
            this.process.destroy();
        } catch (Throwable th) { }
    }
	
	public interface Callback {
		public void output(CharSequence charSequence);
	}
	
	public static void dispose() {
		if (instance == null) return;
		
		if (instance.isAlive()) instance.interrupt();
		instance = null;
	}
}
