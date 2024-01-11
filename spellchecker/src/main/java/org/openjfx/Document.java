package org.openjfx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

/**
 * Class for processing and spell-checking a document
 */
public class Document {
    /**
     * Spell checker to be used for spell-checking document
     */
    SpellChecker checker;

    /**
     * File of document to be spell-checked
     */
    File file;

    /**
     * Buffered reader for reading to file
     */
    BufferedReader br;

    /**
     * Buffered writer for writing to file
     */
    BufferedWriter bw;

    /**
     * Word count of document
     */
    int wordCount;

    /**
     * Word count of document
     */
    public int lineCount;

    /**
     * Char count of document
     */
    public int charCount;
    
    /**
     * Total file size of document
     */
    float totalFileSize;

    /**
     * Bytes read of document
     */
    public float bytesRead;

    /**
     * Progress of spell-checking document
     */
    public float progress;

    /**
     * Path of temporary output file
     */
    public String tempOutput;

    /**
     * Map of words to replace with
     */
    private final HashMap<String, String> replaceAllWords = new HashMap<>();

    /**
     * Flag to skip tags in html or xml
     */
    boolean skipTags;

    /**
     * Class for each error found in document
     */
    public enum ErrorType {
        SPELLING, CAPITALIZATION, MISCAPITALIZATION, DOUBLE_WORD;
    }

    /**
     * Count of each error type
     */
    public HashMap<ErrorType, Integer> errorCounts;

    /**
     * Current error in document
     */
    public Error currentError;

    /**
     * Current error type in document
     */
    public ErrorType currentErrorType;

    /**
     * Current context of document
     */
    public String currentContext;

    /**
     * Current suggestions for error
     */
    public String[] currentSuggestions;

    /**
     * Current word index of error in context
     */
    public int currWordIndex;

    /**
     * Constructor, initializes document with file and spell checker
     * @param file    File to be spell-checked
     * @param checker Spell checker to be used for spell-checking
     * @throws IOException If file not found
     */
    public Document(File file, SpellChecker checker, Config config) throws IOException {
        this.file = file;
        this.checker = checker;
        this.tempOutput = config.TEMP_OUTPUT_PATH;
                                                                                                                         // +
        // Creating a buffered reader here to remember last line read
        this.br = new BufferedReader(new FileReader(this.file));
        this.bw = new BufferedWriter(new FileWriter(tempOutput));

        // stats
        this.totalFileSize = file.length();
        this.bytesRead = 0;

        // setup current state
        this.currentContext = readLine();
        this.currWordIndex = 0;
        this.errorCounts = new HashMap<ErrorType, Integer>();
        this.progress = currentContext == null ? 100 : 0;
    }

    /**
     * Start spell-checking document
     * @return Current error in document
     */
    public Error startSpellCheck() {
        this.currentError = currentContext == null ? null : spellCheckFile(currentContext, currWordIndex);
        return this.currentError;
    }

