package com.thatmg393.treesitter;

import io.github.rosemoe.sora.editor.ts.TsLanguageSpec;

import java.io.Closeable;
import java.io.IOException;

public class TSLanguageSpecExt implements Closeable {
	public TSLanguageSpecExt(
		TsLanguageSpec langSpec,
		String indentsQueryScm
	) {
		System.out.println("no-op :)");
	}
	
	@Override
	public void close() throws IOException { }
}
