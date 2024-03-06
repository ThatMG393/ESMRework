package com.thatmg393.treesitter.lua.sora;

import com.itsaky.androidide.treesitter.TSQuery;
import com.itsaky.androidide.treesitter.TSQueryError;
import com.itsaky.androidide.treesitter.lua.TSLanguageLua;

import io.github.rosemoe.sora.editor.ts.LocalsCaptureSpec;
import io.github.rosemoe.sora.editor.ts.TsLanguageSpec;
import io.github.rosemoe.sora.editor.ts.predicate.TsPredicate;

import io.github.rosemoe.sora.editor.ts.predicate.builtin.MatchPredicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LuaLanguageSpec extends TsLanguageSpec {
	private TSQuery indentsQuery;
	
	public LuaLanguageSpec(
		String highlightsScmSrc,
		String localsScmSrc,
		String indentsScmSrc
		// String tagsScmSrc
	) {
		super(
			TSLanguageLua.getInstance(),
			highlightsScmSrc,
			"",
			"",
			localsScmSrc,
			LocalsCaptureSpec.Companion.getDEFAULT(),
			new ArrayList<TsPredicate>() /* Arrays.asList(new TsPredicate[] {
				new MatchPredicate()
			}) */
		);
		
		indentsQuery = createTSQuery("indents", indentsScmSrc);
	}
	
	@Override
	public void close() {
		super.close();
		if (indentsQuery != null) indentsQuery.close();
	}
	
	private TSQuery createTSQuery(String queryName, String querySrc) {
		TSQuery q = (querySrc.isBlank()) ? TSQuery.EMPTY : TSQuery.create(getLanguage(), querySrc);
		if (!q.canAccess()) {
			throw new RuntimeException(
				"java.lang.IllegalArgumentException : Query source is invalid!"
			);
		}
		if (q != null && q.getErrorType() != TSQueryError.None) {
			throw new RuntimeException(
				"java.lang.IllegalArgumentException : query(name:" + queryName + ") parsing failed with error " + q.getErrorType().name() + " at text offset " + q.getErrorOffset()
			);
		}
		
		return q;
	}
}