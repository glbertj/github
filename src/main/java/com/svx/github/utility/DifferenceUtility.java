package com.svx.github.utility;

import com.svx.github.model.Highlight;
import com.svx.github.model.LineDifference;
import java.util.ArrayList;
import java.util.List;

public class DifferenceUtility {

    public static List<LineDifference> getDifference(String oldContent, String newContent) {
        String[] oldLines = (oldContent != null && !oldContent.trim().isEmpty()) ? oldContent.split("\n") : new String[0];
        String[] newLines = (newContent != null && !newContent.trim().isEmpty()) ? newContent.split("\n") : new String[0];

        List<LineDifference> diff = new ArrayList<>();

        if (oldLines.length == 0 && newLines.length == 0) {
            return diff;
        }

        int oldIndex = 0, newIndex = 0;
        boolean isOldEmpty = oldLines.length == 0;
        boolean isNewEmpty = newLines.length == 0;

        if (isOldEmpty && !isNewEmpty) {
            for (String newLine : newLines) {
                diff.add(new LineDifference(" +     " + newLine, LineDifference.LineType.ADDED));
            }
            return diff;
        }

        if (!isOldEmpty && isNewEmpty) {
            for (String oldLine : oldLines) {
                diff.add(new LineDifference(" -     " + oldLine, LineDifference.LineType.REMOVED));
            }
            return diff;
        }

        while (oldIndex < oldLines.length || newIndex < newLines.length) {
            String oldLine = (oldIndex < oldLines.length) ? oldLines[oldIndex].trim() : null;
            String newLine = (newIndex < newLines.length) ? newLines[newIndex].trim() : null;

            if (oldLine != null && newLine != null) {
                if (oldLine.equals(newLine)) {
                    diff.add(new LineDifference("       " + oldLines[oldIndex], LineDifference.LineType.UNCHANGED));
                } else {
                    diff.add(new LineDifference(" -     " + oldLines[oldIndex], LineDifference.LineType.REMOVED, highlightDiffs(oldLines[oldIndex], newLines[newIndex])));
                    diff.add(new LineDifference(" +     " + newLines[newIndex], LineDifference.LineType.ADDED, highlightDiffs(newLines[newIndex], oldLines[oldIndex])));
                }
                oldIndex++;
                newIndex++;
            } else if (oldLine != null) {
                diff.add(new LineDifference(" -     " + oldLines[oldIndex], LineDifference.LineType.REMOVED));
                oldIndex++;
            } else {
                diff.add(new LineDifference(" +     " + newLines[newIndex], LineDifference.LineType.ADDED));
                newIndex++;
            }
        }

        return diff;
    }

    private static List<Highlight> highlightDiffs(String line1, String line2) {
        List<Highlight> highlights = new ArrayList<>();
        int minLength = Math.min(line1.length(), line2.length());
        for (int i = 0; i < minLength; i++) {
            if (line1.charAt(i) != line2.charAt(i)) {
                highlights.add(new Highlight(i, i + 1));
            }
        }
        if (line1.length() > line2.length()) {
            highlights.add(new Highlight(minLength, line1.length()));
        } else if (line2.length() > line1.length()) {
            highlights.add(new Highlight(minLength, line2.length()));
        }
        return highlights;
    }
}