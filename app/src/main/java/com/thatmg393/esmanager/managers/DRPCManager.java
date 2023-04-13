package com.thatmg393.esmanager.managers;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.thatmg393.esmanager.interfaces.IRPCListener;
import com.thatmg393.esmanager.managers.rpc.RPCService;
import com.thatmg393.esmanager.managers.rpc.RPCSocketClient;
import com.thatmg393.esmanager.models.DiscordProfileModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.Logger;

public class DRPCManager implements ServiceConnection {
    private static final Logger LOG = new Logger("ESM/DRPCManager");
	
	private static volatile DRPCManager INSTANCE;

    public static synchronized DRPCManager getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Initialize first, use 'DRPCManager#initializeInstance(MainActivity)'");
        }

        return INSTANCE;
    }

    public static synchronized DRPCManager initializeInstance() {
        if (INSTANCE == null) INSTANCE = new DRPCManager();

        return INSTANCE;
    }
	
    private final Intent rpcIntent;
    private DRPCManager() {
        if (INSTANCE != null) { throw new RuntimeException("Please use 'DRPCManager#getInstance()'!"); }

        this.rpcIntent = new Intent(ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext(), RPCService.class);
    }

    public void startDiscordRPC() {
		System.out.println("got started");
		ActivityUtils.getInstance().bindService(rpcIntent, this);
	}
	
	public void stopDiscordRPC() {
		ActivityUtils.getInstance().unbindService(this);
	}
	
	private RPCService SERVICE_INSTANCE;
    @Override
    public void onServiceConnected(ComponentName cn, IBinder binder) {
		System.out.println("we connected we brothers");
		SERVICE_INSTANCE = ((RPCService.RPCBinder) binder).getInstance();
		
		SERVICE_INSTANCE.startRPC();
		
		System.out.println("bro finna connect to them internet");
		addListener(new IRPCListener() {
			@Override
			public void onConnected() {
				SERVICE_INSTANCE.rpcWebsocketClient.sendPresence();
				System.out.println("scare them");
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
		
		if (SERVICE_INSTANCE.rpcWebsocketClient.isOpen()) rpcListener.onConnected();
	}
	
	public DiscordProfileModel getCurrentProfile() {
		if (SERVICE_INSTANCE != null || SERVICE_INSTANCE.rpcWebsocketClient.discordProfile != null) {
			return SERVICE_INSTANCE.rpcWebsocketClient.discordProfile;
		}
		
		return null;
	}
	
	public void dispose() {
		stopDiscordRPC();
		INSTANCE = null;
	}
}
