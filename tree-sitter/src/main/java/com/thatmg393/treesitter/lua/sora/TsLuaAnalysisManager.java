package com.thatmg393.treesitter.lua.sora;

import io.github.rosemoe.sora.editor.ts.TsAnalyzeManager;
import io.github.rosemoe.sora.editor.ts.TsLanguageSpec;
import io.github.rosemoe.sora.editor.ts.TsTheme;
import io.github.rosemoe.sora.lang.styling.Styles;

public class TsLuaAnalysisManager extends TsAnalyzeManager {
	public TsLuaAnalysisManager(TsLanguageSpec langSpec, TsTheme tsTheme) {
		super(langSpec, tsTheme);
		
		setStyles(new Styles());
		setSpanFactory(new TsLuaSpanFactory(
			getReference(),
			langSpec.getTsQuery(),
			getStyles()
		));
	}
}
