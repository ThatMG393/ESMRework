package com.thatmg393.esmanager.managers.editor.project;

import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;
import com.thatmg393.esmanager.models.ProjectModel;

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
	
	public void setCurrentProject(ProjectModel newProject) {
		this.currentProject = newProject;
	}
	
	public ProjectModel getCurrentProject() {
		return this.currentProject;
	}
}
