package com.thatmg393.esmanager.managers;

import com.thatmg393.esmanager.interfaces.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.lsp.lua.LuaLSPService;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.models.ProjectModel;
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
        if (INSTANCE == null) { throw new RuntimeException("Initialize first, use 'LSPManager#initializeInstance()'"); }
        return INSTANCE;
    }

    public static synchronized LSPManager initializeInstance() {
        if (INSTANCE == null) INSTANCE = new LSPManager();
        return INSTANCE;
    }
	
	private HashMap<String, LanguageServerModel> languageServerRegistry = new HashMap<String, LanguageServerModel>();
	private ProjectModel currentProject;
	
    private LSPManager() {
		if (INSTANCE != null) throw new RuntimeException("Please use 'LSPManager#getInstance()'!");
    }
	
	public void startLSPForAllLanguage() {
		for (String language : languageServerRegistry.keySet()) {
			startLSPForLanguage(language);
		}
	}

    public void startLSPForLanguage(String language) {
		LOG.d("Starting LSP for language: " + language);
		
		LanguageServerModel lsm = languageServerRegistry.get(language);
		if (lsm != null) lsm.startLSP();
		else LOG.d("No LSP for language: " + language);
	}
	
	public void stopLSPServices() {
		languageServerRegistry.values().forEach(LanguageServerModel::stopLSP);
	}
	
	public void registerNewLSPServer(String language, LanguageServerModel lspModel) {
		languageServerRegistry.put(language, lspModel);
	}
	
	public LanguageServerModel getLanguageServer(String language) {
		return languageServerRegistry.get(language);
	}
	
	public void dispose() {
		stopLSPServices(); // Sanity
		languageServerRegistry.clear();
		INSTANCE = null;
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
	
	public void setCurrentProject(ProjectModel newProject) {
		this.currentProject = newProject;
	}
	
	public ProjectModel getCurrentProject() {
		return this.currentProject;
	}
}
