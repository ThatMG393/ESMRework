package com.thatmg393.esmanager.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.main.HomeFragment;
import com.thatmg393.esmanager.fragments.main.ModsFragment;
import com.thatmg393.esmanager.fragments.main.ProjectsFragment;
import com.thatmg393.esmanager.fragments.main.SettingsFragmentActivity;
import com.thatmg393.esmanager.managers.editor.lsp.LSPManager;
import com.thatmg393.esmanager.managers.rpc.DRPCManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.PermissionUtils;
import com.thatmg393.esmanager.utils.SharedPreference;
import com.thatmg393.esmanager.utils.StorageUtils;

public class MainActivity extends BaseActivity {
	private Toolbar mainToolbar;
	private FrameLayout mainFragmentContainer;
	private BottomNavigationView mainBottomNav;
	
	@Override
	public void onInit(Bundle savedInstanceState) {
		super.onInit(savedInstanceState);
		
		if (savedInstanceState == null) {
			ActivityUtils.getInstance().registerActivity(this);
			StorageUtils.initStorageHelper();
			PermissionUtils.askForUsageStatsPermission(getApplicationContext());
			GlobalConstants.getInstance().initConstants();
			
			setContentView(R.layout.activity_main);
			
			LinearLayout buttonContainer = findViewById(R.id.main_fragment_button_container);
			buttonContainer.setTranslationY(getResources().getDimension(com.intuit.sdp.R.dimen._8sdp));
			buttonContainer.bringToFront();
		
			mainToolbar = findViewById(R.id.main_toolbar);
			setSupportActionBar(mainToolbar);
		
			mainBottomNav = findViewById(R.id.main_bottom_nav);
			mainBottomNav.setOnItemSelectedListener((menuItem) -> {
				if (menuItem.getItemId() == R.id.main_bottom_mods && mainBottomNav.getSelectedItemId() != R.id.main_bottom_mods) {
					getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new ModsFragment()).commit();
					return true;
				} else if (menuItem.getItemId() == R.id.main_bottom_projects && mainBottomNav.getSelectedItemId() != R.id.main_bottom_projects) {
					getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new ProjectsFragment(), ProjectsFragment.TAG).commit();
					return true;
				} else if (menuItem.getItemId() == R.id.main_bottom_home && mainBottomNav.getSelectedItemId() != R.id.main_bottom_home) {
					getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
					return true;
				}
				return false;
			});
			
			mainBottomNav.setSelectedItemId(R.id.main_bottom_home);
		} else {
			mainBottomNav.setSelectedItemId(savedInstanceState.getInt("bottomNav_selectedItem"));
		}
		
		// Shared preference stuff
		if (SharedPreference.getInstance().getBool("main_rpc_active")) DRPCManager.getInstance().startDiscordRPC();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("bottomNav_selectedItem", mainBottomNav.getSelectedItemId());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_action_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.main_action_settings) {
			SettingsFragmentActivity.start(getApplicationContext());
			return true;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ActivityUtils.getInstance().registerActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (isFinishing()) {
			LSPManager.getInstance().stopLSPForAllLanguage();
			DRPCManager.getInstance().stopDiscordRPC();
		
			ActivityUtils.dispose();
		}
	}
}
