package com.thatmg393.esmanager;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.os.Environment;

import com.thatmg393.esmanager.utils.ActivityUtils;

public final class GlobalConstants {
	public static final String PREFERENCE_NAME = "imaginefindingthisandchangingasettingbutitdoesntdoanything";
	public static final String ES_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "Android/data/com.evertechsandbox/";
	public static final String ESM_ROOT_FOLDER = getRootFolder();
	
	public static final class RequestCodes {
		public static final int REQUEST_WRITE_ACCESS = 0;
		public static final int REQUEST_EVERTECH_FOLDER_ACCESS = 1;
	}
	
	private static String getRootFolder() {
		if (SDK_INT < VERSION_CODES.R) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "ESManager";
		} else {
			return ActivityUtils.getInstance().getRegisteredActivity().getExternalFilesDir("").getAbsolutePath();
		}
	}
}
