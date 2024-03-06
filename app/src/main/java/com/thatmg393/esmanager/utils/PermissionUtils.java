package com.thatmg393.esmanager.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.ArrayMap;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.thatmg393.esmanager.utils.logging.Logger;

public class PermissionUtils {
	private static final Logger LOG = new Logger("ESM/Permissiontils");
	
	public enum Status {
		GRANTED, DENIED, CANNOT_ASK, FAILURE, WAITING
	}
	
	@RequiresApi(VERSION_CODES.R)
	public static boolean hasAllFileAccess() {
		return Environment.isExternalStorageManager();
	}
	
	public static Status checkForUsageStatsPermission(Context context) {
		if (SDK_INT >= VERSION_CODES.TIRAMISU) {
			try {
				PackageManager packageManager = context.getPackageManager();
				ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.ApplicationInfoFlags.of(0));
				AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
			
				int mode = appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, appInfo.uid, appInfo.packageName);
				
				return ( mode == AppOpsManager.MODE_ALLOWED ) ? Status.GRANTED : Status.DENIED;
			} catch (PackageManager.NameNotFoundException e) { }
		} else {
			try {
				PackageManager packageManager = context.getPackageManager();
				ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
				AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
				
				int mode = 69420;
				if (SDK_INT >= VERSION_CODES.Q) {
					mode = appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, appInfo.uid, appInfo.packageName);
				} else {
					mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, appInfo.uid, appInfo.packageName);
				}
				
				return ( mode == AppOpsManager.MODE_ALLOWED ) ? Status.GRANTED : Status.DENIED;
			} catch (PackageManager.NameNotFoundException e) { }
		}
		return Status.FAILURE;
	}
	
	public static void askForUsageStatsPermission(Context context) {
		Status result = checkForUsageStatsPermission(context);
		if (result == Status.DENIED || result == Status.FAILURE) {
			Intent uspIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
			uspIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(uspIntent);
		}
	}
	
	public static void checkForPermission(Context context, String permission, PermissionAskListener listener) {
		if (permission == null) return;
		if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && SDK_INT >= VERSION_CODES.R) listener.onPermissionGranted();
				
		if (shoudAskForPermission(context, permission)) {
			if (((Activity) context).shouldShowRequestPermissionRationale(permission)) {
				listener.onPermissionPreviouslyDenied();
			} else {
				if (isFirstTimeAskingForPermission(permission)) {
					ActivityCompat.requestPermissions((Activity) context, new String[] { permission }, 69);
					SharedPreference.getInstance().putBool(permission, true);
				} else {
					listener.onPermissionNeverAskAgain();
				}
			}
		} else {
			listener.onPermissionGranted();
		}
	}
	
	public static ArrayMap<String, Status> checkForPermissions(Activity activity, String[] permissions, int requestCode) {
		if (permissions == null) return null;

		ArrayMap<String, Status> isPermissionAllowed = new ArrayMap<>();
		
		if (permissions.length > 1) {
			for (int idx = 0; idx < permissions.length; idx++) {
				final int i = idx;
				checkForPermission(activity, permissions[idx], new PermissionAskListener() {
					@Override
					public void onPermissionPreviouslyDenied() {
						isPermissionAllowed.put(permissions[i], Status.DENIED);
					}
					
					@Override	
					public void onPermissionNeverAskAgain() {
						isPermissionAllowed.put(permissions[i], Status.CANNOT_ASK);
					}
						
					@Override
					public void onPermissionGranted() {
						isPermissionAllowed.put(permissions[i], Status.GRANTED);
					}
				});
			}
		}
		
		return isPermissionAllowed;
	}
	
	public static boolean shoudAskForPermission(Context context, String permission) {
		int permissionResult = ActivityCompat.checkSelfPermission(context, permission);
		return permissionResult != PackageManager.PERMISSION_GRANTED;
	}
	
	public static boolean isFirstTimeAskingForPermission(String permission) {
		return SharedPreference.getInstance().getBoolFallback(permission, true);
	}
	
	public static interface PermissionAskListener {
		public default void onPermissionGranted() { }
		public default void onPermissionPreviouslyDenied() { }
		public default void onPermissionNeverAskAgain() { }
	}
}
