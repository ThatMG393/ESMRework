package com.thatmg393.esmanager.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.fragments.main.settings.SettingsFragment;
import com.thatmg393.esmanager.utils.ActivityUtils;

public class SettingsFragmentActivity extends BaseActivity {
	public static void start(Context context) {
		Intent launchIntent = new Intent(context, SettingsFragmentActivity.class);
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(launchIntent);
	}
	
	@Override
	public void onInit(Bundle savedInstanceState) {
		super.onInit(savedInstanceState);
		
		ActivityUtils.getInstance().registerActivity(this);
		setContentView(R.layout.activity_fragment_base);
		setSupportActionBar(findViewById(R.id.fragment_base_toolbar));
		
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base_frame, new SettingsFragment()).commit();
	}
}
