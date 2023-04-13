package com.thatmg393.esmanager.interfaces.impl;

public interface OnProcessListenerImpl {
	public default void onProcessStarted() { }
	public default void onProcessForeground() { }
	public default void onProcessBackground() { }
	public default void onProcessDestroyed() { }
}
