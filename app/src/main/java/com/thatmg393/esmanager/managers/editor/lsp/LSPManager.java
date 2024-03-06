package com.thatmg393.esmanager.managers.editor.lsp;

import androidx.annotation.Nullable;

import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.managers.editor.lsp.lua.LuaLSPService;
import com.thatmg393.esmanager.managers.editor.project.ProjectManager;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.models.ProjectModel;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.esmanager.utils.io.NetworkUtils;

import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.lsp.editor.LspProject;
import java.util.HashMap;

public class LSPManager {
	private static final Logger LOG = new Logger("ESM/LSPManager");
	private static volatile LSPManager INSTANCE;

	public static synchronized LSPManager getInstance() {
		if (INSTANCE == null) INSTANCE = new LSPManager();
		return INSTANCE;
	}
	
	private HashMap<String, LanguageServerModel> languageServerRegistry = new HashMap<>();
	
	private LSPManager() {
		if (INSTANCE != null) throw new RuntimeException("Please use 'LSPManager#getInstance()'!");
	}
	
	public void startLSPForAllLanguage() {
		languageServerRegistry.keySet().forEach((language) -> startLSPForLanguage(language));
	}
	
	public void stopLSPForAllLanguage() {
		languageServerRegistry.keySet().forEach((language) -> stopLSPForLanguage(language));
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
	
	public void registerNewLSPServer(LspProject project, String language, LanguageServerModel lspModel) {
		languageServerRegistry.put(language, lspModel);
		project.addServerDefinition(lspModel.getServerDefinition());
	}
	
	public void registerLangServers() {
		/* Register a language server
		registerNewLSPServer("languagename", 
			LanguageLSPService.class,
			NetworkUtils.generateRandomPort()
		);
		*/
		
		LspProject project = ProjectManager.getInstance().getCurrentLspProject();
		
		registerNewLSPServer(
			project, "lua",
			new LanguageServerModel(
				"lua",
				LuaLSPService.class,
				NetworkUtils.generateRandomPort()
			)
		);
	}
}
