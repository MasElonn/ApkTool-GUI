package org.codex.apktoolgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class ApktoolGUI extends Application {

    // Executor for running Apktool commands
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // UI Components
    public static TextArea outputArea;
    public static ProgressBar progressBar;
    public static Label statusLabel;

    // File choosers
    private final FileChooser fileChooser = new FileChooser();
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    // Apktool path
    private String apktoolPath = "apktool.jar";

    // Theme
    public static boolean darkMode = true;

    @Override
    public void start(Stage primaryStage) {
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
                createAPKToolTab(),
                createOtherToolTab(),
                createFrameworkTab(),
                createOtherTab(),
                createSettingsTab()
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
        Apktool.checkApktoolAvailability();

        // Load settings
        loadSettings();
    }

    private void initializeFileChoosers() {
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
        checkApktoolItem.setOnAction(e -> Apktool.checkApktoolAvailability());

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

    private Tab createAPKToolTab() {
        Tab apkToolTab = new Tab("APKTOOL");
        apkToolTab.setClosable(false);
        apkToolTab.setGraphic(createIcon("🔨"));

        // Create main container with two sections
        VBox mainBox = new VBox(30);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("dark-container");

        // ========== DECOMPILE SECTION ==========
        VBox decompileSection = createDecompileSection();

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // ========== RECOMPILE SECTION ==========
        VBox recompileSection = createRecompileSection();

        mainBox.getChildren().addAll(decompileSection, separator, recompileSection);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(new Insets(0));

        apkToolTab.setContent(scrollPane);
        return apkToolTab;
    }

    // Decompile configuration class
    private static class DecompileConfig {
        private boolean noRes = false;
        private boolean noSrc = false;
        private boolean noAssets = false;
        private boolean onlyManifest = false;
        private boolean force = false;
        private boolean noDebug = false;
        private boolean matchOriginal = false;
        private boolean keepBroken = false;
        private boolean onlyMainClasses = false;
        private String apiLevel = "";
        private String jobs = "1";
        private String frameworkPath = "";

        // Getters
        public boolean isNoRes() { return noRes; }
        public boolean isNoSrc() { return noSrc; }
        public boolean isNoAssets() { return noAssets; }
        public boolean isOnlyManifest() { return onlyManifest; }
        public boolean isForce() { return force; }
        public boolean isNoDebug() { return noDebug; }
        public boolean isMatchOriginal() { return matchOriginal; }
        public boolean isKeepBroken() { return keepBroken; }
        public boolean isOnlyMainClasses() { return onlyMainClasses; }
        public String getApiLevel() { return apiLevel; }
        public String getJobs() { return jobs; }
        public String getFrameworkPath() { return frameworkPath; }

        // Setters
        public void setNoRes(boolean value) { noRes = value; }
        public void setNoSrc(boolean value) { noSrc = value; }
        public void setNoAssets(boolean value) { noAssets = value; }
        public void setOnlyManifest(boolean value) { onlyManifest = value; }
        public void setForce(boolean value) { force = value; }
        public void setNoDebug(boolean value) { noDebug = value; }
        public void setMatchOriginal(boolean value) { matchOriginal = value; }
        public void setKeepBroken(boolean value) { keepBroken = value; }
        public void setOnlyMainClasses(boolean value) { onlyMainClasses = value; }
        public void setApiLevel(String value) { apiLevel = value; }
        public void setJobs(String value) { jobs = value; }
        public void setFrameworkPath(String value) { frameworkPath = value; }
    }

    // Recompile configuration class
    private static class RecompileConfig {
        private boolean debug = false;
        private boolean copyOriginal = false;
        private boolean force = false;
        private boolean noApk = false;
        private boolean noCrunch = false;
        private boolean useAapt1 = false;
        private boolean netSec = false;
        private String aaptPath = "";
        private String frameworkPath = "";

        // Getters
        public boolean isDebug() { return debug; }
        public boolean isCopyOriginal() { return copyOriginal; }
        public boolean isForce() { return force; }
        public boolean isNoApk() { return noApk; }
        public boolean isNoCrunch() { return noCrunch; }
        public boolean isUseAapt1() { return useAapt1; }
        public boolean isNetSec() { return netSec; }
        public String getAaptPath() { return aaptPath; }
        public String getFrameworkPath() { return frameworkPath; }

        // Setters
        public void setDebug(boolean value) { debug = value; }
        public void setCopyOriginal(boolean value) { copyOriginal = value; }
        public void setForce(boolean value) { force = value; }
        public void setNoApk(boolean value) { noApk = value; }
        public void setNoCrunch(boolean value) { noCrunch = value; }
        public void setUseAapt1(boolean value) { useAapt1 = value; }
        public void setNetSec(boolean value) { netSec = value; }
        public void setAaptPath(String value) { aaptPath = value; }
        public void setFrameworkPath(String value) { frameworkPath = value; }
    }

    // Instance variables
    private final DecompileConfig decompileConfig = new DecompileConfig();
    private final RecompileConfig recompileConfig = new RecompileConfig();

    private VBox createDecompileSection() {
        VBox decompileSection = new VBox(20);
        decompileSection.getStyleClass().add("section-container");

        Label decodeTitle = new Label("Decompile APK");
        decodeTitle.getStyleClass().add("section-title");
        decodeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);
        fileGrid.getStyleClass().add("dark-grid");

        Label apkLabel = new Label("APK File:");
        apkLabel.getStyleClass().add("dark-label");

        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        apkPathField.getStyleClass().add("dark-text-field");
        apkPathField.setPrefWidth(400);

        Button browseApkButton = createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
            }
        });

        Label outputLabel = new Label("Output Directory:");
        outputLabel.getStyleClass().add("dark-label");

        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: apk.out");
        outputPathField.getStyleClass().add("dark-text-field");

        Button browseOutputButton = createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(null);
            if (dir != null) {
                outputPathField.setText(dir.getAbsolutePath());
            }
        });

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);

        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Current Configuration Display
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Decompile Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label decompileConfigSummary = new Label("No options configured. Click 'Configure Options' to set.");
        decompileConfigSummary.setId("decompile-config-summary");
        decompileConfigSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        decompileConfigSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, decompileConfigSummary);

        // Button Box for Options and Decompile
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button decompileOptionsButton = createStyledButton("⚙ Configure Options", "secondary");
        decompileOptionsButton.setPrefWidth(180);
        decompileOptionsButton.setOnAction(e -> showDecompileOptionsDialog(decompileConfigSummary));

        Button decodeButton = createStyledButton("▶ Decompile APK", "large-primary");
        decodeButton.setPrefWidth(180);
        decodeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            String outputPath = outputPathField.getText();

            if (apkPath == null || apkPath.trim().isEmpty()) {
                showAlert("Error", "Please select an APK file first.");
                return;
            }

            executeDecompile(apkPath, outputPath.isEmpty() ? "apk.out" : outputPath);
        });

        buttonBox.getChildren().addAll(decompileOptionsButton, decodeButton);

        decompileSection.getChildren().addAll(decodeTitle, fileGrid, configBox, buttonBox);
        return decompileSection;
    }

    private VBox createRecompileSection() {
        VBox recompileSection = new VBox(20);
        recompileSection.getStyleClass().add("section-container");

        Label buildTitle = new Label("Recompile APK");
        buildTitle.getStyleClass().add("section-title");
        buildTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);
        fileGrid.getStyleClass().add("dark-grid");

        Label inputLabel = new Label("Project Directory:");
        inputLabel.getStyleClass().add("dark-label");

        TextField inputPathField = new TextField();
        inputPathField.setPromptText("Select decoded APK directory...");
        inputPathField.setPrefWidth(400);
        inputPathField.getStyleClass().add("dark-text-field");

        Button browseInputButton = createStyledButton("Browse", "primary");
        browseInputButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(null);
            if (dir != null) {
                inputPathField.setText(dir.getAbsolutePath());
            }
        });

        Label outputLabel = new Label("Output APK:");
        outputLabel.getStyleClass().add("dark-label");

        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: dist/name.apk");
        outputPathField.getStyleClass().add("dark-text-field");

        Button browseOutputButton = createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> {
            fileChooser.setTitle("Save APK");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                outputPathField.setText(file.getAbsolutePath());
            }
            fileChooser.setTitle("Select APK File");
        });

        fileGrid.add(inputLabel, 0, 0);
        fileGrid.add(inputPathField, 1, 0);
        fileGrid.add(browseInputButton, 2, 0);

        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Current Configuration Display
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Recompile Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label recompileConfigSummary = new Label("No options configured. Click 'Configure Options' to set.");
        recompileConfigSummary.setId("recompile-config-summary");
        recompileConfigSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        recompileConfigSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, recompileConfigSummary);

        // Button Box for Options and Build
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button recompileOptionsButton = createStyledButton("⚙ Configure Options", "secondary");
        recompileOptionsButton.setPrefWidth(180);
        recompileOptionsButton.setOnAction(e -> showRecompileOptionsDialog(recompileConfigSummary));

        Button buildButton = createStyledButton("▶ Build APK", "large-primary");
        buildButton.setPrefWidth(180);
        buildButton.setOnAction(e -> {
            String inputPath = inputPathField.getText();
            String outputPath = outputPathField.getText();

            if (inputPath == null || inputPath.trim().isEmpty()) {
                showAlert("Error", "Please select a project directory first.");
                return;
            }

            executeRecompile(inputPath, outputPath.isEmpty() ?
                    new File(inputPath).getParent() + File.separator + "dist" + File.separator + "output.apk" :
                    outputPath);
        });

        buttonBox.getChildren().addAll(recompileOptionsButton, buildButton);

        recompileSection.getChildren().addAll(buildTitle, fileGrid, configBox, buttonBox);
        return recompileSection;
    }

    private void showDecompileOptionsDialog(Label configSummary) {
        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Decompile Configuration");
        dialog.setHeaderText("Configure decompilation options");

        // Set the button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        // Create checkboxes with current configuration
        CheckBox noResCheck = new CheckBox("No Resources (-r)");
        noResCheck.setSelected(decompileConfig.isNoRes());

        CheckBox noSrcCheck = new CheckBox("No Sources (-s)");
        noSrcCheck.setSelected(decompileConfig.isNoSrc());

        CheckBox noAssetsCheck = new CheckBox("No Assets");
        noAssetsCheck.setSelected(decompileConfig.isNoAssets());

        CheckBox onlyManifestCheck = new CheckBox("Only Manifest");
        onlyManifestCheck.setSelected(decompileConfig.isOnlyManifest());

        CheckBox forceCheck = new CheckBox("Force Decompile (-f)");
        forceCheck.setSelected(decompileConfig.isForce());

        CheckBox noDebugCheck = new CheckBox("No Debug Info (-b)");
        noDebugCheck.setSelected(decompileConfig.isNoDebug());

        CheckBox matchOriginalCheck = new CheckBox("Match Original (-m)");
        matchOriginalCheck.setSelected(decompileConfig.isMatchOriginal());

        CheckBox keepBrokenCheck = new CheckBox("Keep Broken Resources (-k)");
        keepBrokenCheck.setSelected(decompileConfig.isKeepBroken());

        CheckBox onlyMainClassesCheck = new CheckBox("Only Main Classes");
        onlyMainClassesCheck.setSelected(decompileConfig.isOnlyMainClasses());

        // Advanced options
        Label advancedLabel = new Label("Advanced Options:");
        advancedLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        HBox apiBox = new HBox(10);
        apiBox.setAlignment(Pos.CENTER_LEFT);
        Label apiLabel = new Label("API Level:");
        TextField apiField = new TextField(decompileConfig.getApiLevel());
        apiField.setPromptText("e.g., 30");
        apiField.setPrefWidth(80);
        apiBox.getChildren().addAll(apiLabel, apiField);

        HBox jobsBox = new HBox(10);
        jobsBox.setAlignment(Pos.CENTER_LEFT);
        Label jobsLabel = new Label("Threads (Jobs):");
        TextField jobsField = new TextField(decompileConfig.getJobs());
        jobsField.setPrefWidth(80);
        jobsBox.getChildren().addAll(jobsLabel, jobsField);

        HBox frameworkBox = new HBox(10);
        frameworkBox.setAlignment(Pos.CENTER_LEFT);
        Label frameworkLabel = new Label("Framework Path:");
        TextField frameworkField = new TextField(decompileConfig.getFrameworkPath());
        frameworkField.setPromptText("(Optional)");
        frameworkField.setPrefWidth(200);
        Button browseFrameworkButton = new Button("Browse");
        browseFrameworkButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(null);
            if (dir != null) {
                frameworkField.setText(dir.getAbsolutePath());
            }
        });
        frameworkBox.getChildren().addAll(frameworkLabel, frameworkField, browseFrameworkButton);

        // Set tooltips
        noResCheck.setTooltip(new Tooltip("Do not decode resources (resources.arsc)"));
        noSrcCheck.setTooltip(new Tooltip("Do not decode sources (classes.dex)"));
        noAssetsCheck.setTooltip(new Tooltip("Do not decode assets folder"));
        onlyManifestCheck.setTooltip(new Tooltip("Decode only the AndroidManifest.xml"));
        forceCheck.setTooltip(new Tooltip("Force delete destination directory"));
        noDebugCheck.setTooltip(new Tooltip("Remove debug info from .smali files"));
        matchOriginalCheck.setTooltip(new Tooltip("Keep files as close to original as possible"));
        keepBrokenCheck.setTooltip(new Tooltip("Keep broken resources instead of throwing exceptions"));
        onlyMainClassesCheck.setTooltip(new Tooltip("Only decompile main classes (faster but incomplete)"));
        apiField.setTooltip(new Tooltip("Target API level for decompilation"));
        jobsField.setTooltip(new Tooltip("Number of threads to use (1-8 recommended)"));
        frameworkField.setTooltip(new Tooltip("Path to framework files (.apk)"));

        // Create a grid layout with 3 columns
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));

        // Column 1
        grid.add(noResCheck, 0, 0);
        grid.add(noSrcCheck, 0, 1);
        grid.add(noAssetsCheck, 0, 2);
        grid.add(onlyManifestCheck, 0, 3);

        // Column 2
        grid.add(forceCheck, 1, 0);
        grid.add(noDebugCheck, 1, 1);
        grid.add(matchOriginalCheck, 1, 2);
        grid.add(keepBrokenCheck, 1, 3);
        grid.add(onlyMainClassesCheck, 1, 4);

        // Column 3 - Advanced options
        grid.add(advancedLabel, 2, 0);
        grid.add(apiBox, 2, 1);
        grid.add(jobsBox, 2, 2);
        grid.add(frameworkBox, 2, 3, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(700, 350);

        // Handle dialog buttons
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                // Save configuration
                decompileConfig.setNoRes(noResCheck.isSelected());
                decompileConfig.setNoSrc(noSrcCheck.isSelected());
                decompileConfig.setNoAssets(noAssetsCheck.isSelected());
                decompileConfig.setOnlyManifest(onlyManifestCheck.isSelected());
                decompileConfig.setForce(forceCheck.isSelected());
                decompileConfig.setNoDebug(noDebugCheck.isSelected());
                decompileConfig.setMatchOriginal(matchOriginalCheck.isSelected());
                decompileConfig.setKeepBroken(keepBrokenCheck.isSelected());
                decompileConfig.setOnlyMainClasses(onlyMainClassesCheck.isSelected());
                decompileConfig.setApiLevel(apiField.getText());
                decompileConfig.setJobs(jobsField.getText());
                decompileConfig.setFrameworkPath(frameworkField.getText());

                // Update config summary
                updateDecompileConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) {
                    // Keep dialog open
                    return null;
                }
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void showRecompileOptionsDialog(Label configSummary) {
        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recompile Configuration");
        dialog.setHeaderText("Configure recompilation options");

        // Set the button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        // Create checkboxes with current configuration
        CheckBox debugCheck = new CheckBox("Set Debuggable (-d)");
        debugCheck.setSelected(recompileConfig.isDebug());

        CheckBox copyOriginalCheck = new CheckBox("Copy Original (-c)");
        copyOriginalCheck.setSelected(recompileConfig.isCopyOriginal());

        CheckBox forceCheck = new CheckBox("Force Build (-f)");
        forceCheck.setSelected(recompileConfig.isForce());

        CheckBox noApkCheck = new CheckBox("No APK (-na)");
        noApkCheck.setSelected(recompileConfig.isNoApk());

        CheckBox noCrunchCheck = new CheckBox("No Crunch (-nc)");
        noCrunchCheck.setSelected(recompileConfig.isNoCrunch());

        CheckBox useAapt1Check = new CheckBox("Use AAPT1");
        useAapt1Check.setSelected(recompileConfig.isUseAapt1());

        CheckBox netSecCheck = new CheckBox("Add Net Security Config (-n)");
        netSecCheck.setSelected(recompileConfig.isNetSec());

        // Advanced options
        Label advancedLabel = new Label("Advanced Options:");
        advancedLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        HBox aaptBox = new HBox(10);
        aaptBox.setAlignment(Pos.CENTER_LEFT);
        Label aaptLabel = new Label("AAPT Path:");
        TextField aaptField = new TextField(recompileConfig.getAaptPath());
        aaptField.setPromptText("(Optional) Use default");
        aaptField.setPrefWidth(200);
        Button browseAaptButton = new Button("Browse");
        browseAaptButton.setOnAction(e -> {
            FileChooser aaptChooser = new FileChooser();
            aaptChooser.setTitle("Select AAPT Binary");
            File file = aaptChooser.showOpenDialog(null);
            if (file != null) {
                aaptField.setText(file.getAbsolutePath());
            }
        });
        aaptBox.getChildren().addAll(aaptLabel, aaptField, browseAaptButton);

        HBox frameworkBox = new HBox(10);
        frameworkBox.setAlignment(Pos.CENTER_LEFT);
        Label frameworkLabel = new Label("Framework Path:");
        TextField frameworkField = new TextField(recompileConfig.getFrameworkPath());
        frameworkField.setPromptText("(Optional)");
        frameworkField.setPrefWidth(200);
        Button browseFrameworkButton = new Button("Browse");
        browseFrameworkButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(null);
            if (dir != null) {
                frameworkField.setText(dir.getAbsolutePath());
            }
        });
        frameworkBox.getChildren().addAll(frameworkLabel, frameworkField, browseFrameworkButton);

        // Set tooltips
        debugCheck.setTooltip(new Tooltip("Set android:debuggable=\"true\" in manifest"));
        copyOriginalCheck.setTooltip(new Tooltip("Copy original AndroidManifest.xml and META-INF"));
        forceCheck.setTooltip(new Tooltip("Skip changes detection and build all files"));
        noApkCheck.setTooltip(new Tooltip("Disable repacking into new APK"));
        noCrunchCheck.setTooltip(new Tooltip("Disable crunching of resource files"));
        useAapt1Check.setTooltip(new Tooltip("Use aapt1 binary instead of aapt2"));
        netSecCheck.setTooltip(new Tooltip("Add network security configuration"));
        aaptField.setTooltip(new Tooltip("Path to custom AAPT binary"));
        frameworkField.setTooltip(new Tooltip("Path to framework files (.apk)"));

        // Create a grid layout with 3 columns
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));

        // Column 1
        grid.add(debugCheck, 0, 0);
        grid.add(copyOriginalCheck, 0, 1);
        grid.add(forceCheck, 0, 2);

        // Column 2
        grid.add(noApkCheck, 1, 0);
        grid.add(noCrunchCheck, 1, 1);
        grid.add(useAapt1Check, 1, 2);
        grid.add(netSecCheck, 1, 3);

        // Column 3 - Advanced options
        grid.add(advancedLabel, 2, 0);
        grid.add(aaptBox, 2, 1, 1, 2);
        grid.add(frameworkBox, 2, 3, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(700, 300);

        // Handle dialog buttons
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                // Save configuration
                recompileConfig.setDebug(debugCheck.isSelected());
                recompileConfig.setCopyOriginal(copyOriginalCheck.isSelected());
                recompileConfig.setForce(forceCheck.isSelected());
                recompileConfig.setNoApk(noApkCheck.isSelected());
                recompileConfig.setNoCrunch(noCrunchCheck.isSelected());
                recompileConfig.setUseAapt1(useAapt1Check.isSelected());
                recompileConfig.setNetSec(netSecCheck.isSelected());
                recompileConfig.setAaptPath(aaptField.getText());
                recompileConfig.setFrameworkPath(frameworkField.getText());

                // Update config summary
                updateRecompileConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) {
                    // Keep dialog open
                    return null;
                }
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void updateDecompileConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();

        // Count selected options
        int selectedCount = 0;
        if (decompileConfig.isNoRes()) selectedCount++;
        if (decompileConfig.isNoSrc()) selectedCount++;
        if (decompileConfig.isNoAssets()) selectedCount++;
        if (decompileConfig.isOnlyManifest()) selectedCount++;
        if (decompileConfig.isForce()) selectedCount++;
        if (decompileConfig.isNoDebug()) selectedCount++;
        if (decompileConfig.isMatchOriginal()) selectedCount++;
        if (decompileConfig.isKeepBroken()) selectedCount++;
        if (decompileConfig.isOnlyMainClasses()) selectedCount++;

        if (selectedCount == 0) {
            summary.append("Using default decompile options");
        } else {
            summary.append(selectedCount).append(" decompile option(s) configured: ");

            // Add key options
            List<String> activeOptions = new ArrayList<>();
            if (decompileConfig.isNoRes()) activeOptions.add("No Resources");
            if (decompileConfig.isNoSrc()) activeOptions.add("No Sources");
            if (decompileConfig.isForce()) activeOptions.add("Force");
            if (decompileConfig.isOnlyManifest()) activeOptions.add("Only Manifest");

            if (!activeOptions.isEmpty()) {
                summary.append(String.join(", ", activeOptions));
            }

            // Add API/jobs if set
            if (!decompileConfig.getApiLevel().isEmpty()) {
                summary.append(" | API: ").append(decompileConfig.getApiLevel());
            }
            if (!decompileConfig.getJobs().equals("1")) {
                summary.append(" | Threads: ").append(decompileConfig.getJobs());
            }
        }

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
    }

    private void updateRecompileConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();

        // Count selected options
        int selectedCount = 0;
        if (recompileConfig.isDebug()) selectedCount++;
        if (recompileConfig.isCopyOriginal()) selectedCount++;
        if (recompileConfig.isForce()) selectedCount++;
        if (recompileConfig.isNoApk()) selectedCount++;
        if (recompileConfig.isNoCrunch()) selectedCount++;
        if (recompileConfig.isUseAapt1()) selectedCount++;
        if (recompileConfig.isNetSec()) selectedCount++;

        if (selectedCount == 0) {
            summary.append("Using default recompile options");
        } else {
            summary.append(selectedCount).append(" recompile option(s) configured: ");

            // Add key options
            List<String> activeOptions = new ArrayList<>();
            if (recompileConfig.isDebug()) activeOptions.add("Debug");
            if (recompileConfig.isCopyOriginal()) activeOptions.add("Copy Original");
            if (recompileConfig.isForce()) activeOptions.add("Force");
            if (recompileConfig.isUseAapt1()) activeOptions.add("AAPT1");

            if (!activeOptions.isEmpty()) {
                summary.append(String.join(", ", activeOptions));
            }

            // Add custom paths if set
            if (!recompileConfig.getAaptPath().isEmpty()) {
                summary.append(" | Custom AAPT");
            }
            if (!recompileConfig.getFrameworkPath().isEmpty()) {
                summary.append(" | Custom Framework");
            }
        }

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");
    }

    private void executeDecompile(String apkPath, String outputPath) {
        // Execute decompile with stored configuration
        Apktool.executeDecode(
                apkPath,
                outputPath,
                decompileConfig.getFrameworkPath().isEmpty() ? "framework path" : decompileConfig.getFrameworkPath(),
                decompileConfig.getApiLevel(),
                decompileConfig.getJobs(),
                decompileConfig.isNoRes(),
                decompileConfig.isNoSrc(),
                decompileConfig.isNoAssets(),
                decompileConfig.isOnlyManifest(),
                decompileConfig.isForce(),
                decompileConfig.isNoDebug(),
                decompileConfig.isMatchOriginal(),
                decompileConfig.isKeepBroken(),
                decompileConfig.isOnlyMainClasses()
        );
    }

    private void executeRecompile(String inputPath, String outputPath) {
        // Execute recompile with stored configuration
        Apktool.executeBuild(
                inputPath,
                outputPath,
                recompileConfig.getAaptPath(),
                recompileConfig.getFrameworkPath(),
                recompileConfig.isDebug(),
                recompileConfig.isCopyOriginal(),
                recompileConfig.isForce(),
                recompileConfig.isNoApk(),
                recompileConfig.isNoCrunch(),
                recompileConfig.isUseAapt1(),
                recompileConfig.isNetSec()
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private Tab createOtherToolTab() {
        Tab buildTab = new Tab("Other Tools");
        buildTab.setClosable(false);
        buildTab.setGraphic(createIcon("🔨"));

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("dark-container");

        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        apkPathField.getStyleClass().add("dark-text-field");
        apkPathField.setPrefWidth(400);

        Button browseApkButton = createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
            }
        });

        Label outputLabel = new Label("Output Directory:");
        outputLabel.getStyleClass().add("dark-label");

        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: apk.out");
        outputPathField.getStyleClass().add("dark-text-field");

        Button browseOutputButton = createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(null);
            if (dir != null) {
                outputPathField.setText(dir.getAbsolutePath());
            }
        });

        mainBox.getChildren().addAll(apkPathField,browseApkButton,outputLabel,outputPathField,browseOutputButton);

        return buildTab;
    }

    private Tab createFrameworkTab() {
        Tab frameworkTab = new Tab("Framework");
        frameworkTab.setClosable(false);
        frameworkTab.setGraphic(createIcon("📦"));

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("dark-container");

        // Install Framework Section
        VBox installSection = new VBox(15);
        installSection.getStyleClass().add("section-box");

        Label installLabel = new Label("Install Framework");
        installLabel.getStyleClass().add("section-label");

        Label frameworkApkLabel = new Label("Framework APK:");
        frameworkApkLabel.getStyleClass().add("dark-label");

        TextField frameworkApkField = new TextField();
        frameworkApkField.setPromptText("Select framework APK...");
        frameworkApkField.getStyleClass().add("dark-text-field");

        Button browseFrameworkButton = createStyledButton("Browse", "primary");
        browseFrameworkButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                frameworkApkField.setText(file.getAbsolutePath());
            }
        });

        Label tagLabel = new Label("Tag (optional):");
        tagLabel.getStyleClass().add("dark-label");
        TextField tagField = new TextField();
        tagField.setPromptText("e.g., android-30");
        tagField.getStyleClass().add("dark-text-field");

        Button installButton = createStyledButton("Install Framework", "success");
        installButton.setPrefWidth(200);
        installButton.setOnAction(e -> Apktool.executeInstallFramework(
                frameworkApkField.getText(),
                tagField.getText()
        ));

        installSection.getChildren().addAll(
                installLabel, frameworkApkLabel, frameworkApkField, browseFrameworkButton,
                tagLabel, tagField, installButton
        );

        // Other Framework Actions
        VBox actionsSection = new VBox(15);
        actionsSection.getStyleClass().add("section-box");

        Label actionsLabel = new Label("Framework Management");
        actionsLabel.getStyleClass().add("section-label");

        Button listFrameworksButton = createStyledButton("📋 List Installed Frameworks", "secondary");
        listFrameworksButton.setPrefWidth(250);
        listFrameworksButton.setOnAction(e -> Apktool.executeListFrameworks());

        Button emptyFrameworkButton = createStyledButton("🗑️ Empty Framework Directory", "danger");
        emptyFrameworkButton.setPrefWidth(250);
        emptyFrameworkButton.setOnAction(e -> Apktool.executeEmptyFrameworkDir());

        actionsSection.getChildren().addAll(
                actionsLabel, listFrameworksButton, emptyFrameworkButton
        );

        mainBox.getChildren().addAll(installSection, actionsSection);

        frameworkTab.setContent(mainBox);
        return frameworkTab;
    }

    private Tab createOtherTab() {
        Tab otherTab = new Tab("Other Tools");
        otherTab.setClosable(false);
        otherTab.setGraphic(createIcon("⚙️"));

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("dark-container");

        // Publicize Resources
        VBox publicizeSection = new VBox(15);
        publicizeSection.getStyleClass().add("section-box");

        Label publicizeLabel = new Label("Publicize Resources");
        publicizeLabel.getStyleClass().add("section-label");

        Label arscLabel = new Label("ARSC File:");
        arscLabel.getStyleClass().add("dark-label");

        TextField arscPathField = new TextField();
        arscPathField.setPromptText("Select compiled resources file...");
        arscPathField.getStyleClass().add("dark-text-field");

        Button browseArscButton = createStyledButton("Browse", "secondary");
        browseArscButton.setOnAction(e -> {
            FileChooser arscChooser = new FileChooser();
            arscChooser.setTitle("Select ARSC File");
            arscChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("ARSC Files", "*.arsc")
            );
            File file = arscChooser.showOpenDialog(null);
            if (file != null) {
                arscPathField.setText(file.getAbsolutePath());
            }
        });

        Button publicizeButton = createStyledButton("Publicize Resources", "primary");
        publicizeButton.setPrefWidth(200);
        publicizeButton.setOnAction(e -> Apktool.executePublicizeResources(
                arscPathField.getText()
        ));

        publicizeSection.getChildren().addAll(
                publicizeLabel, arscLabel, arscPathField, browseArscButton, publicizeButton
        );

        // Utility Tools
        VBox utilitySection = new VBox(15);
        utilitySection.getStyleClass().add("section-box");

        Label utilityLabel = new Label("Utility Tools");
        utilityLabel.getStyleClass().add("section-label");

        HBox buttonBox = new HBox(15);

        Button versionButton = createStyledButton("Version", "info");
        versionButton.setPrefWidth(150);
        versionButton.setOnAction(e -> Apktool.executeVersionCheck());

        Button helpButton = createStyledButton("Help", "info");
        helpButton.setPrefWidth(150);
        helpButton.setOnAction(e -> Apktool.executeHelp());

        buttonBox.getChildren().addAll(versionButton, helpButton);

        utilitySection.getChildren().addAll(utilityLabel, buttonBox);

        mainBox.getChildren().addAll(publicizeSection, utilitySection);

        otherTab.setContent(mainBox);
        return otherTab;
    }

    private Tab createSettingsTab() {
        Tab settingsTab = new Tab("Settings");
        settingsTab.setClosable(false);
        settingsTab.setGraphic(createIcon("⚙️"));

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("dark-container");

        // Path Settings
        VBox pathSection = new VBox(15);
        pathSection.getStyleClass().add("section-box");

        Label pathLabel = new Label("Path Settings");
        pathLabel.getStyleClass().add("section-label");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.getStyleClass().add("dark-grid");

        // Apktool Path
        Label apktoolLabel = new Label("Apktool Path:");
        apktoolLabel.getStyleClass().add("dark-label");

        TextField apktoolPathField = new TextField(apktoolPath);
        apktoolPathField.getStyleClass().add("dark-text-field");

        Button browseApktoolButton = createStyledButton("Browse", "secondary");
        browseApktoolButton.setOnAction(e -> {
            FileChooser jarChooser = new FileChooser();
            jarChooser.setTitle("Select Apktool JAR");
            jarChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JAR Files", "*.jar")
            );
            File file = jarChooser.showOpenDialog(null);
            if (file != null) {
                apktoolPathField.setText(file.getAbsolutePath());
                apktoolPath = file.getAbsolutePath();
            }
        });

        // Default Directory
        Label defaultDirLabel = new Label("Default Directory:");
        defaultDirLabel.getStyleClass().add("dark-label");

        TextField defaultDirField = new TextField(System.getProperty("user.dir"));
        defaultDirField.getStyleClass().add("dark-text-field");

        Button browseDefaultDirButton = createStyledButton("Browse", "secondary");
        browseDefaultDirButton.setOnAction(e -> {
            File dir = directoryChooser.showDialog(null);
            if (dir != null) {
                defaultDirField.setText(dir.getAbsolutePath());
            }
        });

        grid.add(apktoolLabel, 0, 0);
        grid.add(apktoolPathField, 1, 0);
        grid.add(browseApktoolButton, 2, 0);

        grid.add(defaultDirLabel, 0, 1);
        grid.add(defaultDirField, 1, 1);
        grid.add(browseDefaultDirButton, 2, 1);

        // Save Button
        Button saveButton = createStyledButton("💾 Save Settings", "success");
        saveButton.setPrefWidth(200);
        saveButton.setOnAction(e -> Apktool.saveSettings(
                apktoolPathField.getText(),
                defaultDirField.getText()
        ));

        pathSection.getChildren().addAll(pathLabel, grid, saveButton);

        // Theme Settings
        VBox themeSection = new VBox(15);
        themeSection.getStyleClass().add("section-box");

        Label themeLabel = new Label("Theme Settings");
        themeLabel.getStyleClass().add("section-label");

        HBox themeBox = new HBox(15);
        themeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        RadioButton darkThemeBtn = new RadioButton("Dark Theme");
        darkThemeBtn.setSelected(darkMode);
        darkThemeBtn.getStyleClass().add("dark-radio");

        RadioButton lightThemeBtn = new RadioButton("Light Theme");
        lightThemeBtn.setSelected(!darkMode);
        lightThemeBtn.getStyleClass().add("dark-radio");

        ToggleGroup themeGroup = new ToggleGroup();
        darkThemeBtn.setToggleGroup(themeGroup);
        lightThemeBtn.setToggleGroup(themeGroup);

        darkThemeBtn.setOnAction(e -> switchTheme(true));
        lightThemeBtn.setOnAction(e -> switchTheme(false));

        themeBox.getChildren().addAll(darkThemeBtn, lightThemeBtn);

        themeSection.getChildren().addAll(themeLabel, themeBox);

        mainBox.getChildren().addAll(pathSection, themeSection);

        settingsTab.setContent(mainBox);
        return settingsTab;
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
    private Button createStyledButton(String text, String style) {
        Button button = new Button(text);
        button.getStyleClass().addAll("dark-button", style);
        return button;
    }

    private CheckBox createCheckBox(String text, String tooltip) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.getStyleClass().add("dark-checkbox");
        if (tooltip != null) {
            checkBox.setTooltip(new Tooltip(tooltip));
        }
        return checkBox;
    }

    private Label createIcon(String emoji) {
        Label icon = new Label(emoji);
        icon.getStyleClass().add("icon");
        return icon;
    }

    // Theme Switching
    private void switchTheme(boolean dark) {
        darkMode = dark;
        // In a real implementation, you would reload the CSS
        appendOutput("Theme switched to " + (dark ? "Dark" : "Light") + " mode");
    }



    private void loadSettings() {
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

    @Override
    public void stop() {
        executor.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}