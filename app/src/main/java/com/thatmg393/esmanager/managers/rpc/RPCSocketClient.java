package com.thatmg393.esmanager.managers.rpc;

import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.managers.DRPCManager;
import com.thatmg393.esmanager.models.DiscordProfileModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.DaemonThread;
import com.thatmg393.esmanager.utils.FileUtils;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.SharedPreference;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

// Original: https://github.com/khanhduytran0/MRPC/blob/main/app/src/main/java/com/kdt/mrpc/DiscordSocketClient.java
public class RPCSocketClient extends WebSocketClient {
	private static final Logger LOG = new Logger("ESM/RPCSocketClient");
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

	private final RPCService serviceInstance;
	private final DaemonThread heartbeatThread;
	
	private int heartbeatInterval, sequence;
	
	public volatile DiscordProfileModel discordProfile;
	public boolean isConnected;

	public RPCSocketClient(RPCService serviceInstance) throws URISyntaxException {
		super(new URI("wss://gateway.discord.gg/?encoding=json&v=10"));
		
		this.serviceInstance = serviceInstance;
		this.heartbeatThread = new DaemonThread(() -> {
			if (isConnected) {
				try {
					if (heartbeatInterval < 10000) throw new RuntimeException("Invalid Heartbeat Interval!");
					Thread.sleep(heartbeatInterval);
				
					send(("{\"op\":1, \"d\":" + ( sequence == 0 ? "null" : Integer.toString(sequence) ) + "}"));
					
					LOG.d("Heartbeat sent! Wait for another " + heartbeatInterval + "ms");
					serviceInstance.updateNotificationContent("Heartbeat sent!");
				} catch (InterruptedException ignore) { }
			}
		});
	}

	@Override
	public void onOpen(ServerHandshake sh) { }

	@Override
	public void onMessage(String message) {
		if (Thread.currentThread().isInterrupted()) close(1);
		
		ArrayMap<String, Object> messageMap = GSON.fromJson(
			message, new TypeToken<ArrayMap<String, Object>>() { }.getType()
		);
		
		Double seqN = (Double) messageMap.get("s");
		if (seqN != null) sequence = seqN.intValue();
		
		int opcode = ((Double) messageMap.get("op")).intValue();
		switch (opcode) {
			case 0: // Dispatch Event
				onMessageDispatch(messageMap);
				
				break;
			case 10: // Hello
				Map dataMap = (Map) messageMap.get("d");
				
				heartbeatInterval = ((Double) dataMap.get("heartbeat_interval")).intValue();
				
				if (heartbeatThread.isRunning()) heartbeatThread.stop();
				heartbeatThread.start();
				
				sendIdentify();
				
				break;
			case 1: // Heartbeat Request
				if (heartbeatThread.isRunning()) heartbeatThread.stop();
				
				send("{\"op\":1, \"d\":" + ( sequence == 0 ? "null" : Integer.toString(sequence) ) + "}");
				
				break;
			case 11: // Heartbeat ACK
				if (!heartbeatThread.isRunning()) heartbeatThread.start();
				break;
		}
	}
	
	private void onMessageDispatch(ArrayMap<String, Object> dataMap) {
		String state = (String) dataMap.get("t");
		
		switch (state) {
			case "READY": // Hello
				discordProfile = new DiscordProfileModel(dataMap);
				
				LOG.d("Connected to the discord account!");
				serviceInstance.updateNotificationTitle("Connected to " + discordProfile.getFullUsername());
				serviceInstance.callbackOnConnected();
				
				isConnected = true;
				
				break;
			case "SESSIONS_REPLACE": // Status change like dnd -> idle
				Map tmpMap2 = (Map) ((List)dataMap.get("d")).get(0);
				String currentStatus = (String) tmpMap2.get("status");
				
				LOG.d("Changed status to " + currentStatus);
				serviceInstance.updateNotificationContent("Changed status to " + currentStatus);
				serviceInstance.callbackShutdown();
				
				discordProfile.setNewStatus(currentStatus);
				
				break;
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		_close();
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
	}
	
	@Override
	public void close() {
		super.close();
		_close();
	}
	
	private void _close() {
		LOG.d("Closing RPCSocketClient");
		if (heartbeatThread.isRunning()) heartbeatThread.stop();
		isConnected = false;
	}
	
	private void sendIdentify() {
		ArrayMap<String, Object> prop = new ArrayMap<>();
		prop.put("$os", "linux");
		prop.put("$browser", "Discord Android");
		prop.put("$device", "unknown");

		ArrayMap<String, Object> data = new ArrayMap<>();
		data.put("token", SharedPreference.getInstance().getString("lol69420"));
		data.put("properties", prop);
		data.put("compress", false);
		data.put("intents", 0);

		ArrayMap<String, Object> arr = new ArrayMap<>();
		arr.put("op", 2);
		arr.put("d", data);
		
		send(GSON.toJson(arr));
	}
	
	public void sendPresence() {
		long current = System.currentTimeMillis();

		ArrayMap<String, Object> presence = new ArrayMap<>();
		
		ArrayMap<String, Object> activity = new ArrayMap<>();
		activity.put("name", "Test Name");
  	  activity.put("state", "Test State");
		activity.put("details", "Test Details");
		activity.put("type", 0);
		activity.put("application_id", "956735773716123659");
		
		// Images
		ArrayMap<String, Object> assets = new ArrayMap<>();
		assets.put("large_image", processImageLink("https://media.discordapp.net/attachments/729671788187091024/1095905788763066388/Screenshot_2023-04-13-10-56-28-989_com.roblox.client.jpg"));
		assets.put("small_image", processImageLink("https://media.discordapp.net/attachments/729671788187091024/1095905788763066388/Screenshot_2023-04-13-10-56-28-989_com.roblox.client.jpg"));
		activity.put("assets", assets);

		// Buttons
		ArrayMap<String, Object> button = new ArrayMap<>();
		button.put("label", "Test button 1");
		button.put("url", "https://github.com");
		
		activity.put("buttons", button);

		ArrayMap<String, Object> timestamps = new ArrayMap<>();
		timestamps.put("start", current);
		activity.put("timestamps", timestamps);

		presence.put("activities", new Object[] {activity});
		presence.put("afk", true);
		presence.put("since", current);
		presence.put("status", "dnd");

		ArrayMap<String, Object> arr = new ArrayMap<>();
		arr.put("op", 3);
		arr.put("d", presence);
		
		String t = GSON.toJson(arr);
		LOG.d(t);
		send(t);
	}
	
	private String processImageLink(String link) {
		if (link.isEmpty()) return null;
		if (link.contains("://")) link = link.split("://")[1];
		if (link.startsWith("media.discordapp.net/")) return link.replace("media.discordapp.net/", "mp:");
		else if (link.startsWith("cdn.discordapp.com")) {
			// Trick: allow using CDN URL for custom image
			// https://cdn.discordapp.com/app-assets/application-id/../../whatever.png_or_gif#.png
			// ".." resolves to the parent directory
			// "#" at the end to exclude ".png" from the link
			// so it becomes
			// https://cdn.discordapp.com/whatever.png_or_gif
			return link.replace("cdn.discordapp.com/", "../../") + "#";
		}
		return link;
	}
}
