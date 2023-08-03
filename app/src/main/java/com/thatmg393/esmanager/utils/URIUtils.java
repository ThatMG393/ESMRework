package com.thatmg393.esmanager.utils;


import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;

public class URIUtils {
	public static DocumentFile[] listFileTree(@NonNull Context context, @NonNull Uri path) {
		DocumentFile fromTreeUri = DocumentFile.fromTreeUri(context, path);
		DocumentFile[] documentFiles = fromTreeUri.listFiles();

		return documentFiles;
	}

	/**
	 * From Ing.N.Nyerges 2019 V2.0
	 *
	 * <p>Storage Access Framework(SAF) Uri's creator from File (java.IO), for removable external
	 * storages
	 *
	 * @param context Application Context
	 * @param file File path
	 * @return SAF tree uri
	 */
	public static Uri getTreeUriFromFile(@NonNull Context context, @NonNull File file) {
		if (file.isFile()) return null;
		if (file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
			file = new File(file.toString().replace(Environment.getExternalStorageDirectory().getAbsolutePath(), ""));
		}
		
		String scheme = "content";
		String authority = "com.android.externalstorage.documents";
		
		// Separate each element of the File path
		// File format: "/storage/XXXX-XXXX/sub-folder1/sub-folder2..../filename"
		// (XXXX-XXXX) is external removable number
		String[] ele = file.getPath().split(File.separator);
		//  ele[0 to n] = folders
		
		// Construct folders strings using SAF format
		StringBuilder folders = new StringBuilder();
		for (int i = 0; i < ele.length; ++i) {
			folders.append(ele[i]);
			if (ele.length > (i + 1)) folders.append("%2F");
		}
		
		String common = "primary%3A" + folders.toString() + "/documents/primary%3A" + folders.toString();
		
		// Construct TREE Uri
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(scheme);
		builder.authority(authority);
		builder.encodedPath("/tree/" + common);
		return builder.build();
	}
	
	/**
	 * From Ing.N.Nyerges 2019 V2.0
	 *
	 * <p>Storage Access Framework(SAF) Uri's creator from File (java.IO), for removable external
	 * storages
	 *
	 * @param context Application Context
	 * @param file File path
	 * @return SAF tree uri
	 */
	public static Uri getDocumentUriFromFile(@NonNull Context context, @NonNull File file) {
		if (file.isFile()) return null;
		if (file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
			file = new File(file.toString().replace(Environment.getExternalStorageDirectory().getAbsolutePath(), ""));
		}
		
		String scheme = "content";
		String authority = "com.android.externalstorage.documents";
		
		// Separate each element of the File path
		// File format: "/storage/XXXX-XXXX/sub-folder1/sub-folder2..../filename"
		// (XXXX-XXXX is external removable number
		String[] ele = file.getAbsolutePath().split(File.separator);
		// ele[0 to (n-1)] = folders
		// ele[n] = file
		
		// Construct folders strings using SAF format
		StringBuilder folders = new StringBuilder();
		for (int i = 0; i < ele.length; ++i) {
			folders.append(ele[i]);
			if (ele.length > (i + 1)) folders.append("%2F");
		}
		
		String common = "primary%3A" + folders.toString();
		
		// Construct TREE Uri
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(scheme);
		builder.authority(authority);
		builder.encodedPath("/tree/" + common);
		return builder.build();
	}
}
