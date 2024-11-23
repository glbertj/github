package com.svx.github.utility;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DesktopUtility {

    public static boolean openSystemExplorer(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists()) {
            return false;
        }

        Desktop desktop = Desktop.getDesktop();
        desktop.open(directory);
        return true;
    }
}
