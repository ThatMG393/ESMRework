package com.thatmg393.esmanager.utils;

import android.net.Uri;
import android.widget.Toast;
import com.anggrayudi.storage.file.DocumentFileCompat;
import com.anggrayudi.storage.file.DocumentFileType;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentIO;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

import java.io.FileInputStream;
import org.eclipse.tm4e.core.registry.IThemeSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class EditorUtils {
	private static final Logger LOG = new Logger("ESM/EditorUtils");
	
	public static final String[] tmThemes = {
		"darcula",
		"quietlight"
	};

	public static void ensureTMTheme(CodeEditor editor) {
		EditorColorScheme colorScheme = editor.getColorScheme();
		if (!(colorScheme instanceof TextMateColorScheme)) {
			try {
				colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
			editor.setColorScheme(colorScheme);
		}
	}
	
	public static TextMateLanguage createTMLanguage(String language) {
		try {
			return TextMateLanguage.create("source." + language, true);
		} catch (Exception e) {
			LOG.w(language + " is not supported!");
		}
		return null;
	}
	
	public static void loadTMThemes() {
		ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
		for(String theme : tmThemes) {
			try {
				String themePath = "tm/themes/" + theme + ".json";
				themeRegistry.loadTheme(
					new ThemeModel(
						IThemeSource.fromInputStream(
							FileProviderRegistry.getInstance().tryGetInputStream(themePath),
							themePath, null
						), theme
					)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void loadFileToEditor(CodeEditor editor, String path) {
		CompletableFuture.runAsync(() -> {
			try {
				Content text = ContentIO.createFrom(
					new FileInputStream(path)
				);
				
				ActivityUtils.getInstance().runOnUIThread(() -> {
					try {
						editor.setText(text);
					} catch (Exception e) {
						LOG.e("Failed to set editor text!");
						e.printStackTrace(System.err);
								
						Toast.makeText(editor.getContext(), "Failed to load file contents!", Toast.LENGTH_SHORT).show();
						editor.setEditable(false);
					}
				});
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
	}
}
