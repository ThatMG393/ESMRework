package com.thatmg393.esmanager.activities;

import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.fragments.main.HomeFragment;
import com.thatmg393.esmanager.fragments.main.ModsFragment;
import com.thatmg393.esmanager.fragments.main.ProjectsFragment;
import com.thatmg393.esmanager.managers.editor.lsp.LSPManager;
import com.thatmg393.esmanager.managers.rpc.DRPCManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.PermissionUtils;

public class MainActivity extends BaseActivity {
	private Toolbar mainToolbar;
	private FrameLayout mainFragmentContainer;
	private BottomNavigationView mainBottomNav;
	
	private boolean hasAccessToMods;
	
	@Override
	public void init() {
		super.init();
		ActivityUtils.getInstance().registerActivity(this);
		String tmp = GlobalConstants.ES_MOD_FOLDER;
		PermissionUtils.askForUsageStatsPermission(getApplicationContext());
		
		setContentView(R.layout.activity_main);
		
		mainToolbar = findViewById(R.id.main_toolbar);
		setSupportActionBar(mainToolbar);
		
		mainBottomNav = findViewById(R.id.main_bottom_nav);
		mainBottomNav.setOnItemSelectedListener((menuItem) -> {
			if (menuItem.getItemId() == R.id.main_bottom_mods) {
				getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new ModsFragment()).commit();
				return true;
			} else if (menuItem.getItemId() == R.id.main_bottom_projects) {
				getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new ProjectsFragment()).commit();
				return true;
			} else if (menuItem.getItemId() == R.id.main_bottom_home) {
				getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
				return true;
			}
			return false;
		});
		
		mainBottomNav.setSelectedItemId(R.id.main_bottom_home);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ActivityUtils.getInstance().registerActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LSPManager.getInstance().stopLSPForAllLanguage();
		DRPCManager.getInstance().stopDiscordRPC();
	}
}
