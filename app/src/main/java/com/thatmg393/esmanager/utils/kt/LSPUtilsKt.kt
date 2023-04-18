package com.thatmg393.esmanager.utils.kt

import android.widget.Toast

import com.thatmg393.esmanager.GlobalConstants
import com.thatmg393.esmanager.ProjectActivity
import com.thatmg393.esmanager.managers.LSPManager

import io.github.rosemoe.sora.lsp.editor.LspEditor
import io.github.rosemoe.sora.lsp.editor.LspEditorManager
import io.github.rosemoe.sora.lsp.client.languageserver.wrapper.EventHandler
import io.github.rosemoe.sora.lsp.client.connection.SocketStreamConnectionProvider
import io.github.rosemoe.sora.lsp.client.languageserver.serverdefinition.CustomLanguageServerDefinition
import io.github.rosemoe.sora.lsp.utils.URIUtils
import io.github.rosemoe.sora.widget.CodeEditor

import org.apache.commons.io.FilenameUtils

import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LSPUtilsKt {
	companion object {
		@JvmStatic var instance: LSPUtilsKt = LSPUtilsKt()
	}
	
	public final fun generateServerDefinition(language: String, port: Int, eventListener: EventHandler.EventListener): CustomLanguageServerDefinition {
		val customLSDefinition = object : CustomLanguageServerDefinition(".$language", 
			{ SocketStreamConnectionProvider { port } }) {
				override fun getEventListener(): EventHandler.EventListener {
					return eventListener
				}
			}

		return customLSDefinition
	}

	public final suspend fun connectLSPToEditor(lspEditor: LspEditor) {
		val esm_root = GlobalConstants.ESM_ROOT_FOLDER
		
		try {
			println("Will connect to the LSP")
			withContext(Dispatchers.IO) {
				lspEditor.connectWithTimeout()
				lspEditor.requestManager?.didChangeWorkspaceFolders(
					DidChangeWorkspaceFoldersParams().apply {
						this.event = WorkspaceFoldersChangeEvent().apply {
							added = listOf(WorkspaceFolder("file://$esm_root/Lua/std/Lua53"))
						}
					}
				)
			}
			
			/*
			withContext(Dispatchers.Main) {
				Toast.makeText(lspEditor.editor?.getContext(), "Successfully connected to the LSP Server!", Toast.LENGTH_SHORT).show();
			}
			lspEditor.editor?.editable = true
			*/
		} catch (e: Exception) {
			println("Exception inside LSPUtilsKt.connectLSPToEditor(LspEditor)!!!")
			e.printStackTrace()
		}
	}
}
