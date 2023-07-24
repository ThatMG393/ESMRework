package com.thatmg393.esmanager.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class ProjectModel implements Serializable {
    public final ModPropertiesModel projectProperties;
	public final String projectPath;

    public ProjectModel(
		@NonNull ModPropertiesModel projectProperties,
		@NonNull String projectPath
	) {
		this.projectProperties = projectProperties;
        this.projectPath = projectPath;
    }
}
