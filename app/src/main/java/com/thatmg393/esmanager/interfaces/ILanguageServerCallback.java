package com.thatmg393.esmanager.interfaces;

public interface ILanguageServerCallback {
	public default void onStart() { }
	public default void onShutdown() { }
}
