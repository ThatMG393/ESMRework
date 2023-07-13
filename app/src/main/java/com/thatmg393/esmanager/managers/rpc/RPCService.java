package com.thatmg393.esmanager.managers.rpc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thatmg393.esmanager.R;
import com.thatmg393.esmanager.interfaces.IRPCListener;
import com.thatmg393.esmanager.managers.DRPCManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.SharedPreference;
import com.thatmg393.esmanager.utils.ThreadPlus;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Queue;

@SuppressWarnings("deprecation")
public class RPCService extends Service {
	private static final String CHANNEL_ID = "DRPCService";
	private static final int NOTIFICATION_ID = 1;
	
	private final RPCBinder binder = new RPCBinder();
	
	private ThreadPlus websocketThread;
	private RPCSocketClient rpcWebsocketClient;
	
	private NotificationCompat.Builder notificationBuilder;
	
	@Override
	public IBinder onBind(Intent smth) {
		startRPC();
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent smth) {
		websocketThread.kill();
		getNotificationManager().cancel(NOTIFICATION_ID);
		
		stopSelf();
		return true;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		ActivityUtils.getInstance().createNotificationChannel(CHANNEL_ID, "Discord Rich Presence Service", NotificationManager.IMPORTANCE_DEFAULT);
		
		try {
			if (rpcWebsocketClient == null) rpcWebsocketClient = new RPCSocketClient(this);
		} catch (URISyntaxException ignore) { }
		
		this.websocketThread = new ThreadPlus(() -> {
			try {
				if (!rpcWebsocketClient.isOpen()) rpcWebsocketClient.connectBlocking();
			} catch (InterruptedException ignore) { }
		}) {
			@Override
			public void stop() {
				super.stop();
				rpcWebsocketClient.close();
			}
		};
		
		websocketThread.stop();
		
		this.notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("Starting RPC...")
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.setWhen(System.currentTimeMillis())
				.setOngoing(true)
				.setAutoCancel(false)
				.setAllowSystemGeneratedContextualActions(false);
				
		Notification serviceNotification = notificationBuilder.build();
		
		getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	
	public void startRPC() {
		if (SharedPreference.getInstance().getString("lol69420") != null) {
			websocketThread.start();
			return;
		}
		
		View layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.rpc_login_dialog, null, false);
		AlertDialog loginDialog = new MaterialAlertDialogBuilder(ActivityUtils.getInstance().getRegisteredActivity())
										.setView( layout )
										.setNegativeButton("Later", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int idk) {
												dialog.dismiss();
												DRPCManager.getInstance().stopDiscordRPC();
											}
										})
										.setOnCancelListener(new DialogInterface.OnCancelListener() {
											@Override
											public void onCancel(DialogInterface dialog) {
												DRPCManager.getInstance().stopDiscordRPC();
											}
										}).create();
										
		WebView webLoginView = layout.findViewById(R.id.rcp_webview);
		webLoginView.getSettings().setJavaScriptEnabled(true);
		webLoginView.getSettings().setDatabaseEnabled(true);
		webLoginView.getSettings().setDomStorageEnabled(true);
		
		webLoginView.setWebViewClient(new WebViewClient() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView v, String url) {
				if (url.endsWith("/app")) {
					SharedPreference.getInstance().putString("lol69420", extractToken());
					
					loginDialog.dismiss();
					websocketThread.start();
				}
				return true;
			}
		});
		
		int LAYOUT_FLAG = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
		loginDialog.getWindow().setType(LAYOUT_FLAG);
		loginDialog.show();
		
		webLoginView.loadUrl("https://discord.com/login");
	}
	
	private Queue<String> contentFIFO = new CircularFifoQueue<>(10);
	public void updateNotificationContent(String content) {
		contentFIFO.add(content);
		
		StringBuilder tmpSb = new StringBuilder();
		contentFIFO.forEach((value) -> {
			tmpSb.append(value + System.getProperty("line.separator"));
		});
		
		notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(tmpSb.toString()));
		getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	
	public void updateNotificationTitle(String text) {
		notificationBuilder.setContentTitle(text);
		getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	
	private NotificationManager getNotificationManager() {
		return (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private String extractToken() {
		try {
			File f = new File(getFilesDir().getParentFile(), "app_webview/Default/Local Storage/leveldb");
			File[] fArr = f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File file, String name) {
						return name.endsWith(".log");
					}
				});
			if (fArr.length == 0) return null;
			f = fArr[0];
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("token")) break;
			}
			line = line.substring(line.indexOf("token") + 5);
			line = line.substring(line.indexOf("\"") + 1);
			
			return line.substring(0, line.indexOf("\""));
		} catch (Throwable e) {
			return null;
		}
	}
	
	public ArrayList<IRPCListener> rpcListeners = new ArrayList<>();
	public void callbackOnConnected() {
		rpcListeners.forEach(IRPCListener::onConnected);
	}
	public void callbackShutdown() {
		rpcListeners.forEach(IRPCListener::shutdown);
	}
	
	public RPCSocketClient getRPCSocketClient() {
		return rpcWebsocketClient;
	}
	
	public class RPCBinder extends Binder {
		public RPCService getInstance() {
			return RPCService.this;
		}
	}
}
