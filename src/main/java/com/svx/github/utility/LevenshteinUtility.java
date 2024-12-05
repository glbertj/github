package com.svx.github.utility;

public class LevenshteinUtility {

    public static int getLevenshteinDistance(String s1, String s2) {
        int lenS1 = s1.length();
        int lenS2 = s2.length();
        int[][] distance = new int[lenS1 + 1][lenS2 + 1];

        for (int i = 0; i <= lenS1; i++) {
            for (int j = 0; j <= lenS2; j++) {
                if (i == 0) {
                    distance[i][j] = j;
                } else if (j == 0) {
                    distance[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + cost);
                }
            }
        }
        return distance[lenS1][lenS2];
    }
}

