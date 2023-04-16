package com.thatmg393.esmanager.utils;

import android.net.Uri;
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
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

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
	
	public static boolean checkDrawOverlayPermission(Context context) {
		return Settings.canDrawOverlays(context);
	}
	
	public static void requestDrawOverlayPermission(Context context, PermissionResult pmr) {
		if (!checkDrawOverlayPermission(context)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + context.getPackageName()));
					
					
			ActivityUtils.getInstance().createNewARLI(new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (checkDrawOverlayPermission(context)) {
						pmr.onReturn(Status.GRANTED);
					} else {
						pmr.onReturn(Status.DENIED);
					}
				}
			}).launch(intent);
		} else {
			pmr.onReturn(Status.GRANTED);
		}
	}
	
	public static boolean checkForPermission(Activity activity, String permission, int requestCode) {
		if (permission == null) return false;
		if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE ||
			SDK_INT >= VERSION_CODES.R) { return false; }
				
		if (activity.getApplicationContext().checkSelfPermission(permission) ==
			PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
			return true;
		}
		
		return false;
	}
	
	public static ArrayMap<String, Boolean> checkForPermissions(Activity activity, String[] permissions, int requestCode) {
		if (permissions == null) return null;

		ArrayMap<String, Boolean> isPermissionAllowed = new ArrayMap<>();
		if (permissions.length > 1) {
			for (int idx = 0; idx < permissions.length; idx++) {
				isPermissionAllowed.put(permissions[idx], checkForPermission(activity, permissions[idx], requestCode));
			}
		}
		
		return isPermissionAllowed;
	}
	
	public static interface PermissionResult {
		public void onReturn(Status returnValue);
	}
}
