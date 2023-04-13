package com.thatmg393.esmanager.fragments.projectfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.thatmg393.esmanager.interfaces.impl.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.utils.EditorUtils;
import com.thatmg393.esmanager.utils.LSPUtils;
import com.thatmg393.esmanager.utils.Logger;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;

import java.io.FileNotFoundException;

public class ProjectTabEditorFragment extends Fragment {
	private static final Logger LOG = new Logger("ESM/ProjectTabEditorFragment");
	
	private CodeEditor editor;
	
	private Language editorLanguage;
	
	private String currentFilePath;
	private String fileExtension;
	
	public void initializeEditor(final Context context, final String pathToFile) {
		editor = new CodeEditor(context);
		fileExtension = FilenameUtils.getExtension(pathToFile);
		editorLanguage = EditorUtils.createLanguage(fileExtension);
		currentFilePath = pathToFile;
		
		EditorUtils.initializeEditor(editor, fileExtension);
		EditorUtils.openFile(editor, pathToFile);
		
		LSPManager.getInstance().languageServerRegistry.get(fileExtension).addListener(new ILanguageServiceCallback() {
			@Override
			public void onReady() {
				LSPUtils.newLspEditorForFile(editor, currentFilePath, LSPManager.getInstance().languageServerRegistry.get(fileExtension).getServerDefinition());
			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return editor;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		editor.requestFocus();
	}
}
