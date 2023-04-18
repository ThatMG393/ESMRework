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
		return EditorUtilsKt.getInstance().createNewLanguage(language);
	}
	
	public static void initializeEditor(CodeEditor editor, String language) {
		editor.setEditorLanguage(createLanguage(language));
		ensureDarculaTheme(editor);
	}
	
	public static void openFile(CodeEditor editor, String path) {
		EditorUtilsKt.getInstance().openFileNDisplay(editor, FileUtils.openFile(path),
			SuspendFunctionCallback.Companion.call((result, err) -> { })
		);
	}
}
