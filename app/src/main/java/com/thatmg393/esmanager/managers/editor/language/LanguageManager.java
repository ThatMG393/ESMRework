package com.thatmg393.esmanager.managers.editor.language;

import android.content.res.AssetManager;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.itsaky.androidide.treesitter.TreeSitter;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.treesitter.lua.sora.LuaLanguageSpec;
import com.thatmg393.treesitter.lua.sora.TsLanguageLua;

import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class LanguageManager {
	private static final Logger LOG = new Logger("ESM/LanguageManager");
	private static volatile LanguageManager INSTANCE;

	public static synchronized LanguageManager getInstance() {
		if (INSTANCE == null) INSTANCE = new LanguageManager();
		return INSTANCE;
	}
	
	private ArrayMap<String, Supplier<? extends Language>> languageRegistry = new ArrayMap<>();
	
	private LanguageManager() { }
	
	@Nullable
	public Language getLanguage(String name) {
		Supplier<? extends Language> lambda = languageRegistry.get(name);
		if (lambda == null) {
			System.out.println("Lambda null IMPASSABAL!!! does it relly exists?" + languageRegistry.containsKey(name));
			return new EmptyLanguage();
		}
		
		return lambda.get();
	}
	
	public void initTreeSitter() {
		TreeSitter.loadLibrary();
		LOG.d("TreeSitter v" + TreeSitter.getLanguageVersion() + " loaded!");
	}
	
	public void registerTreeSitterLanguages(AssetManager assets) {
		initTreeSitter();
		
		languageRegistry.put("lua", () -> {
			return new TsLanguageLua(
				new LuaLanguageSpec(
					loadScheme(assets, "lua", "highlights"),
					loadScheme(assets, "lua", "locals"),
					loadScheme(assets, "lua", "indents")
				), true
			);
		});
	}
	
	public void registerTextMateLanguages() {
		// languageRegistry.put("lua", () -> TextMateLanguage.create("source.lua", true) );
		languageRegistry.put("json", () -> TextMateLanguage.create("source.json", true) );
	}
	
	private String loadScheme(AssetManager assets, String language, String type) {
		try {
		    return new String(assets.open(language + "/" + type + ".scm").readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOG.w("Scheme " + type + " for " + language + " not found.");
			return "";
		}
	}
}
