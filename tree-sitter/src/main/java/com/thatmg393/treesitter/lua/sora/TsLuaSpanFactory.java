package com.thatmg393.treesitter.lua.sora;

import com.itsaky.androidide.treesitter.TSQuery;
import com.itsaky.androidide.treesitter.TSQueryCapture;

import io.github.rosemoe.sora.editor.ts.spans.DefaultSpanFactory;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TsLuaSpanFactory extends DefaultSpanFactory {
	public static final String HEX_REGEX = "null";
	
    private ContentReference contentRef;
    private TSQuery query;
    private Styles styles;

    public TsLuaSpanFactory(ContentReference contentRef, TSQuery query, Styles styles) {
        this.contentRef = contentRef;
        this.query = query;
        this.styles = styles;
    }
	
	@Override
	public List<Span> createSpans(TSQueryCapture capture, int column, long spanStyle) {
		List<Span> spans = new ArrayList<>();
		
		Content content = Objects.requireNonNull(contentRef.getReference());
		
		String captureName = query.getCaptureNameForId(capture.getIndex());
		System.out.println(captureName);
		
		return super.createSpans(capture, column, spanStyle);
	}
	
	@Override
	public void close() {
		super.close();
		
		contentRef = null;
		query = null;
		styles = null;
	}
}
