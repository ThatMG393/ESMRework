package com.thatmg393.esmanager.managers.editor;

import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.fragments.project.TabEditorFragment;

public class EditorManager {
	private static final Logger LOG = new Logger("ESM/EditorManager");
    private static volatile EditorManager INSTANCE;

    public static synchronized EditorManager getInstance() {
        if (INSTANCE == null) INSTANCE = new EditorManager();
        return INSTANCE;
    }
	
	private EditorManager() {
		if (INSTANCE != null) throw new RuntimeException("Please use 'EditorManager#getInstance()'!");
    }
	
	private TabEditorFragment focusedTabEditor = null;
		
	public void setFocusedTabEditor(TabEditorFragment editor) {
		this.focusedTabEditor = editor;
	}
		
	public TabEditorFragment getFocusedTabEditor() {
		return this.focusedTabEditor;
	}
}
