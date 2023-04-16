package com.thatmg393.esmanager.interfaces;

public interface IOnProcessListener  {
	public default void onProcessStarted() { }
	public default void onProcessForeground() { }
	public default void onProcessBackground() { }
	public default void onProcessDestroyed() { }
}
