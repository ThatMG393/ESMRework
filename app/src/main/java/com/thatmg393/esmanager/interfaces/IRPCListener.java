package com.thatmg393.esmanager.interfaces;
import com.thatmg393.esmanager.models.DiscordProfileModel;

public interface IRPCListener {
	public default void onConnected() { }
	public default void shutdown() { }
}
