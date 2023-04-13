package com.thatmg393.esmanager.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModPropertiesModel {
    private final String MOD_NAME;
    private final String MOD_DESCRIPTION;
    private final String MOD_VERSION;
    private final String MOD_AUTHOR;

    private final String MOD_PREVIEW;
	
	public ModPropertiesModel(
		@NotNull String MOD_NAME,
		@NotNull String MOD_DESCRIPTION,
		@NotNull String MOD_VERSION,
		@NotNull String MOD_AUTHOR,
		@Nullable String MOD_PREVIEW
	) {
		this.MOD_NAME = MOD_NAME;
		this.MOD_DESCRIPTION = MOD_DESCRIPTION;
		this.MOD_VERSION = MOD_VERSION;
		this.MOD_AUTHOR = MOD_AUTHOR;
		
		this.MOD_PREVIEW = MOD_PREVIEW;
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
}
