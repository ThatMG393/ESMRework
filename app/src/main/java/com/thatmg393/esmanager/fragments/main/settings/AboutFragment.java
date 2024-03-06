package com.thatmg393.esmanager.fragments.main.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.main.SettingsFragmentActivity;
import com.thatmg393.esmanager.utils.SharedPreference;

public class AboutFragment extends PreferenceFragmentCompat {
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		((SettingsFragmentActivity) requireActivity()).updateToolbarState();
		getPreferenceManager().setPreferenceDataStore(SharedPreference.getInstance().getDefaultEncryptedPreferenceDataStore());
		setPreferencesFromResource(R.xml.xml_about_preference, rootKey);
	}
}