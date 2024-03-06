package com.thatmg393.esmanager.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.PopupMenu;
import android.widget.Toast;

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

import com.thatmg393.esmanager.interfaces.IUIThreadTask;
import java.util.Locale;
import java.util.function.Supplier;

public class ActivityUtils {
	private static volatile ActivityUtils INSTANCE;
	public synchronized static ActivityUtils getInstance() {
		if (INSTANCE == null) INSTANCE = new ActivityUtils();
		return INSTANCE;
	}
	
	public synchronized static void dispose() {
		if (INSTANCE != null) INSTANCE = null;
	}
	
	private AppCompatActivity registeredActivity;
	private ActivityUtils() {
		if (INSTANCE != null) throw new UnsupportedOperationException("Please use 'ActivityUtils#getInstance()'!");
		mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
	}
	
	private final Handler mainThread;
	
	public ActivityResultLauncher<Intent> registerForActivityResult(@NonNull ActivityResultCallback<ActivityResult> arc) {
		return getRegisteredActivity().registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), arc);
	}
	
	public void bindService(@NonNull Intent serviceIntent, @NonNull ServiceConnection serviceCallback) {
		try {
			getRegisteredActivity().bindService(serviceIntent, serviceCallback, Context.BIND_AUTO_CREATE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void unbindService(@NonNull ServiceConnection serviceCallback) {
		try {
			getRegisteredActivity().unbindService(serviceCallback);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public void runOnUIThread(Runnable toBeRun) {
		mainThread.post(toBeRun);
	}
	
	public void runOnUIThread(IUIThreadTask toBeRun) {
		mainThread.post(() -> toBeRun.run(getRegisteredActivity().getApplicationContext()));
	}
	
	public void createNotificationChannel(
		@NonNull String channelID,
		@NonNull String channelName,
		@NonNull int notificationImportance
	) {
		NotificationChannel serviceChannel = new NotificationChannel(
			channelID,
			channelName,
			notificationImportance
		);

		NotificationManager manager = getRegisteredActivity().getSystemService(NotificationManager.class);
		manager.createNotificationChannel(serviceChannel);
	}
	
	public void showToast(
		@NonNull String message,
		@NonNull int length
	) {
		Toast.makeText(getRegisteredActivity(), message, length).show();
	}
	
	public PopupMenu showPopupMenuAt(
		@NonNull View anchor,
		@MenuRes int menuRes,
		@Nullable PopupMenu.OnMenuItemClickListener listener
	) {
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
	
	public AlertDialog createAlertDialog(
		@NonNull String title,
		@Nullable String message,
		@NonNull Pair<String, DialogInterface.OnClickListener> negativeButton,
		@NonNull Pair<String, DialogInterface.OnClickListener> positiveButton
	) {
		return new MaterialAlertDialogBuilder(getRegisteredActivity())
			.setTitle(title)
			.setMessage(message)
			.setNegativeButton(negativeButton.first, negativeButton.second)
			.setPositiveButton(positiveButton.first, positiveButton.second)
			.create();
	}
	
	public void registerActivity(@NonNull AppCompatActivity newActivity) {
		this.registeredActivity = newActivity;
	}
	
	public AppCompatActivity getRegisteredActivity() {
		return registeredActivity;
	}
	
	@SuppressWarnings("DiscouragedApi")
	public boolean isUserUsingNavigationBar() {
		int id = getRegisteredActivity().getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) return getRegisteredActivity().getResources().getBoolean(id);
		else {
			boolean hasMenuKey = ViewConfiguration.get(getRegisteredActivity()).hasPermanentMenuKey();
			boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
			return !hasMenuKey && !hasBackKey;
		}
	}
	
	public boolean isUserUsingHuawei() {
		return Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("huawei") && Build.BRAND.toLowerCase(Locale.getDefault()).contains("huawei");
	}
}
