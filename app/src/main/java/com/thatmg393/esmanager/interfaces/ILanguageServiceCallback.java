package com.thatmg393.esmanager.interfaces.impl;
import com.thatmg393.esmanager.managers.lsp.base.BaseLSPService;
import com.thatmg393.esmanager.utils.Logger;

public interface ILanguageServiceCallback {
	public static final Logger LOG = new Logger("ESM/LanguageServiceCallbackImpl");

	public default void onReady() {
		LOG.i("Language service started!");
	}
	
	public default void onShutdown() {
		LOG.e("Language service SHUTDOWN!");
	}
}
