package org.codex.apktoolgui.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codex.apktoolgui.services.ApkToolService;
import org.codex.apktoolgui.services.ApkEditorService;
import org.codex.apktoolgui.services.executor.CommandExecutor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import org.codex.apktoolgui.views.tabs.*;

import static org.codex.apktoolgui.services.ApkToolService.getApkToolPath;

public class MainView {
    ApkToolService apkToolService = new ApkToolService();
    ApkEditorService apkEditorService = new ApkEditorService();

    // UI Components
    public static TextArea outputArea;
    public static ProgressBar progressBar;
    public static Label statusLabel;

    // File choosers
    public final FileChooser fileChooser = new FileChooser();
    public final DirectoryChooser directoryChooser = new DirectoryChooser();

    // Theme
    public static boolean darkMode = true;

    private Stage primaryStage;
    String apktoolPath = getApkToolPath();

    public MainView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void stop() {
        CommandExecutor.shutdown();
    }


    public void initialize(Stage primaryStage) {
        primaryStage.setTitle("Apktool GUI v2.12.0");

        // Initialize
        initializeFileChoosers();

        // Create main layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Create menu bar
        root.setTop(createMenuBar(primaryStage));

        // Create tab pane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("dark-tab-pane");
        tabPane.getTabs().addAll(
                new ApkToolTab(this, apkToolService).createAPKToolTab(),
                new ApkEditorTab(this, apkEditorService).createAPKEditorTab(),
                new ApkInfoTab(this, apkEditorService).createApkInfoTab()
        );
        root.setCenter(tabPane);

        // Create bottom panel
        root.setBottom(createBottomPanel());

        Scene scene = new Scene(root, 800, 645);

        // Apply dark theme CSS
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/org/codex/apktoolgui/dark-theme.css"),
                        "dark-theme.css not found on classpath"
                ).toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.show();

        // Check for apktool
        apkToolService.checkApktoolAvailability();

