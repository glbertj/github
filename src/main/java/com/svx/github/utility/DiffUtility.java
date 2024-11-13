package com.svx.github.utility;

import java.util.ArrayList;
import java.util.List;

public class DiffUtility {

    public static String getDifference(String oldContent, String newContent) {
        String[] oldLines = oldContent.split("\n");
        String[] newLines = newContent.split("\n");

        List<String> diff = new ArrayList<>();

        int oldIndex = 0, newIndex = 0;
        while (oldIndex < oldLines.length || newIndex < newLines.length) {
            if (oldIndex < oldLines.length && newIndex < newLines.length) {
                if (oldLines[oldIndex].equals(newLines[newIndex])) {
                    diff.add("  " + oldLines[oldIndex]);
                    oldIndex++;
                    newIndex++;
                } else {
                    diff.add("- " + oldLines[oldIndex]);
                    diff.add("+ " + newLines[newIndex]);
                    oldIndex++;
                    newIndex++;
                }
            } else if (oldIndex < oldLines.length) {
                diff.add("- " + oldLines[oldIndex]);
                oldIndex++;
            } else {
                diff.add("+ " + newLines[newIndex]);
                newIndex++;
            }
        }

        return String.join("\n", diff);
    }
}
