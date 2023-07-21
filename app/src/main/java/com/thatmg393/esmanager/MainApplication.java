package com.thatmg393.esmanager;

import android.app.Application;
import android.content.Context;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import com.thatmg393.esmanager.utils.EditorUtils;

public class MainApplication extends Application {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
		
		FileProviderRegistry.getInstance().addFileProvider(
			new AssetsFileResolver(getAssets())
		);
		
		GrammarRegistry.getInstance().loadGrammars("tm/languages/languages.json");
		EditorUtils.loadTMThemes();
    }
}
