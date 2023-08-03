package com.thatmg393.esmanager;

import android.app.Application;
import android.content.Context;

import com.itsaky.androidide.logsender.ILogSender;
import com.itsaky.androidide.logsender.LogSender;
import com.itsaky.androidide.logsender.utils.LogSenderInstaller;
import com.itsaky.androidide.utils.AndroidLogger;
import com.itsaky.androidide.utils.ILogger;
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
		
		GrammarRegistry.getInstance().loadGrammars("tm/languages/languages.json");
		EditorUtils.loadTMThemes();
	}
}
