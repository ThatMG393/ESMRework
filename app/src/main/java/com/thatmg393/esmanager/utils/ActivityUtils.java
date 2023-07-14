package com.thatmg393.esmanager.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Looper;

import android.view.View;
import android.widget.PopupMenu;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

public class ActivityUtils {
	private static volatile ActivityUtils INSTANCE;
	
	public synchronized static ActivityUtils getInstance() {
		if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'ActivityUtils#initializeInstance(MainActivity)'"); }
		
		return INSTANCE;
	}
	
	public synchronized static ActivityUtils initializeInstance(@NonNull AppCompatActivity activity) {
		if (INSTANCE == null) INSTANCE = new ActivityUtils(activity);
		
		return INSTANCE;
	}
	
	private final AppCompatActivity activity;
	private ActivityUtils(AppCompatActivity activity) {
		if (INSTANCE != null) { throw new RuntimeException("Please use 'ActivityUtils#getInstance()'!"); }
		
		this.activity = activity;
	}
	
	private Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
	
	public ActivityResultLauncher<Intent> registerForActivityResult(@NonNull ActivityResultCallback<ActivityResult> arc) {
		return activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), arc);
	}
	
	public void bindService(final Intent serviceIntent, final ServiceConnection serviceCallback) {
		try {
			activity.bindService(serviceIntent, serviceCallback, Context.BIND_AUTO_CREATE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void unbindService(final ServiceConnection serviceCallback) {
		try {
			activity.unbindService(serviceCallback);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void runOnUIThread(Runnable toBeRun) {
		mainThread.postAtFrontOfQueue(toBeRun);
	}
	
	public void createNotificationChannel(String channelID, String channelName, int notificationImportance) {
		if (SDK_INT >= VERSION_CODES.O) {
			NotificationChannel serviceChannel = new NotificationChannel(
					channelID,
					channelName,
					notificationImportance
			);

			NotificationManager manager = activity.getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		} else {
			
		}
	}
	
	public PopupMenu showPopupMenuAt(@NonNull View anchor, @MenuRes int menuRes, @Nullable PopupMenu.OnMenuItemClickListener listener) {
		PopupMenu popupMenu = new PopupMenu(activity.getApplicationContext(), anchor);
		popupMenu.inflate(menuRes);
		popupMenu.setOnMenuItemClickListener(listener);
		popupMenu.show();
		
		return popupMenu;
	}

    public AppCompatActivity getRegisteredActivity() {
		return activity;
	}
	
	public static void dispose() {
		INSTANCE = null;
	}
}
