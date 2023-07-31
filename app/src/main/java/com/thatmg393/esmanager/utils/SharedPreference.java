package com.thatmg393.esmanager.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import java.util.Set;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.thatmg393.esmanager.GlobalConstants;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedPreference {
	private static final Logger LOG = new Logger("ESM/SharedPreference");

    private static volatile SharedPreference INSTANCE;
    public static synchronized SharedPreference getInstance() {
        if (INSTANCE == null) {
			try { INSTANCE = new SharedPreference(); }
			catch (Exception e) {
				throw new RuntimeException("Failed to initialize SharedPreference!");
			}
		}

        return INSTANCE;
    }

    private final SharedPreferences SP_INSTANCE;
	private final EncryptedPreferenceDataStore preferenceDataStore;
    private SharedPreference() throws GeneralSecurityException, IOException {
        if (INSTANCE != null) { throw new RuntimeException("Please use 'SharedPreference#getInstance()'!"); }

		MasterKey mk = new MasterKey.Builder(ActivityUtils.getInstance().getRegisteredActivity(), MasterKey.DEFAULT_MASTER_KEY_ALIAS).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
		this.preferenceDataStore = new EncryptedPreferenceDataStore(EncryptedSharedPreferences.create(ActivityUtils.getInstance().getRegisteredActivity(), GlobalConstants.PREFERENCE_NAME, mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM));
		this.SP_INSTANCE = preferenceDataStore.getSharedPreferences();
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
	
	public EncryptedPreferenceDataStore getDefaultEncryptedPreferenceDataStore() {
		return preferenceDataStore;
	}
	
	public static class EncryptedPreferenceDataStore extends PreferenceDataStore {
   	 private final SharedPreferences mSharedPreferences;
		
   	 public EncryptedPreferenceDataStore(@NonNull SharedPreferences sharedPreferences) {
     	   mSharedPreferences = sharedPreferences;
    	}

   	 @NonNull
  	  public SharedPreferences getSharedPreferences() {
    	    return mSharedPreferences;
    	}

   	 @Override
   	 public void putString(String key, @Nullable String value) {
    	    mSharedPreferences.edit().putString(key, value).apply();
   	 }

   	 @Override
   	 public void putStringSet(String key, @Nullable Set<String> values) {
     	   mSharedPreferences.edit().putStringSet(key, values).apply();
    	}

    	@Override
   	 public void putInt(String key, int value) {
      	  mSharedPreferences.edit().putInt(key, value).apply();
   	 }

    	@Override
    	public void putLong(String key, long value) {
    	    mSharedPreferences.edit().putLong(key, value).apply();
    	}

    	@Override
    	public void putFloat(String key, float value) {
    	    mSharedPreferences.edit().putFloat(key, value).apply();
    	}

    	@Override
   	 public void putBoolean(String key, boolean value) {
      	  mSharedPreferences.edit().putBoolean(key, value).apply();
    	}

   	 @Nullable
   	 @Override
   	 public String getString(String key, @Nullable String defValue) {
    	    return mSharedPreferences.getString(key, defValue);
   	 }

    	@Nullable
    	@Override
   	 public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
       	 return mSharedPreferences.getStringSet(key, defValues);
    	}

   	 @Override
    	public int getInt(String key, int defValue) {
      	  return mSharedPreferences.getInt(key, defValue);
    	}

   	 @Override
  	  public long getLong(String key, long defValue) {
       	 return mSharedPreferences.getLong(key, defValue);
   	 }

   	 @Override
    	public float getFloat(String key, float defValue) {
      	  return mSharedPreferences.getFloat(key, defValue);
    	}

   	 @Override
  	  public boolean getBoolean(String key, boolean defValue) {
     	   return mSharedPreferences.getBoolean(key, defValue);
   	 }
	}
}
