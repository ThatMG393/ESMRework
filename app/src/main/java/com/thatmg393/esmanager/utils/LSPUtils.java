package com.thatmg393.esmanager.utils;

import android.widget.Toast;

import com.thatmg393.esmanager.ProjectActivity;
import com.thatmg393.esmanager.utils.kt.LSPUtilsKt;
import com.thatmg393.esmanager.utils.kt.SuspendFunctionCallback;

import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.lsp.editor.LspEditorManager;
import io.github.rosemoe.sora.lsp.utils.URIUtils;
import io.github.rosemoe.sora.widget.CodeEditor;

public class LSPUtils {
	private static final Logger LOG = new Logger("ESM/LSPUtils");
	
	public static CustomLanguageServerDefinition generateServerDefinition(String fileExt, int port, EventHandler.EventListener eventListener) {
		return LSPUtilsKt.getInstance().generateServerDefinition(fileExt, port, eventListener);
	}
	
	public static void connectLSPToEditor(LspEditor lEditor) {
		LSPUtilsKt.getInstance().connectLSPToEditor(lEditor,
			SuspendFunctionCallback.Companion.call((result, err) -> { })
		);
	}
	
	public static LspEditor newLspEditorForFile(CodeEditor editor, String filePath, CustomLanguageServerDefinition clsd) {
		LspEditor lspEditor = LspEditorManager.getOrCreateEditorManager(ProjectActivity.getProjectPath())
			.createEditor(
				URIUtils.fileToURI(filePath).toString(),
				clsd
			);
			
		lspEditor.setWrapperLanguage(editor.getEditorLanguage());
		lspEditor.setEditor(editor);
		
		connectLSPToEditor(lspEditor);
		
		return lspEditor;
	}
}
