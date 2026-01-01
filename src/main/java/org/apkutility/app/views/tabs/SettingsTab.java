package org.apkutility.app.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apkutility.app.services.ApkToolService;
import org.apkutility.app.services.LogOutput;
import org.apkutility.app.services.SettingsManager;
import org.apkutility.app.services.UserNotifier;
import org.apkutility.app.utils.UiUtils;

import java.io.File;

/**
 * Settings Tab for configuring tool paths and application preferences
 */
public class SettingsTab {
    
    private final LogOutput logOutput;
    private final UserNotifier userNotifier;
    private final SettingsManager settingsManager;
    private ApkToolService apkToolService;
    
    // Tool Path Fields
    private TextField apktoolPathField;
    private TextField apkEditorPathField;
    private TextField adbPathField;
    private TextField zipalignPathField;
    private TextField apksignerPathField;
    private TextField aaptPathField;
    private TextField aapt2PathField;
    private TextField javaPathField;
    private TextField frameworkPathField;
    private TextField workingDirField;
    
    // Validation Labels
    private Label apktoolValidLabel;
    private Label apkEditorValidLabel;
    private Label adbValidLabel;
    private Label zipalignValidLabel;
    private Label apksignerValidLabel;
    private Label aaptValidLabel;
    private Label aapt2ValidLabel;
    private Label javaValidLabel;
    private Label frameworkValidLabel;
    
    // Preference Controls
    private CheckBox darkModeCheckBox;
    private CheckBox autoSaveCheckBox;
    
    // Framework Manager Controls
    private TextField frameworkApkField;
    private TextField frameworkTagField;
    
    public SettingsTab(LogOutput logOutput, UserNotifier userNotifier, SettingsManager settingsManager) {
        this.logOutput = logOutput;
        this.userNotifier = userNotifier;
        this.settingsManager = settingsManager;
    }
    
    public Node createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("tab-content");
        
        // Header
        Label titleLabel = new Label("⚙️ Settings");
        titleLabel.getStyleClass().add("section-title");
        
        Label descLabel = new Label("Configure tool paths and application preferences");
        descLabel.getStyleClass().add("section-description");
        
        // Create sections
        Node toolPathsSection = createToolPathsSection();
        Node frameworkSection = createFrameworkSection();
        Node frameworkManagerSection = createFrameworkManagerSection();
        Node preferencesSection = createPreferencesSection();
        Node actionsSection = createActionsSection();
        
        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(20, titleLabel, descLabel, toolPathsSection, frameworkSection, frameworkManagerSection, preferencesSection, actionsSection);
        content.setPadding(new Insets(10));
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edge-to-edge");
        
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Load current settings into UI
        loadSettingsToUI();
        
