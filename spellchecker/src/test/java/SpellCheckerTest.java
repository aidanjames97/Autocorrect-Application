import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openjfx.Config;
import org.openjfx.Dictionary;
import org.openjfx.SpellChecker;

import java.util.Arrays;


/**
 * This class contains a suite of tests for the SpellChecker class, ensuring its correctness.
 * It covers a range of scenarios from basic word validation and capitalization to handling of complex strings and ignored words.
 */

public class SpellCheckerTest {

    Config config = new Config();
    private final Dictionary dictionary = new Dictionary(config.STD_DICTIONARY_PATH, config.USER_DICTIONARY_PATH);
    private final SpellChecker spellChecker = new SpellChecker(dictionary);


    /**
     * Tests the checkCapitalization method for proper identification of capitalized words.
     * It should return true for correctly capitalized words and false for improperly capitalized ones.
     */
    @Test
    public void testCheckCapitalization() {
        assertTrue(spellChecker.checkCapitalization("Java"));
        assertFalse(spellChecker.checkCapitalization("java"));
        assertTrue(spellChecker.checkCapitalization("I"));
        assertTrue(spellChecker.checkCapitalization("Canada")); // Acronym
        assertFalse(spellChecker.checkCapitalization("cAnada"));
    }

    /**
     * Validates the functionality of the isValidWord method in recognizing dictionary words.
     * Words that are in the dictionary should return true, while those that aren't should return false.
     */
    @Test
    public void testIsValidWord() {
        // Add test words to your dictionary if they are not already there
        dictionary.addWord("test");
        dictionary.addWord("java");

        assertTrue(spellChecker.isValidWord("test"));
        assertFalse(spellChecker.isValidWord("nonexistentword"));
    }

    /**
     * Evaluates the getSuggestions method's ability to provide recommendations for misspelled words.
     * The method should offer at least one suggestion for a misspelled word, assuming the correct word is in the dictionary.
     */
    @Test
    public void testGetSuggestions() {
        String[] suggestions = spellChecker.getSuggestions("testt"); // Assuming 'test' is in the dictionary
        assertTrue(suggestions.length > 0); // At least one suggestion expected
        for (String word : suggestions) System.out.println(word);
        assertTrue(Arrays.asList(suggestions).contains("test")); // 'test' should be a suggestion
    }


    /**
     * Tests spellchecker's levenshtein edit distance algorithm
     */
    @Test
    public void levenshteinEditDistance() {
        assertEquals(1, spellChecker.levenshteinEditDistance("test", "testt"));
        assertEquals(1, spellChecker.levenshteinEditDistance("test", "tes"));
        assertEquals(1, spellChecker.levenshteinEditDistance("test", "trst"));
    }

    /**
     * Checks the functionality of the ignoreAll method by adding a word to the ignore list.
     * Once ignored, the word should not be flagged by the spell checker as incorrect.
     */

    @Test
    public void testIgnoreAll() {
        spellChecker.ignoreAll("testword");
        assertTrue(spellChecker.ignoreWords.contains("testword")); // Check if the word is in the ignore list
    }

    /**
     * Verifies the ignoreOnce method's behavior, which should always return true.
     */

    @Test
    public void testIgnoreOnce() {
        assertTrue(spellChecker.ignoreOnce("testword")); // This method should just return true
    }

    /**
     * Evaluates how the SpellChecker's checkCapitalization method handles an empty string.
     * The method should return false when checking the capitalization of an empty string
     * as it does not contain any characters to determine capitalization.
     */

    @Test
    public void testEmptyStringCapitalization() {
        assertFalse(spellChecker.checkCapitalization(""));
    }

    /**
    * Examines the SpellChecker's isValidWord method to see how it handles an empty string.
    * An empty string should not be recognized as a valid word, and the method is expected
    * to return false in such cases.
    */

    @Test
    public void testEmptyStringValidWord() {
        assertFalse(spellChecker.isValidWord(""));
    }

