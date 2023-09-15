package com.thatmg393.esmanager.fragments.main.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.managers.rpc.DRPCManager;
import com.thatmg393.esmanager.utils.SharedPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
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
		
		findPreference("main_about");
	}
}