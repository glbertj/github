package com.svx.github.utility;

import com.svx.github.view.dialog.DialogView;
import javafx.scene.Parent;
import java.io.File;

public class FileUtility {

    // Usable only in DialogView (we can make actually improve this but nah)
    public static void checkRepositoryValidity(String gitPath, DialogView<? extends Parent> view) {
        if (gitPath.isBlank()) {
            view.getErrorLabel().setText("");
            view.getConfirmButton().setDisable(true);
            return;
        }

        File configFile = new File(gitPath.trim(), "config");

        if (!configFile.exists() || !configFile.isFile()) {
            view.getErrorLabel().setText("This folder may not be a repository, did you select the right folder?");
            view.getConfirmButton().setDisable(true);
        } else {
            view.getErrorLabel().setText("");
            view.getConfirmButton().setDisable(false);
        }
    }
}
