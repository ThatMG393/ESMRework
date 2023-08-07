package com.thatmg393.esmanager.fragments.project;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout.Tab;
import com.thatmg393.esmanager.activities.ProjectActivity;
import com.thatmg393.esmanager.interfaces.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.editor.EditorManager;
import com.thatmg393.esmanager.managers.editor.lsp.LSPManager;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.utils.EditorUtils;
import com.thatmg393.esmanager.utils.LSPUtils;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.SharedPreference;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.lsp.utils.URIUtils;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TabEditorFragment extends Fragment {
	private static final Logger LOG = new Logger("ESM/ProjectTabEditorFragment");
	
	private boolean isModified;
	
	private Tab tab;
	private CodeEditor editor;
	private File editorFile;
	private String fileExtension;
	
	public TabEditorFragment() { }
	public TabEditorFragment(final String pathToFile) {
		this.fileExtension = FilenameUtils.getExtension(pathToFile);
		this.editorFile = new File(pathToFile);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return editor;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		editor.requestFocus();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " for " + editorFile.getAbsolutePath();
	}
	
	@RestrictTo(RestrictTo.Scope.LIBRARY)
	public void setCurrentTabObject(Tab currentTab) {
		if (tab != null) return;
		this.tab = currentTab;
	}
	
	public void initEditor(Context context) {
		editor = new CodeEditor(context);
		editor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) EditorManager.getInstance().setFocusedTabEditor(TabEditorFragment.this);
				else EditorManager.getInstance().setFocusedTabEditor(null);
				
				((ProjectActivity) requireActivity()).invalidateOptionsMenu();
			}
		});
		
		editor.subscribeEvent(ContentChangeEvent.class, (event, unsub) -> {
			switch (event.getAction()) {
				case ContentChangeEvent.ACTION_INSERT:
				case ContentChangeEvent.ACTION_DELETE:
					tab.setText("*" + FilenameUtils.getName(editorFile.getAbsolutePath()));
					isModified = true;
					break;
			}
		});
		
		ThemeRegistry.getInstance().setTheme(SharedPreference.getInstance().getStringFallback("editor_code_theme", "darcula"));
		EditorUtils.ensureTMTheme(editor);
		
		Language editorLang = EditorUtils.createTMLanguage(fileExtension);
		if (editorLang != null) editor.setEditorLanguage(editorLang);
		
		LanguageServerModel lsModel = LSPManager.getInstance().getLanguageServer(fileExtension);
		if (lsModel != null) {
			lsModel.addListener(new ILanguageServiceCallback() {
				@Override
				public void onReady() {
					LSPUtils.connectToLsp(
						LSPUtils.createNewLspEditor(
							URIUtils.fileToURI(editorFile).toString(),
							LSPManager.getInstance().getLanguageServer(fileExtension).getServerDefinition(),
							editor
						)
					);
				}
			});
		}
		
		EditorUtils.loadFileToEditor(editor, getCurrentFilePath());
	}
	
	public void save() {
		if (!isModified) return;
		CompletableFuture<Boolean> success = EditorUtils.saveFileFromEditor(editor, editorFile.getAbsolutePath());
		try {
			isModified = !success.get();
			tab.setText(FilenameUtils.getName(editorFile.getAbsolutePath()));
		} catch (ExecutionException | InterruptedException ignore) { }
	}
	
	public CodeEditor getEditor() {
		return this.editor;
	}
	
	public String getCurrentFilePath() {
		return this.editorFile.getAbsolutePath();
	}
}
