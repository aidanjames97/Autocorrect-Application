package org.openjfx;
import java.io.*;
import java.util.Arrays;

public class Dictionary {
    public Trie trie;
    private BufferedWriter bw;
    private String[] allWordsCache;

    /**
     * Creates a new Dictionary object.
     * @param dictPath the path to the dictionary file
     * @param userDictPath the path to the user dictionary file
     */
    public Dictionary(String dictPath, String userDictPath) {
        //this creates a new Trie named trie
        this.trie = new Trie();

        buildTree(new File(dictPath), new File(userDictPath));

        //this try is used to write the new words into the users dictionary
        try {
            this.bw = new BufferedWriter(new FileWriter(new File(userDictPath), true));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating user dictionary file");
        }
    }
    /**
     * @param Word 
     * the addWord boolean method is used to add a word.
     * Using and if statement that takes advantage of the search method in the Trie class
     * to see if the word exists. if it does then it returns false
     * and if it does not then it adds it to the Trie and users Dictionary and returns true
     */
    public boolean addWord(String Word){
        // this if statement is used to search if the word already exists
        if (!Word.matches("^[a-zA-Z]*$")) {
            return false;
        }

        if(trie.search(Word)){
            return false;
        }
        //adds the word into the trie
        trie.add(Word);
        //saves the word into the users dictionary 
        saveToUserDictionary(Word);
        allWordsCache = null;
        return true;
    }
    /**
     * @param Word 
     * returns the searched word by using the search method in trie 
     */
    public boolean searchWord(String Word) {
        return trie.search(Word);
    }

    public String[] getAllWords() {
        if (allWordsCache == null) {
            allWordsCache = trie.getAllWords().toArray(new String[0]);
        }
        return allWordsCache;
    }
    /**
     * @param stockDictionary
     * @param userDictionary
     * this method uses the private method loadWords to read the files and add them to the trie
     */
    public void buildTree(File stockDictionary, File userDictionary){
        // just loads it
        loadWords(stockDictionary);
        loadWords(userDictionary);
    }
    /**
     * @param file
     * reads the file that is given and then adds it to the trie
     */
    private void loadWords(File file) {
        // a try statement that uses BufferedReader to read the file 
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // makeing a string named line to used it in the while loop 
            String line;
            // while loop which uses line to read the file line by line and adding it to the trie
            while ((line = br.readLine()) != null) {
                trie.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading dictionary file");
        }
    }
    /**
     * @param Word
     * this private method uses word to save the word into the trie
     */
    private void saveToUserDictionary(String Word){
        // try statement which writes the given word in the users dictionary
        try {
            bw.write(Word.toLowerCase());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close buff to save user dict changes
    public void handleBwClose(){
        try {
            if (bw != null) {
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error closing BufferedWriter");
        }
    }

    public static void main(String[] args) {
        Dictionary dict = new Dictionary("/words_alpha.txt","/userDictionary.txt");
        SpellChecker spellChecker = new SpellChecker(dict);

        System.out.println("hello: " + spellChecker.isValidWord("hello"));

        long startTime;
        long endTime;
        long duration;

        System.out.println("amaaizng: " + spellChecker.isValidWord("amaaizng"));

        startTime = System.nanoTime();
        System.out.println("suggestions for amaaizng: " + Arrays.toString(spellChecker.getSuggestions("amaaizng")));
        endTime = System.nanoTime();

        duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("time taken: " + duration + " ms");


        System.out.println("monsster: " + spellChecker.isValidWord("monsster"));

        startTime = System.nanoTime();
        System.out.println("suggestions for monsster: " + Arrays.toString(spellChecker.getSuggestions("monsster")));
        endTime = System.nanoTime();

        duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("time taken: " + duration + " ms");


        System.out.println("spectaruclar: " + spellChecker.isValidWord("spectaruclar"));

        startTime = System.nanoTime();
        System.out.println("suggestions for spectaruclar: " + Arrays.toString(spellChecker.getSuggestions("spectaruclar")));
        endTime = System.nanoTime();

        duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("time taken: " + duration + " ms");

        System.out.println("program complete");
    }
}