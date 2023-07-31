package com.thatmg393.esmanager;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.net.Uri;
import android.os.Environment;

import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.StorageUtils;

public final class GlobalConstants {
	public static final String PREFERENCE_NAME = "imaginefindingthisandchangingasettingbutitdoesntdoanything";
	public static String ES_MOD_FOLDER = getESModFolder();
	public static final String ESM_ROOT_FOLDER = getESMRootFolder();
	
	public static final class RequestCodes {
		public static final int REQUEST_WRITE_ACCESS = 0;
		public static final int REQUEST_EVERTECH_FOLDER_ACCESS = 1;
	}
	
	private static String getESModFolder() {
		/* (SDK_INT <= VERSION_CODES.Q)
			We expect:
			content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.evertechsandbox%2Ffiles%2Fmods/document/primary%3AAndroid%2Fdata%2Fcom.evertechsandbox%2Ffiles%2Fmods
		*/
		StorageUtils.getInstance().askForDirectoryAccess("Android/data/com.evertechsandbox/files/mods", 0, (reqCode, absolutePath) -> {
			ES_MOD_FOLDER = absolutePath.toString();
		});
		
		return ES_MOD_FOLDER;
	}
	
	private static String getESMRootFolder() {
		if (SDK_INT < VERSION_CODES.R) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "ESManager/";
		} else {
			return ActivityUtils.getInstance().getRegisteredActivity().getExternalFilesDir("").getAbsolutePath();
		}
	}
}
