package com.thatmg393.esmanager.managers;

import androidx.annotation.Nullable;

import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.managers.lsp.lua.LuaLSPService;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.models.ProjectModel;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.NetworkUtils;

import java.util.HashMap;

public class LSPManager {
    private static final Logger LOG = new Logger("ESM/LSPManager");
    private static volatile LSPManager INSTANCE;

    public static synchronized LSPManager getInstance() {
        if (INSTANCE == null) INSTANCE = new LSPManager();
        return INSTANCE;
    }
	
	private HashMap<String, LanguageServerModel> languageServerRegistry = new HashMap<String, LanguageServerModel>();
	
    private LSPManager() {
		if (INSTANCE != null) throw new RuntimeException("Please use 'LSPManager#getInstance()'!");
    }
	
	public void startLSPForAllLanguage() {
		languageServerRegistry.keySet().forEach((language) -> startLSPForLanguage(language));
	}
	
	public void stopLSPForAllLanguage() {
		languageServerRegistry.values().forEach((language) -> stopLSPForLanguage(language));
	}
	
	public void startLSPForLanguage(String language) {
		LOG.d("Starting LSP for language: " + language);
		
		LanguageServerModel lsm = languageServerRegistry.get(language);
		
		if (lsm != null) lsm.startLSP();
		else LOG.d("No LSP for language: " + language);
	}
	
	public void stopLSPForLanguage(String language) {
		LOG.d("Stopping LSP for language: " + language);
		
		LanguageServerModel lsm = languageServerRegistry.get(language);
		
		if (lsm != null) lsm.stopLSP();
		else LOG.d("No LSP for language: " + language);
	}
	
	public LanguageServerModel getLanguageServer(String language) {
		return languageServerRegistry.get(language);
	}
	
	public void registerNewLSPServer(String language, LanguageServerModel lspModel) {
		languageServerRegistry.put(language, lspModel);
	}
	
	public void registerLangServers() {
		/* Register a language server
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
}
