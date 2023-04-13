package com.thatmg393.esmanager.utils;

import android.content.Context;

import com.thatmg393.esmanager.classes.Tuple;
import com.thatmg393.esmanager.utils.kt.EditorUtilsKt;
import com.thatmg393.esmanager.utils.kt.SuspendFunctionCallback;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;

import java.util.HashMap;
import java.util.Map;

public class EditorUtils {
	private static final Logger LOG = new Logger("ESM/EditorUtils");
	
	public static final Map<String, TextMateLanguage> cachedLanguages = new HashMap<>();
	
	public static void ensureDarculaTheme(CodeEditor editor) {
		EditorUtilsKt.getInstance().ensureDarculaTheme(editor.getContext(), editor);
	}
	
	public static TextMateLanguage createLanguage(String language) {
		if (cachedLanguages.get(language) != null) return cachedLanguages.get(language);
		
		TextMateLanguage tmp = EditorUtilsKt.getInstance().createNewLanguage(language);
		
		cachedLanguages.put(language, tmp);
		return tmp;
	}
	
	public static void initializeEditor(CodeEditor editor, String language) {
		editor.setEditorLanguage(createLanguage(language));
		try { ensureDarculaTheme(editor); }
		catch (Exception ignore) { }
	}
	
	public static void openFile(CodeEditor editor, String path) {
		EditorUtilsKt.getInstance().openFileNDisplay(editor, FileUtils.openFile(path),
			SuspendFunctionCallback.Companion.call((result, err) -> { })
		);
	}
}
