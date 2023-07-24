package com.thatmg393.esmanager.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.anggrayudi.storage.SimpleStorage;
import com.anggrayudi.storage.SimpleStorageHelper;
import com.anggrayudi.storage.file.DocumentFileUtils;
import com.anggrayudi.storage.file.FileFullPath;
import com.anggrayudi.storage.file.StorageId;
import com.anggrayudi.storage.file.StorageType;
import com.anggrayudi.storage.permission.ActivityPermissionRequest;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.interfaces.IOnAllowFolderAccess;

public class StorageUtils {
	private static final Logger LOG = new Logger("ESM/StorageUtils");
	
	private volatile static StorageUtils INSTANCE;
	public synchronized static StorageUtils getInstance() {
		if (INSTANCE == null) INSTANCE = new StorageUtils();
		return INSTANCE;
	}
	
	private StorageUtils() {
		if (INSTANCE != null) { throw new RuntimeException("Please use 'StorageUtils#getInstance()'!"); }
		
		SSH = new SimpleStorageHelper(ActivityUtils.getInstance().getRegisteredActivity());
	}
	
	private SimpleStorageHelper SSH;
	
	public void askForDirectoryAccess(@NonNull String path, @NonNull int requestCode, @NonNull IOnAllowFolderAccess ioafa) {
		SSH.setOnStorageAccessGranted((rCode, root) -> {
			ioafa.onAllowFolderAccess(rCode, root.getUri());
			SharedPreference.getInstance().putBool(DocumentFileUtils.getAbsolutePath(root, ActivityUtils.getInstance().getRegisteredActivity()), true);
			
			Toast.makeText(ActivityUtils.getInstance().getRegisteredActivity(), "Access granted for folder: " + DocumentFileUtils.getAbsolutePath(root, ActivityUtils.getInstance().getRegisteredActivity()), Toast.LENGTH_SHORT).show();
			return null;
		});
		
		FileFullPath fullPath = getFileFullPath(path);
		if (!SharedPreference.getInstance().getBool(fullPath.getAbsolutePath())) {
			SSH.requestStorageAccess(
				requestCode,
				fullPath,
				StorageType.EXTERNAL,
				path
			);
		} else {
			ioafa.onAllowFolderAccess(requestCode, fullPath.toDocumentUri(ActivityUtils.getInstance().getRegisteredActivity()));
		}
	}
	
	public void requestFoStoragePermission(ActivityPermissionRequest resultListener) {
		if (SDK_INT >= VERSION_CODES.O) return;
		resultListener.check();
	}
	
	public static FileFullPath getFileFullPath(@NonNull String path) {
		FileFullPath fullPath = null;
		
		if (SDK_INT >= VERSION_CODES.R) { fullPath = new FileFullPath(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), StorageType.EXTERNAL, path); }
		else if (SDK_INT <= VERSION_CODES.Q) { fullPath = new FileFullPath(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), StorageId.PRIMARY, path); }
		
		return fullPath;
	}
	
	public boolean isStoragePermissionGranted() {
		return PermissionUtils.checkForPermission(ActivityUtils.getInstance().getRegisteredActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, GlobalConstants.RequestCodes.REQUEST_WRITE_ACCESS);
	}
	
	public SimpleStorageHelper getStorageHelper() {
		return SSH;
	}
	
	public enum Status {
		GRANTED, DENIED, CANNOT_ASK, FAILURE
	}
}