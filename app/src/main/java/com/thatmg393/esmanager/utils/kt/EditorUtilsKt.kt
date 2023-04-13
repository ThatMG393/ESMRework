package com.thatmg393.esmanager.utils.kt

import android.content.Context

import com.thatmg393.esmanager.ProjectActivity
import com.thatmg393.esmanager.utils.ActivityUtils
import com.thatmg393.esmanager.utils.EditorUtils
import com.thatmg393.esmanager.utils.FileUtils

import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.lsp.utils.URIUtils
import io.github.rosemoe.sora.widget.CodeEditor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.eclipse.tm4e.core.registry.IThemeSource

import java.io.InputStream

class EditorUtilsKt {
	companion object {
		@JvmStatic var instance: EditorUtilsKt = EditorUtilsKt()
	}
	
	public final fun createNewLanguage(language: String): TextMateLanguage {
		FileProviderRegistry.getInstance().addFileProvider(
			AssetsFileResolver(
				ActivityUtils.getInstance().getMainActivityInstance().getApplicationContext().assets
			)
		)

		GrammarRegistry.getInstance().loadGrammars(
			languages {
				language("$language") {
					grammar = "textmate/$language/syntaxes/$language.tmLanguage.json"
					scopeName = "source.$language"
					languageConfiguration = "textmate/$language/language-configuration.json"
				}
			}
		)
		
		val tmp = TextMateLanguage.create("source.$language", false)
		tmp.useTab(true)
		
		return tmp
	}
	
	public final fun ensureDarculaTheme(context: Context, editor: CodeEditor) {
		var editorCS = editor.colorScheme
		
		if (editorCS !is TextMateColorScheme) {
			FileProviderRegistry.getInstance().addFileProvider(
				AssetsFileResolver(
					context.assets
				)
			)
			
			val themeReg = ThemeRegistry.getInstance()
			val path = "textmate/darcula.json"
			
			themeReg.loadTheme(
				ThemeModel(
					IThemeSource.fromInputStream(
						FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
					), "darcula"
				)
			)
			
			themeReg.setTheme("darcula")
			
			editorCS = TextMateColorScheme.create(themeReg)
			editor.colorScheme = editorCS
		}
	}
	
	public final suspend fun openFileNDisplay(editor: CodeEditor, fileInputStream: InputStream) = withContext(Dispatchers.IO) {
		editor.setText(FileUtils.openFileAsContent(fileInputStream), null)
	}
}