    /**
     * Returns current error in document
     * @param currentContext Current context of document
     * @param idx Current word index of error in context
     * @return Error object of current error
     */
    public Error spellCheckFile(String currentContext, int idx) {
        // Iterating through words, ignoring indexes to be ignored, spaces (as they
        // count as a word), double spaces
        // Flagging duplicate words, double spaces b/w words, and single extra spaces at
        // the beginning and end of a sentece

        this.currentSuggestions = null;

        String[] words = currentContext.split("(?<=\\s)|(?=\\s)");

        for (; idx < words.length; idx++) {
            this.currWordIndex = idx;

            // skip empty lines and spaces
            if (words[idx].equals("") || words[idx].equals(" ")) {
                continue;
            }

            wordCount++;

            // Checking next words
            if (idx + 2 < words.length && words[idx].equals(words[idx + 2])) {
                // double word
                System.out.println("Double word: " + words[idx]);
                errorCounts.put(ErrorType.DOUBLE_WORD, errorCounts.getOrDefault(ErrorType.DOUBLE_WORD, 0) + 1);
                this.currentErrorType = ErrorType.DOUBLE_WORD;
                this.currentSuggestions = checker.getSuggestions(words[idx]);
                return new Error(words[idx]);
            }
            // Prev words
            if (idx - 2 >= 0 && words[idx].equals(words[idx - 2])) {
                // double word
                System.out.println("Double word: " + words[idx]);
                errorCounts.put(ErrorType.DOUBLE_WORD, errorCounts.getOrDefault(ErrorType.DOUBLE_WORD, 0) + 1);
                this.currentErrorType = ErrorType.DOUBLE_WORD;
                this.currentSuggestions = checker.getSuggestions(words[idx]);
                return new Error(words[idx]);
            }
            // ** Play around with pos?**
            // ex. .. sentence. secondz ... detects secondz as a cap error forcing user to correct
            // if they manually correct and fail to capitialize this error triggers again
            // if they "" and misspell the block below is triggered 
            if (idx - 2 >= 0 && hasEndPunct(words[idx - 2])) {  // Curr word not capped and prev word has punct

                if (checker.isAcronym(checker.removeTags(words[idx]))) continue;    // if acronym ignore

                if (!checker.checkCapitalization(checker.removeTags(words[idx]))) {
                    System.out.println("Not capitalized but should be: " + words[idx]);
                    errorCounts.put(ErrorType.CAPITALIZATION, errorCounts.getOrDefault(ErrorType.CAPITALIZATION, 0) + 1);
                    this.currentErrorType = ErrorType.CAPITALIZATION;
                    this.currentSuggestions = checker.getSuggestions(words[idx]);
                    return new Error(words[idx]);
                }
            }
            // Current word is cap but prev word has no punct
            if (idx - 2 >= 0 && !words[idx - 2].equals(" ") && !hasEndPunct(words[idx - 2])){

                if (checker.isAcronym(checker.removeTags(words[idx]))) continue;   

                if (checker.checkCapitalization(checker.removeTags(words[idx]))) {
                    System.out.println("Capitalized but shouldn't be: " + words[idx]);
                    errorCounts.put(ErrorType.MISCAPITALIZATION, errorCounts.getOrDefault(ErrorType.MISCAPITALIZATION, 0) + 1);
                    this.currentErrorType = ErrorType.MISCAPITALIZATION;
                    this.currentSuggestions = checker.getSuggestions(words[idx]);
                    return new Error(words[idx]);
                }
            }
            if (hasEndPunct(words[currWordIndex])){
                if (!checker.isValidWord(words[idx].substring(0, words[idx].length() - 1))) {
                    System.out.println("Not a valid word: " + words[idx]);
                    this.currentSuggestions = checker.getSuggestions(words[idx]);   
                    errorCounts.put(ErrorType.SPELLING, errorCounts.getOrDefault(ErrorType.SPELLING, 0) + 1);
                    this.currentErrorType = ErrorType.SPELLING;
                    return new Error(words[idx]); // error if word is not in dictionary
                }
            }
            else if (!hasEndPunct(words[currWordIndex])){
                if (!checker.isValidWord(words[idx])) {
                    System.out.println("Not a valid word: " + words[idx]);
                    this.currentSuggestions = checker.getSuggestions(words[idx]);   
                    errorCounts.put(ErrorType.SPELLING, errorCounts.getOrDefault(ErrorType.SPELLING, 0) + 1);
                    this.currentErrorType = ErrorType.SPELLING;
                    return new Error(words[idx]); // error if word is not in dictionary
                }
            }

        }
        this.charCount += this.currentContext.length();
        updateProgress(currentContext);

        // Write words to file and go to next line in document
        if (!saveContext(words))
            throw new Error("Write to buffer failed: spellCheckFile()");

        String nextLine = readLine();

        if (nextLine != null) {
            lineCount++;
            this.currWordIndex = 0;
            this.currentContext = nextLine;
            this.currentContext = updateContextReplaceAll(); // get next line and update replace
            return spellCheckFile(this.currentContext, this.currWordIndex);
        }

        System.out.println("Document: Spell-checking complete.");
        this.currentError = null;
        this.progress = 100; // *FIX Not working for html files, never shows 100% at completion*/
        return null;
    }


    /**
     * Clean context with words already requested to be replaced
     * @return Current error in document
     */
    private String updateContextReplaceAll() {
        String[] words = currentContext.split("(?<=\\s)|(?=\\s)");
        String[] updatedWords = new String[words.length];

        for (int i = 0; i < words.length; i++) {
            if (replaceAllWords.containsKey(words[i])) { // replace replace-all words
                updatedWords[i] = replaceAllWords.get(words[i]);
            } else {
                updatedWords[i] = words[i];
            }
        }

        return String.join("", updatedWords);
    }

