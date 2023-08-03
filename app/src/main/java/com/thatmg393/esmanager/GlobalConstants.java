package com.thatmg393.esmanager;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.net.Uri;
import android.os.Environment;

import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.StorageUtils;

public final class GlobalConstants {
	private static volatile GlobalConstants INSTANCE;
	public synchronized static GlobalConstants getInstance() {
		if (INSTANCE == null) INSTANCE = new GlobalConstants();
		return INSTANCE;
	}
	
	private GlobalConstants() {
		if (INSTANCE != null) throw new UnsupportedOperationException("Please use 'GlobalConstants#getInstance()'!");
	}
	
	private String PREFERENCE_NAME = "imaginefindingthisandchangingasettingbutitdoesntdoanything";
	private String ES_MOD_FOLDER;
	private String ESM_ROOT_FOLDER;
	
	public void initConstants() {
		ES_MOD_FOLDER = constructESModFolder();
		ESM_ROOT_FOLDER = constructESMRootFolder();
	}
	
	private String constructESModFolder() {
		/* (SDK_INT <= VERSION_CODES.Q)
			We expect:
			content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.evertechsandbox%2Ffiles%2Fmods/document/primary%3AAndroid%2Fdata%2Fcom.evertechsandbox%2Ffiles%2Fmods
		*/
		StorageUtils.askForDirectoryAccess("Android/data/com.evertechsandbox/files/mods", (path) -> {
			ES_MOD_FOLDER = path.toString();
		});
		
		return ES_MOD_FOLDER;
	}
	
	private String constructESMRootFolder() {
		if (SDK_INT < VERSION_CODES.R) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "ESManager/";
		} else {
			return ActivityUtils.getInstance().getRegisteredActivity().getExternalFilesDir("").getAbsolutePath();
		}
	}
	
	public static final class RequestCodes {
		public static final int REQUEST_WRITE_ACCESS = 0;
		public static final int REQUEST_EVERTECH_FOLDER_ACCESS = 1;
	}

	public String getPreferenceName() {
	    return this.PREFERENCE_NAME;
	}
	
	public String getESModFolder() {
	    return this.ES_MOD_FOLDER;
	}
	
	public String getESMRootFolder() {
	    return this.ESM_ROOT_FOLDER;
	}
}
