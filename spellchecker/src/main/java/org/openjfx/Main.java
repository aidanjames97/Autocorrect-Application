package org.openjfx;

// JavaFX imports
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// Other imports
//import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
// import java.io.IOException;

public class Main extends Application {
    /**
     * Stage primaryStage: the primary window of the application
     * AnchorPane root: root container for UI elements within the scene
     * rootFontSize: init font size
     */
    private Stage primaryStage; // The primary window of the application
    private AnchorPane root; // The root container for UI elements within the scene
    private double rootFontSize = 14.0; // Init font size

    /**
     * Open menu item in menu bar
     */
    private MenuItem openMenuItem;

    /**
     * selectedFileToCheck: the file user has browsed and selected to spell check
     * browseButton: to open file browser
     * startSpellCheckButton: to start spellchecking proccess after file has been
     * browsed and selected
     * filePathField: shows file path
     */
    java.io.File selectedFileToCheck;
    private Button startSpellCheckButton, browseButton; // For input area items
    private VBox fileContentsContainer;
    private TextField filePathField;

    /**
     * spellCheckerContainer: spell checker items (suggestions, correction options)
     * selectedSuggWord: selecteted suggested words to replace error word
     * currSpellingError: the spelling error
     * errorTypeLabel: displays the type of spelling error (miscap, double word,
     * spelling error)
     * currentErrorField: UI area that displays the string currSpellingError
     */
    private AnchorPane spellCheckerContainer; // For spell checker items (suggestions, correction options)
    private String selectedSuggWord = "";
    private String currSpellingError;
    private Label errorTypeLabel;
    private TextField currentErrorField;
    private Button replaceButton, replaceAllButton, ignoreButton, ignoreAllButton, addToDictButton, deleteTextButton;
    ListView<String> suggestListView;
    ObservableList<String> currentSuggestions; // Suggested word items

    /**
     * fileTextField: UI area displaying fileContents and manually editing
     * fileContents: contents of document
     * manualEditField: UI area to enter manual correction
     * saveManualChanges: button to save manual changes to error
     */
    private TextArea fileTextField; // For displaying file text and manually editing
    private String fileContents; // Contains contents of document and displays in fileTextField text area
    private TextArea manualEditField; // Manual correction
    private Button saveManualChanges;

    /**
     * progressLabel: displays progress after spellchecking a line
     */
    Label progressLabel;

    /**
     * document: document that's being spellchecked
     * dictionary: user and stock dictionaries
     * spellchecker: backend spellchecker handling spell checking
     */
    private Document document;
    private Dictionary dictionary;
    private SpellChecker spellChecker;

    /**
     * spellCheckingComplete: flag to indicate if no more errors remain
     * spellingStatistics: type of spelling error and count
     */
    private boolean spellCheckingComplete = false;
    private Map<String, Integer> spellingStatistics = new HashMap<>();

    /**
     * @param primaryStage main stage for app
     *                     Create display areas, set tooltips, apply style, and show
     *                     window
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Assign the primary stage to the class variable
        // primaryStage.setResizable(false); // Disable window resizing
        root = new AnchorPane(); // The main container that organizes UI elements by setting to different anchor
                                 // points

        // **Create display areas here**
        createMenuBar();
        createInputContainer(); // Sets up the file input section
        createSpellCheckerContainer();
        ; // Sets up the spell checker: suggestions and spellchecking options
        createfileContentsContainer(); // Sets up the area to display file contents
        createFooterContainer(); // Sets up footer area including progress, radio-buttons, in future spelling
                                 // stats
        // Setting tool-tips/hover-text for all buttons
        // Will modify it to set custom messages like:
        // startSpellCheckButton.setTooltip(new Tooltip("Start the spellchecking
        // process."));
        setTooltipsForButtons(
                startSpellCheckButton, browseButton,
                replaceButton, replaceAllButton, ignoreButton, ignoreAllButton, addToDictButton, deleteTextButton);

        // Create a scene
        Scene scene = new Scene(root, 720, 720); // The view containing UI elements
        primaryStage.setMinWidth(740); // Prevent from resizing below minimum size (not equal to 720 as above due to
                                       // styling and OS)
        primaryStage.setMinHeight(760);

        applyStylesheet("/styles.css", scene);

        // Set the scene for the primary stage
        primaryStage.setScene(scene);

        // Set the title of the window
        primaryStage.setTitle("Spell-Checker App");

        // Set the application icon
        Image icon = new Image(getClass().getResourceAsStream("/program_icon.png"));
        primaryStage.getIcons().add(icon);

        // Add an event handler to the window's close button (X button)
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to prevent the window from closing directly
            handleExitApp(); // Call your exitApplication method
        });

        // Show the window
        primaryStage.show();
    }

    /**
     * @param stylesheet
     * @param scene      main scene, only one scene used
     *                   Helper to start, applies stylesheet
     */
    private void applyStylesheet(String stylesheet, Scene scene) {
        if (scene == null)
            return;

        URL resource = getClass().getResource(stylesheet);
        if (resource == null) {
            System.out.println("Resource not found: " + stylesheet);
            return;
        }

        scene.getStylesheets().clear();
        scene.getStylesheets().add(resource.toExternalForm());
    }

