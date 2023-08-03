package com.thatmg393.esmanager.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.Manifest;
import android.os.Build.VERSION_CODES;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.anggrayudi.storage.SimpleStorageHelper;
import com.anggrayudi.storage.file.DocumentFileUtils;
import com.anggrayudi.storage.file.FileFullPath;
import com.anggrayudi.storage.file.StorageId;
import com.anggrayudi.storage.file.StorageType;
import com.anggrayudi.storage.permission.ActivityPermissionRequest;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.interfaces.IOnAllowFolderAccess;
import com.thatmg393.esmanager.interfaces.IOnFilePick;

import java.io.File;

public class StorageUtils {
	private static final Logger LOG = new Logger("ESM/StorageUtils");
	private static SimpleStorageHelper SSH;
	
	public static void askForDirectoryAccess(
		@NonNull String path,
		@NonNull IOnAllowFolderAccess ioafa
	) {
		checkIfStorageHelperNonNull();
		
		SSH.setOnStorageAccessGranted((rCode, root) -> {
			ioafa.onAllowFolderAccess(root.getUri());
			SharedPreference.getInstance().putBool(DocumentFileUtils.getAbsolutePath(root, ActivityUtils.getInstance().getRegisteredActivity()), true);
			
			Toast.makeText(ActivityUtils.getInstance().getRegisteredActivity(), "Access granted for folder: " + DocumentFileUtils.getAbsolutePath(root, ActivityUtils.getInstance().getRegisteredActivity()), Toast.LENGTH_SHORT).show();
			return null;
		});
		
		FileFullPath fullPath = getFileFullPath(path);
		if (!SharedPreference.getInstance().getBool(fullPath.getAbsolutePath())) {
			SSH.requestStorageAccess(
				GlobalConstants.RequestCodes.REQUEST_EVERTECH_FOLDER_ACCESS,
				fullPath,
				StorageType.EXTERNAL,
				path
			);
		} else {
			ioafa.onAllowFolderAccess(URIUtils.getTreeUriFromFile(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), new File(path)));
		}
	}
	
	public static void pickFile(
		@NonNull String mimeType,
		@NonNull IOnFilePick iofp
	) {
		checkIfStorageHelperNonNull();
		
		SSH.setOnFileSelected((rCode, file) -> {
			iofp.onFilePick(file.get(0).getUri());
			return null;
		});
		
		SSH.openFilePicker(mimeType);
	}
	
	public static void requestForStoragePermission(ActivityPermissionRequest resultListener) {
		resultListener.check();
	}
	
	public static FileFullPath getFileFullPath(@NonNull String path) {
		FileFullPath fullPath = null;
		
		if (SDK_INT >= VERSION_CODES.R) { fullPath = new FileFullPath(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), StorageType.EXTERNAL, path); }
		else if (SDK_INT <= VERSION_CODES.Q) { fullPath = new FileFullPath(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), StorageId.PRIMARY, path); }
		
		return fullPath;
	}
	
	public static boolean isStoragePermissionGranted() {
		return PermissionUtils.checkForPermission(ActivityUtils.getInstance().getRegisteredActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, GlobalConstants.RequestCodes.REQUEST_WRITE_ACCESS);
	}
	
	public static void initStorageHelper() {
		SSH = new SimpleStorageHelper(ActivityUtils.getInstance().getRegisteredActivity());
	}
	
	public static SimpleStorageHelper getStorageHelper() {
		checkIfStorageHelperNonNull();
		return SSH;
	}
	
	private static void checkIfStorageHelperNonNull() {
		if (SSH == null) throw new UnsupportedOperationException("Call 'StorageUtils#initStorageHelper()' first");
	}
	
	public static enum Status {
		GRANTED, DENIED, CANNOT_ASK, FAILURE
	}
}