        return root;
    }
    
    private Node createToolPathsSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("🔧 Tool Paths");
        sectionTitle.getStyleClass().add("subsection-title");
        
        // APKTool
        apktoolPathField = new TextField();
        apktoolValidLabel = new Label();
        HBox apktoolRow = createPathRow("APKTool JAR:", apktoolPathField, apktoolValidLabel, true);
        
        // APKEditor
        apkEditorPathField = new TextField();
        apkEditorValidLabel = new Label();
        HBox apkEditorRow = createPathRow("APKEditor JAR:", apkEditorPathField, apkEditorValidLabel, true);
        
        // ADB
        adbPathField = new TextField();
        adbValidLabel = new Label();
        HBox adbRow = createPathRow("ADB Executable:", adbPathField, adbValidLabel, true);
        
        // ZipAlign
        zipalignPathField = new TextField();
        zipalignValidLabel = new Label();
        HBox zipalignRow = createPathRow("ZipAlign:", zipalignPathField, zipalignValidLabel, true);
        
        // APK Signer
        apksignerPathField = new TextField();
        apksignerValidLabel = new Label();
        HBox apksignerRow = createPathRow("APK Signer JAR:", apksignerPathField, apksignerValidLabel, true);
        
        // AAPT
        aaptPathField = new TextField();
        aaptValidLabel = new Label();
        HBox aaptRow = createPathRow("AAPT:", aaptPathField, aaptValidLabel, true);
        
        // AAPT2
        aapt2PathField = new TextField();
        aapt2ValidLabel = new Label();
        HBox aapt2Row = createPathRow("AAPT2:", aapt2PathField, aapt2ValidLabel, true);
        
        // Java
        javaPathField = new TextField();
        javaValidLabel = new Label();
        HBox javaRow = createPathRow("Java:", javaPathField, javaValidLabel, false);
        
        section.getChildren().addAll(sectionTitle, apktoolRow, apkEditorRow, adbRow, zipalignRow, apksignerRow, aaptRow, aapt2Row, javaRow);
        return section;
    }
    
    private Node createFrameworkSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("📁 Directories");
        sectionTitle.getStyleClass().add("subsection-title");
        
        // Framework Path
        frameworkPathField = new TextField();
        frameworkValidLabel = new Label();
        HBox frameworkRow = createPathRow("Framework Directory:", frameworkPathField, frameworkValidLabel, false);
        
        // Working Directory
        workingDirField = new TextField();
        Label workingDirValidLabel = new Label(); // Not validated, just shown
        HBox workingDirRow = createPathRow("Default Working Dir:", workingDirField, workingDirValidLabel, false);
        
        section.getChildren().addAll(sectionTitle, frameworkRow, workingDirRow);
        return section;
    }
    
    private Node createFrameworkManagerSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("📦 Framework Manager");
        sectionTitle.getStyleClass().add("subsection-title");
        
        Label desc = new Label("Install and manage apktool framework files");
        desc.getStyleClass().add("field-description");
        
        // Framework APK picker
        HBox apkRow = new HBox(10);
        apkRow.setAlignment(Pos.CENTER_LEFT);
        Label apkLabel = new Label("Framework APK:");
        apkLabel.setPrefWidth(150);
        apkLabel.getStyleClass().add("field-label");
        
        frameworkApkField = new TextField();
        frameworkApkField.setPromptText("Select framework APK file...");
        frameworkApkField.setPrefWidth(400);
        HBox.setHgrow(frameworkApkField, Priority.ALWAYS);
        
        Button browseApkBtn = new Button("📂");
        browseApkBtn.getStyleClass().add("button-icon");
        browseApkBtn.setOnAction(e -> browseFrameworkApk());
        apkRow.getChildren().addAll(apkLabel, frameworkApkField, browseApkBtn);
        
        // Tag field
        HBox tagRow = new HBox(10);
        tagRow.setAlignment(Pos.CENTER_LEFT);
        Label tagLabel = new Label("Tag (optional):");
        tagLabel.setPrefWidth(150);
        tagLabel.getStyleClass().add("field-label");
        
        frameworkTagField = new TextField();
        frameworkTagField.setPromptText("e.g., samsung, miui");
        frameworkTagField.setPrefWidth(400);
        HBox.setHgrow(frameworkTagField, Priority.ALWAYS);
        tagRow.getChildren().addAll(tagLabel, frameworkTagField);
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        Button installBtn = new Button("📥 Install Framework");
        installBtn.getStyleClass().add("button-primary");
        installBtn.setOnAction(e -> installFramework());
        
        Button listBtn = new Button("📋 List Frameworks");
        listBtn.getStyleClass().add("button-secondary");
        listBtn.setOnAction(e -> listFrameworks());
        
        Button emptyBtn = new Button("🗑️ Empty Framework Dir");
        emptyBtn.getStyleClass().add("button-secondary");
        emptyBtn.setOnAction(e -> emptyFrameworkDir());
        
        actions.getChildren().addAll(installBtn, listBtn, emptyBtn);
        
        section.getChildren().addAll(sectionTitle, desc, apkRow, tagRow, actions);
        return section;
    }
    
    private void browseFrameworkApk() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Framework APK");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("APK Files", "*.apk"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            frameworkApkField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    private void installFramework() {
        if (apkToolService == null) {
            userNotifier.showError("ApkToolService not initialized");
            return;
        }
        String apkPath = frameworkApkField.getText();
        String tag = frameworkTagField.getText();
        apkToolService.executeInstallFramework(apkPath, tag);
    }
    
    private void listFrameworks() {
        if (apkToolService == null) {
            userNotifier.showError("ApkToolService not initialized");
            return;
        }
        apkToolService.executeListFrameworks();
    }
    
    private void emptyFrameworkDir() {
        if (apkToolService == null) {
            userNotifier.showError("ApkToolService not initialized");
            return;
        }
        apkToolService.executeEmptyFrameworkDir();
    }
    
    public void setApkToolService(ApkToolService apkToolService) {
        this.apkToolService = apkToolService;
    }
    
    private Node createPreferencesSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("🎨 Preferences");
        sectionTitle.getStyleClass().add("subsection-title");
        
        darkModeCheckBox = new CheckBox("Dark Mode");
        darkModeCheckBox.getStyleClass().add("setting-checkbox");
        
        autoSaveCheckBox = new CheckBox("Auto-save settings on change");
        autoSaveCheckBox.getStyleClass().add("setting-checkbox");
        
        section.getChildren().addAll(sectionTitle, darkModeCheckBox, autoSaveCheckBox);
        return section;
    }
    
    private Node createActionsSection() {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        Button saveBtn = new Button("💾 Save Settings");
        saveBtn.getStyleClass().addAll("button-primary");
        saveBtn.setOnAction(e -> saveSettings());
        
        Button validateBtn = new Button("✅ Validate Paths");
        validateBtn.getStyleClass().addAll("button-secondary");
        validateBtn.setOnAction(e -> validateAllPaths());
        
        Button resetBtn = new Button("🔄 Reset to Defaults");
        resetBtn.getStyleClass().addAll("button-secondary");
        resetBtn.setOnAction(e -> resetToDefaults());
        
        actions.getChildren().addAll(saveBtn, validateBtn, resetBtn);
        return actions;
    }
    
    private HBox createPathRow(String labelText, TextField pathField, Label validLabel, boolean isFile) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(labelText);
        label.setPrefWidth(150);
        label.getStyleClass().add("field-label");
        
        pathField.setPromptText("Enter path...");
        pathField.setPrefWidth(400);
        HBox.setHgrow(pathField, Priority.ALWAYS);
        
        Button browseBtn = new Button("📂");
        browseBtn.getStyleClass().add("button-icon");
        browseBtn.setOnAction(e -> browsePath(pathField, isFile));
        
        validLabel.setPrefWidth(20);
        validLabel.setAlignment(Pos.CENTER);
        
        row.getChildren().addAll(label, pathField, browseBtn, validLabel);
        return row;
    }
    
    private void browsePath(TextField pathField, boolean isFile) {
        if (isFile) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            File currentFile = new File(pathField.getText());
            if (currentFile.exists() && currentFile.getParentFile() != null) {
                fileChooser.setInitialDirectory(currentFile.getParentFile());
            }
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                pathField.setText(selectedFile.getAbsolutePath());
            }
        } else {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Directory");
            File currentDir = new File(pathField.getText());
            if (currentDir.exists() && currentDir.isDirectory()) {
                dirChooser.setInitialDirectory(currentDir);
            }
            File selectedDir = dirChooser.showDialog(null);
            if (selectedDir != null) {
                pathField.setText(selectedDir.getAbsolutePath());
            }
        }
    }
    
    private void loadSettingsToUI() {
        var settings = settingsManager.getSettings();
        
        apktoolPathField.setText(settings.getApktoolPath());
        apkEditorPathField.setText(settings.getApkEditorPath());
        adbPathField.setText(settings.getAdbPath());
        zipalignPathField.setText(settings.getZipalignPath());
        apksignerPathField.setText(settings.getApksignerPath());
        aaptPathField.setText(settings.getAaptPath());
        aapt2PathField.setText(settings.getAapt2Path());
        javaPathField.setText(settings.getJavaPath());
        frameworkPathField.setText(settings.getFrameworkPath());
        workingDirField.setText(settings.getDefaultWorkingDir());
        
        darkModeCheckBox.setSelected(settings.isDarkMode());
        autoSaveCheckBox.setSelected(settings.isAutoSave());
        
        // Validate on load
        validateAllPaths();
    }
    
    private void saveSettings() {
        var settings = settingsManager.getSettings();
        
        // Update settings from UI
        settings.setApktoolPath(apktoolPathField.getText());
        settings.setApkEditorPath(apkEditorPathField.getText());
        settings.setAdbPath(adbPathField.getText());
        settings.setZipalignPath(zipalignPathField.getText());
        settings.setApksignerPath(apksignerPathField.getText());
        settings.setAaptPath(aaptPathField.getText());
        settings.setAapt2Path(aapt2PathField.getText());
        settings.setJavaPath(javaPathField.getText());
        settings.setFrameworkPath(frameworkPathField.getText());
        settings.setDefaultWorkingDir(workingDirField.getText());
        settings.setDarkMode(darkModeCheckBox.isSelected());
        settings.setAutoSave(autoSaveCheckBox.isSelected());
        
        // Save to file
        try {
            settingsManager.saveSettings();
            logOutput.append("✅ Settings saved to: " + settingsManager.getConfigPath());
            
            // Apply dark mode if changed
            if (darkModeCheckBox.getScene() != null) {
                UiUtils.switchTheme(darkModeCheckBox.getScene(), settings.isDarkMode());
            }
            
            userNotifier.showError("Settings saved successfully!"); // Using showError as notification dialog
        } catch (Exception e) {
            userNotifier.showError("Failed to save settings: " + e.getMessage());
        }
    }
    
    private void validateAllPaths() {
        var settings = settingsManager.getSettings();
        
        // Temporarily update settings for validation
        settings.setApktoolPath(apktoolPathField.getText());
        settings.setApkEditorPath(apkEditorPathField.getText());
        settings.setAdbPath(adbPathField.getText());
        settings.setZipalignPath(zipalignPathField.getText());
        settings.setApksignerPath(apksignerPathField.getText());
        settings.setAaptPath(aaptPathField.getText());
        settings.setAapt2Path(aapt2PathField.getText());
        settings.setJavaPath(javaPathField.getText());
        settings.setFrameworkPath(frameworkPathField.getText());
        
        // Validate each path
        updateValidationLabel(apktoolValidLabel, settings.validateApktoolPath());
        updateValidationLabel(apkEditorValidLabel, settings.validateApkEditorPath());
        updateValidationLabel(adbValidLabel, settings.validateAdbPath());
        updateValidationLabel(zipalignValidLabel, settings.validateZipalignPath());
        updateValidationLabel(apksignerValidLabel, settings.validateApksignerPath());
        updateValidationLabel(aaptValidLabel, settings.validateAaptPath());
        updateValidationLabel(aapt2ValidLabel, settings.validateAapt2Path());
        updateValidationLabel(javaValidLabel, settings.validateJavaPath());
        updateValidationLabel(frameworkValidLabel, settings.validateFrameworkPath());
        
        boolean allValid = settings.validateApktoolPath() && 
                          settings.validateApkEditorPath() && 
                          settings.validateAdbPath() && 
                          settings.validateZipalignPath() && 
                          settings.validateApksignerPath() &&
                          settings.validateAaptPath() &&
                          settings.validateAapt2Path() &&
                          settings.validateJavaPath() &&
                          settings.validateFrameworkPath();
        
        if (allValid) {
            logOutput.append("✅ All paths validated successfully");
        } else {
            logOutput.append("⚠️ Some paths are invalid - please check the red X indicators");
        }
    }
    
    private void updateValidationLabel(Label label, boolean isValid) {
        if (isValid) {
            label.setText("✅");
            label.setStyle("-fx-text-fill: #28a745;");
        } else {
            label.setText("❌");
            label.setStyle("-fx-text-fill: #dc3545;");
        }
    }
    
    private void resetToDefaults() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Settings");
        confirm.setHeaderText("Reset to Default Values");
        confirm.setContentText("Are you sure you want to reset all settings to defaults?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            settingsManager.resetToDefaults();
            loadSettingsToUI();
            logOutput.append("🔄 Settings reset to defaults");
        }
    }
}
