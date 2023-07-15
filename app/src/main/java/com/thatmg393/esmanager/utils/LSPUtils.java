package com.thatmg393.esmanager.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.thatmg393.esmanager.managers.LSPManager;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.LanguageServerWrapper;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.lsp.editor.LspEditorManager;
import io.github.rosemoe.sora.widget.CodeEditor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class LSPUtils {
	private static final Logger LOG = new Logger("ESM/LSPUtils");
	
	public static LanguageServerWrapper createNewServerWrapper(
		@NonNull String language,
		@NonNull StreamConnectionProvider connectionProvider,
		@NonNull String projectPath
	) {
		return new LanguageServerWrapper(
			new CustomLanguageServerDefinition(
				"." + language,
				(workingDir) -> {
					return connectionProvider;
				}
			) {
				@Override
				public EventHandler.EventListener getEventListener() {
					return EventHandler.EventListener.DEFAULT;
				}
			}, projectPath
		);
	}
	
	public static void connectToLsp(
		@NonNull LspEditor lspEditor
	) {
		CompletableFuture.runAsync(() -> {
			try {
				lspEditor.connectWithTimeout();
			} catch(TimeoutException | InterruptedException e) {
				e.printStackTrace(System.err);
				ActivityUtils.getInstance().runOnUIThread(
					() -> Toast.makeText(lspEditor.getEditor().getContext(), "Failed to connect to LSP!\nNo completions will be provided!", Toast.LENGTH_SHORT).show()
				);
			}
		});
	} 
	
	public static LspEditor createNewLspEditor(
		@NonNull String fileUri,
		@NonNull LanguageServerDefinition serverDefinition,
		@NonNull CodeEditor editor
	) {
		LspEditor lspEditor = LspEditorManager.getOrCreateEditorManager(LSPManager.getInstance().getCurrentProject().projectPath)
			.createEditor(
				fileUri,
				serverDefinition
			);
					
		lspEditor.setWrapperLanguage((TextMateLanguage)editor.getEditorLanguage());
		lspEditor.setEditor(editor);
		
		return lspEditor;
	}
}
