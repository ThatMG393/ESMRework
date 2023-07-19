package com.thatmg393.esmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.managers.DRPCManager;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.models.ProjectModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.PermissionUtils;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MaterialButton startPA = findViewById(R.id.launch_pa);
		startPA.setOnClickListener((v) -> {
			Intent projectIntent = new Intent(getApplicationContext(), ProjectActivity.class);
			projectIntent.putExtra("project", new ProjectModel(
				"Roblox AFS Script",
				GlobalConstants.ESM_ROOT_FOLDER + "/Roblox AFS Script",
				"v0.1",
				"ThatMG393"
			));
			
			startActivity(projectIntent);
		});
		
		MaterialButton startRPC = findViewById(R.id.launch_rpc);
		PermissionUtils.requestDrawOverlayPermission(getApplicationContext(), (returnValue) -> {
			if (returnValue == PermissionUtils.Status.GRANTED) {
				startRPC.setOnClickListener((v) -> {
					DRPCManager.getInstance().startDiscordRPC();
				});
			} else {
				Toast.makeText(getApplicationContext(), "Cannot start RPC, please grant 'Appear on top' on Settings", Toast.LENGTH_SHORT).show();
			}
		});
		
		MaterialButton stopRPC = findViewById(R.id.kill_rpc);
		stopRPC.setOnClickListener((v) -> {
			DRPCManager.getInstance().stopDiscordRPC();
		});
	}
	
	@Override
	public void init() {
		super.init();
		ActivityUtils.getInstance().registerActivity(this);
		PermissionUtils.askForUsageStatsPermission(getApplicationContext());
		
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ActivityUtils.getInstance().registerActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LSPManager.getInstance().stopLSPServices();
		DRPCManager.getInstance().stopDiscordRPC();
	}
}
