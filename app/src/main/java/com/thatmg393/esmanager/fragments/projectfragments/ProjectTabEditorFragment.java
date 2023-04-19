package com.thatmg393.esmanager.fragments.projectfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.thatmg393.esmanager.interfaces.ILanguageServerCallback;
import com.thatmg393.esmanager.interfaces.impl.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.managers.lsp.base.BaseLSPService;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.utils.EditorUtils;
import com.thatmg393.esmanager.utils.LSPUtils;
import com.thatmg393.esmanager.utils.Logger;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;

public class ProjectTabEditorFragment extends Fragment {
	private static final Logger LOG = new Logger("ESM/ProjectTabEditorFragment");
	
	public CodeEditor editor;
	public LspEditor lspEditor;
	
	public TextMateLanguage editorLanguage;
	
	public String currentFilePath;
	public String fileExtension;
	
	public void initializeEditor(final Context context, final String pathToFile) {
		editor = new CodeEditor(context);
		fileExtension = FilenameUtils.getExtension(pathToFile);
		editorLanguage = EditorUtils.createLanguage(fileExtension);
		currentFilePath = pathToFile;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return editor;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		
		EditorUtils.initializeEditor(editor, editorLanguage);
		
		LanguageServerModel lsm = LSPManager.getInstance().languageServerRegistry.get(fileExtension);
		if (lsm != null) {
			lsm.addListener(new ILanguageServiceCallback() {
				@Override
				public void onReady(BaseLSPService serviceInstance) {
					serviceInstance.addServerListener(new ILanguageServerCallback() {
						@Override
						public void onStart() {
							lspEditor = LSPUtils.newLspEditorForFile(editor, currentFilePath, LSPManager.getInstance().languageServerRegistry.get(fileExtension).getServerDefinition());
						}
					});
				}
			});
		}
		
		EditorUtils.openFile(editor, currentFilePath);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		editor.requestFocus();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (lspEditor != null) lspEditor.close();
		if (editor != null) editor.release();
	}
}
