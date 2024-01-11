import static org.junit.jupiter.api.Assertions.*;

import org.openjfx.Config;
import org.openjfx.Dictionary;
import org.openjfx.Document;
import org.openjfx.SpellChecker;

import java.io.*;

import org.junit.jupiter.api.Test;

public class DocumentTest {
    Config config = new Config();
    Dictionary dict = new Dictionary(config.STD_DICTIONARY_PATH, config.USER_DICTIONARY_PATH);
    SpellChecker spellChecker = new SpellChecker(dict);

    /**
     * Create a {@link Document} object that is used for testing purposes.
     * @return a {@link Document} object that is used for testing purposes.
     */
    public Document getTestDocument() {
        String filePath = Document.class.getResource("/testing.txt").getPath().replace("%20", " ");
        File file = new File(filePath);

        Document doc = null;

        try {
            doc = new Document(file, spellChecker, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return doc;
    }

    /**
     * Tests if a {@link Document} object is successfully created and loaded. It verifies that the
     * document and its current context (the text it holds) are not null, ensuring the document can
     * be interacted with upon loading.
     */
    @Test
    public void loadDocument() {
        Document doc = getTestDocument();

        assertNotNull(doc);
        assertNotNull(doc.currentContext);
    }

    /**
     * Verifies the Document's ability to correctly update its progress in terms of bytes read
     * after reading a line of text. This test ensures that the progress tracking mechanism is
     * accurate and reliable for user feedback and internal processing.
     */
    @Test
    public void updateProgress() {
        Document doc = getTestDocument();

        float read = doc.bytesRead;

        String sampleLine = "Hello World";

        doc.updateProgress(sampleLine);

        assertEquals(read + sampleLine.length(), doc.bytesRead);
    }

    /**
     * Checks the 'replace' functionality of the Document. It replaces the first word of a given
     * text with another word and confirms if the replacement is successful.
     */

    @Test
    public void replace() {
        Document doc = getTestDocument();

        String sampleLine = "Hello World";
        int targetIdx = 0;
        String replace = "Goodbye";

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.handleEvent("replace:" + replace);

        String result = doc.currentContext;
        String expected = "Goodbye World";

        assertEquals(expected, result);
    }

    /**
    * Validates the 'replace-all' feature of the Document. It replaces all instances of a specific
    * word within the text and checks the outcome. This test confirms that global text replacements
    * are executed correctly across the entire document.
    */
    @Test
    public void replaceAll() {
        Document doc = getTestDocument();

        String sampleLine = "Hello World Hello World";
        int targetIdx = 0;
        String replace = "Goodbye";

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.handleEvent("replace-all:" + replace);

        String result = doc.currentContext;
        String expected = "Goodbye World Goodbye World";

        assertEquals(expected, result);
    }

    /**
     * Confirms the functionality of the 'ignore' feature. When the Document encounters a spelling
     * error or a flagged word, this feature allows it to be ignored once without changing the text.
     */
    @Test
    public void ignore() {
        Document doc = getTestDocument();

        String sampleLine = "Hello World";
        int targetIdx = 0;

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.handleEvent("ignore");

        String result = doc.currentContext;
        String expected = sampleLine;

        assertEquals(expected, result);
    }

    /**
     * Assesses the ignoreAll ability to consistently ignore all instances of a specific
     * word during the spell check across the entire Document.
     */
    @Test
    public void ignoreAll() {
        Document doc = getTestDocument();

        String sampleLine = "Hello World Hello World";
        int targetIdx = 0;

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.handleEvent("ignore-all");

        String result = doc.currentContext;
        String expected = sampleLine;

        assertEquals(expected, result);

        sampleLine = "Hello";
        targetIdx = 0;

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        Error res = doc.spellCheckFile(doc.currentContext, doc.currWordIndex);
        String errorWord = res.getMessage();

        assertFalse(errorWord.equals("Hello"));
        assertTrue(spellChecker.ignoreWords.contains("Hello"));
    }

    /**
    * Tests the delete functionality of the Document. It removes a word from the text and verifies
    * if the text is correctly updated. This test ensures that the delete operation is properly
    * integrated and functional.
    */
    @Test
    public void delete() {
        Document doc = getTestDocument();

        String sampleLine = "Hello World.";
        int targetIdx = 0;

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.handleEvent("delete");

        String result = doc.currentContext;
        String expected = "World.";

        assertEquals(expected, result);
    }

    /**
     * Tests whether the spell checker recognizes a previously unrecognized word after adding it to
     * the dictionary.
     */
    @Test
    public void addToDict() {
        Document doc = getTestDocument();

        String sampleLine = "Hllo World";
        int targetIdx = 0;

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.startSpellCheck();

        assertNotNull(doc.currentError);
        String wrongWord = doc.currentError.getMessage();
        assertEquals(wrongWord, "Hllo");

        doc.handleEvent("add-to-dict");

        boolean exists = spellChecker.isValidWord("Hllo");
        assertTrue(exists);
    }

    /**
     * Checks the Document's ability to handle manual edits to the text. This feature allows for
     * direct text modifications outside the standard spell check process, ensuring that changes
     * made by the user are respected and retained in the Document.
     */
    @Test
    public void manualEdit() {
        Document doc = getTestDocument();

        String sampleLine = "Hllo World";
        int targetIdx = 0;

        doc.currentContext = sampleLine;
        doc.currWordIndex = targetIdx;

        doc.startSpellCheck();

        assertEquals(doc.currentError.getMessage(), "Hllo");

        String change = "Hello my";

        doc.handleEvent("manual-edit:" + change);

        String result = doc.currentContext;
        String expected = "Hello my World";

        assertEquals(expected, result);
    }
}
