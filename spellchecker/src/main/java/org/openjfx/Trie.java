package org.openjfx;
import java.util.ArrayList;

/**
 * Interface for Trie class
 */
interface ITrie {
    void add(String word);
    boolean search(String word);
    ArrayList<String> getAllWords();
}

/**
 * Class for each node to be used in Trie
 */
class TrieNode {
    /**
     * Array of Trie Nodes to hold 26 letters (children)
     */
    public TrieNode[] children;
    /**
     * Holds if letter is end of a word or not
     */
    public boolean endWord;

    /**
     * Constructor, initializes array of 26 Trie Nodes and sets endWord to false
     */
    public TrieNode() {
        children = new TrieNode[26]; // 26 b/c 26 letters in alphabet
        endWord = false;
    }
}

/**
 * Class for methods to modify and view Trie structure
 */
public class Trie implements ITrie{
    /**
     * Instance variables, of type Trie Node to hold root of Trie. Int holds length of longest word to be stored
     */
    private TrieNode root; // root of trie variable
    private int longer = 0; // longest word in Trie

    /**
     * Constructor, sets root variable to a new TrieNode
     */
    public Trie() { root = new TrieNode(); }

    /**
     * Adds word into Trie structure
     * @param word: Word to be added to Trie
     */
    public void add(String word) {
        TrieNode node = root; // setting 'node' to root of trie
        if(word.length() > longer) {
            longer = word.length();
        }
        // loop through chars in word
        for (char c : word.toCharArray()) {
            int index = c - 'a'; // setting index to numeric value of char (a=0, b=1, etc.)
            // checking if letter exists already
            if (node.children[index] == null) {
                node.children[index] = new TrieNode(); // letter not in list, add to children array
            }
            node = node.children[index]; // letter in list, point to it
        }
        node.endWord = true; // end of word, set end to true
    }

    /**
     * Searches for word in Trie structure
     * @param word: Word to search for
     * @return True if word is found, else false
     */
    public boolean search(String word) {
        TrieNode node = root; // setting 'node' to root of trie
        // loop through chars in word
        for (char c : word.toCharArray()) {
            int index = c - 'a'; // setting index to numeric value of char (a=0, b=1, etc.)

            if (index < 0 || index > 25) {
                return false;
            }

            //System.out.println("Trie: " + c);
            // checking for letter in children list
            if (node.children[index] == null) {
                // letter not found therefor word not in trie
                return false;
            }
            // advance to next node
            node = node.children[index];
        }
        // node not null and we have reached endWord = True
        return node != null && node.endWord;
    }

    /**
     * Function called from outside, calls private getAllWords function
     * @return True if successful, else returns false
     */
    public ArrayList<String> getAllWords() {
        TrieNode node = root;
        char[] str = new char[longer];
        ArrayList<String> out = new ArrayList<>();
        return getAllWords(node, str, 0, out);
    }

    /**
     * Private method called from public getAllWords method, returns array list of all words in Trie, works recursively
     * @param root: root node of Trie
     * @param str: char array for current word we are finding in Trie
     * @param level: depth level of Trie
     * @param out: Array list we will return containing all words in Trie
     * @return Array list of all words
     */
    private ArrayList<String> getAllWords(TrieNode root, char[] str, int level, ArrayList<String> out) {
        // checking for end of string
        if (root.endWord) {
            for (int k = level; k < str.length; k++)
                str[k] = ' '; // setting all other chars in array to blank
            String tmp = new String(str);
            out.add(tmp.replaceAll(" ", "")); // appending string to our AL
        }
        int i;
        for (i = 0; i < 26; i++) {
            // recursively check for another child (more letters in word)
            if (root.children[i] != null) {
                str[level] = (char) (i + 'a');
                getAllWords(root.children[i], str, level + 1, out);
            }
        }
        return out;
    }


    /**
     * Method for testing Trie methods
     */
    // main for testing
    public static void test() {
        Trie trie = new Trie();
        System.out.println("Testing add ...");
        trie.add("apple");
        trie.add("app");
        trie.add("banana");
        trie.add("car");
        trie.add("cat");
        System.out.println("Testing search ...");
        System.out.println(trie.search("apple")); // true
        System.out.println(trie.search("app"));   // true
        System.out.println(trie.search("ap"));    // false
        System.out.println(trie.search("banana")); // true
        System.out.println("Testing getAllWords():");
        System.out.println(trie.getAllWords().toString());
    }

    public static void main(String[] args) { test();}
}