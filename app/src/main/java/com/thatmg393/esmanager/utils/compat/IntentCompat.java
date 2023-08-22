package com.thatmg393.esmanager.utils.compat;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

import android.content.Intent;
import java.io.Serializable;

public class IntentCompat {
	public static Object getSerializableExtra(Intent intent, String key, Class<? extends Serializable> clazz) {
		if (SDK_INT >= VERSION_CODES.TIRAMISU) return intent.getSerializableExtra(key, clazz);
		else return intent.getSerializableExtra(key);
	}
}
