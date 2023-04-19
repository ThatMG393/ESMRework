package com.thatmg393.esmanager.utils;

import com.thatmg393.esmanager.utils.kt.EditorUtilsKt;
import com.thatmg393.esmanager.utils.kt.SuspendFunctionCallback;

import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;

public class EditorUtils {
	private static final Logger LOG = new Logger("ESM/EditorUtils");
	
	public static void ensureDarculaTheme(CodeEditor editor) {
		EditorUtilsKt.getInstance().ensureDarculaTheme(editor.getContext(), editor);
	}
	
	public static TextMateLanguage createLanguage(String language) {
		try {
			return EditorUtilsKt.getInstance().createNewLanguage(language);
		} catch (Exception e) {
			LOG.w(language + " is not supported!");
			e.printStackTrace(System.err);
		}
		
		return null;
	}
	
	public static void initializeEditor(CodeEditor editor, String language) {
		TextMateLanguage tml = createLanguage(language);
		if (tml != null) editor.setEditorLanguage(tml);
		
		ensureDarculaTheme(editor);
	}
	
	public static void initializeEditor(CodeEditor editor, TextMateLanguage language) {
		if (language != null) editor.setEditorLanguage(language);
		ensureDarculaTheme(editor);
	}
	
	public static void openFile(CodeEditor editor, String path) {
		EditorUtilsKt.getInstance().openFileNDisplay(editor, FileUtils.openFile(path),
			SuspendFunctionCallback.Companion.call((result, err) -> { })
		);
	}
}
