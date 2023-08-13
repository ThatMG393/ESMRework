package com.thatmg393.esmanager.managers.editor.language;

import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;

import java.util.HashMap;

public class LanguageManager {
	private static volatile LanguageManager INSTANCE;

	public static synchronized LanguageManager getInstance() {
		if (INSTANCE == null) INSTANCE = new LanguageManager();
		return INSTANCE;
	}
	
	private HashMap<String, Language> languageRegistry = new HashMap<>();
	
	private LanguageManager() { }
	
	@Nullable
	public Language getLanguage(String name) {
		return languageRegistry.get(name);
	}
	
	public void registerLanguages() {
		languageRegistry.put("lua", TextMateLanguage.create("source.lua", true));
		languageRegistry.put("json", TextMateLanguage.create("source.json", true));
	}
}
