package com.thatmg393.esmanager.interfaces.impl;

import com.thatmg393.esmanager.interfaces.impl.LanguageServiceCallbackImpl;

public interface ILanguageServiceCallback extends LanguageServiceCallbackImpl {
	@Override
	public default void onReady() {
		LOG.i("Language service started!");
	}
	
	@Override
	public default void onShutdown() {
		LOG.e("Language service SHUTDOWN!");
	}
}
