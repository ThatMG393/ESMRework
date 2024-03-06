package com.thatmg393.esmanager.fragments.project.base;

import android.os.Bundle;
import java.io.File;

public class PathedTabFragment extends BaseTabFragment {
	private File filePath;
	
	public PathedTabFragment(final String pathToFile) {
		this.filePath = new File(pathToFile);
	}
	
	public File getCurrentFile() {
		return this.filePath;
	}
	
	public String getCurrentFilePath() {
		return this.filePath.getAbsolutePath();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			String path = savedInstanceState.getString("filePath");
			if (path != null) {
				filePath = new File(path);
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putString("filePath", getCurrentFilePath());
	}
}
