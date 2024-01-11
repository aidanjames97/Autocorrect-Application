package org.openjfx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class Config {
    public static String DOWNLOAD_DICTIONARY_URL = "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt";
    public String STD_DICTIONARY_PATH;
    public String USER_DICTIONARY_PATH;
    public String TEMP_OUTPUT_PATH;
    public String TEST_STD_DICTIONARY_PATH;
    public String TEST_USER_DICTIONARY_PATH;

    public Config() {
        String path = System.getProperty("user.home") + File.separator + ".uwoSpellChecker/";

        STD_DICTIONARY_PATH = path + "words_alpha.txt";
        USER_DICTIONARY_PATH = path + "user_dictionary.txt";
        TEMP_OUTPUT_PATH = path + "temp_output.txt";

        TEST_STD_DICTIONARY_PATH = path + "test_dictionary.txt";
        TEST_USER_DICTIONARY_PATH = path + "test_user_dictionary.txt";

        File dir = new File(path);

        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();

            try {
                File stdDict = new File(STD_DICTIONARY_PATH);
                downloadUsingStream(DOWNLOAD_DICTIONARY_URL, STD_DICTIONARY_PATH);

                File userDict = new File(USER_DICTIONARY_PATH);
                userDict.createNewFile();

                File testStdDict = new File(TEST_STD_DICTIONARY_PATH);
                testStdDict.createNewFile();

                File testUserDict = new File(TEST_USER_DICTIONARY_PATH);
                testUserDict.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void downloadUsingStream(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int count=0;
        while((count = bis.read(buffer,0,1024)) != -1) {
            fis.write(buffer, 0, count);
        }

        fis.close();
        bis.close();
    }

    public boolean resetDictionaries() {
        try {
            File stdDict = new File(STD_DICTIONARY_PATH);
            stdDict.delete();
            stdDict.createNewFile();
            downloadUsingStream(DOWNLOAD_DICTIONARY_URL, STD_DICTIONARY_PATH);

            File userDict = new File(USER_DICTIONARY_PATH);
            userDict.delete();
            userDict.createNewFile();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean resetTestDictionaries() {
        try {
            File testStdDict = new File(TEST_STD_DICTIONARY_PATH);
            testStdDict.delete();
            testStdDict.createNewFile();

            File testUserDict = new File(TEST_USER_DICTIONARY_PATH);
            testUserDict.delete();
            testUserDict.createNewFile();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
