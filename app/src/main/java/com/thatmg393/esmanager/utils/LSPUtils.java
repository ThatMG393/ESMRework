package com.thatmg393.esmanager.utils;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.thatmg393.esmanager.managers.editor.project.ProjectManager;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import io.github.rosemoe.sora.lsp.client.languageserver.requestmanager.RequestManager;
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
	
	public static LspEditor createNewLspEditor(
		@NonNull String fileUri,
		@NonNull LanguageServerDefinition serverDefinition,
		@NonNull CodeEditor editor
	) {
		LspEditor lspEditor = LspEditorManager.getOrCreateEditorManager(ProjectManager.getInstance().getCurrentProject().projectPath)
			.createEditor(
				fileUri,
				serverDefinition
			);
		
		if (lspEditor == null) {
			LOG.w("LSPEditor for " + fileUri.toString() + " is somehow null...");
			lspEditor = LspEditorManager.getOrCreateEditorManager(ProjectManager.getInstance().getCurrentProject().projectPath)
				.createEditor(
					fileUri,
					serverDefinition
				);
		}
		
		lspEditor.setWrapperLanguage(editor.getEditorLanguage());
		lspEditor.setEditor(editor);
		
		return lspEditor;
	}
	
	public static void connectToLsp(
		@NonNull LspEditor lspEditor
	) {
		CompletableFuture.runAsync(() -> {
			try {
				lspEditor.connectWithTimeout();
				onLspConnected(lspEditor);
			} catch(TimeoutException | InterruptedException e) {
				e.printStackTrace(System.err);
				ActivityUtils.getInstance().runOnUIThread(
					() -> Toast.makeText(lspEditor.getEditor().getContext(), "Failed to connect to LSP!\nNo completions will be provided.", Toast.LENGTH_SHORT).show()
				);
			}
		});
	} 
	
	public static void onLspConnected(LspEditor lspEditor) {
		RequestManager lspRequestManager = lspEditor.getRequestManager();
		if (lspRequestManager != null) {
			/*
			WorkspaceFoldersChangeEvent wfce = new WorkspaceFoldersChangeEvent();
			wfce.setAdded(Arrays.asList(
				new WorkspaceFolder(LSPManager.getInstance().getCurrentProject().projectPath)
			));
			
			DidChangeWorkspaceFoldersParams dwfp = new DidChangeWorkspaceFoldersParams();
			dwfp.setEvent(wfce);
			
			lspRequestManager.didChangeWorkspaceFolders(dwfp);
			*/
		}
		
	}
}
