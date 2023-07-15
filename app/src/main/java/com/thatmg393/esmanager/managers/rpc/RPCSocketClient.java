package com.thatmg393.esmanager.managers.rpc;

import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thatmg393.esmanager.GlobalConstants;
import com.thatmg393.esmanager.models.DiscordProfileModel;
import com.thatmg393.esmanager.utils.FileUtils;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.SharedPreference;

import com.thatmg393.esmanager.utils.ThreadPlus;
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
	private final ThreadPlus heartbeatThread;
	
	private int heartbeatInterval, sequence;
	
	public volatile DiscordProfileModel discordProfile;
	
	public boolean isConnecting;
	public boolean isConnected;

	public RPCSocketClient(RPCService serviceInstance) throws URISyntaxException {
		super(new URI("wss://gateway.discord.gg/?encoding=json&v=10"));
		
		this.serviceInstance = serviceInstance;
		this.heartbeatThread = new ThreadPlus(() -> {
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
		
		// serviceInstance.updateNotificationTitle("Connecting to Discord API...");
	}

	@Override
	public void onOpen(ServerHandshake sh) { }

	@Override
	public void onMessage(String message) {
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
				heartbeatThread.stop();
				heartbeatThread.start();
				
				sendIdentify();
				break;
			case 1: // Heartbeat Request
				heartbeatThread.stop();
				
				send("{\"op\":1, \"d\":" + ( sequence == 0 ? "null" : Integer.toString(sequence) ) + "}");
				break;
			case 11: // Heartbeat ACK
				heartbeatThread.start();
				break;
			case 9:  // Invalid Session
				heartbeatThread.kill();
				serviceInstance.callbackShutdown();
				close();
				break;
		}
	}
	
	private void onMessageDispatch(ArrayMap<String, Object> dataMap) {
		String state = (String) dataMap.get("t");
		
		switch (state) {
			case "READY": // Connected to the GATEWAY
				discordProfile = new DiscordProfileModel(dataMap);
				
				LOG.d("Connected to the Discord API Gateway!");
				serviceInstance.updateNotificationTitle("Connected to " + discordProfile.getFullUsername());
				serviceInstance.callbackOnConnected();
				
				isConnecting = false;
				isConnected = true;
				break;
			case "SESSIONS_REPLACE": // Status change like, dnd -> idle
				String currentStatus = (String) ((Map) ((List) dataMap.get("d")).get(0)).get("status");
				
				if (discordProfile.getStatus() != currentStatus) {
					LOG.d("Changed status to " + currentStatus);
					discordProfile.setNewStatus(currentStatus);
					serviceInstance.updateNotificationContent("Changed status to " + currentStatus);
				}
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
	
	@Override
	public boolean isOpen() {
		return super.isOpen() && (isConnected || isConnecting);
	}
	
	@Override
	public boolean connectBlocking() throws InterruptedException {
		isConnecting = true;
		return super.connectBlocking();
	}
	
	private void _close() {
		LOG.d("Closing RPCSocketClient");
		
		isConnected = false;
		heartbeatThread.kill();
		serviceInstance.callbackShutdown();
	}
	
	private void sendIdentify() {
		LOG.d("Sending 'Identify' payload");
		
		ArrayMap<String, String> prop = new ArrayMap<>();
		prop.put("os", "linux");
		prop.put("browser", "Discord Android");
		prop.put("device", "android");

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
		LOG.d("Sending Rich Presence");
		serviceInstance.updateNotificationContent("Sending Rich Presence");
		
		long current = System.currentTimeMillis();
		
		ArrayMap<String, Object> activity = new ArrayMap<>();
		activity.put("name", "What?");
		activity.put("state", "Sheeshable");
		activity.put("details", "I don't know");
		activity.put("type", 5);
		activity.put("application_id", "956735773716123659");
		
		// Images
		ArrayMap<String, String> assets = new ArrayMap<>();
		assets.put("large_image", processImageLink("https://media.discordapp.net/attachments/729671788187091024/1095905788763066388/Screenshot_2023-04-13-10-56-28-989_com.roblox.client.jpg"));
		assets.put("small_image", processImageLink("https://media.discordapp.net/attachments/729671788187091024/1095905788763066388/Screenshot_2023-04-13-10-56-28-989_com.roblox.client.jpg"));
		activity.put("assets", assets);

		// Buttons
		ArrayMap<String, String> button = new ArrayMap<>();
		button.put("label", "Test button 1");
		button.put("url", "https://github.com/ThatMG393/ESMRework");
		
		ArrayMap<String, String> button2 = new ArrayMap<>();
		button2.put("label", "Test button 2");
		button2.put("url", "https://github.com/ThatMG393/ESMRework");
		
		// activity.put("buttons", new Object[] { button, button2 });
		
		ArrayMap<String, Long> timestamps = new ArrayMap<>();
		timestamps.put("start", current);
		activity.put("timestamps", timestamps);
	
		ArrayMap<String, Object> presence = new ArrayMap<>();
		presence.put("activities", new Object[] { activity });
		presence.put("afk", false);
		presence.put("since", current);
		presence.put("status", (discordProfile.getStatus() == null ? "dnd" : discordProfile.getStatus()) );
		
		ArrayMap<String, Object> arr = new ArrayMap<>();
		arr.put("op", 3);
		arr.put("d", presence);
		
		send(GSON.toJson(arr));
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