    /**
     * Tooltips to display on hover over buttons
     * 
     * @param buttons
     */
    private void setTooltipsForButtons(Button... buttons) {
        for (Button button : buttons) {
            Tooltip tooltip = new Tooltip(button.getText());
            button.setTooltip(tooltip);
        }
    }

    /**
     * Menu bar components, file menu items: open, exit, view
     */
    private void createMenuBar() {
        // Create a menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        // Create a "File" menu
        Menu fileMenu = new Menu("File");
        fileMenu.getStyleClass().add("file-menu");

        // Create "Open" and "Exit" menu items
        openMenuItem = new MenuItem("Open");
        openMenuItem.getStyleClass().add("file-menu-open");
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.getStyleClass().add("file-menu-exit");

        Menu helpMenu = new Menu("Help");
        MenuItem helpMenuItem = new MenuItem("Help");
        helpMenuItem.getStyleClass().add("file-menu-help");

        // Set actions for the "Open" and "Exit" menu items
        openMenuItem.setOnAction(e -> handleBrowseFile(primaryStage));
        helpMenuItem.setOnAction(e -> showHelpMenu());
        exitMenuItem.setOnAction(e -> handleExitApp());

        // Add file and help menu items
        fileMenu.getItems().addAll(openMenuItem, exitMenuItem);
        helpMenu.getItems().addAll(helpMenuItem);

        // View menu
        Menu viewMenu = new Menu("View");
        viewMenu.getStyleClass().add("view-menu");

        // Create "Increase Text Size," "Decrease Text Size," and "Reset Text Size" menu
        // items
        MenuItem increaseTextSizeMenuItem = new MenuItem("Increase Text Size");
        MenuItem resetTextSizeMenuItem = new MenuItem("Reset Text Size");
        resetTextSizeMenuItem.setDisable(true);

        // Event handles for these items
        increaseTextSizeMenuItem.setOnAction(e -> {
            // Allow an increase up to 18.0 in font size and disable once at 18.0
            if (rootFontSize < 18.0) {
                resetTextSizeMenuItem.setDisable(false);
                if (rootFontSize == 16.0) {
                    increaseTextSizeMenuItem.setDisable(true);
                }
                adjustTextSize(2.0);
            }
        });

        resetTextSizeMenuItem.setOnAction(e -> {
            rootFontSize = 14.0;
            String fontSizeStyle = "-fx-font-size: " + rootFontSize + "px;";
            root.setStyle(fontSizeStyle);
            increaseTextSizeMenuItem.setDisable(false);
            resetTextSizeMenuItem.setDisable(true);
        });

        // Add menu items to the "View" menu
        viewMenu.getItems().addAll(increaseTextSizeMenuItem, resetTextSizeMenuItem);
        // Add the "File" and "View" menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        // Add help menu item

        // Anchoring edges of menuBar to allow accessible viewing
        AnchorPane.setTopAnchor(menuBar, 0.0); // Setting position of menubar at the top
        AnchorPane.setLeftAnchor(menuBar, 0.0);
        AnchorPane.setRightAnchor(menuBar, 0.0);
        root.getChildren().add(menuBar); // Adding the menubar to the root container
    }

