package com.thatmg393.esmanager.models;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thatmg393.esmanager.interfaces.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.editor.lsp.base.BaseLSPBinder;
import com.thatmg393.esmanager.managers.editor.lsp.base.BaseLSPService;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.esmanager.utils.sora.LSPUtils;

import io.github.rosemoe.sora.lsp.client.connection.SocketStreamConnectionProvider;
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.LanguageServerWrapper;

import java.util.ArrayList;

public class LanguageServerModel implements ServiceConnection {
	private static final Logger LOG = new Logger("ESM/LanguageServerModel");
	
	public final String serverName;
	private final LanguageServerWrapper serverWrapper;
	
	@Nullable
	private Intent serverServiceIntent = null;
	
	private int serverPort = -1;
	
	public LanguageServerModel(
		@NonNull String serverName,
		@NonNull Class<? extends BaseLSPService> serverServiceClass,
		@NonNull int serverPort
	) {
		this.serverName = serverName;
		this.serverServiceIntent = new Intent(ActivityUtils.getInstance().getRegisteredActivity().getApplicationContext(), serverServiceClass);
		this.serverPort = serverPort;
		this.serverWrapper = LSPUtils.createNewServerWrapper(
			serverName,
			new SocketStreamConnectionProvider(serverPort,  null)
		);
	}
	
	public LanguageServerModel(
		@NonNull String serverName,
		@NonNull StreamConnectionProvider serverConnectionProvider
	) {
		this.serverName = serverName;
		this.serverWrapper = LSPUtils.createNewServerWrapper(
			serverName,
			serverConnectionProvider
		);
	}
	
	public int getServerPort() {
		return this.serverPort;
	}
	
	public LanguageServerWrapper getServerWrapper() {
		return this.serverWrapper;
	}
	
	public CustomLanguageServerDefinition getServerDefinition() {
		return (CustomLanguageServerDefinition)this.serverWrapper.getServerDefinition();
	}
	
	// Start of implementation
	public void startLSP() {
		if (serverServiceIntent == null) return;
		ActivityUtils.getInstance().bindService(serverServiceIntent, this);
	}
	
	public void stopLSP() {
		if (serverServiceIntent == null) {
			dispatchOnShutdown();
			return;
		}
		
		ActivityUtils.getInstance().unbindService(this);
	}
	
	private BaseLSPService SERVICE_INSTANCE;
	@Override
	public void onServiceConnected(ComponentName cn, IBinder binder) {
		SERVICE_INSTANCE = ((BaseLSPBinder) binder).getInstance();
		dispatchOnReady();
	}
	
	@Override
	public void onServiceDisconnected(ComponentName cn) {
		dispatchOnShutdown();
	}
	
	public boolean isServiceRunning() {
		if (SERVICE_INSTANCE == null) { return false; }
		return true;
	}
	
	private ArrayList<ILanguageServiceCallback> lspCallbackArr = new ArrayList<>();
	public void addListener(ILanguageServiceCallback lspCallback) {
		lspCallbackArr.add(lspCallback);
		if (isServiceRunning() || serverServiceIntent == null) {
			lspCallback.onReady();
		}
	}
	
	public void dispatchOnReady() {
		lspCallbackArr.forEach(ILanguageServiceCallback::onReady);
	}
	public void dispatchOnShutdown() {
		lspCallbackArr.forEach(ILanguageServiceCallback::onShutdown);
	}
}
