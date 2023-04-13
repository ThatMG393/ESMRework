package com.thatmg393.esmanager.managers.lsp.base;

import android.os.Binder;

public abstract class BaseLSPBinder<T extends BaseLSPService> extends Binder {
	public abstract T getInstance();
}
