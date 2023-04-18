package com.thatmg393.esmanager.models;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.thatmg393.esmanager.interfaces.impl.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.lsp.base.BaseLSPBinder;
import com.thatmg393.esmanager.managers.lsp.base.BaseLSPService;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.LSPUtils;
import com.thatmg393.esmanager.utils.Logger;

import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.ArrayList;

public class LanguageServerModel implements ServiceConnection {
	private static final Logger LOG = new Logger("ESM/LanguageServerModel");
	private static final Logger LOG_LSP = new Logger("ESM/LuaLanguageServer");
	
	private final Intent lspServiceIntent;
	private final int lspPort;
	
	private final CustomLanguageServerDefinition serverDefinition;
	
    public LanguageServerModel(
		@NonNull String serverLanguage,
		@NonNull Class<? extends BaseLSPService> serviceClass,
		@NonNull int lspPort
	) {
		this.lspServiceIntent = new Intent(ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext(), serviceClass);
        this.lspPort = lspPort;
		
		this.serverDefinition = LSPUtils.generateServerDefinition("." + serverLanguage, lspPort, new EventHandler.EventListener() {
			@Override
			public void initialize(LanguageServer server, InitializeResult result) {
				LOG_LSP.d("LSP has been started and initialized!");
			}
			
			@Override
			public void onHandlerException(Exception e) {
				LOG_LSP.w("An internal exception occured!");
				e.printStackTrace(System.err);
			}
			
			@Override
			public void onShowMessage(MessageParams messageParams) { 
				LOG_LSP.i(messageParams.getMessage());
			}
			
			@Override
			public void onLogMessage(MessageParams messageParams) {
				LOG_LSP.i(messageParams.getMessage());
			}
		});
    }

    public Intent getLspServiceIntent() {
        return this.lspServiceIntent;
    }

    public int getLspPort() {
        return this.lspPort;
    }
	
	public CustomLanguageServerDefinition getServerDefinition() {
		return this.serverDefinition;
	}
	
    // Start of implementation
	public void startLSPService() {
		ActivityUtils.getInstance().bindService(lspServiceIntent, this);
	}
	
	public void stopLSPService() {
		ActivityUtils.getInstance().unbindService(this);
	}
	
	private BaseLSPService SERVICE_INSTANCE;
    @Override
    public void onServiceConnected(ComponentName cn, IBinder binder) {
		SERVICE_INSTANCE = ((BaseLSPBinder)binder).getInstance();
		callbackOnReady();
	}
	
    @Override
    public void onServiceDisconnected(ComponentName cn) {
		callbackOnShutdown();
	}
	
	public boolean isServerRunning() {
		if (SERVICE_INSTANCE == null) { return false; }
		return SERVICE_INSTANCE.isServerRunning();
	}
	
	private ArrayList<ILanguageServiceCallback> lspCallbackArr = new ArrayList<>();
	public void addListener(ILanguageServiceCallback lspCallback) {
		lspCallbackArr.add(lspCallback);
		
		if (isServerRunning()) { lspCallback.onReady(); }
	}
	
	public void callbackOnReady() {
		lspCallbackArr.forEach((callback) -> {
			callback.onReady();
			callback.onReady(SERVICE_INSTANCE);
		});
	}
	public void callbackOnShutdown() {
		lspCallbackArr.forEach(ILanguageServiceCallback::onShutdown);
	}
}