    /**
     * Helper event handler to show the help "menu"
     * Not a real menu. Just an alert.
     */
    private void showHelpMenu(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        String contentText = "How to use app:\n\n" +
            "Spelling error is displayed in red. Use the buttons to make changes.\n\n" +
            "Type in your manual correction by the manual correction textbox and press save to make changes.\n\n" +
            "Errors can be replaced with suggested word after selecting a suggested word and pressing replace/replace all.\n\n" +
            "Context of the current line in document is shown under file contents\n\n" +
            "Hover over statistics label at the bottom center to see full stats.";

    alert.setContentText(contentText);
    alert.showAndWait();
    }

    /**
     * Helper event handler for file menu increase text size
     * 
     * @param pixelChange always 2.0
     */
    private void adjustTextSize(double pixelChange) {
        rootFontSize += pixelChange;
        String fontSizeStyle = "-fx-font-size: " + rootFontSize + "px;";
        root.setStyle(fontSizeStyle);
    }

    /**
     * Input items: file path, browse and start buttons
     * Items: filepathfield, browseButton, startSpellCheckButton
     */
    private void createInputContainer() {
        // Create a VBox to hold the input group and text field
        Label titleLabel = new Label("File path:");
        VBox inputContainer = new VBox();
        inputContainer.getStyleClass().add("input-container"); // Example of adding a container, can add an ID too using
                                                               // .setId("inputContainer");

        // Create an HBox to group the "Browse," "Start Spell Check," and
        // style-switching buttons
        HBox buttonGroup = new HBox();

        // Create a text field to display the selected file path
        filePathField = new TextField();
        filePathField.setEditable(false);
        filePathField.setDisable(true);

        // Create a button to browse for a file
        browseButton = new Button("Browse");
        browseButton.setOnAction(e -> handleBrowseFile(primaryStage));

        // Create a button to start spell-check
        startSpellCheckButton = new Button("Start Spell Check");
        startSpellCheckButton.setDisable(true);
        startSpellCheckButton.setOnAction(e -> startSpellCheck());

        // Add the "Browse" and "Start Spell Check" buttons to the button group
        buttonGroup.getChildren().addAll(browseButton, startSpellCheckButton);

        // Add the button group and text field to the main VBox
        inputContainer.getChildren().addAll(titleLabel, filePathField, buttonGroup);
        AnchorPane.setTopAnchor(inputContainer, 40.0); // Setting position of inputContainer
        AnchorPane.setLeftAnchor(inputContainer, 20.0);
        AnchorPane.setRightAnchor(inputContainer, 20.0);
        // Add the main VBox to the root
        root.getChildren().addAll(inputContainer);
    }

