package com.svx.github.utility;

import com.svx.github.view.dialog.DialogView;
import javafx.scene.Parent;
import java.io.File;

public class FileUtility {

    // Usable only in DialogView (we can make actually improve this but nah)
    public static boolean hasRepository(String gitPath, DialogView<? extends Parent> view) {
        if (gitPath.isBlank()) {
            view.getErrorLabel().setText("");
            view.getConfirmButton().setDisable(true);
            return false;
        }

        File configFile = new File(gitPath.trim(), ".git/config");

        return configFile.exists() && configFile.isFile();
    }
}
