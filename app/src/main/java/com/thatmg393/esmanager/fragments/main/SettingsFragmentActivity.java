package com.thatmg393.esmanager.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;

import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.fragments.main.settings.SettingsFragment;
import com.thatmg393.esmanager.utils.ActivityUtils;
import java.util.Arrays;
import java.util.List;

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
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_undo);
		
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base_frame, new SettingsFragment()).commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (prevFrag != null) {
					try {
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base_frame, (PreferenceFragmentCompat)prevFrag.newInstance()).commit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
         	   return true;
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		if (prevFrag != null) {
			try {
				removeAndReplaceFragment((PreferenceFragmentCompat)prevFrag.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Class<? extends PreferenceFragmentCompat> prevFrag;
	public void updateToolbarState() {
		PreferenceFragmentCompat f = getFragmentPreviousOfCurrent();
		if (f != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			prevFrag = f.getClass();
			
			return;
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		prevFrag = null;
	}
	
	public PreferenceFragmentCompat getFragmentPreviousOfCurrent() {
   	 List<Fragment> fragments = getSupportFragmentManager().getFragments();
 	   if (fragments != null) {
  	      for (int i = 0; i < fragments.size(); i++) {
				if (fragments.get(i) != null
				&& fragments.get(i) instanceof PreferenceFragmentCompat
				&& fragments.get(i).isVisible()) {
					ActivityUtils.getInstance().showToast(fragments.toString(), Toast.LENGTH_SHORT);
					Fragment tmpPrevFrag = null;
					try {
						for (int j = i; 0 > j; j--) {
							if (fragments.get(j) != null
							&& fragments.get(j) instanceof PreferenceFragmentCompat) {
								tmpPrevFrag = fragments.get(i-1);
							}
						}
					} catch (IndexOutOfBoundsException e) { }
					
					return (PreferenceFragmentCompat)tmpPrevFrag;
				}
			};
	    }
  	  return null;
	}
	
	private void removeAndReplaceFragment(PreferenceFragmentCompat frag) {
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_base_frame, frag).commit();
	}
}
