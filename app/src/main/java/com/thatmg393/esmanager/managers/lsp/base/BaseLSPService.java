package com.thatmg393.esmanager.managers.lsp.base;

import android.app.Service;

public abstract class BaseLSPService extends Service {
	public abstract boolean isServerRunning();
	
	public abstract void startLSPServer();
	public abstract void stopLSPServer();
	
	protected abstract void startServer() throws Exception;
}
