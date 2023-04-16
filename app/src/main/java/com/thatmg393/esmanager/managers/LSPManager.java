package com.thatmg393.esmanager.managers;

import com.thatmg393.esmanager.interfaces.impl.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.lsp.LuaLSPService;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.Logger;

import com.thatmg393.esmanager.utils.NetworkUtils;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class LSPManager {
    private static final Logger LOG = new Logger("ESM/LSPManager");

    private static volatile LSPManager INSTANCE;

    public static synchronized LSPManager getInstance() {
        if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'LSPManager#initializeInstance(MainActivity)'"); }
        return INSTANCE;
    }

    public static synchronized LSPManager initializeInstance() {
        if (INSTANCE == null) INSTANCE = new LSPManager();
        return INSTANCE;
    }
	
	public HashMap<String, LanguageServerModel> languageServerRegistry = new HashMap<String, LanguageServerModel>();
	
    private LSPManager() {
		if (INSTANCE != null) throw new RuntimeException("Please use 'LSPManager#getInstance()'!");
		
		// Register a language server
		/*
		registerNewLSPServer("languagename", 
			LanguageLSPService.class,
			NetworkUtils.generateRandomPort()
		);
		*/
		
		registerNewLSPServer("lua",
			new LanguageServerModel(
				"lua",
				LuaLSPService.class,
				NetworkUtils.generateRandomPort()
			)
		);
    }
	
	public void startLSPForAll() {
		for (String language : languageServerRegistry.keySet()) {
			startLSPForLanguage(language);
		}
	}

    public void startLSPForLanguage(String language) {
		LOG.d("Starting LSP for language: " + language);
		
		languageServerRegistry.get(language).startLSPService();
	}
	
	public void stopLSPServices() {
		languageServerRegistry.values().forEach(LanguageServerModel::stopLSPService);
	}
	
	public void registerNewLSPServer(String language, LanguageServerModel lspModel) {
		languageServerRegistry.put(language, lspModel);
	}
	
	public void dispose() {
		stopLSPServices(); // Sanity
		languageServerRegistry.clear();
		INSTANCE = null;
	}
	
	public void addListener(String language, ILanguageServiceCallback lspCallback) {
		languageServerRegistry.get(language).addListener(lspCallback);
	}
}
