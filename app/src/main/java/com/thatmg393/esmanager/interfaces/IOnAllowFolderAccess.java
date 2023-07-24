package com.thatmg393.esmanager.interfaces;

import android.net.Uri;

public interface IOnAllowFolderAccess {
	public void onAllowFolderAccess(int requestCode, Uri absolutePath);
}
