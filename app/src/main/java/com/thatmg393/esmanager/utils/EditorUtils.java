package com.thatmg393.esmanager.utils;

import android.net.Uri;
import android.widget.Toast;
import androidx.core.util.Pair;
import com.anggrayudi.storage.file.DocumentFileCompat;
import com.anggrayudi.storage.file.DocumentFileType;

import com.google.android.material.tabs.TabLayout;
import com.thatmg393.esmanager.adapters.TabEditorAdapter;
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
		switch (language) {
			default:
				try {
					return TextMateLanguage.create("source." + language, true);
				} catch (Exception e) {
					LOG.w(language + " is not supported!");
					return null;
				}
		}
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
				Content text = FileUtils.openFileAsContent(path);
				
				editor.post(() -> editor.setText(text));
			} catch (IOException e) {
				LOG.e("Failed to load file!");
				e.printStackTrace(System.err);
				
				editor.post(() -> {
					Toast.makeText(editor.getContext(), "Failed to load file!", Toast.LENGTH_SHORT).show();
					editor.setEditable(false);
				});
			}
		});
	}
	
	public static void saveFileFromEditor(CodeEditor editor, String path) {
		CompletableFuture.runAsync(() -> {
			try {
				FileUtils.writeToFileUsingContent(editor.getText(), path);
			
				editor.post(() -> {
					Toast.makeText(editor.getContext(), "Success!", Toast.LENGTH_SHORT).show();
				});
			} catch (IOException e) {
				LOG.e("Failed to save file!");
				e.printStackTrace(System.err);
				
				editor.post(() -> {
					Toast.makeText(editor.getContext(), "Failed save file!", Toast.LENGTH_SHORT).show();
				});
			}
		});
	}
	
	public static void saveFileFromEditor(Pair<CodeEditor, String>... pairs) {
		CompletableFuture.runAsync(() -> {
			for (Pair<CodeEditor, String> pair : pairs) {
				try {
					FileUtils.writeToFileUsingContent(pair.first.getText(), pair.second);
						
					pair.first.post(() -> {
						Toast.makeText(pair.first.getContext(), "Success!", Toast.LENGTH_SHORT).show();
					});
				} catch (IOException e) {
					LOG.e("Failed to save file!");
					e.printStackTrace(System.err);
					
					pair.first.post(() -> {
						Toast.makeText(pair.first.getContext(), "Failed to save " + pair.second + "!", Toast.LENGTH_SHORT).show();
					});
				}
			}
		});
	}
}