    /**
     * Updates progress of spell-checking document
     * @param currentContext Current context of document
     */
    public void updateProgress(String currentContext) {
        this.bytesRead += currentContext.length();
        this.progress = (bytesRead / totalFileSize) * 100;
    }

    /**
     * Handles event from UI for current error
     * @param eventType Event type to be handled
     * @return True if event was handled, else false
     */
    public boolean handleEvent(String eventType) {
        if (eventType.startsWith("replace:")) {
            handleReplace(eventType);
            return true;
        }

        if (eventType.startsWith("replace-all:")) {
            handleReplaceAll(eventType);
            return true;
        }

        if (eventType.equals("ignore")) { // Ignore once -- working
            handleIgnore();
            return true;
        }

        if (eventType.equals("ignore-all")) {
            handleIgnoreAll();
            return true;
        }

        if (eventType.equals("delete")) { // Delete once -- all cases working
            handleDelete();
            return true;
        }

        if (eventType.equals("add-to-dict")) {
            handleAddToDict();
            return true;
        }

        if (eventType.startsWith("manual-edit:")) { // Context manually edited, change that word and copy other words
            handleManualEdit(eventType);
            return true;
        }

        // Exit events (all working)
        if (eventType.equals("premature-exit")) { // after this it goes to exit event below
            handlePrematureExit();
            return true;
        }

        if (eventType.startsWith("exit:")) {
            handleExit(eventType);
            return true;
        }

        if (eventType.equals("destroy-file")) {
            handleDestroyFile();
            return true;
        }

        return false;
    }

    /**
     * Close buffered reader and writer and destroy output file
     */
    private void handleDestroyFile() {
        closeBufferReader();
        closeBufferedWriter();
        if (!destroyOutputFile())
            throw new Error(
                    "Error destroying output file. May still exist in directory of program. But not a big deal because it gets overwritten");
    }

    /**
     * Handle event for exiting document
     * @param eventType Event type being handled
     */
    private void handleExit(String eventType) {
        closeBufferReader(); // Probably don't need this b/c exiting when no more context in readLine() and
                             // it closes br and bw
        closeBufferedWriter();
        String savePath = eventType.substring("exit:".length());
        System.out.println("savepath (doc): " + savePath);
        if (!moveOutputFile(savePath))
            throw new Error("Failed to move temp output file to savepath: " + savePath);
    }

    /**
     * Handle event for exiting document prematurely
     */
    private void handlePrematureExit() {
        if (!saveContext(currentContext.split("(?<=\\s)|(?=\\s)")))
            throw new Error("Write to buffer failed: in handle-Event(), premature-exit"); // write whatever context we
    }

    /**
     * Handle event for adding current error to dictionary
     */
    private void handleAddToDict(){
        // added to dictionary in UI button handler before coming to document
        String[] words = currentContext.split("(?<=\\s)|(?=\\s)");
        if (!checker.addToDictionary(words[currWordIndex])) {
            throw new Error("Failed to add word to dictionary: " + words[currWordIndex]);
        }

        this.currentError = spellCheckFile(currentContext, currWordIndex+1);
    }

    /**
     * Handle event for manually editing current error
     * @param eventType Event type being handled
     */
    private void handleManualEdit(String eventType) {
        // **FIX** for html
        String manualCorrection = eventType.substring("manual-edit:".length());
        String[] wordArr = currentContext.split("(?<=\\s)|(?=\\s)");
        String[] updatedWords = new String[wordArr.length];

        System.arraycopy(wordArr, 0, updatedWords, 0, currWordIndex); // src arr, start index, dest array, start index,
                                                                      // len
        updatedWords[currWordIndex] = manualCorrection;
        System.arraycopy(wordArr, currWordIndex + 1, updatedWords, currWordIndex + 1,
                wordArr.length - 1 - currWordIndex);

        this.currentContext = String.join("", updatedWords);
        this.currentError = spellCheckFile(currentContext, currWordIndex);
    }

