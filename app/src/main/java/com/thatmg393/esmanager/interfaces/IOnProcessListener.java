package com.thatmg393.esmanager.interfaces;

import com.thatmg393.esmanager.interfaces.impl.OnProcessListenerImpl;

public interface IOnProcessListener extends OnProcessListenerImpl  {
	@Override
	public default void onProcessStarted() { }
	
	@Override
	public default void onProcessForeground() { }
	
	@Override
	public default void onProcessBackground() { }
	
	@Override
	public default void onProcessDestroyed() { }
}
