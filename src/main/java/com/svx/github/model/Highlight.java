package com.svx.github.model;

public class Highlight {
    private final int start;
    private final int end;

    public Highlight(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}