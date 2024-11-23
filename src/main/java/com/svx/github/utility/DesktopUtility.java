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

    public static void openVSCode(String path) throws IOException, InterruptedException {
        String userHome = System.getenv("USERPROFILE");
        String vscodePath = userHome + "\\AppData\\Local\\Programs\\Microsoft VS Code\\bin\\code.cmd";

        ProcessBuilder processBuilder = new ProcessBuilder(vscodePath, path);
        processBuilder.start();
    }
}
