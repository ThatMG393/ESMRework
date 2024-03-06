package com.thatmg393.treesitter.sora;

import com.itsaky.androidide.treesitter.TSQuery;

import io.github.rosemoe.sora.editor.ts.TsThemeBuilder;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.lang.styling.TextStyleKt;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class TSThemeBuilder {
	/* makeStyle(
		type,
		bg, bold, italic, strkthr, comp
	) */
	
	public static TsThemeBuilder buildThemeLua(TsThemeBuilder themeBuilder) {
		themeBuilder.applyTo(
			TextStyle.makeStyle(
				EditorColorScheme.COMMENT,
				0, false, true, false, false
			), "comment"
		);
				
		themeBuilder.applyTo(
			TextStyle.makeStyle(
				EditorColorScheme.KEYWORD,
				0, true, false, false, false
			), "keyword"
		);
				
		themeBuilder.applyTo(
			TextStyle.makeStyle(
				EditorColorScheme.LITERAL,
				0, false, false, false, false
			), new String[] {
				"constant.builtin", "string",
				"number"
			}
		);
				
		themeBuilder.applyTo(
			TextStyle.makeStyle(
				EditorColorScheme.IDENTIFIER_VAR,
				0, false, false, false, false
					), new String[] {
				"variable.builtin", "variable",
				"constant"
			}
		);
				
		themeBuilder.applyTo(
			TextStyle.makeStyle(
				EditorColorScheme.IDENTIFIER_VAR,
				0, false, false, false, false
			), new String[] {
				"function.method",
				"function.builtin", "variable.field"
			}
		);
				
		themeBuilder.applyTo(
			TextStyle.makeStyle(
				EditorColorScheme.OPERATOR,
				0, false, false, false, false
			), "operator"
		);
		
		return themeBuilder;
	}
}
