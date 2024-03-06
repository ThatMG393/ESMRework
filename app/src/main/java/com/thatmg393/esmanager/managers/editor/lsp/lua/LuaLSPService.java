package com.thatmg393.esmanager.managers.editor.lsp.lua;

import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.MainThread;

import com.tang.vscode.LuaLanguageClient;
import com.tang.vscode.LuaLanguageServer;
import com.thatmg393.esmanager.interfaces.ILanguageServerCallback;
import com.thatmg393.esmanager.managers.editor.lsp.LSPManager;
import com.thatmg393.esmanager.managers.editor.lsp.base.BaseLSPBinder;
import com.thatmg393.esmanager.managers.editor.lsp.base.BaseLSPService;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.esmanager.utils.threading.ThreadPlus;

import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LuaLSPService extends BaseLSPService {
	private final Logger LOG = new Logger("ESM/LSPManager.LSPService");
	private final LuaLSPBinder binder = new LuaLSPBinder();
	
	private volatile boolean isServerRunning;
	
// Client variables {
	private ThreadPlus serverThread;
	private ExecutorService jsonRPCThreadPool = Executors.newCachedThreadPool();
	private volatile AsynchronousSocketChannel serverClientSocket;
// }
	
// Server variables {
	private volatile AsynchronousServerSocketChannel serverSocket;
	
	private volatile InputStream serverIS;
	private volatile OutputStream serverOS;
	
	private volatile LuaLanguageServer luaServer = new LuaLanguageServer();
// }
	
	@Override
	public IBinder onBind(Intent smth) {
		startLSPServer();
		return binder;
	}

	@Override
	public boolean onUnbind(Intent smth) {
		stopLSPServer();
		stopSelf();
		
		return true;
	}

	@Override
	public void onCreate() {
		serverThread = new ThreadPlus(() -> {
			try {
				initializeServer();
				startServer();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ActivityUtils.getInstance().runOnUIThread(() -> fullyCloseServer());
			}
		}) {
			@Override
			public void stop() {
				fullyCloseServer();
				super.stop();
			}
		};
	}
	
	@Override
	public void startLSPServer() {
		LOG.i("Starting Lua LSP Server");
		if (!serverThread.isRunning()) serverThread.start();
	}
	
	@Override
	public void stopLSPServer() {
		LOG.i("Gracefully shutting down the Lua LSP Server");
		if (serverThread.isRunning()) serverThread.stop();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void startServer() throws Exception {
		callbackOnStart();
		serverClientSocket = serverSocket.accept().get();
		
		while (serverThread.isRunning()) {
			if (!isServerRunning()) {
				serverIS = Channels.newInputStream(serverClientSocket);
				serverOS = Channels.newOutputStream(serverClientSocket);
				
				Launcher serverLauncher = new Launcher.Builder()
					.setLocalService(luaServer)
					.setRemoteInterface(LuaLanguageClient.class)
					.setInput(serverIS)
					.setOutput(serverOS)
					.setExecutorService(jsonRPCThreadPool)
					.create();
				
				luaServer.connect((LuaLanguageClient) serverLauncher.getRemoteProxy());
				serverLauncher.startListening();
				
				isServerRunning = true;
			}
		}
	}
	
	private synchronized void fullyCloseServer() {
		try {
			if (luaServer != null) luaServer.shutdown();
			
			if (serverIS != null) serverIS.close();
			if (serverOS != null) serverOS.close();
			
			jsonRPCThreadPool.shutdown();
			
			if (serverSocket != null) serverSocket.close();
			if (serverClientSocket != null) serverClientSocket.close();
		} catch (IOException ignore) { }
		
		isServerRunning = false;
		callbackOnShutdown();
	}
	
	private synchronized void initializeServer() throws Exception {
		int serverPort = LSPManager.getInstance().getLanguageServer("lua").getServerPort();
		
		if (serverSocket == null) {
			LOG.d("Binding serverSocket to 'localhost:" + serverPort + "'");
			
			serverSocket = AsynchronousServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress("localhost", serverPort));
		}
	}
	
	@Override
	public boolean isServerRunning() {
		return isServerRunning && serverThread.isRunning();
	}
	
	@Override
	public void addServerListener(ILanguageServerCallback ilsc) {
		listeners.add(ilsc);
		
		if (isServerRunning) ilsc.onStart();
	}
	
	@MainThread
	private void callbackOnStart() {
		listeners.forEach(ILanguageServerCallback::onStart);
	}
	
	@MainThread
	private void callbackOnShutdown() {
		listeners.forEach(ILanguageServerCallback::onShutdown);
	}
	
	private class LuaLSPBinder extends BaseLSPBinder {
		@Override
		public LuaLSPService getInstance() {
			return LuaLSPService.this;
		}
	}
}
