package com.thatmg393.esmanager.activities;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
	private boolean isRecreating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isRecreating) {
			init(savedInstanceState);
			isRecreating = false;
		}
	}
	
	@Override
	public Object onRetainCustomNonConfigurationInstance() { 
		isRecreating = true;
		
		return super.onRetainCustomNonConfigurationInstance();
	}
	
	// Always override this for a lot of initiaizing stuff
	@CallSuper
	public void init(@Nullable Bundle savedInstanceState) { }
}
