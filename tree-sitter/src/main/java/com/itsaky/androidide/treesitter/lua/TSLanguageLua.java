package com.itsaky.androidide.treesitter.lua;

import android.util.Log;

import dalvik.annotation.optimization.FastNative;

import com.itsaky.androidide.treesitter.TSLanguage;
import com.itsaky.androidide.treesitter.TSLanguageCache;

public class TSLanguageLua {
	public static class Native {
		@FastNative
		public static native long getInstance();
	}
	
	public static TSLanguage getInstance() {
        TSLanguage tSLanguage = TSLanguageCache.get("lua");
        if (tSLanguage != null) return tSLanguage;
		
		long libHandle = Native.getInstance();
		Log.d("TSJNI", "Got TSLuaLib handle! Handle: " + libHandle);
        tSLanguage = TSLanguage.create("lua", libHandle);
        TSLanguageCache.cache("lua", tSLanguage);
		
        return tSLanguage;
    }
	
	private TSLanguageLua() {
        throw new UnsupportedOperationException();
    }
	
    static {
		Log.i("TSLanguageLua", "Loading tree-sitter-lua, my native library!");
        System.loadLibrary("tree-sitter-lua");
    }

    @Deprecated
    public static TSLanguage newInstance() {
        return getInstance();
    }
}
