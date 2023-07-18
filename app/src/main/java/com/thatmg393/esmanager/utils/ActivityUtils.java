package com.thatmg393.esmanager.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.core.util.Pair;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ActivityUtils {
	private static volatile ActivityUtils INSTANCE;
	
	public synchronized static ActivityUtils getInstance() {
		if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'ActivityUtils#initializeInstance()'"); }
		
		return INSTANCE;
	}
	
	public synchronized static ActivityUtils initializeInstance() {
		if (INSTANCE == null) INSTANCE = new ActivityUtils();
		
		return INSTANCE;
	}
	
	private AppCompatActivity registeredActivity;
	private ActivityUtils() {
		if (INSTANCE != null) { throw new RuntimeException("Please use 'ActivityUtils#getInstance()'!"); }
	}
	
	private Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
	
	public ActivityResultLauncher<Intent> registerForActivityResult(@NonNull ActivityResultCallback<ActivityResult> arc) {
		return getRegisteredActivity().registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), arc);
	}
	
	public void bindService(final Intent serviceIntent, final ServiceConnection serviceCallback) {
		try {
			getRegisteredActivity().bindService(serviceIntent, serviceCallback, Context.BIND_AUTO_CREATE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void unbindService(final ServiceConnection serviceCallback) {
		try {
			getRegisteredActivity().unbindService(serviceCallback);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		}
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

			NotificationManager manager = getRegisteredActivity().getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		}
	}
	
	public PopupMenu showPopupMenuAt(@NonNull View anchor, @MenuRes int menuRes, @Nullable PopupMenu.OnMenuItemClickListener listener) {
		PopupMenu popupMenu = new PopupMenu(getRegisteredActivity(), anchor);
		popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(listener);
		popupMenu.show();
		
		return popupMenu;
	}
	
	public AlertDialog createAlertDialog(
		@NonNull String title,
		@Nullable View view,
		@NonNull Pair<String, DialogInterface.OnClickListener> negativeButton,
		@NonNull Pair<String, DialogInterface.OnClickListener> positiveButton
	) {
		return new MaterialAlertDialogBuilder(getRegisteredActivity())
			.setTitle(title)
			.setView(view)
			.setNegativeButton(negativeButton.first, negativeButton.second)
			.setPositiveButton(positiveButton.first, positiveButton.second)
			.create();
	}
	
	public boolean isUserUsingNavigationBar() {
		int id = getRegisteredActivity().getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			return getRegisteredActivity().getResources().getBoolean(id);
		} else {
			boolean hasMenuKey = ViewConfiguration.get(getRegisteredActivity()).hasPermanentMenuKey();
        	boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        	return !hasMenuKey && !hasBackKey;
    	}
	}

    public AppCompatActivity getRegisteredActivity() {
		return registeredActivity;
	}
	
	public void registerActivity(AppCompatActivity newActivity) {
		this.registeredActivity = newActivity;
	}
	
	public static void dispose() {
		INSTANCE = null;
	}
}
