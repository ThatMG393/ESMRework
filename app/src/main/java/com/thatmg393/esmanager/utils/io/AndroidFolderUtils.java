package com.thatmg393.esmanager.utils.io;

import android.content.Context;
import android.net.Uri;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import java.io.File;
import java.util.ArrayList;

public class AndroidFolderUtils {
	public static class AndroidFSNode {
		protected final DocumentFileCompat file;
		protected final Context context;
		
		protected AndroidFSNode(Context context, DocumentFileCompat file) {
			this.context = context;
			this.file = file;
		}
		
		public AndroidFile getFile(String filename) {
			return new AndroidFile(context, appendToUri(file.getUri(), filename));
		}
		
		public AndroidDirectory getDirectory(String foldername) {
			return new AndroidDirectory(context, appendToUri(file.getUri(), foldername));
		}
		
		public AndroidDirectory getParent() {
			return new AndroidDirectory(context, file.getParentFile().getUri());
		}
		
		public ArrayList<AndroidFSNode> listFSNodes() {
			ArrayList<AndroidFSNode> alafsn = new ArrayList<>();
			
			file.listFiles().stream().parallel().forEach(e -> alafsn.add(
				e.isFile() ? new AndroidFile(context, e.getUri()) : new AndroidDirectory(context, e.getUri())
			));
			
			return alafsn;
		}
		
		public void rename(String newName) {
			file.renameTo(newName);
		}
	
		public boolean delete(String name) {
			if (file.findFile(name).isDirectory()) return getDirectory(name).deleteMe();
			return getFile(name).deleteMe();
		}
		
		public boolean deleteMe() {
			return file.delete();
		}
		
		protected final Uri appendToUri(Uri old, String f) {
			return old.buildUpon().appendPath(f).build();
		}
	}
	
	public static class AndroidDirectory extends AndroidFSNode {
		private String folderName;
		
		public AndroidDirectory(Context context, Uri uri) {
			super(context, DocumentFileCompat.fromTreeUri(context, uri));
			this.folderName = file.getName();
		}
		
		@Override
		public void rename(String newName) {
			super.rename(newName);
			this.folderName = newName;
		}
		
		public String getFolderName() {
			return this.folderName;
		}
	}
	
	public static class AndroidFile extends AndroidFSNode {
		private String fileName;
		
		public AndroidFile(Context context, Uri uri) {
			super(context, DocumentFileCompat.fromSingleUri(context, uri));
			this.fileName = file.getName();
		}
		
		@Override
		public void rename(String newName) {
			super.rename(newName);
			this.fileName = newName;
		}
		
		public String getFileName() {
			return this.fileName;
		}
	}
	
	public static AndroidDirectory getDataFolder(Context context) {
		return new AndroidDirectory(
			context,
		    DocumentFileCompat.fromFile(context, new File("/storage/emulated/0/Android/data")).getUri()
		);
	}
}
