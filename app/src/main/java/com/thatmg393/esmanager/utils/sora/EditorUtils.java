package com.thatmg393.esmanager.utils.sora;

import android.widget.Toast;

import androidx.core.util.Pair;

import com.thatmg393.esmanager.managers.editor.themes.ThemeManager;
import com.thatmg393.esmanager.utils.ActivityUtils;
import com.thatmg393.esmanager.utils.io.FileUtils;
import com.thatmg393.esmanager.utils.logging.Logger;
import com.thatmg393.treesitter.base.BaseTSLanguage;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class EditorUtils {
	private static final Logger LOG = new Logger("ESM/EditorUtils");
	
	public static void checkAndCorrectColorScheme(CodeEditor editor, Language lang) {
		if (lang instanceof BaseTSLanguage) {
			ensureTSTheme(editor);
		} else if (lang instanceof TextMateLanguage) {
			ensureTMTheme(editor);
		} else {
			editor.setColorScheme(new EditorColorScheme());
		}
	}
	
	public static void ensureTMTheme(CodeEditor editor) {
		EditorColorScheme colorScheme = editor.getColorScheme();
		if (!(colorScheme instanceof TextMateColorScheme)) {
			colorScheme = ThemeManager.getInstance().getTheme("quitelight");
			editor.setColorScheme(colorScheme);
		}
	}
	
	public static void ensureTSTheme(CodeEditor editor) {
		EditorColorScheme tsTheme = ThemeManager.getInstance().getTheme("darcula");
		editor.setColorScheme(tsTheme);
	}
	
	public static void loadFileToEditor(CodeEditor editor, String path) {
		CompletableFuture.runAsync(() -> {
			try {
				Content text = FileUtils.openFileAsContent(path);
				
				if (text != null) editor.post(() -> editor.setText(text));
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
	
	public static CompletableFuture<Boolean> saveFileFromEditor(CodeEditor editor, String path) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				FileUtils.writeToFileUsingContent(editor.getText(), path);
			
				editor.post(() -> {
					Toast.makeText(editor.getContext(), "Success!", Toast.LENGTH_SHORT).show();
				});
				return true;
			} catch (IOException e) {
				LOG.e("Failed to save file!");
				e.printStackTrace(System.err);
				
				editor.post(() -> {
					Toast.makeText(editor.getContext(), "Failed save file!", Toast.LENGTH_SHORT).show();
				});
			}
			return false;
		});
	}
	
	public static CompletableFuture<Boolean> saveFileFromEditor(Pair<CodeEditor, String>... pairs) {
		return CompletableFuture.supplyAsync(() -> {
			boolean failed = false;
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
					failed = true;
				}
			}
			return failed;
		});
	}
	
	private static boolean instanceOf(Object class1, Class<?> class2) {
		return class1.getClass().equals(class2);
	}
}
