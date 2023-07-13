package com.thatmg393.esmanager.utils;

import android.util.Log;


public class Logger {
	private final String logTag;
	
	public Logger(String logTag) {
		this.logTag = logTag;
	}
	
	public void i(final String tx) {
		Log.i(logTag, tx);
	}
	
	public void w(final String tx) {
		Log.w(logTag, tx);	
	}
	
	public void e(final String tx) {
		Log.e(logTag, tx);
	}
	
	public void d(final String tx) {
		// if (!BuildConfig.DEBUG) return;
		Log.d(logTag, tx);
	}
}
