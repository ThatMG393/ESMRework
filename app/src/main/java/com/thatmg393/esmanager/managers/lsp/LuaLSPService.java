package com.thatmg393.esmanager.managers.lsp;

import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.tang.vscode.LuaLanguageClient;
import com.tang.vscode.LuaLanguageServer;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.managers.lsp.base.BaseLSPBinder;
import com.thatmg393.esmanager.managers.lsp.base.BaseLSPService;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.Logger;

import io.github.rosemoe.sora.lsp.editor.LspEditorManager;

import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class LuaLSPService extends BaseLSPService {
    private final Logger LOG = new Logger("ESM/LSPManager.LSPService");
    private final LuaLSPBinder binder = new LuaLSPBinder();
	
	private boolean isServerThreadAlive;
    private volatile boolean isServerRunning;
	
	private Thread serverThread;
	
// Server variables {
	private ServerSocket serverSocket;
	private Socket serverClientSocket;
	
	private InputStream serverIS;
	private OutputStream serverOS;
	
	private LuaLanguageServer luaServer = new LuaLanguageServer();
// }
	
    @Override
    public IBinder onBind(Intent smth) { return binder; }

    @Override
    public boolean onUnbind(Intent smth) {
        stopLSPServer();
		
        stopSelf();
        return true;
    }

    @Override
    public void onCreate() {
        serverThread = new Thread(() -> {
			isServerThreadAlive = true;
			
			try {
				initializeServer();
				startServer();
			} catch (Exception e) {
				e.printStackTrace();
				ActivityUtils.getInstance().runOnUIThread(() -> {
					Toast.makeText(getApplicationContext(), "LSP failed!", Toast.LENGTH_SHORT).show();
				});
			}
				
			isServerThreadAlive = false;
		});
    }
	
	@Override
    public void startLSPServer() {
        LOG.i("Starting Lua LSP Server");
        if (!isServerThreadAlive) serverThread.start();
    }
	
	@Override
	public void stopLSPServer() {
		LOG.i("Gracefully shutting down the Lua LSP Server");
		if (isServerThreadAlive) serverThread.interrupt();
	}

    @Override
    protected void startServer() throws Exception {
		LOG.d("Get server connections...");
		serverClientSocket = serverSocket.accept();
		
		while (!Thread.currentThread().isInterrupted()) {
			if (!isServerRunning()) {
				serverIS = serverClientSocket.getInputStream();
				serverOS = serverClientSocket.getOutputStream();
				
				Launcher serverLauncher = Launcher.createLauncher(luaServer, LuaLanguageClient.class, serverIS, serverOS);
				
				luaServer.connect((LuaLanguageClient) serverLauncher.getRemoteProxy());
				
				serverLauncher.startListening().get(Long.MAX_VALUE, TimeUnit.SECONDS);
				isServerRunning = true;
				
				LOG.d("Server is up and running!");
			}
		}
		
		fullyCloseServer();
    }
	
	private void fullyCloseServer() {
		LOG.d("Closing server");
		luaServer.shutdown();
		
		try {
			if (serverIS != null) serverIS.close();
			if (serverOS != null) serverOS.close();
			
			if (serverSocket != null) serverSocket.close();
			if (serverClientSocket != null) serverClientSocket.close();
		} catch (IOException ignore) { }
		
		isServerRunning = false;
	}
	
	private synchronized void initializeServer() throws Exception {
		int serverPort = LSPManager.getInstance().languageServerRegistry.get("lua").getLspPort();
		
		if (serverSocket == null) {
			LOG.d("Binding serverSocket to 'localhost:" + serverPort + "'");
			
			// serverSocket = AsynchronousServerSocketChannel.open();
			// serverSocket.bind(new InetSocketAddress("localhost", serverPort));
			
			serverSocket = new ServerSocket(serverPort);
		}
	}
	
	private class LuaLSPBinder extends BaseLSPBinder {
		@Override
		public LuaLSPService getInstance() {
			return LuaLSPService.this;
		}
	}
	
	@Override
    public boolean isServerRunning() {
        return isServerRunning;
    }
}