    /**
     * Handle event for deleting current error
     */
    private void handleDelete() {
        String[] words = currentContext.split("(?<=\\s)|(?=\\s)");
        String[] updatedWordsAfterDeletion = new String[words.length];

        boolean isLastWord = currWordIndex == words.length - 1;
        
        // If only word
        if (words.length == 1) {
            this.currentContext = "";
            currentError = spellCheckFile(this.currentContext, this.currWordIndex + 1);
            wordCount--;
            return;
        }

        // Deleting last word
        if (isLastWord) {
            System.out.println("Del last word");
            updatedWordsAfterDeletion = new String[words.length - 2]; // Delete space before last word

            if (hasEndPunct(words[currWordIndex])) { // Add punct
                String punct = String.valueOf(words[currWordIndex].charAt(words[currWordIndex].length() - 1));
                words[currWordIndex - 2] += punct;
            }

            System.arraycopy(words, 0, updatedWordsAfterDeletion, 0, words.length - 2);
        }

        // Delete all other words
        else if (words[currWordIndex + 1].equals(" ") && !words[currWordIndex].equals(" ")) {
            System.out.println("Del any word");
            updatedWordsAfterDeletion = new String[words.length - 2];

            // If current letter has punct, cap next letter and add punct to end of prev word
            if (currWordIndex + 2 >= 0 && currWordIndex + 2 < words.length && !words[currWordIndex + 2].equals(" ")
                    && hasEndPunct(words[currWordIndex])) {
                String punct = String.valueOf(words[currWordIndex].charAt(words[currWordIndex].length() - 1));
                words[currWordIndex + 2] = Character.toUpperCase(words[currWordIndex + 2].charAt(0))
                        + words[currWordIndex + 2].substring(1);
                words[currWordIndex - 2] += punct;
            }

            // If prev letter has punct cap next letter
            if (currWordIndex + 2 >= 0 && currWordIndex + 2 < words.length && !words[currWordIndex + 2].equals(" ")
                    && currWordIndex - 2 >= 0 && hasEndPunct(words[currWordIndex - 2])) {
                words[currWordIndex + 2] = Character.toUpperCase(words[currWordIndex + 2].charAt(0))
                        + words[currWordIndex + 2].substring(1);
            }
            

            System.arraycopy(words, 0, updatedWordsAfterDeletion, 0, currWordIndex);
            System.arraycopy(words, currWordIndex + 2, updatedWordsAfterDeletion, currWordIndex,
                    words.length - 2 - currWordIndex);
        }

        String updatedContext = String.join("", updatedWordsAfterDeletion);
        currentContext = updatedContext;
        currentError = spellCheckFile(updatedContext, this.currWordIndex);
        // if (currWordIndex < words.length && !words[currWordIndex].equals(" ")) wordCount--;
        wordCount--;
    }

    /**
     * Handle event for ignoring current error fro remainder of document
     */
    private void handleIgnoreAll() {
        String[] wordArr = currentContext.split("(?<=\\s)|(?=\\s)");
        checker.ignoreAll(wordArr[currWordIndex]);
        this.currentError = spellCheckFile(currentContext, this.currWordIndex += 1);
    }

    /**
     * Handle event for ignoring current error
     */
    private void handleIgnore() {
        this.currentError = spellCheckFile(currentContext, this.currWordIndex += 1);
    }

    /**
     * Handle event for replacing all instances of current error with a target word
     * @param eventType Event type being handled
     */
    private void handleReplaceAll(String eventType) {
        String[] words = currentContext.split("(?<=\\s)|(?=\\s)");

        String target = eventType.substring("replace-all:".length());
        replaceAllWords.put(words[currWordIndex], target);

        this.currentContext = updateContextReplaceAll();
        this.currentError = spellCheckFile(currentContext, this.currWordIndex += 1);
    }

    /**
     * Handle event for replacing current error with a target word
     * @param eventType Event type being handled
     */
    private void handleReplace(String eventType) {
        String[] words = currentContext.split("(?<=\\s)|(?=\\s)");
        String[] updatedWords = new String[words.length];

        String target = eventType.substring("replace:".length()); // get replacement word after 'replace:..'

        System.arraycopy(words, 0, updatedWords, 0, currWordIndex); // src arr, start index, dest array, start index,
                                                                    // len
        if (hasEndPunct(words[currWordIndex])) {
            char lastPunctuation = words[currWordIndex].charAt(words[currWordIndex].length() - 1);
            updatedWords[currWordIndex] = target+lastPunctuation;
        }
        else updatedWords[currWordIndex] = target;
        System.arraycopy(words, currWordIndex + 1, updatedWords, currWordIndex + 1, words.length - 1 - currWordIndex);

        currentContext = String.join("", updatedWords);
        currentError = spellCheckFile(currentContext, this.currWordIndex += 1);
    }

