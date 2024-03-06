package com.thatmg393.treesitter.base;

import com.itsaky.androidide.treesitter.util.Consumer;

import io.github.rosemoe.sora.editor.ts.TsLanguage;
import io.github.rosemoe.sora.editor.ts.TsLanguageSpec;
import io.github.rosemoe.sora.editor.ts.TsThemeBuilder;

import kotlin.Unit;

public abstract class BaseTSLanguage extends TsLanguage {
	public BaseTSLanguage(
		TsLanguageSpec langSpec,
		boolean tab,
		Consumer<TsThemeBuilder> theme
	) {
		super(langSpec, tab, (builder) -> {
			theme.accept(builder);
			return Unit.INSTANCE;
		});
	}
}
