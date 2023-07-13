package com.thatmg393.esmanager;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;
import com.thatmg393.esmanager.utils.ActivityUtils;

public final class GlobalConstants {
	public final String PREFERENCE_NAME;
	public final String ES_ROOT_PATH;
	public final String ESM_ROOT_FOLDER;
	
	public static final class RequestCodes {
		public static final int REQUEST_WRITE_ACCESS = 0;
		public static final int REQUEST_EVERTECH_FOLDER_ACCESS = 1;
	}
	
	private static GlobalConstants INSTANCE;
	public static GlobalConstants getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GlobalConstants();
		}
		return INSTANCE;
	}
	
	private GlobalConstants() {
		PREFERENCE_NAME = "imaginefindingthisandchangingasettingbutitdoesntdoanything";
		ES_ROOT_PATH = "Android/data/com.evertechsandbox/";
		ESM_ROOT_FOLDER = getRootFolder();
	}
	
	private String getRootFolder() {
		if (SDK_INT < VERSION_CODES.R) {
			return "/storage/emulated/0/ESManager";
		} else {
			return ActivityUtils.getInstance().getRegisteredActivity().getExternalFilesDir("").getAbsolutePath();
		}
	}
}
