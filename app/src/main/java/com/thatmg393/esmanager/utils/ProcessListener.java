package com.thatmg393.esmanager.utils;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Binder;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import com.thatmg393.esmanager.interfaces.IOnProcessListener;

import java.util.HashMap;

public class ProcessListener implements ServiceConnection {
	public static int ACTIVITY_DESTROYED = 23; /* It's somehow hidden... for me. */
	private static volatile ProcessListener INSTANCE;

	public static synchronized ProcessListener getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("Initialize first, use 'ProcessListener#initializeInstance(AppCompatActivity)'");
		}

		return INSTANCE;
	}

	public static synchronized ProcessListener initializeInstance(@NonNull AppCompatActivity context) {
		if (INSTANCE == null) INSTANCE = new ProcessListener(context);

		return INSTANCE;
	}

	private final AppCompatActivity context;
	private final Intent listenerServiceIntent;

	private ProcessListener(AppCompatActivity context) {
		if (INSTANCE != null) {
			throw new RuntimeException("Please use 'ProcessListener#getInstance()'!");
		}

		this.context = context;
		this.listenerServiceIntent = new Intent(context, ProcessListenerService.class);
		startService();
	}

	public void startListening(String packageName, IOnProcessListener processListener, boolean stopOnAppStop) {
		if (listenerService == null) return;
		listenerService.startListeningThread(packageName, processListener, stopOnAppStop);
	}

	public void startListening(String packageName, IOnProcessListener processListener) {
		startListening(packageName, processListener, true);
	}

	public void stopListening(String packageName) {
		if (listenerService == null) return;
		listenerService.stopListeningThread(packageName);
	}

	public void startService() {
		context.startForegroundService(listenerServiceIntent);
	}

	public void stopService() {
		context.stopService(listenerServiceIntent);
	}

	private ProcessListenerService listenerService;

	@Override
	public void onServiceConnected(ComponentName cn, IBinder binder) {
		listenerService = ((ProcessListenerService.ProcessListenerBinder) binder).getInstance();
	}

	@Override
	public void onServiceDisconnected(ComponentName cn) {
		listenerService = null;
	}
	
	private class ProcessListenerService extends Service {
		private final HashMap<String, ProcessListenerThread> threads = new HashMap<String, ProcessListenerThread>();
		private final ProcessListenerBinder plBinder = new ProcessListenerBinder();

		public void startListeningThread(
			String packageToListenTo,
			IOnProcessListener processListenerCallback,
			boolean stopOnAppStop
		) {
			ProcessListenerThread plThread = new ProcessListenerThread(packageToListenTo, processListenerCallback, stopOnAppStop);
			threads.put(packageToListenTo, plThread);

			plThread.start();
		}

		public void stopListeningThread(String packageThatItListensTo) {
			threads.get(packageThatItListensTo).interrupt();
		}

		@Override
		public IBinder onBind(Intent smth) {
			return plBinder;
		}

		@Override
		public void onDestroy() {
			threads.values().forEach(Thread::interrupt);
			threads.clear();
		}

		private final boolean isCurrentEventEqualTo(int currentEvent, int activityCode) {
			if (SDK_INT < VERSION_CODES.R) {
				switch (currentEvent) {
					case UsageEvents.Event.ACTIVITY_PAUSED:
						return currentEvent == UsageEvents.Event.MOVE_TO_BACKGROUND;
					case UsageEvents.Event.ACTIVITY_RESUMED:
						return currentEvent == UsageEvents.Event.MOVE_TO_FOREGROUND;
					default:
						break;
				}
			}

			return currentEvent == activityCode;
		}

		private final class ProcessListenerThread extends Thread {
			private final String processName;
			private final IOnProcessListener callback;
			private final boolean stopOnAppStop;

			private ProcessListenerThread(
				final String processName,
				final IOnProcessListener callback,
				final boolean stopOnAppStop
			) {
				this.processName = processName;
				this.callback = callback;
				this.stopOnAppStop = stopOnAppStop;
			}

			@Override
			public void run() {
				super.run();
				
				// TODO: Implement functionality
			}
		}

		private class ProcessListenerBinder extends Binder {
			public ProcessListenerService getInstance() {
				return ProcessListenerService.this;
			}
		}
	}
}
