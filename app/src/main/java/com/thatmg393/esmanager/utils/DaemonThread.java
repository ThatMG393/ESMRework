package com.thatmg393.esmanager.utils;

import androidx.annotation.NonNull;
import java.lang.Thread;

public class DaemonThread implements Runnable {
	private final Logger LOG = new Logger("ESM/" + this.getClass().getName());

	private volatile boolean isRunning;
	
	private final Thread thread;
	private final Runnable runnable;
	
	public DaemonThread(@NonNull Runnable runnable) {
		this.runnable = runnable;
		this.thread = new Thread(this);
		
		thread.setDaemon(true);
	}

	@Override
	public void run() {
		while (isRunning && !thread.isInterrupted()) {
			runnable.run();
		}
		thread.interrupt();
		LOG.d("Shutting down -> " + thread);
		isRunning = false;
	}
	
	public synchronized void start() {
		if (isRunning) stop();
		
		thread.start();
		isRunning = true;
	}
	
	public synchronized void stop() {
		if (!isRunning) return;
		
		thread.interrupt();
		isRunning = false;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
}
