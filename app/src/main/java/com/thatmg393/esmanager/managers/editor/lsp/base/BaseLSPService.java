package com.thatmg393.esmanager.managers.editor.lsp.base;

import android.app.Service;
import com.thatmg393.esmanager.interfaces.ILanguageServerCallback;

public abstract class BaseLSPService extends Service {
	public abstract boolean isServerRunning();
	
	public abstract void startLSPServer();
	public abstract void stopLSPServer();
	
	protected abstract void startServer() throws Exception;
	
	public abstract void addServerListener(ILanguageServerCallback ilsc);
}
