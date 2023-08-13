package com.thatmg393.esmanager;

import android.app.Application;
import android.content.Context;

import com.thatmg393.esmanager.managers.editor.language.LanguageManager;
import com.thatmg393.esmanager.utils.EditorUtils;

import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;

public class MainApplication extends Application {
	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		FileProviderRegistry.getInstance().addFileProvider(
			new AssetsFileResolver(getAssets())
		);
		
		try {
			GrammarRegistry.getInstance().loadGrammars("tm/languages/languages.json");
			EditorUtils.loadTMThemes();
			LanguageManager.getInstance().registerLanguages();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
