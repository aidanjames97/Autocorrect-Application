import org.junit.jupiter.api.Test;
import org.openjfx.Trie;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    @Test
    void addWordApple() {
        Trie tree = new Trie();
        tree.add("apple");
        assertTrue(tree.search("apple")); // word
        assertFalse(tree.search("app")); // word inside
        assertFalse(tree.search("apples")); // longer word
    }

    @Test
    void addWordOrange() {
        Trie tree = new Trie();
        tree.add("orange");
        assertTrue(tree.search("orange")); // word
        assertFalse(tree.search("orxnge")); // wrong middle letter
        assertFalse(tree.search("orangs")); // wrong last letter
        assertFalse(tree.search("xrange")); // wrong first letter
    }

    @Test
    void addOneLetterWord() {
        Trie tree = new Trie();
        tree.add("a");
        assertTrue(tree.search("a"));
        tree.add("app");
        assertTrue(tree.search("app"));
        assertFalse(tree.search("apple"));
    }

    @Test
    void searchWordNullTrie() {
        Trie tree = new Trie();
        assertFalse(tree.search("apple"));
    }

    @Test
    void searchWordThere() {
        Trie tree = new Trie();
        tree.add("apple");
        tree.add("orange");
        tree.add("app");

        assertTrue(tree.search("apple"));
        assertTrue(tree.search("orange"));
        assertTrue(tree.search("app"));
    }

    @Test
    void searchWordNotThere() {
        Trie tree = new Trie();
        tree.add("apple");
        tree.add("banana");

        assertFalse(tree.search("app"));
        assertFalse(tree.search("orange"));
        assertFalse(tree.search("appleorange"));
    }

    @Test
    void getAllWordsTest() {
        Trie tree = new Trie();
        tree.add("apple");
        tree.add("orange");
        tree.add("banana");
        tree.add("app");
        tree.add("a");
        tree.add("apples");
        String verify[] = {"a", "app", "apple", "apples", "banana", "orange"};

        int i = 0;
        for(Object word : tree.getAllWords()) {
            assertEquals(verify[i], word.toString());
            i++;
        }
    }
}