    /**
    * Assesses the behavior of the SpellChecker's checkCapitalization method when it is provided with a null input.
    * The method is expected to return false, indicating that null is not recognized as having proper capitalization.
    */
    
    @Test
    public void testNullCapitalization() {
        assertFalse(spellChecker.checkCapitalization(null));
    }

    /**
     * Evaluates how the SpellChecker's isValidWord method reacts to a null input.
     * The method is expected to return false as a null value does not correspond to a valid word.
     */

    @Test
    public void testNullValidWord() {
        assertFalse(spellChecker.isValidWord(null));
    }

    /**
     * Tests how the spell checker handles words with special characters, such as "@java!".
     * The isValidWord method is expected to return false, indicating that words with special
     * characters are not valid.
     */

    @Test
    public void testSpecialCharacters() {
        assertFalse(spellChecker.isValidWord("@java!"));
        assertEquals(10, spellChecker.getSuggestions("@java!").length);
    }

    /**
     * Evaluates the spell checker's response to numeric input ("123").
     * The isValidWord method should not recognize numbers as valid words,
     * and hence should return false.
     */
    
    @Test
    public void testNumericInput() {
        assertFalse(spellChecker.isValidWord("123"));
        assertEquals(10, spellChecker.getSuggestions("123").length);
    }

    /**
     * Checks the effectiveness of the ignoreAll method in the SpellChecker.
     * Initially verifies that a specific word ("temporary") is not in the ignored list.
     * After using ignoreAll to ignore the word, it checks that the word is now correctly
     * recognized as ignored by the isIgnored method.
     */

    @Test
    public void testIgnoreWordEffectiveness() {
        String wordToIgnore = "temporary";
        assertFalse(spellChecker.isIgnored(wordToIgnore));
        spellChecker.ignoreAll(wordToIgnore);
        assertTrue(spellChecker.isIgnored(wordToIgnore)); // Should be false due to being ignored
    }

    /**
     * Checks the behavior of the SpellChecker for acronyms or words in all uppercase letters,
     * using "UPPERCASE" as the test word. It verifies that such words are recognized as correctly
     * capitalized and are valid dictionary words.
     */

    @Test
    public void testAcronym() {
        assertTrue(spellChecker.checkCapitalization("UPPERCASE"));
        assertTrue(spellChecker.isValidWord("UPPERCASE"));
    }

    /**
     * Validates the behavior of the SpellChecker when a word ("ignoree") is added to the ignore list
     * and then checked for validity. The test ensures that after invoking ignoreAll with "ignoree",
     * the word is correctly added to the ignore list and still recognized as a valid word by the
     * isValidWord method, suggesting that ignored words remain valid in the dictionary.
     */

    @Test
    public void testWordAlreadyIgnored() {
        spellChecker.ignoreAll("ignoree");
        assertTrue(spellChecker.ignoreWords.contains("ignoree"));
        assertTrue(spellChecker.isValidWord("ignoree"));
    }

    /**
    * Assesses how the spell checker handles a complex string mixed with characters and numbers ("Java123").
    * The test checks proper capitalization recognition, validity as a dictionary word, and suggestions provided.
    * Asserts that "Java123" is recognized as correctly capitalized, verifies that "Java123" is not considered a valid dictionary word,
    */

    @Test
    public void testComplexString() {
        assertTrue(spellChecker.checkCapitalization("Java123"));
        assertFalse(spellChecker.isValidWord("Java123"));
        assertEquals(10, spellChecker.getSuggestions("Java123").length);
    }

    /**
     * Tests the SpellChecker's ability to recognize hyphenated words as valid.
     * This test specifically checks if a hyphenated word like "spell-casting" is
     * correctly identified as a valid word in the dictionary.
     */

    @Test
    public void testWordWithHyphen() {
        assertTrue(spellChecker.isValidWord("spell-casting"));
    }
}