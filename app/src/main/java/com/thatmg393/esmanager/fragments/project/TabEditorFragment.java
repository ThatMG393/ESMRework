package com.thatmg393.esmanager.fragments.project;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.activities.ProjectActivity;
import com.thatmg393.esmanager.adapters.TabEditorAdapter;
import com.thatmg393.esmanager.interfaces.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.utils.EditorUtils;
import com.thatmg393.esmanager.utils.FileUtils;
import com.thatmg393.esmanager.utils.LSPUtils;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.SharedPreference;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.lsp.utils.URIUtils;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class TabEditorFragment extends Fragment {
	private static final Logger LOG = new Logger("ESM/ProjectTabEditorFragment");
	
	private CodeEditor editor;
	private String currentFilePath;
	private String fileExtension;
	
	public TabEditorFragment() { }
	public TabEditorFragment(final String pathToFile) {
		this.fileExtension = FilenameUtils.getExtension(pathToFile);
		this.currentFilePath = pathToFile;
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
		return this.getClass().getName() + " for " + currentFilePath;
	}
	
	public void initEditor(Context context) {
		editor = new CodeEditor(context);
		
		ThemeRegistry.getInstance().setTheme(SharedPreference.getInstance().getStringFallback("editor_code_theme", "darcula"));
		EditorUtils.ensureTMTheme(editor);
		
		Language editorLang = EditorUtils.createTMLanguage(fileExtension);
		if (editorLang != null) editor.setEditorLanguage(editorLang);
		
		EditorUtils.loadFileToEditor(editor, currentFilePath);
		LanguageServerModel lsModel = LSPManager.getInstance().getLanguageServer(fileExtension);
		if (lsModel != null) {
			lsModel.addListener(new ILanguageServiceCallback() {
				@Override
				public void onReady() {
					LSPUtils.connectToLsp(
						LSPUtils.createNewLspEditor(
							URIUtils.fileToURI(new File(currentFilePath)).toString(),
							LSPManager.getInstance().getLanguageServer(fileExtension).getServerDefinition(),
							editor
						)
					);
				}
			});
		}
	}
	
	public boolean saveContent() {
		return FileUtils.writeToFileUsingContent(editor.getText(), currentFilePath);
	}
	
	public String getCurrentFilePath() {
		return this.currentFilePath;
	}
}
