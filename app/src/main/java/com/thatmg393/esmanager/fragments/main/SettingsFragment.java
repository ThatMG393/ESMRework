package com.thatmg393.esmanager.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.managers.rpc.DRPCManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.SharedPreference;

public class SettingsFragment extends BaseActivity {
	public static void start(Context context) {
		Intent launchIntent = new Intent(context, SettingsFragment.class);
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(launchIntent);
	}
	
	@Override
	public void init() {
		super.init();
		ActivityUtils.getInstance().registerActivity(this);
		setContentView(R.layout.activity_fragment_base);
		setSupportActionBar(findViewById(R.id.fragment_base_toolbar));
		
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base_frame, new SettingsFragmentImpl()).commit();
	}
	
	public static class SettingsFragmentImpl extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			getPreferenceManager().setPreferenceDataStore(SharedPreference.getInstance().getDefaultEncryptedPreferenceDataStore());
			setPreferencesFromResource(R.xml.xml_main_preference, rootKey);
			SharedPreference.getInstance().getDefaultEncryptedPreferenceDataStore().getSharedPreferences().registerOnSharedPreferenceChangeListener((prefs, key) -> {
				switch (key) {
					case "main_rpc_active":
						if (prefs.getBoolean("main_rpc_active", false)) {
							DRPCManager.getInstance().startDiscordRPC();
						} else {
							DRPCManager.getInstance().stopDiscordRPC();
						}
						return;
					default:
						System.out.println(key);
						return;
				}
			});
		}
	}
}
