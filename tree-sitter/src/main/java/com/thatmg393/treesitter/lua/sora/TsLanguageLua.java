package com.thatmg393.treesitter.lua.sora;

import com.thatmg393.treesitter.base.BaseTSLanguage;
import com.thatmg393.treesitter.sora.TSThemeBuilder;

import io.github.rosemoe.sora.editor.ts.TsAnalyzeManager;

public class TsLanguageLua extends BaseTSLanguage {
	public TsLanguageLua(LuaLanguageSpec luaSpec, boolean tab) {
		super(luaSpec, tab, builder -> TSThemeBuilder.buildThemeLua(builder));
	}
	
    @Override
    public TsAnalyzeManager getAnalyzeManager() {
        return new TsLuaAnalysisManager(getLanguageSpec(), getTsTheme());
    }
}
