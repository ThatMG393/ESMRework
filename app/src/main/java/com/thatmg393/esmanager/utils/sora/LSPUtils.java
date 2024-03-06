package com.thatmg393.esmanager.utils.sora;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.thatmg393.esmanager.managers.editor.project.ProjectManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.logging.ErrorHandler;
import com.thatmg393.esmanager.utils.logging.Logger;

import io.github.rosemoe.sora.lsp.client.connection.StreamConnectionProvider;
import io.github.rosemoe.sora.lsp.client.languageserver.requestmanager.RequestManager;
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler;
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.LanguageServerWrapper;
import io.github.rosemoe.sora.lsp.editor.LspEditor;
import io.github.rosemoe.sora.widget.CodeEditor;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.concurrent.CompletableFuture;

public class LSPUtils {
	private static final Logger LOG = new Logger("ESM/LSPUtils");
	private static final class NoImplEventListener implements EventHandler.EventListener {
		public static final NoImplEventListener INSTANCE = new NoImplEventListener();
		
		@Override
		public void initialize(LanguageServer arg0, InitializeResult arg1) { }
		
		@Override
		public void onHandlerException(Exception arg0) { }
		
		@Override
		public void onLogMessage(MessageParams arg0) { }
		
        @Override
        public void onShowMessage(MessageParams arg0) { }
    }
	
	public static LanguageServerWrapper createNewServerWrapper(
		@NonNull String language,
		@NonNull StreamConnectionProvider connectionProvider
	) {
		return new LanguageServerWrapper(
			new CustomLanguageServerDefinition(
				language,
				(workingDir) -> {
					return connectionProvider;
				}
			) {
				@Override
				public EventHandler.EventListener getEventListener() {
					return NoImplEventListener.INSTANCE;
				}
			}, ProjectManager.getInstance().getCurrentLspProject()
		);
	}
	
	public static LspEditor createNewLspEditor(
		@NonNull String fileUri,
		@NonNull CodeEditor editor
	) {
		LspEditor lspEditor = ProjectManager.getInstance().getCurrentLspProject()
		    .getOrCreateEditor(fileUri);
		
		if (lspEditor == null) {
			lspEditor = ProjectManager.getInstance().getCurrentLspProject()
				.getOrCreateEditor(fileUri);
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
				lspEditor.connectWithTimeoutBlocking();
				onLspConnected(lspEditor);
			} catch(Exception e) {
				e.printStackTrace(System.err);
				ActivityUtils.getInstance().runOnUIThread(
					(context) -> {
						Toast.makeText(context, "Failed to connect to LSP!\nNo completions will be provided.", Toast.LENGTH_SHORT).show();
					}
				);
				ErrorHandler.writeError(e);
			}
		});
	}
	
	public static void disconnectToLsp(
		@NonNull LspEditor lspEditor
	) {
		CompletableFuture.runAsync(() -> {
		    try {
			    lspEditor.disconnect();
		    } catch (Exception e) {
			    ErrorHandler.writeError(e);
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