        // Load settings
        loadSettings();
    }

    public void initializeFileChoosers() {
        fileChooser.setTitle("Select APK File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("APK Files", "*.apk"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        directoryChooser.setTitle("Select Directory");
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("dark-menu-bar");

        // File Menu
        Menu fileMenu = new Menu("File");
        fileMenu.getStyleClass().add("dark-menu");

        MenuItem clearOutputItem = new MenuItem("Clear Output");
        clearOutputItem.setOnAction(e -> outputArea.clear());

        MenuItem reloadItem = new MenuItem("Reload Settings");
        reloadItem.setOnAction(e -> loadSettings());

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        Menu themeMenu = new Menu("Theme");
        RadioMenuItem darkThemeItem = new RadioMenuItem("Dark Theme");
        darkThemeItem.setSelected(true);
        RadioMenuItem lightThemeItem = new RadioMenuItem("Light Theme");

        ToggleGroup themeGroup = new ToggleGroup();
        darkThemeItem.setToggleGroup(themeGroup);
        lightThemeItem.setToggleGroup(themeGroup);

        darkThemeItem.setOnAction(e -> switchTheme(true));
        lightThemeItem.setOnAction(e -> switchTheme(false));

        themeMenu.getItems().addAll(darkThemeItem, lightThemeItem);

        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(clearOutputItem, reloadItem, separator1,
                themeMenu, separator2, exitItem);

        // Tools Menu
        Menu toolsMenu = new Menu("Tools");
        toolsMenu.getStyleClass().add("dark-menu");

        MenuItem checkApktoolItem = new MenuItem("Check Apktool");
        checkApktoolItem.setOnAction(e -> apkToolService.checkApktoolAvailability());

        MenuItem openOutputDirItem = new MenuItem("Open Output Directory");
        openOutputDirItem.setOnAction(e -> openOutputDirectory());

        toolsMenu.getItems().addAll(checkApktoolItem, openOutputDirItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        helpMenu.getStyleClass().add("dark-menu");

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());

        MenuItem documentationItem = new MenuItem("Documentation");
        documentationItem.setOnAction(e -> openDocumentation());

        helpMenu.getItems().addAll(aboutItem, documentationItem);

        menuBar.getMenus().addAll(fileMenu, toolsMenu, helpMenu);
        return menuBar;
    }


    private VBox createBottomPanel() {
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.getStyleClass().add("bottom-panel");

        // Status Bar
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("dark-progress-bar");

        // Create copy/clear buttons
        Button clearButton = createStyledButton("Clear", "small");
        clearButton.setOnAction(e -> outputArea.clear());

        Button copyButton = createStyledButton("Copy", "small");
        copyButton.setOnAction(e -> {
            outputArea.selectAll();
            outputArea.copy();
        });

        // Create container for copy/clear buttons
        HBox buttonContainer = new HBox(4);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().addAll(clearButton, copyButton);

        // Create spacers
        HBox leftSpacer = new HBox();
        HBox centerSpacer = new HBox();
        HBox rightSpacer = new HBox();

        // Make center spacer expand to push progress bar to right
        HBox.setHgrow(centerSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.SOMETIMES);

        // Add components to status bar
        statusBar.getChildren().addAll(
                statusLabel,       // Left: Status text
                leftSpacer,        // Small space after status
                centerSpacer,      // Expands to push next items right
                progressBar,       // Middle-right: Progress bar
                rightSpacer,       // Space between progress and buttons
                buttonContainer    // Right: Copy/Clear buttons
        );

        // Output Area
        Label outputLabel = new Label("Output Console:");
        outputLabel.getStyleClass().add("section-label");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(100);
        outputArea.getStyleClass().add("dark-text-area");

        bottomBox.getChildren().addAll(
                new Separator(),
                outputLabel,
                outputArea,
                new Separator(),
                statusBar  // Status bar now contains everything
        );

        return bottomBox;
    }

    // Helper Methods for UI Components
    public Button createStyledButton(String text, String style) {
        Button button = new Button(text);
        button.getStyleClass().addAll("dark-button", style);
        return button;
    }

    public CheckBox createCheckBox(String text, String tooltip) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.getStyleClass().add("dark-checkbox");
        if (tooltip != null) {
            checkBox.setTooltip(new Tooltip(tooltip));
        }
        return checkBox;
    }

    public Label createIcon(String emoji) {
        Label icon = new Label(emoji);
        icon.getStyleClass().add("icon");
        return icon;
    }

    // Theme Switching
    public void switchTheme(boolean dark) {
        darkMode = dark;
        // In a real implementation, you would reload the CSS
        appendOutput("Theme switched to " + (dark ? "Dark" : "Light") + " mode");
    }



    public void loadSettings() {
        try {
            Path configPath = Path.of(System.getProperty("user.home"), ".apktool-gui.properties");
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(configPath)) {
                    props.load(in);
                }

                apktoolPath = props.getProperty("apktool.path", "apktool.jar");
                darkMode = Boolean.parseBoolean(props.getProperty("dark.mode", "true"));

                appendOutput("✅ Settings loaded from: " + configPath);
            }
        } catch (Exception e) {
            // Use defaults
        }
    }

    public static void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text + "\n");
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Style the alert for dark mode
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStyleClass().add("dark-dialog");

            alert.showAndWait();
        });
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Apktool GUI v1.0");
        alert.setContentText("A modern graphical interface for Apktool\n\n" +
                "Based on Apktool 2.12.0\n" +
                "Created with JavaFX\n" +
                "Dark Theme v1.0\n\n" +
                "GitHub: https://github.com/yourusername/apktool-gui");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dark-dialog");

        alert.showAndWait();
    }

    private void openDocumentation() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://ibotpeaches.github.io/Apktool/"));
        } catch (Exception e) {
            showError("Could not open documentation: " + e.getMessage());
        }
    }

    private void openOutputDirectory() {
        try {
            java.awt.Desktop.getDesktop().open(new File(System.getProperty("user.dir")));
        } catch (Exception e) {
            showError("Could not open output directory: " + e.getMessage());
        }
    }
}