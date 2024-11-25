package com.svx.github.model;

import java.util.ArrayList;
import java.util.List;

public class LineDifference {
    private final String content;
    private final LineType type;
    private final List<Highlight> highlights;

    public LineDifference(String content, LineType type) {
        this(content, type, new ArrayList<>());
    }

    public LineDifference(String content, LineType type, List<Highlight> highlights) {
        this.content = content;
        this.type = type;
        this.highlights = highlights;
    }

    public String getContent() {
        return content;
    }

    public LineType getType() {
        return type;
    }

    public List<Highlight> getHighlights() {
        return highlights;
    }

    public enum LineType {
        UNCHANGED,
        ADDED,
        REMOVED
    }
}