package com.thatmg393.esmanager.managers.editor.themes;

import androidx.collection.ArrayMap;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;

import org.eclipse.tm4e.core.registry.IThemeSource;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ThemeManager {
	private static volatile ThemeManager INSTANCE;

	public static synchronized ThemeManager getInstance() {
		if (INSTANCE == null) INSTANCE = new ThemeManager();
		return INSTANCE;
	}
	
	private ArrayMap<String, Supplier<EditorColorScheme>> themes = new ArrayMap<>();
	
	private ThemeManager() { }
	
	public EditorColorScheme getTheme(String name) {
		Supplier<EditorColorScheme> theme = themes.get(name);
		
		if (theme == null) {
			return new EditorColorScheme();
		}
		
		return theme.get();
	}
	
	public void registerThemes() {
		themes.put("darcula", () -> new SchemeDarcula());
		// themes.put("darcula", () -> createTM("darcula"));
		themes.put("quitelight", () -> createTM("quitelight"));
	}
	
	public EditorColorScheme createTM(String name) {
		String themePath = "tm/themes/" + name + ".json";
		try {
		    return TextMateColorScheme.create(new ThemeModel(
			    IThemeSource.fromInputStream(
				    FileProviderRegistry.getInstance().tryGetInputStream(themePath),
				    themePath, null
			    ), name
		    ));
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return new EditorColorScheme();
		}
	}
}