    /**
     * Spell checker container, including spelling suggestions and non manual
     * corrections
     * Items: suggestListView (for suggested words), currentErrorField
     * Buttons: replace, ignore, delete, add
     */
    private void createSpellCheckerContainer() {
        spellCheckerContainer = new AnchorPane();
        spellCheckerContainer.getStyleClass().add("spellchecker-container");
        spellCheckerContainer.setVisible(false);

        VBox suggestedWordsContainer = new VBox();
        Label suggestedWordsLabel = new Label("Suggested words:");
        suggestedWordsLabel.setId("label-suggested-words");
        suggestListView = new ListView<>();
        suggestListView.setId("suggested-word-list");
        currentSuggestions = FXCollections.observableArrayList();
        suggestListView.setItems(currentSuggestions);
        // Add an event handler to capture the selected word
        suggestListView.setOnMouseClicked(event -> {
            String selectedWord = suggestListView.getSelectionModel().getSelectedItem();
            if (selectedWord != null) {
                selectedSuggWord = selectedWord;
                replaceButton.setDisable(false);
                replaceAllButton.setDisable(false);
            }
        });
        suggestedWordsContainer.getChildren().addAll(suggestedWordsLabel, suggestListView);

        VBox correctionContainer = new VBox(); // error word, label for buttons and buttons
        correctionContainer.getStyleClass().add("correction-container");

        // Displaying spelling error
        Label errorWordLabel = new Label("Spelling error: ");
        errorWordLabel.setId("label-spelling-error");
        errorTypeLabel = new Label();
        errorTypeLabel.setId("label-error-type");
        HBox errorLabelContainer = new HBox(errorWordLabel, errorTypeLabel);

        currentErrorField = new TextField(currSpellingError);
        currentErrorField.getStyleClass().add("spelling-error-field");
        currentErrorField.setEditable(false);
        ScrollPane errorFieldScrollPane = new ScrollPane(currentErrorField);
        errorFieldScrollPane.setFitToWidth(true);

        Label correctionLabel = new Label("Correction options:");
        correctionLabel.setId("label-correction-options");
        HBox correctionButtonsGroup = new HBox(); // (replace, ignore) buttons
        correctionButtonsGroup.getStyleClass().add("correction-button-group");

        // "Replace" and "Ignore" button groups
        VBox replaceButtonsGroup = new VBox();
        replaceButtonsGroup.getStyleClass().add("replace-button-group");
        replaceButton = new Button("Replace");
        replaceButton.setOnAction(e -> handleReplace());
        replaceAllButton = new Button("Replace All");
        replaceAllButton.setOnAction(e -> handleReplaceAll());
        replaceButtonsGroup.getChildren().addAll(replaceButton, replaceAllButton);

        VBox ignoreButtonGroup = new VBox();
        ignoreButtonGroup.getStyleClass().add("ignore-button-group");
        ignoreButton = new Button("Ignore");
        ignoreButton.setOnAction(e -> handleIgnore());
        ignoreAllButton = new Button("Ignore All");
        ignoreAllButton.setOnAction(e -> handleIgnoreAll());
        ignoreButtonGroup.getChildren().addAll(ignoreButton, ignoreAllButton);

        VBox addDeleteButtonGroup = new VBox();
        addDeleteButtonGroup.getStyleClass().add("add-delete-button-group");
        deleteTextButton = new Button("Delete");
        deleteTextButton.setOnAction(e -> handleDeleteError());
        addToDictButton = new Button("Add to Dictionary");
        addToDictButton.setOnAction(e -> handleAddToDict());
        addDeleteButtonGroup.getChildren().addAll(deleteTextButton, addToDictButton);

        correctionButtonsGroup.getChildren().addAll(replaceButtonsGroup, ignoreButtonGroup, addDeleteButtonGroup);
        correctionContainer.getChildren().addAll(errorLabelContainer, currentErrorField, correctionLabel,
                correctionButtonsGroup);
        spellCheckerContainer.getChildren().addAll(suggestedWordsContainer, correctionContainer);

        // Setting positions of the correction and suggested words to around the center
        spellCheckerContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            double midPoint = spellCheckerContainer.getWidth() / 2;
            AnchorPane.setRightAnchor(correctionContainer, midPoint - 70);
            AnchorPane.setLeftAnchor(suggestedWordsContainer, midPoint + 100);
        });
        // Setting position for spell checker container
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            AnchorPane.setTopAnchor(spellCheckerContainer, root.getHeight() / 4);
            AnchorPane.setLeftAnchor(spellCheckerContainer, 10.0);
            AnchorPane.setRightAnchor(spellCheckerContainer, 10.0);
        });

        root.getChildren().add(spellCheckerContainer);
    }

    /**
     * File contents and manual corrections container
     * items: fileTextField, manualEditField, saveManualChanges button
     */
    private void createfileContentsContainer() {
        Label titleLabel = new Label("File contents:");
        titleLabel.setId("label-file-contents");

        fileContentsContainer = new VBox(); // Outermost area, container for text area and buttons
        fileContentsContainer.getStyleClass().add("file-contents-container");
        fileContentsContainer.setVisible(false);

        fileTextField = new TextArea();
        fileTextField.getStyleClass().add("file-text-field");
        fileTextField.setMinWidth(300);
        fileTextField.setWrapText(true);
        fileTextField.setEditable(false);

        HBox manualCorrectionGroup = new HBox();
        manualCorrectionGroup.getStyleClass().add("manual-correction-group");
        Label manualCorrGroupLabel = new Label("Manually correct:");
        manualCorrGroupLabel.setId("label-manual-correction-group");

        manualEditField = new TextArea();
        manualEditField.getStyleClass().add("manual-edit-field");
        manualEditField.setWrapText(true);
        // consume enter key press
        manualEditField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
            }
        });

        saveManualChanges = new Button("Manually change");
        saveManualChanges.getStyleClass().add("button-save-manual-changes");
        saveManualChanges.setDisable(true); // Enable button when text in manual edit field
        manualEditField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveManualChanges.setDisable(newValue.trim().isEmpty());
        });
        saveManualChanges.setOnAction(e -> handleManualEdit(manualEditField.getText()));

        manualCorrectionGroup.getChildren().addAll(manualCorrGroupLabel, manualEditField, saveManualChanges);
        fileContentsContainer.getChildren().addAll(titleLabel, fileTextField, manualCorrectionGroup);

        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            AnchorPane.setTopAnchor(fileContentsContainer, (root.getHeight() / 2));
            AnchorPane.setLeftAnchor(fileContentsContainer, 20.0);
            AnchorPane.setRightAnchor(fileContentsContainer, 20.0);
        });

        root.getChildren().add(fileContentsContainer);
    }

    /**
     * Footer area, incl. progress, stats, and light-mode radio buttons
     * items: progressLabel, statsLabel,lightModeButton and darkModeButton radio
     * groups
     */
    private void createFooterContainer() {
        AnchorPane footerContainer = new AnchorPane();
        footerContainer.getStyleClass().add("footer-container");

        progressLabel = new Label("Progress:");
        progressLabel.setId("label-progress");

        Label statsLabel = new Label("Statistics");
        statsLabel.setId("label-statistics");

        // Create a ToggleGroup for the radio buttons
        ToggleGroup styleToggleGroup = new ToggleGroup();

        RadioButton lightModeButton = new RadioButton("Light Mode");
        lightModeButton.setToggleGroup(styleToggleGroup);

        RadioButton darkModeButton = new RadioButton("Dark Mode");
        darkModeButton.setToggleGroup(styleToggleGroup);
        darkModeButton.setSelected(true); // Dark mode is default for now
        root.getStyleClass().add("dark-mode");

        // Listener to toggle between styles
        styleToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == darkModeButton) {
                // Apply the dark mode stylesheet
                root.getStyleClass().remove("light-mode"); // Remove the light-mode class
                root.getStyleClass().add("dark-mode");
            } else if (newValue == lightModeButton) {
                // Apply the light mode stylesheet
                root.getStyleClass().remove("dark-mode"); // Remove the dark-mode class
                root.getStyleClass().add("light-mode");
            }
        });

        HBox toggleGroupBox = new HBox();
        toggleGroupBox.getStyleClass().add("toggle-lighting-box");

        toggleGroupBox.getChildren().addAll(lightModeButton, darkModeButton);

        // Set anchors for the Progress and Stats label and toggleGroupBox
        AnchorPane.setLeftAnchor(progressLabel, 5.0);
        AnchorPane.setTopAnchor(progressLabel, 5.0);
        AnchorPane.setRightAnchor(toggleGroupBox, 5.0);
        AnchorPane.setTopAnchor(toggleGroupBox, 5.0);
        // Get root size and place stats label in middle
        root.widthProperty().addListener((observable, oldValue, newValue) -> {
            AnchorPane.setLeftAnchor(statsLabel, (newValue.doubleValue() - statsLabel.getWidth()) / 2);
        });
        AnchorPane.setTopAnchor(statsLabel, 5.0);
        Tooltip statsTooltip = new Tooltip();
        statsLabel.setOnMouseEntered(e -> statsTooltip.setText(getStatisticsAsString()));
        statsLabel.setTooltip(statsTooltip);

        footerContainer.getChildren().addAll(progressLabel, statsLabel, toggleGroupBox);
        AnchorPane.setBottomAnchor(footerContainer, 0.0);
        AnchorPane.setLeftAnchor(footerContainer, 0.0);
        AnchorPane.setRightAnchor(footerContainer, 0.0);

        root.getChildren().add(footerContainer);
    }

    /**
     * Opens a FileChooser and checks selected file is of valid type
     * If valid, sets it as selectedFile and enable start spell check button
     * 
     * @param primaryStage main stage
     */
    private void handleBrowseFile(Stage primaryStage) {
        // Instead of doing that all the time, just replace the path of a file of your
        // choosing:
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        selectedFileToCheck = fileChooser.showOpenDialog(primaryStage);

        if (selectedFileToCheck != null) {
            String filePath = selectedFileToCheck.getAbsolutePath();

            if (filePath.toLowerCase().endsWith(".txt")){
                filePathField.setText(filePath);
                // Allow spell check to start after txt file is browsed
                startSpellCheckButton.setDisable(false);
            } else {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Invalid File Type");
                alert.setHeaderText("Please enter a valid .txt format. HTML or XML formats are not supported.");
                alert.showAndWait();
                filePathField.clear();
            }
        }

        // // Instead of doing that all the time, just replace the path of a file of
        // your choosing:
        // selectedFileToCheck = new File("C:\\Users\\ryati\\Desktop\\ez.txt"); //
        // Create a File object with the file path
        // startSpellCheckButton.setDisable(false);

    }

    /**
     * Helper event handler for closing from menu bar or window's close (x) button
     * Prompts user based on spell checking progress
     * premature-exits = spellchecking not complete
     * program-complete = spell checking not started or is compelete
     */
    private void handleExitApp() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit?");

        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return; // User didn't confirm, so exit early
            }

            if (!browseButton.isDisabled() && filePathField.getText().equals("")) { // just close if spell checking
                                                                                    // hasn't started
                primaryStage.close();

            }
            // Spellchecking has started
            else {
                if (!spellCheckingComplete) {
                    saveToFilePrompt("premature-exit");
                } else {
                    saveToFilePrompt("program-complete");
                }
            }
        });
    }

    /**
     * Starts program when start is pressed after a valid file is selected
     * Inits document and spellChecker
     * Enables/disables buttons, and updatesDisplays
     */
    private void startSpellCheck() {
        try {
            Config config = new Config();
            dictionary = new Dictionary(config.STD_DICTIONARY_PATH,config.USER_DICTIONARY_PATH);
            spellChecker = new SpellChecker(dictionary);
            this.document = new Document(selectedFileToCheck, spellChecker, config);
            document.startSpellCheck();
        } catch (Exception e) {
            // TODO: handle exception
        }

        // Disable fieldpath form, browse button, and start button
        // Show file contents and spelling options
        filePathField.setDisable(true);
        browseButton.setDisable(true);
        openMenuItem.setDisable(true);
        startSpellCheckButton.setDisable(true);
        fileContentsContainer.setVisible(true);
        spellCheckerContainer.setVisible(true);

        // Show initial context, error, and suggestions
        updateDisplays();
    }

    /**
     * Gets new contents, error, suggested words, progress and stats
     * Resets buttons
     */
    private void updateDisplays() {
        fileContents = document.currentContext;
        fileTextField.setText(fileContents);
        if (document.currentError != null) {
            currSpellingError = document.currentError.toString().substring("java.lang.Error: ".length());
            currentErrorField.setText(currSpellingError);
        }
        if (document.currentError == null) { // If no errors remain, spellchecking is complete
            spellCheckingComplete = true;
            progressLabel.setText("Progress: " + document.progress + "%");
            handleCheckingComplete();
        }

        // Get current suggestions from document then add to here
        // if (document.currentSuggestions != null) {
        // currentSuggestions =
        // FXCollections.observableArrayList(document.currentSuggestions);
        // suggestListView.setItems(currentSuggestions);
        // }

        // Update statistics, add errors
        if (document.currentErrorType != null) errorTypeLabel.setText(document.currentErrorType.toString());
        progressLabel.setText("Progress: " + document.progress + "%");
        spellingStatistics.put("word count", document.wordCount);
        spellingStatistics.put("line count", document.lineCount);
        spellingStatistics.put("char count", document.charCount);
        // Get error counts
        spellingStatistics.put("spelling errors", document.getErrorCounts("spelling-errors"));
        spellingStatistics.put("double word errors", document.getErrorCounts("double-words"));
        spellingStatistics.put("capitalization errors", document.getErrorCounts("cap-errors"));
        spellingStatistics.put("miscapitalization errors", document.getErrorCounts("miscap-errors"));
        

        // Refresh and enable buttons
        selectedSuggWord = null; // clear prev selected sugg word
        replaceButton.setDisable(true);
        replaceAllButton.setDisable(true);
        manualEditField.clear();

        // Get suggestions
        currentSuggestions.clear();
        if (document.currentSuggestions != null)
            currentSuggestions.addAll(document.currentSuggestions);
    }

    /**
     * Helper to get and display stats from hashmap spellingStatistics
     */
    private String getStatisticsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : spellingStatistics.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Disable buttons, clears contents and errors, and shows completion prompt when
     * spellchecking complete
     */
    private void handleCheckingComplete() {
        // Clear and disable display items
        currentErrorField.setText("");
        spellCheckerContainer.setDisable(true);
        fileContentsContainer.setDisable(true);
        // editTextFieldButton.setDisable(true);
        // Disable highlighted text (prev error)...to be implemented
        saveToFilePrompt("program-complete");
        System.out.println("Program has finished.");
    }

    /**
     * @param exitEvent "program-complete" if finish or "premature-exit" if in
     *                  progress
     *                  Can branch to exitWithoutSavePrompt() to show alert
     *                  Closes documents buffered writer
     */
    private void saveToFilePrompt(String exitEvent) {
        Alert alert = new Alert(AlertType.CONFIRMATION);

        if (exitEvent.equals("program-complete")) {
            alert.setTitle("Spellchecking complete.");
            alert.setHeaderText("No errors remaining. Save to file and exit?");
        } else if (exitEvent.equals("premature-exit")) { // ask document to write to buffer of what's been read/checked
                                                         // so far
            alert.setTitle("Spellcheck in progress!");
            alert.setHeaderText("Spellchecking is not complete. Save to file and exit?");
        }

        // Set the buttons to Yes and No
        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonData.OK_DONE);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                File file = fileChooser.showSaveDialog(null);

                if (file == null) { // If close button pressed on filechooser
                    return;
                }

                if (exitEvent.equals("premature-exit")) {
                    document.handleEvent("premature-exit");
                }

                String savePath = file.getAbsolutePath();

                if (document.handleEvent("exit:" + savePath)) {
                    primaryStage.close();

                } else {
                    // ???
                }
            } else if (response == buttonTypeNo) {
                exitWithoutSavePrompt();
            }
        });

        dictionary.handleBwClose(); // Will always close regardless of branch
    }

    /**
     * One last alert prompt if user exits w/o saving
     * Destroys temp file with spellchecked words, if user exits w/o saving
     */
    private void exitWithoutSavePrompt() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Exit without saving?");
        alert.setHeaderText("Are you sure you want to exit without saving?");

        // Set the buttons to Yes and No
        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonData.OK_DONE);
        ButtonType buttonTypeNo = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
            
                if (document.handleEvent("destroy-file")) {
                    primaryStage.close();
                }
                // if it fails destorying file??
            } else {
                return;
            }
        });
    }

    /**
     * Event handler for replace button.
     * Provides document w/ selected word (from suggestions) to replace error with.
     * Updates displays after.
     */
    private void handleReplace() {
        if (document.handleEvent("replace:" + selectedSuggWord)) {
            updateDisplays();
        }
    }

    /**
     * Event handler for replaceAll button.
     * Provides document w/ selected word (from suggestions) to replace all
     * occurences of the same word. Updates displays after.
     */
    private void handleReplaceAll() {
        if (document.handleEvent("replace-all:" + selectedSuggWord)) {
            updateDisplays();
        }
    }

    /**
     * Event handler ignore (once) button.
     * Calls documents ignore event handler to ignore current error. Updates
     * displays after.
     */
    private void handleIgnore() {
        if (document.handleEvent("ignore")) {
            updateDisplays();
        }
        // ??? what if document fails?
    }

    /**
     * Event handler for ignore all button.
     * Calls documents ignore all event handler to ignore all occurences of current
     * error. Updates displays after.
     */
    private void handleIgnoreAll() {
        if (document.handleEvent("ignore-all")) {
            updateDisplays();
        }
    }

    /**
     * Event handler delete button.
     * Calls document delete event handler to delte current error. Updates displays
     * after.
     */
    private void handleDeleteError() {
        if (document.handleEvent("delete")) {
            updateDisplays();
        }
    }

    /**
     * Event handler for add to dictionary button.
     * Provides document w/ current error to add to user dictionary. Updates
     * displays after.
     */
    private void handleAddToDict() {
        System.out.println(currSpellingError);
        // String strippedError = currSpellingError.substring("java.lang.Error: ".length());
        if (document.handleEvent("add-to-dict")) {
            updateDisplays();
        }
    }

    /**
     * Event handler for save changes to manual edit button.
     * Provides document w/ manually corrected word to replace error with. Updates
     * displays after.
     */
    private void handleManualEdit(String manualCorrection) {
        if (document.handleEvent("manual-edit:" + manualCorrection)) {
            updateDisplays();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
