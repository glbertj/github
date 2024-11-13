package com.svx.github.manager;

import com.svx.github.model.Commit;

public class ReferenceManager {
    private Commit head;

    public Commit getHead() {
        return head;
    }

    public void setHead(Commit commit) {
        this.head = commit;
    }

    public boolean hasCommits() {
        return head != null;
    }
}

