package com.thatmg393.esmanager.models;

public class ProjectModel {
    public final String projectName;
    public final String projectPath;
    public final String projectVersion;
	public final String projectAuthor;

    public ProjectModel(String projectName, String projectPath, String projectVersion, String projectAuthor) {
        this.projectName = projectName;
        this.projectPath = projectPath;
        this.projectVersion = projectVersion;
		this.projectAuthor = projectAuthor;
    }
}