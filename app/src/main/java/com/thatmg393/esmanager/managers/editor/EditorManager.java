package com.thatmg393.esmanager.managers.editor;

import com.thatmg393.esmanager.fragments.project.base.PathedTabFragment;
import com.thatmg393.esmanager.utils.logging.Logger;
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
	
	private PathedTabFragment focusedTabEditor = null;
		
	public void setFocusedTabEditor(PathedTabFragment editor) {
		this.focusedTabEditor = editor;
	}
		
	public PathedTabFragment getFocusedTabEditor() {
		return this.focusedTabEditor;
	}
}
