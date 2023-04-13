package com.thatmg393.esmanager.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION.SDK_INT;

import androidx.core.app.ActivityCompat;
import com.anggrayudi.storage.SimpleStorageHelper;
import com.anggrayudi.storage.file.DocumentFileUtils;
import com.anggrayudi.storage.file.FileFullPath;
import com.anggrayudi.storage.file.StorageId;
import com.anggrayudi.storage.file.StorageType;
import com.anggrayudi.storage.permission.ActivityPermissionRequest;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.MainActivity;
import com.thatmg393.esmanager.interfaces.IOnAllowFolderAccess;

import org.jetbrains.annotations.NotNull;

public class StorageUtils {
	private static final Logger LOG = new Logger("ESM/StorageUtils");
	
	private volatile static StorageUtils INSTANCE;
	public synchronized static StorageUtils getInstance() {
		if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'StorageUtils#initializeInstance()'"); }
		
		return INSTANCE;
	}
	
	public synchronized static StorageUtils initializeInstance() {
		if (INSTANCE == null) INSTANCE = new StorageUtils();
		
		return INSTANCE;
	}
	
	private StorageUtils() {
		if (INSTANCE != null) { throw new RuntimeException("Please use 'StorageUtils#getInstance()'!"); }
		
		SSH = new SimpleStorageHelper(ActivityUtils.getInstance().getMainActivityInstance());
	}
	
	private static final int REQUEST_FOLDER_ACCESS = 0;
	
	private SimpleStorageHelper SSH;
	public SimpleStorageHelper getSimpleStorageHelper() { return SSH; }
	
	public void askForDirectoryAccess(@NotNull String path, @NotNull int requestCode, @NotNull IOnAllowFolderAccess ioafa) {
		SSH.setOnStorageAccessGranted((rCode, root) -> {
			String absolutePath = DocumentFileUtils.getAbsolutePath(root, ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext());
			ioafa.onAllowFolderAccess(rCode, absolutePath);
				
			// Toast.makeText(context, "Access granted for folder: " + absolutePath, Toast.LENGTH_LONG).show();
			return null;
		});
		
		FileFullPath fullPath = null;
		if (SDK_INT >= 30) { fullPath = new FileFullPath(ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext(), StorageType.EXTERNAL, path); }
		else if (SDK_INT <= 29) { fullPath = new FileFullPath(ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext(), StorageId.PRIMARY, path); }
		
		SSH.requestStorageAccess(
			requestCode,
			fullPath,
			StorageType.EXTERNAL,
			path
		);
	}
	
	public void requestFoStoragePermission(ActivityPermissionRequest resultListener) {
		if (SDK_INT <= VERSION_CODES.O) { LOG.w("Attempting to request storage permission above SDK 28"); return; }
		resultListener.check();
	}
	
	public boolean isStoragePermissionGranted() {
		return PermissionUtils.checkForPermission(ActivityUtils.getInstance().getMainActivityInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE, GlobalConstants.RequestCodes.REQUEST_WRITE_ACCESS);
	}
	
	public enum Status {
		GRANTED, DENIED, CANNOT_ASK, FAILURE
	}
	
	public static void dispose() {
		INSTANCE = null;
	}
}