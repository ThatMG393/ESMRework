package com.thatmg393.esmanager.models;

import android.util.ArrayMap;
import androidx.annotation.NonNull;
import com.google.gson.internal.LinkedTreeMap;
import com.thatmg393.esmanager.managers.rpc.RPCSocketClient;
import java.util.List;
import java.util.Map;

public class DiscordProfileModel {
	private final String username;
	private final String discriminator;
	private String status;
	
	private final String email;
	private final boolean hasNitro;

	// Dev Stuff
	private final String id;
	private final String avatarUrl;
	
	// Tmp Stuff
	private String oldStatus;

	public DiscordProfileModel(@NonNull ArrayMap<String, Object> dataMap) {
		Map tmpMap = (Map) ((Map) dataMap.get("d")).get("user");
		this.username = (String) tmpMap.get("username");
		this.discriminator = (String) tmpMap.get("discriminator");
		this.id = (String) tmpMap.get("id");
		
		this.hasNitro = (Boolean) tmpMap.get("premium");
		this.email = (String) tmpMap.get("email");
		
		this.avatarUrl = "https://cdn.discord.com/avatars" + this.id + "/" + tmpMap.get("avatar") + ".png";
		
		this.status = (String) ((Map) ((List) ((Map) dataMap.get("d")).get("sessions")).get(0)).get("status");
	}

	public void setNewStatus(String status) {
		this.oldStatus = this.status;
		this.status = status;
	}

	public String getUsername() {
		return this.username;
	}

	public String getDiscriminator() {
		return this.discriminator;
	}
	
	public String getFullUsername() {
		return getUsername() + "#" + getDiscriminator();
	}

	public String getStatus() {
		return this.status;
	}

	public String getEmail() {
		return this.email;
	}

	public String getId() {
		return this.id;
	}

	public String getOldStatus() {
		return this.oldStatus;
	}
}
