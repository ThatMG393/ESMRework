package com.thatmg393.esmanager.utils;

import android.widget.Toast;

import com.thatmg393.esmanager.ProjectActivity;
import com.thatmg393.esmanager.managers.LSPManager;
import com.thatmg393.esmanager.utils.kt.LSPUtilsKt;
import com.thatmg393.esmanager.utils.kt.SuspendFunctionCallback;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.lsp.editor.LspEditorManager;
import io.github.rosemoe.sora.lsp.utils.URIUtils;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.HashMap;
import java.util.Map;

public class LSPUtils {
	private static final Logger LOG = new Logger("ESM/LSPUtils");
	
	public static CustomLanguageServerDefinition generateServerDefinition(String fileExt, EventHandler.EventListener eventListener) {
		return LSPUtilsKt.getInstance().generateServerDefinition(fileExt, eventListener);
	}
	
	public static void connectLSPToEditor(LspEditor lEditor) {
		LSPUtilsKt.getInstance().connectLSPToEditor(lEditor,
			SuspendFunctionCallback.Companion.call((result, err) -> { })
		);
	}
	
	public static void newLspEditorForFile(CodeEditor editor, String filePath, CustomLanguageServerDefinition clsd) {
		LspEditor lspEditor = LspEditorManager.getOrCreateEditorManager(ProjectActivity.getProjectPath())
			.createEditor(
				URIUtils.fileToURI(filePath).toString(),
				clsd
			);
			
		lspEditor.setWrapperLanguage(editor.getEditorLanguage());
		lspEditor.setEditor(editor);
		
		Toast.makeText(editor.getContext(), "Connecting to the LSP Server", Toast.LENGTH_LONG).show();
		connectLSPToEditor(lspEditor);
	}
}
