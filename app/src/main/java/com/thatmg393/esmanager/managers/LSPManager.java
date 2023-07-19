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
	private ProjectModel currentProject;
	private EditorManager editorManager = new EditorManager();
	
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
	
	public EditorManager getEditorManager() {
		return this.editorManager;
	}
	
	public ProjectModel getCurrentProject() {
		return this.currentProject;
	}
	
	public static class EditorManager {
		@Nullable
		private TabEditorFragment focusedTabEditor = null;
		
		public void setFocusedTabEditor(TabEditorFragment editor) {
			this.focusedTabEditor = editor;
		}
		
		public TabEditorFragment getFocusedTabEditor() {
			return this.focusedTabEditor;
		}
	}
}
