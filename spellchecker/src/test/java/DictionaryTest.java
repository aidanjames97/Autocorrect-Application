import org.junit.jupiter.api.Test;
import org.openjfx.Config;
import org.openjfx.Dictionary;

import static org.junit.jupiter.api.Assertions.*;



class DictionaryTest {

    Config config = new Config();
    Dictionary dict = new Dictionary(config.TEST_STD_DICTIONARY_PATH, config.TEST_USER_DICTIONARY_PATH);

    /**
     * tests for existing word in the dictionary and returns false if the 
     * word exists because it can not add it in
     */
    @Test
    void addAlreadyExisting() {
        config.resetTestDictionaries();

        dict.addWord("bob");
        assertFalse(dict.addWord("bob"));
    }
    /**
     * adds a word into the dictionary and returns true if the word has been added
     * and is not in the dictionary 
     */
    @Test
    void addWord() {
        config.resetTestDictionaries();

        assertTrue(dict.addWord("maaz"));
    }
    /**
     * searches for a existing word in the dictionary and returns 
     * true if the word is there
     */
    @Test
    void searchExistingWordInDictionary() {
        config.resetTestDictionaries();

        dict.addWord("bob");

        assertTrue(dict.searchWord("bob"));
    }
    /**
     * searches for a word which has been added into the dictionary 
     * and returns true if it is there
     */
    @Test
    void searchaddedwordindictonary() {
        config.resetTestDictionaries();

        assertFalse(dict.searchWord("blue"));

        dict.addWord("blue");

        assertTrue(dict.searchWord("blue"));
    }

    /**
     * searches for a word which is not in the dictionary and returns
     * false if it is not there
     */
    @Test
    void searchwordindictionary() {
        config.resetTestDictionaries();
        dict.addWord("red");

        assertTrue(dict.searchWord("red"));
    }

    /**
     * compares the words which are in the dictionary and that have been added to the dictionary
     * to the dictioanry file and sees if they are there
     */
    @Test
    void existinggetAllWords() {
        config.resetTestDictionaries();

        String[] verify = {"apple", "banana", "blue", "bob", "fruit", "orange", "tim", "timmy"};

        for (String word : verify) {
            assertTrue(dict.addWord(word));
        }

        int i = 0;
        for(Object word : dict.getAllWords()) {
            assertEquals(verify[i], word.toString());
            i++;
        }
    }

    /**
     * tests that are to check if the spaceing or added characters matter in the addword
     * method and will return false
     */
    @Test
    void testSpecialChars() {
        config.resetTestDictionaries();

        assertFalse(dict.addWord(" bob"));
        assertFalse(dict.addWord("bob!"));
        assertFalse(dict.addWord("bob."));
    }
}