package com.thatmg393.esmanager.interfaces.impl;

import com.thatmg393.esmanager.utils.Logger;

public interface LanguageServiceCallbackImpl {
	public static final Logger LOG = new Logger("ESM/LanguageServiceCallbackImpl");
	
	public default void onReady() { }
	public default void onShutdown() { }
}
