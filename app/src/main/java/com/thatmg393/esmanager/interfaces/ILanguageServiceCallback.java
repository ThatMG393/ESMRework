package com.thatmg393.esmanager.interfaces;

import com.thatmg393.esmanager.utils.logging.Logger;

public interface ILanguageServiceCallback {
	public static final Logger LOG = new Logger("ESM/ILanguageServiceCallback");

	public default void onReady() {
		LOG.i("A language service has been started!");
	}
	
	public default void onShutdown() {
		LOG.e("A language service is shutting down");
	}
}
