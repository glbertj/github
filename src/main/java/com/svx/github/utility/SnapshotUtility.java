package com.svx.github.utility;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class SnapshotUtility extends ImageView {

    public SnapshotUtility(Node node) {
        WritableImage snapshot = node.snapshot(new SnapshotParameters(), null);
        setImage(snapshot);
    }
}
