package org.openjfx;


import java.util.*;

interface ISpellChecker {
    boolean checkCapitalization(String word);
    boolean isValidWord(String word);
    String[] getSuggestions(String word);
    boolean ignoreAll(String word);
    boolean ignoreOnce(String word);
    public boolean isIgnored(String word);
    public String removeTags(String target);
}


/**
 * A class for spell checking a document.
 */
public class SpellChecker implements ISpellChecker {

    /**
     * The dictionary to use for spell checking.
     */
    Dictionary dictionary;

    /**
     * The list of words to ignore.
     */
    public HashSet<String> ignoreWords;

    /**
     * Creates a new SpellChecker object.
     * @param dictionary The dictionary to use for spell checking.
     */
    public SpellChecker(Dictionary dictionary) {
        this.dictionary = dictionary;
        ignoreWords = new HashSet<String>();
    }

    /**
     * Checks if a word is capitalized correctly.
     * @param word The word to check.
     * @return True if the word is capitalized correctly.
     */
    @Override
    public boolean checkCapitalization(String word) {
        if (word == null || word.equals("")) {
            return false;
        }

        if (isAcronym(word)) {
            return true;
        }

        // Check first character is caps and rest is lower
        boolean firstCharIsCaps = Character.isUpperCase(word.charAt(0));
        boolean restIsLower = word.substring(1).equals(word.substring(1).toLowerCase());

        return firstCharIsCaps && restIsLower;
    }


    /**
     * Checks if a word is an acronym.
     * @param word The word to check.
     * @return True if the word is an acronym.
     */
    public boolean isAcronym(String word) {
        if (word == null) {
            return false;
        }

        boolean isAcronym = word.equals(word.toUpperCase());

        if (isAcronym) {
            if (word.length() > 1) {
                return true;
            } else {
                return word.equals("I");
            }
        }
        return false;
    }

    /**
     * Checks if a word is in the dictionary.
     * @param word The word to check.
     * @return True if the word is in the dictionary or is ignored.
     */
    @Override
    public boolean isValidWord(String word) {
        if (word == null || word.equals("")) {
            return false;
        }

        if (isIgnored(word)) {
            return true;
        }

        boolean isAcronym = word.equals(word.toUpperCase()) && !word.matches("[0-9]+");

        if (isAcronym) {
            if (word.length() == 1) {
                return word.equals("I");
            }

            return true;
        }

        word = word.toLowerCase().replaceAll(
                "-", "");

        return dictionary.searchWord(word);
    }

    /**
     * Gets suggestions for a word.
     * @param word The word to get suggestions for.
     * @return An array of suggested words, upto 10 suggestions.
     */
    @Override
    public String[] getSuggestions(String word) {
        HashMap<String, Integer> wordDistances = new HashMap<String, Integer>();
        PriorityQueue<String> suggestions = new PriorityQueue<String>(10, (o1, o2) -> {
            return -1 * (wordDistances.get(o1) - wordDistances.get(o2));
        });

        for (String w : dictionary.getAllWords()) {
            wordDistances.put(w, levenshteinEditDistance(word, w));

            // add to queue and keep only top 10 closest words
            suggestions.add(w);
            if (suggestions.size() > 10) {
                suggestions.poll();
            }
        }

        // return top 10
        return suggestions.toArray(new String[10]);
    }

    /**
     * Calculates the edit distance between two strings using the Levenshtein algorithm.
     * @param s1 The first string.
     * @param s2 The second string.
     * @return The edit distance between the two strings.
     */
    public int levenshteinEditDistance(String s1, String s2) {

        int[][] dp = new int[s1.length()+1][s2.length()+1];

        // Initialize the table
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        // Populate the table using dynamic programming
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    // replace = dp[i-1][j-1]
                    // insert = dp[i][j-1]
                    // delete = dp[i-1][j]
                    // optimal = 1 + min(replace, insert, delete)

                    // if i > 1 and j > 1 and a[i] = b[j-1] and a[i-1] = b[j] then
                    // d[i, j] = minimum(d[i, j],
                    //                   d[i-2, j-2] + 1)  // transposition

                    dp[i][j] = 1 + Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1]);

                    if (i > 1 && j > 1 && s1.charAt(i-1) == s2.charAt(j-2) && s1.charAt(i-2) == s2.charAt(j-1)) {
                        dp[i][j] = Math.min(dp[i][j], dp[i-2][j-2] + 1);
                    }
                }
            }
        }

        // Return the edit distance
        return dp[s1.length()][s2.length()];
    }

    /**
     * Adds a word to the ignore list for the current session.
     * @param word The word to ignore.
     * @return True if the word was successfully added to the ignore list.
     */
    @Override
    public boolean ignoreAll(String word) {
        try {
            ignoreWords.add(word);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean ignoreOnce(String word) {
        // This is unnecessary, but I'm leaving it here for now.
        // can be handled in @Document.java
        return true;
    }

    @Override
    public boolean isIgnored(String word) {
        return ignoreWords.contains(word);
    }

    /**
     * Method to remove all tags from a string
     * @param target String to be modified
     * @return String with all tags removed
     */
    public String removeTags(String target) {
        return target.replaceAll("(<\\w+( \\w+)*>|</\\w*>)| ", "");
    }


    /**
     * Adds a word to the dictionary.
     * @param word The word to add to the dictionary.
     * @return True if the word was successfully added to the dictionary.
     */
    public boolean addToDictionary(String word) {
        return dictionary.addWord(word.toLowerCase());
    }
}
