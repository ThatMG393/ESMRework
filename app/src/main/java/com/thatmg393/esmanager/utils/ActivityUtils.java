package com.thatmg393.esmanager.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Looper;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;

import com.thatmg393.esmanager.MainActivity;

public class ActivityUtils {
	private static volatile ActivityUtils INSTANCE;
	
	public synchronized static ActivityUtils getInstance() {
		if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'ActivityUtils#initializeInstance(MainActivity)'"); }
		
		return INSTANCE;
	}
	
	public synchronized static ActivityUtils initializeInstance(@NonNull MainActivity context) {
		if (INSTANCE == null) INSTANCE = new ActivityUtils(context);
		
		return INSTANCE;
	}
	
	private final MainActivity context;
	private ActivityUtils(MainActivity context) {
		if (INSTANCE != null) { throw new RuntimeException("Please use 'ActivityUtils#getInstance()'!"); }
		
		this.context = context;
	}
	
	private Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
	
	public ActivityResultLauncher<Intent> createNewARLI(@NonNull ActivityResultCallback<ActivityResult> arc) {
		return context.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), arc);
	}
	
	public void bindService(final Intent serviceIntent, final ServiceConnection serviceCallback) {
		context.bindService(serviceIntent, serviceCallback, Context.BIND_AUTO_CREATE);
	}
	
	public void unbindService(final ServiceConnection serviceCallback) {
		context.unbindService(serviceCallback);
	}
	
	public void runOnUIThread(Runnable toBeRun) {
		mainThread.post(toBeRun);
	}
	
	public void createNotificationChannel(String channelID, String channelName, int notificationImportance) {
		if (SDK_INT >= VERSION_CODES.O) {
			NotificationChannel serviceChannel = new NotificationChannel(
					channelID,
					channelName,
					notificationImportance
			);

			NotificationManager manager = context.getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		}
	}
	
	public MainActivity getMainActivityInstance() {
		return context;
	}
	
	public static void dispose() {
		INSTANCE = null;
	}
}
