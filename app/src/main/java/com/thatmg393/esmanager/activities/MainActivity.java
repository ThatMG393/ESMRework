package com.thatmg393.esmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.managers.DRPCManager;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.PermissionUtils;
import com.thatmg393.esmanager.utils.ProcessListener;
import com.thatmg393.esmanager.utils.SharedPreference;
import com.thatmg393.esmanager.utils.StorageUtils;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PermissionUtils.askForUsageStatsPermission(getApplicationContext());
		
		/*
		ProcessListener.getInstance().startService();
		ProcessListener.getInstance().startListening("com.facebook.katana", new IOnProcessListener() {
			@Override
			public void onProcessStarted() {
				System.out.println("Started!");
			}
			
			@Override
			public void onProcessForeground() {
				System.out.println("Froeground!");
			}
			
			@Override
			public void onProcessBackground() {
				System.out.println("Backgrounded!");
			}
			
			@Override
			public void onProcessDestroyed() {
				System.out.println("Stopped!");
			}
		});
		*/
		
		MaterialButton startPA = findViewById(R.id.launch_pa);
		startPA.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View theView) {
				startActivity(new Intent(getApplicationContext(), ProjectActivity.class));
			}
		});
		
		MaterialButton startRPC = findViewById(R.id.launch_rpc);
		PermissionUtils.requestDrawOverlayPermission(getApplicationContext(), new PermissionUtils.PermissionResult() {
			@Override
			public void onReturn(PermissionUtils.Status returnValue) {
				if (returnValue == PermissionUtils.Status.GRANTED) {
					startRPC.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View theView) {
							DRPCManager.getInstance().startDiscordRPC();
						}
					});
				} else {
					Toast.makeText(getApplicationContext(), "Cannot start RPC, please grant 'Appear on top' on Settings", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		MaterialButton stopRPC = findViewById(R.id.kill_rpc);
		stopRPC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View theView) {
				DRPCManager.getInstance().stopDiscordRPC();
			}
		});
	}
	
	@Override
	public void init() {
		super.init();
		ActivityUtils.initializeInstance(this);
		GlobalConstants.getInstance();
		
		setContentView(R.layout.activity_main);
		
		SharedPreference.initializeInstance(this);
		ProcessListener.initializeInstance(this);
		StorageUtils.initializeInstance();
		DRPCManager.initializeInstance();
		LSPManager.initializeInstance();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.destroy();
	}
}
