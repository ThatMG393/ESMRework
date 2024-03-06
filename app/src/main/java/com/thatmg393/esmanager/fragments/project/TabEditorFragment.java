package com.thatmg393.esmanager.fragments.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import com.thatmg393.esmanager.activities.ProjectActivity;
import com.thatmg393.esmanager.fragments.project.base.PathedTabFragment;
import com.thatmg393.esmanager.interfaces.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.editor.EditorManager;
import com.thatmg393.esmanager.managers.editor.language.LanguageManager;
import com.thatmg393.esmanager.managers.editor.lsp.LSPManager;
import com.thatmg393.esmanager.managers.editor.themes.ThemeManager;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.SharedPreference;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.esmanager.utils.sora.EditorUtils;
import com.thatmg393.esmanager.utils.sora.LSPUtils;

import io.github.rosemoe.sora.editor.ts.TsLanguage;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.lsp.utils.URIUtilsKt;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;

public class TabEditorFragment extends PathedTabFragment {
	private static final Logger LOG = new Logger("ESM/TabEditorFragment");
	
	private EditorState editorState = EditorState.SAVED;
	private CodeEditor editor;
	private String fileExtension;
	
	public TabEditorFragment(final String pathToFile) {
		super(pathToFile);
		this.fileExtension = FilenameUtils.getExtension(pathToFile);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initEditor();
		return editor;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		LanguageServerModel lsModel = LSPManager.getInstance().getLanguageServer(fileExtension);
		if (lsModel != null) {
			lsModel.addListener(new ILanguageServiceCallback() {
				@Override
				public void onReady() {
					LSPUtils.connectToLsp(
						LSPUtils.createNewLspEditor(
							URIUtilsKt.toFileUri(getCurrentFile().getAbsolutePath()),
							editor
						)
					);
				}
			});
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		editor.requestFocus();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		editor.release();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " for " + getCurrentFilePath();
	}
	
	public void save() {
		if (getEditorState() == EditorState.SAVED) return;
		EditorUtils.saveFileFromEditor(editor, getCurrentFilePath()).thenApply(success -> {
			if (!success.booleanValue()) return success;
			updateState(EditorState.SAVED);
			return success;
		});
	}
	
	private void updateState(EditorState state) {
		switch (state.getState()) {
			case 0:
				if (editorState == EditorState.SAVED) return;
				ActivityUtils.getInstance().runOnUIThread(() -> getCurrentTab().setText(FilenameUtils.getName(getCurrentFilePath())));
				editorState = EditorState.SAVED;
				break;
			case 1:
				if (editorState == EditorState.MODIFIED) return;
				ActivityUtils.getInstance().runOnUIThread(() -> getCurrentTab().setText("*" + FilenameUtils.getName(getCurrentFilePath())));
				editorState = EditorState.MODIFIED;
				break;
		}
	}
	
	private void initEditor() {
		editor = new CodeEditor(requireActivity());
		editor.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) EditorManager.getInstance().setFocusedTabEditor(TabEditorFragment.this);
			else EditorManager.getInstance().setFocusedTabEditor(null);
				
			((ProjectActivity) requireActivity()).invalidateOptionsMenu();
		});
		editor.subscribeEvent(ContentChangeEvent.class, (event, unsub) -> {
			switch (event.getAction()) {
				case ContentChangeEvent.ACTION_INSERT:
				case ContentChangeEvent.ACTION_DELETE:
					updateState(EditorState.MODIFIED);
					break;
			}
		});
		editor.setHardwareAcceleratedDrawAllowed(true);
		editor.getProps().deleteEmptyLineFast = false;
		editor.getProps().stickyScroll = true;
		
		EditorUtils.loadFileToEditor(editor, getCurrentFilePath());
		
		System.out.println(fileExtension);
		Language editorLang = LanguageManager.getInstance().getLanguage(fileExtension);
		EditorUtils.checkAndCorrectColorScheme(editor, editorLang);
		editor.setEditorLanguage(editorLang);
	}
	
	public CodeEditor getEditor() {
		return this.editor;
	}
	
	public EditorState getEditorState() {
		return this.editorState;
	}
    
    public enum EditorState {
        MODIFIED(1),
        SAVED(0);
		
		private final int state;
		
		private EditorState(int state) {
			this.state = state;
		}
		
		public final int getState() {
			return this.state;
		}
    }
}
