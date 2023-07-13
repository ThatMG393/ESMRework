package com.thatmg393.esmanager.fragments.project;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.thatmg393.esmanager.interfaces.impl.ILanguageServiceCallback;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.models.LanguageServerModel;
import com.thatmg393.esmanager.utils.EditorUtils;
import com.thatmg393.esmanager.utils.FileUtils;
import com.thatmg393.esmanager.utils.LSPUtils;
import com.thatmg393.esmanager.utils.Logger;
import com.thatmg393.esmanager.utils.SharedPreference;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.lsp.utils.URIUtils;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class TabEditorFragment extends Fragment {
	private static final Logger LOG = new Logger("ESM/ProjectTabEditorFragment");
	
	private CodeEditor editor;
	public final String currentFilePath;
	private final String fileExtension;
	
	public TabEditorFragment(final Context context, final String pathToFile) {
		this.fileExtension = FilenameUtils.getExtension(pathToFile);
		this.currentFilePath = pathToFile;
		
		initEditor(context);
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
	public void onDestroy() {
		super.onDestroy();
		
		if (editor != null) editor.release();
	}
	
	public void initEditor(Context context) {
		editor = new CodeEditor(context);
		
		EditorUtils.loadFileToEditor(editor, currentFilePath);
		EditorUtils.ensureTMTheme(editor);
		
		TextMateLanguage tmLang = EditorUtils.createTMLanguage(fileExtension);
		if (tmLang != null) {
			editor.setEditorLanguage(tmLang);
		}
		
		ThemeRegistry.getInstance().setTheme(SharedPreference.getInstance().getStringFallback("editor_code_theme", "darcula"));
		
		LanguageServerModel langServer = LSPManager.getInstance().getLanguageServer(fileExtension);
		if (langServer != null) {
			langServer.addListener(new ILanguageServiceCallback() {
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
}
