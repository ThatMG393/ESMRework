package com.thatmg393.esmanager.managers.editor.project;

import androidx.annotation.NonNull;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.models.ProjectModel;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import io.github.rosemoe.sora.lsp.editor.LspProject;

public class ProjectManager {
	private static final Logger LOG = new Logger("ESM/ProjectManager");
	private static volatile ProjectManager INSTANCE;

	public static synchronized ProjectManager getInstance() {
		if (INSTANCE == null) INSTANCE = new ProjectManager();
		return INSTANCE;
	}
	
	private ProjectManager() {
		if (INSTANCE != null) throw new RuntimeException("Please use 'ProjectManager#getInstance()'!");
	}
	
	private ProjectModel currentProject = null;
	private LspProject currentProjectAsLsp = null;
	
	public void addServerDefinition(@NonNull LanguageServerDefinition serverDefinition) {
		currentProjectAsLsp.addServerDefinition(serverDefinition);
	}
	
	public void setCurrentProject(@NonNull ProjectModel newProject) {
		this.currentProject = newProject;
		this.currentProjectAsLsp = new LspProject(newProject.projectPath);
	}
	
	public ProjectModel getCurrentProject() {
		return this.currentProject;
	}
	
	public LspProject getCurrentLspProject() {
		return this.currentProjectAsLsp;
	}
}
