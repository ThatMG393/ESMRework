package com.thatmg393.esmanager.utils;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.thatmg393.esmanager.GlobalConstants;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedPreference {
	private static final Logger LOG = new Logger("ESM/SharedPreference");

    private static volatile SharedPreference INSTANCE;

    public static synchronized SharedPreference getInstance() {
        if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'SharedPreference#initializeInstance(AppCompatActivity)'"); }

        return INSTANCE;
    }

    public static synchronized SharedPreference initializeInstance(@NotNull AppCompatActivity context) {
        if (INSTANCE == null) {
			try {
				INSTANCE = new SharedPreference(context);
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize the SharedPreference class!");
			}
		}
		
        return INSTANCE;
    }

    private final AppCompatActivity context;
    private final SharedPreferences SP_INSTANCE;
	
    private SharedPreference(AppCompatActivity context) throws GeneralSecurityException, IOException {
        if (INSTANCE != null) { throw new RuntimeException("Please use 'SharedPreference#getInstance()'!"); }

        this.context = context;
		
		MasterKey mk = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
		this.SP_INSTANCE = EncryptedSharedPreferences.create(context, GlobalConstants.getInstance().PREFERENCE_NAME, mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
	}
	
	public final void putBool(String key, boolean state) {
		SP_INSTANCE.edit().putBoolean(key, state).apply();
	}
	
	public final boolean getBoolFallback(String key, boolean fallback) {
		return SP_INSTANCE.getBoolean(key, fallback);
	}
	
	public final boolean getBool(String key) {
		return getBoolFallback(key, false);
	}
	
	public final void putString(String key, String value) {
		SP_INSTANCE.edit().putString(key, value).apply();
	}
	
	public final String getStringFallback(String key, String fallback) {
		return SP_INSTANCE.getString(key, fallback);
	}
	
	public final String getString(String key) {
		return getStringFallback(key, null);
	}
	
	public static void dispose() {
		INSTANCE = null;
	}
}
