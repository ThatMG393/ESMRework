package com.thatmg393.esmanager.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class ModPropertiesModel implements Serializable {
	private final String MOD_NAME;
	private final String MOD_DESCRIPTION;
	private final String MOD_VERSION;
	private final String MOD_AUTHOR;
	private final String MOD_PREVIEW;
	private final String MOD_PATH;
	
	public ModPropertiesModel(
		@Nullable String MOD_NAME,
		@Nullable String MOD_DESCRIPTION,
		@Nullable String MOD_VERSION,
		@Nullable String MOD_AUTHOR,
		@Nullable String MOD_PREVIEW,
		@Nullable String MOD_PATH
	) {
		this.MOD_NAME = MOD_NAME;
		this.MOD_DESCRIPTION = MOD_DESCRIPTION;
		this.MOD_VERSION = MOD_VERSION;
		this.MOD_AUTHOR = MOD_AUTHOR;
		this.MOD_PREVIEW = MOD_PREVIEW;
		this.MOD_PATH = MOD_PATH;
	}

	public final String getModName() {
		return this.MOD_NAME;
	}

	public final String getModDescription() {
		return this.MOD_DESCRIPTION;
	}

	public final String getModVersion() {
		return this.MOD_VERSION;
	}

	public final String getModAuthor() {
		return this.MOD_AUTHOR;
	}

	public final String getModPreview() {
		return this.MOD_PREVIEW;
	}
	
	public final String getModPath() {
		return this.MOD_PATH;
	}
	
	@Override
	public boolean equals(Object thisObject) {
		if (thisObject instanceof ModPropertiesModel) {
			ModPropertiesModel casted = (ModPropertiesModel) thisObject;
			return (getModName().equals(casted.getModName()) && getModDescription().equals(casted.getModDescription()) && getModVersion().equals(casted.getModVersion()) && getModAuthor().equals(casted.getModAuthor()) && getModPreview().equals(casted.getModPreview()));
		}
		return false;
	}
}
