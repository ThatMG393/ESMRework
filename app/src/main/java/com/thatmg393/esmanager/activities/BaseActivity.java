package com.thatmg393.esmanager.activities;

import android.content.res.Configuration;
import android.os.Bundle;

import android.util.Log;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
	private boolean hasBeenInitialized;
	
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!hasBeenInitialized) {
			onInit(savedInstanceState);
			hasBeenInitialized = true;
		}
	}
	
	@CallSuper
	public void onInit(@Nullable Bundle savedInstanceState) { }
}