    /**
     * Called by main to updateDisplays/stats
     * @param errorType spelling, double words, cap or miscap errors counts are returned
     */
    public int getErrorCounts(String errorType) {
        if (errorType.equals("spelling-errors")){
            return errorCounts.getOrDefault(ErrorType.SPELLING, 0);
        }
        if (errorType.equals("double-words")){
            return errorCounts.getOrDefault(ErrorType.DOUBLE_WORD, 0);
        }
        if (errorType.equals("cap-errors")){
            return errorCounts.getOrDefault(ErrorType.CAPITALIZATION, 0);
        }
        if (errorType.equals("miscap-errors")){
            return errorCounts.getOrDefault(ErrorType.MISCAPITALIZATION, 0);
        }
        return 0;
    }
    /**
     * Checks if word ends with punctuation
     * @param word Word to check for end punctuation
     * @return True if word ends with punctuation, else false
     */
    private boolean hasEndPunct(String word) {
        String[] punctuationMarks = { ".", "!", "?" }; // **FIX* not checking commas
        for (String punct : punctuationMarks) {
            if (word.endsWith(punct))
                return true;
        }
        return false;
    }

    /**
     * Fetches next line from document
     * @return Next line from document
     */
    private String readLine() {
        String line = "";
        try {
            line = br.readLine();

            // EoF
            if (line == null) {
                System.out.println("End of file");
                closeBufferReader();
                closeBufferedWriter();
                return null;
            }

            // skip tags if html or xml--reading lines instead of just extension (.txt can
            // contain html, xml too)
            if (line.trim().startsWith("<!DOCTYPE html") || line.trim().startsWith("<?xml")) {
                // Prompt
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initModality(Modality.APPLICATION_MODAL); // modal to entire app, block other windows
                alert.setTitle("Tags detected");
                alert.setHeaderText("HTML or XML files not supported.");
                alert.setContentText("Unable to spellcheck HTML or XML tags. The program will end.");

                // Add buttons to the alert
                ButtonType okButton = new ButtonType("Ok");
                alert.getButtonTypes().setAll(okButton);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == okButton) {
                    return null;
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception based on your application's requirements
        }

        return line; // Returns all words up until \n, null if EoF
    }

    /**
     * Helper to close buffered reader
     */
    private void closeBufferReader() {
        try {
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to close buffered writer
     */
    private void closeBufferedWriter() {
        try {
            if (bw != null) {
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves context to file
     * @param contextWords Array of words to be saved to file
     * @return True if context was saved to file, else false
     */
    private boolean saveContext(String[] contextWords) {
        try {
            for (String word : contextWords) {
                bw.write(word);
            }
            bw.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Moves the output file to the destination path
     * @param destPathString The destination path for the output file
     * @return True if the file was successfully moved, else false
     */
    private boolean moveOutputFile(String destPathString) {
        // Ensure the destination file has a .txt extension
        String extension = "." + file.getName().substring(file.getName().lastIndexOf('.') + 1);
        if (!destPathString.endsWith(extension)) {
            destPathString += extension;
        }

        Path sourcePath = Path.of(tempOutput);
        Path destinationPath = Path.of(destPathString);

        while (true) {
            try {
                // Check if the destination file exists and append "+copy" if it does
                if (Files.exists(destinationPath)) {
                    String fileName = destinationPath.getFileName().toString();

                    // Get the base name and extension
                    int dotIndex = fileName.lastIndexOf('.');
                    String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
                    String newFileName = baseName + "+copy" + extension; // Explicitly add .txt

                    // Replace the file name in the destination path
                    destinationPath = destinationPath.resolveSibling(newFileName);
                }

                // Move the file
                Files.move(sourcePath, destinationPath);
                System.out.println("File successfully moved to: " + destinationPath);
                return true;
            } catch (FileAlreadyExistsException e) {
                System.err.println("File move failed. Destination file already exists.");
            } catch (IOException e) {
                System.err.println("File move failed. IOException: " + e.getMessage());
                break;
            }
        }

        return false;
    }

    /**
     * Destroys the output file
     * @return True if the file was successfully destroyed, else false
     */
    private boolean destroyOutputFile() {
        Path sourcePath = Path.of(tempOutput);

        try {
            Files.delete(sourcePath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}