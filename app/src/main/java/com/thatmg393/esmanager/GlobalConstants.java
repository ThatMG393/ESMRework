package com.thatmg393.esmanager;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;
import com.thatmg393.esmanager.utils.ActivityUtils;

public final class GlobalConstants {
	public static final String PREFERENCE_NAME = "imaginefindingthisandchangingasettingbutitdoesntdoanything";
	public static final String ES_ROOT_PATH = "Android/data/com.evertechsandbox/";
	public static final String ESM_ROOT_FOLDER = calculateRootFolder();
	
	public static final class RequestCodes {
		public static final int REQUEST_WRITE_ACCESS = 0;
		public static final int REQUEST_EVERTECH_FOLDER_ACCESS = 1;
	}
	
	private static String calculateRootFolder() {
		if (SDK_INT < VERSION_CODES.R) {
			return "/storage/emulated/0/ESManager";
		} else {
			return ActivityUtils.getInstance().getMainActivityInstance().getExternalFilesDir("").getAbsolutePath();
		}
	}
	
	private GlobalConstants() { }
}
