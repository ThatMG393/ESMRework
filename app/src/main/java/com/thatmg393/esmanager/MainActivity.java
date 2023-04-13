package com.thatmg393.esmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.itsaky.utils.logsender.LogSender;
import com.thatmg393.esmanager.activities.BaseActivity;
import com.thatmg393.esmanager.interfaces.IOnProcessListener;
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
		
		MaterialButton startPA = findViewById(R.id.launch_pa);
		startPA.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View theView) {
				startActivity(new Intent(getApplicationContext(), ProjectActivity.class));
			}
		});
		
		MaterialButton startRPC = findViewById(R.id.launch_rpc);
		startRPC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View theView) {
				DRPCManager.getInstance().startDiscordRPC();
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
		setContentView(R.layout.activity_main);
		
		SharedPreference.initializeInstance(this);
		ActivityUtils.initializeInstance(this);
		ProcessListener.initializeInstance(this);
		
		StorageUtils.initializeInstance();
		
		DRPCManager.initializeInstance();
		LSPManager.initializeInstance();
		/*
		StorageUtils.getInstance().askForDirectoryAccess("Android/data/com.android.chrome/files", GlobalConstants.RequestCodes.REQUEST_EVERTECH_FOLDER_ACCESS, new IOnAllowFolderAccess() {
			@Override
			public void onAllowFolderAccess(int requestCode, String absolutePath) {
				if (requestCode == GlobalConstants.RequestCodes.REQUEST_EVERTECH_FOLDER_ACCESS) {
					Toast.makeText(MainActivity.this.getApplicationContext(), "Successfully granted!", Toast.LENGTH_LONG).show();
				}
			}
		});
		*/
		
		PermissionUtils.checkDrawOverlayPermission(getApplicationContext());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		LSPManager.getInstance().dispose();
		DRPCManager.getInstance().dispose();
		
		StorageUtils.dispose();
		LogSender.dispose();
		
		// SharedPreference.getInstance().dispose();
		
		// this.unsrt
		Log.e("MainThread", "*Dies*");
	}
}
