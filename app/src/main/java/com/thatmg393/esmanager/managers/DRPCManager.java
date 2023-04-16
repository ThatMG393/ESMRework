package com.thatmg393.esmanager.managers;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import com.thatmg393.esmanager.interfaces.IRPCListener;
import com.thatmg393.esmanager.managers.rpc.RPCService;
import com.thatmg393.esmanager.models.DiscordProfileModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.PermissionUtils;

public class DRPCManager implements ServiceConnection {
    private static final Logger LOG = new Logger("ESM/DRPCManager");
	
	private static volatile DRPCManager INSTANCE;

    public static synchronized DRPCManager getInstance() {
        if (INSTANCE == null) throw new RuntimeException("Initialize first, use 'DRPCManager#initializeInstance(MainActivity)'");
		
        return INSTANCE;
    }

    public static synchronized DRPCManager initializeInstance() {
        if (INSTANCE == null) INSTANCE = new DRPCManager();

        return INSTANCE;
    }
	
    private final Intent rpcIntent;
    private DRPCManager() {
        if (INSTANCE != null) throw new RuntimeException("Please use 'DRPCManager#getInstance()'!");

        this.rpcIntent = new Intent(ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext(), RPCService.class);
    }

    public void startDiscordRPC() {
		ActivityUtils.getInstance().bindService(rpcIntent, DRPCManager.INSTANCE);
	}
	
	public void stopDiscordRPC() {
		ActivityUtils.getInstance().unbindService(DRPCManager.INSTANCE);
	}
	
	private RPCService SERVICE_INSTANCE;
    @Override
    public void onServiceConnected(ComponentName cn, IBinder binder) {
		SERVICE_INSTANCE = ((RPCService.RPCBinder) binder).getInstance();
		addListener(new IRPCListener() {
			@Override
			public void onConnected() {
				SERVICE_INSTANCE.getRPCSocketClient().sendPresence();
			}
			
			@Override
			public void shutdown() { }
		});
	}

    @Override
    public void onServiceDisconnected(ComponentName cn) {
		SERVICE_INSTANCE = null;
	}
	
	public void addListener(IRPCListener rpcListener) {
		if (SERVICE_INSTANCE == null) return;
		SERVICE_INSTANCE.rpcListeners.add(rpcListener);
		
		if (SERVICE_INSTANCE.getRPCSocketClient().isOpen()) rpcListener.onConnected();
	}
	
	public DiscordProfileModel getCurrentProfile() {
		if (SERVICE_INSTANCE != null || SERVICE_INSTANCE.getRPCSocketClient().discordProfile != null) {
			return SERVICE_INSTANCE.getRPCSocketClient().discordProfile;
		}
		
		return null;
	}
	
	public void dispose() {
		stopDiscordRPC();
		INSTANCE = null;
	}
}
