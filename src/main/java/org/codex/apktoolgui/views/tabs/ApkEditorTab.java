package org.codex.apktoolgui.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.codex.apktoolgui.models.*;
import org.codex.apktoolgui.views.MainView;
import org.codex.apktoolgui.services.ApkEditorService;
import org.codex.apktoolgui.utils.UiUtils;

import java.io.File;

public class ApkEditorTab {
    private final MainView mainView;
    private final ApkEditorService apkEditorService;

    // Configuration objects
    private final ApkEditorDecompileConfig decompileConfig = new ApkEditorDecompileConfig();
    private final ApkEditorBuildConfig buildConfig = new ApkEditorBuildConfig();
    private final ApkEditorMergeConfig mergeConfig = new ApkEditorMergeConfig();
    private final ApkEditorRefactorConfig refactorConfig = new ApkEditorRefactorConfig();
    private final ApkEditorProtectConfig protectConfig = new ApkEditorProtectConfig();

    public ApkEditorTab(MainView mainView, ApkEditorService apkEditorService) {
        this.mainView = mainView;
        this.apkEditorService = apkEditorService;
    }

    public Tab createAPKEditorTab() {
        Tab apkEditorTab = new Tab("APKEDITOR");
        apkEditorTab.setClosable(false);
        apkEditorTab.setGraphic(mainView.createIcon("⚡"));

        // Create main container with 5 sections
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("dark-container");

        // ========== DECOMPILE SECTION ==========
        VBox decompileSection = createDecompileSection();

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(10, 0, 10, 0));

        // ========== BUILD SECTION ==========
        VBox buildSection = createBuildSection();

        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(10, 0, 10, 0));

        // ========== MERGE SECTION ==========
        VBox mergeSection = createMergeSection();

        Separator sep3 = new Separator();
        sep3.setPadding(new Insets(10, 0, 10, 0));

        // ========== REFACTOR SECTION ==========
        VBox refactorSection = createRefactorSection();

        Separator sep4 = new Separator();
        sep4.setPadding(new Insets(10, 0, 10, 0));

        // ========== PROTECT SECTION ==========
        VBox protectSection = createProtectSection();

        mainBox.getChildren().addAll(
                decompileSection, sep1,
                buildSection, sep2,
                mergeSection, sep3,
                refactorSection, sep4,
                protectSection
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(new Insets(0));

        apkEditorTab.setContent(scrollPane);
        return apkEditorTab;
    }

    private VBox createDecompileSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-container");

        Label title = new Label("1. Decompile APK");
        title.getStyleClass().add("section-title");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);

        Label apkLabel = new Label("APK File:");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        apkPathField.setPrefWidth(300);

        Button browseApkButton = mainView.createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> UiUtils.browseFile(mainView.fileChooser, apkPathField, "Select APK", "*.apk", "Select File"));

        Label outputLabel = new Label("Output Directory:");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: apk_decompiled");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> UiUtils.browseDirectory(mainView.directoryChooser, outputPathField));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration summary
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Decompile Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label configSummary = new Label("Default settings (JSON output)");
        configSummary.setId("decompile-config-summary");
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        configSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, configSummary);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        configButton.setPrefWidth(180);
        configButton.setOnAction(e -> showDecompileOptionsDialog(configSummary));

        Button executeButton = mainView.createStyledButton("▶ Decompile APK", "large-primary");
        executeButton.setPrefWidth(180);
        executeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath.isEmpty()) {
                UiUtils.showAlert("Error", "Please select an APK file first.");
                return;
            }

            String finalOutput = outputPathField.getText().isEmpty() ?
                    UiUtils.getDefaultOutputPath(apkPath, "_decompiled") : outputPathField.getText();

            apkEditorService.executeDecompile(
                    apkPath,
                    finalOutput,
                    decompileConfig.isDecompileToXml(),
                    decompileConfig.isLoadDex(),
                    decompileConfig.getDexLibrary()
            );
        });

        buttonBox.getChildren().addAll(configButton, executeButton);

        section.getChildren().addAll(title, fileGrid, configBox, buttonBox);
        return section;
    }

    private VBox createBuildSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-container");

        Label title = new Label("2. Build APK");
        title.getStyleClass().add("section-title");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);

        Label inputLabel = new Label("Project Directory:");
        TextField inputPathField = new TextField();
        inputPathField.setPromptText("Select decompiled directory...");
        inputPathField.setPrefWidth(300);

        Button browseInputButton = mainView.createStyledButton("Browse", "primary");
        browseInputButton.setOnAction(e -> UiUtils.browseDirectory(mainView.directoryChooser, inputPathField));

        Label outputLabel = new Label("Output APK:");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: output.apk");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(mainView.fileChooser, outputPathField, "Save APK", "*.apk", "Select File"));

        fileGrid.add(inputLabel, 0, 0);
        fileGrid.add(inputPathField, 1, 0);
        fileGrid.add(browseInputButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration summary
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Build Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label configSummary = new Label("Default settings (JSON input)");
        configSummary.setId("build-config-summary");
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        configSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, configSummary);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        configButton.setPrefWidth(180);
        configButton.setOnAction(e -> showBuildOptionsDialog(configSummary));

        Button executeButton = mainView.createStyledButton("▶ Build APK", "large-primary");
        executeButton.setPrefWidth(180);
        executeButton.setOnAction(e -> {
            String inputPath = inputPathField.getText();
            if (inputPath.isEmpty()) {
                UiUtils.showAlert("Error", "Please select a project directory first.");
                return;
            }

            String finalOutput = outputPathField.getText().isEmpty() ?
                    new File(inputPath).getParent() + File.separator + "output.apk" :
                    outputPathField.getText();

            apkEditorService.executeBuild(
                    inputPath,
                    finalOutput,
                    buildConfig.isBuildFromXml(),
                    buildConfig.getDexLibrary()
            );
        });

        buttonBox.getChildren().addAll(configButton, executeButton);

        section.getChildren().addAll(title, fileGrid, configBox, buttonBox);
        return section;
    }

    private VBox createMergeSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-container");

        Label title = new Label("3. Merge Split APKs");
        title.getStyleClass().add("section-title");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);

        Label inputLabel = new Label("Input Path:");
        TextField inputPathField = new TextField();
        inputPathField.setPromptText("Directory or XAPK/APKM/APKS file...");
        inputPathField.setPrefWidth(300);

        Button browseInputButton = mainView.createStyledButton("Browse", "primary");
        browseInputButton.setOnAction(e -> {
            File file = mainView.directoryChooser.showDialog(null);
            if (file != null) {
                inputPathField.setText(file.getAbsolutePath());
            }
        });

        Label outputLabel = new Label("Output APK:");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: merged.apk");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(mainView.fileChooser, outputPathField, "Save Merged APK", "*.apk", "Select File"));

        fileGrid.add(inputLabel, 0, 0);
        fileGrid.add(inputPathField, 1, 0);
        fileGrid.add(browseInputButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration summary
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Merge Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label configSummary = new Label("Default settings");
        configSummary.setId("merge-config-summary");
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        configSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, configSummary);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        configButton.setPrefWidth(180);
        configButton.setOnAction(e -> showMergeOptionsDialog(configSummary));

        Button executeButton = mainView.createStyledButton("▶ Merge APKs", "large-primary");
        executeButton.setPrefWidth(180);
        executeButton.setOnAction(e -> {
            String inputPath = inputPathField.getText();
            if (inputPath.isEmpty()) {
                UiUtils.showAlert("Error", "Please select input path first.");
                return;
            }

            String finalOutput = outputPathField.getText().isEmpty() ?
                    new File(inputPath).getParent() + File.separator + "merged.apk" :
                    outputPathField.getText();

            apkEditorService.executeMergeAdvanced(
                    inputPath,
                    finalOutput,
                    mergeConfig.getResDir(),
                    mergeConfig.getExtractNativeLibs(),
                    mergeConfig.isCleanMeta(),
                    false,
                    mergeConfig.isValidateModules(),
                    mergeConfig.isVrd()
            );
        });

        buttonBox.getChildren().addAll(configButton, executeButton);

        section.getChildren().addAll(title, fileGrid, configBox, buttonBox);
        return section;
    }

    private VBox createRefactorSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-container");

        Label title = new Label("4. Refactor Obfuscated Resources");
        title.getStyleClass().add("section-title");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);

        Label apkLabel = new Label("APK File:");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        apkPathField.setPrefWidth(300);

        Button browseApkButton = mainView.createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> UiUtils.browseFile(mainView.fileChooser, apkPathField, "Select APK", "*.apk", "Select File"));

        Label outputLabel = new Label("Output APK:");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: input_refactored.apk");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(mainView.fileChooser, outputPathField, "Save Refactored APK", "*.apk", "Select File"));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration summary
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Refactor Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label configSummary = new Label("Default settings");
        configSummary.setId("refactor-config-summary");
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        configSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, configSummary);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        configButton.setPrefWidth(180);
        configButton.setOnAction(e -> showRefactorOptionsDialog(configSummary));

        Button executeButton = mainView.createStyledButton("▶ Refactor APK", "large-primary");
        executeButton.setPrefWidth(180);
        executeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath.isEmpty()) {
                UiUtils.showAlert("Error", "Please select an APK file first.");
                return;
            }

            String finalOutput = outputPathField.getText().isEmpty() ?
                    UiUtils.getDefaultOutputPath(apkPath, "_refactored") : outputPathField.getText();

            apkEditorService.executeRefactor(
                    apkPath,
                    finalOutput,
                    refactorConfig.getPublicXml(),
                    refactorConfig.isCleanMeta(),
                    false,
                    refactorConfig.isFixTypes()
            );
        });

        buttonBox.getChildren().addAll(configButton, executeButton);

        section.getChildren().addAll(title, fileGrid, configBox, buttonBox);
        return section;
    }

    private VBox createProtectSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-container");

        Label title = new Label("5. Protect/Obfuscate APK");
        title.getStyleClass().add("section-title");

        // File Selection
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);

        Label apkLabel = new Label("APK File:");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        apkPathField.setPrefWidth(300);

        Button browseApkButton = mainView.createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> UiUtils.browseFile(mainView.fileChooser, apkPathField, "Select APK", "*.apk", "Select File"));

        Label outputLabel = new Label("Output APK:");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: input_protected.apk");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(mainView.fileChooser, outputPathField, "Save Protected APK", "*.apk", "Select File"));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration summary
        VBox configBox = new VBox(5);
        configBox.getStyleClass().add("config-box");

        Label configTitle = new Label("Protect Configuration:");
        configTitle.getStyleClass().add("config-title");

        Label configSummary = new Label("Default settings (keep font resources)");
        configSummary.setId("protect-config-summary");
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        configSummary.setWrapText(true);

        configBox.getChildren().addAll(configTitle, configSummary);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        configButton.setPrefWidth(180);
        configButton.setOnAction(e -> showProtectOptionsDialog(configSummary));

        Button executeButton = mainView.createStyledButton("▶ Protect APK", "large-primary");
        executeButton.setPrefWidth(180);
        executeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath.isEmpty()) {
                UiUtils.showAlert("Error", "Please select an APK file first.");
                return;
            }

            String finalOutput = outputPathField.getText().isEmpty() ?
                    UiUtils.getDefaultOutputPath(apkPath, "_protected") : outputPathField.getText();

            apkEditorService.executeProtect(
                    apkPath,
                    finalOutput,
                    protectConfig.getKeepType(),
                    protectConfig.isConfuseZip(),
                    protectConfig.getDicDirNames(),
                    protectConfig.getDicFileNames(),
                    false,
                    protectConfig.isSkipManifest()
            );
        });

        buttonBox.getChildren().addAll(configButton, executeButton);

        section.getChildren().addAll(title, fileGrid, configBox, buttonBox);
        return section;
    }

    // ========== DIALOG METHODS ==========

    private void showDecompileOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Decompile Configuration");
        dialog.setHeaderText("Configure decompilation options");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        // Create checkboxes
        CheckBox xmlOutputCheck = new CheckBox("Decompile to XML (default: JSON)");
        xmlOutputCheck.setSelected(decompileConfig.isDecompileToXml());

        CheckBox loadDexCheck = new CheckBox("Load DEX files (3)");
        loadDexCheck.setSelected(decompileConfig.isLoadDex());

        HBox dexLibBox = new HBox(10);
        Label dexLibLabel = new Label("DEX Library:");
        ComboBox<String> dexLibCombo = new ComboBox<>();
        dexLibCombo.getItems().addAll("jf", "sx", "dx", "auto");
        dexLibCombo.setValue(decompileConfig.getDexLibrary());
        dexLibCombo.setPrefWidth(100);
        dexLibBox.getChildren().addAll(dexLibLabel, dexLibCombo);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));
        grid.add(xmlOutputCheck, 0, 0);
        grid.add(loadDexCheck, 0, 1);
        grid.add(dexLibBox, 0, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 200);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                decompileConfig.setDecompileToXml(xmlOutputCheck.isSelected());
                decompileConfig.setLoadDex(loadDexCheck.isSelected());
                decompileConfig.setDexLibrary(dexLibCombo.getValue());
                updateDecompileConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void showBuildOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Build Configuration");
        dialog.setHeaderText("Configure build options");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        CheckBox xmlInputCheck = new CheckBox("Build from XML (default: JSON)");
        xmlInputCheck.setSelected(buildConfig.isBuildFromXml());

        HBox dexLibBox = new HBox(10);
        Label dexLibLabel = new Label("DEX Library:");
        ComboBox<String> dexLibCombo = new ComboBox<>();
        dexLibCombo.getItems().addAll("jf", "sx", "dx", "auto");
        dexLibCombo.setValue(buildConfig.getDexLibrary());
        dexLibCombo.setPrefWidth(100);
        dexLibBox.getChildren().addAll(dexLibLabel, dexLibCombo);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));
        grid.add(xmlInputCheck, 0, 0);
        grid.add(dexLibBox, 0, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 150);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                buildConfig.setBuildFromXml(xmlInputCheck.isSelected());
                buildConfig.setDexLibrary(dexLibCombo.getValue());
                updateBuildConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void showMergeOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Merge Configuration");
        dialog.setHeaderText("Configure merge options");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        HBox resDirBox = new HBox(10);
        Label resDirLabel = new Label("Resource Dir:");
        TextField resDirField = new TextField(mergeConfig.getResDir());
        resDirField.setPromptText("e.g., r/*");
        resDirField.setPrefWidth(150);
        resDirBox.getChildren().addAll(resDirLabel, resDirField);

        HBox nativeLibsBox = new HBox(10);
        Label nativeLibsLabel = new Label("extractNativeLibs:");
        ComboBox<String> nativeLibsCombo = new ComboBox<>();
        nativeLibsCombo.getItems().addAll("manifest", "none", "true", "false");
        nativeLibsCombo.setValue(mergeConfig.getExtractNativeLibs());
        nativeLibsCombo.setPrefWidth(120);
        nativeLibsBox.getChildren().addAll(nativeLibsLabel, nativeLibsCombo);

        CheckBox cleanMetaCheck = new CheckBox("Clean META-INF");
        cleanMetaCheck.setSelected(mergeConfig.isCleanMeta());

        CheckBox validateModulesCheck = new CheckBox("Validate Modules");
        validateModulesCheck.setSelected(mergeConfig.isValidateModules());

        CheckBox vrdCheck = new CheckBox("Validate Resource Dir");
        vrdCheck.setSelected(mergeConfig.isVrd());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));
        grid.add(resDirBox, 0, 0);
        grid.add(nativeLibsBox, 0, 1);
        grid.add(cleanMetaCheck, 0, 2);
        grid.add(validateModulesCheck, 0, 3);
        grid.add(vrdCheck, 0, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 250);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                mergeConfig.setResDir(resDirField.getText());
                mergeConfig.setExtractNativeLibs(nativeLibsCombo.getValue());
                mergeConfig.setCleanMeta(cleanMetaCheck.isSelected());
                mergeConfig.setValidateModules(validateModulesCheck.isSelected());
                mergeConfig.setVrd(vrdCheck.isSelected());
                updateMergeConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void showRefactorOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Refactor Configuration");
        dialog.setHeaderText("Configure refactor options");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        HBox publicXmlBox = new HBox(10);
        Label publicXmlLabel = new Label("Public XML:");
        TextField publicXmlField = new TextField(refactorConfig.getPublicXml());
        publicXmlField.setPrefWidth(200);
        Button browsePublicXmlButton = new Button("Browse");
        browsePublicXmlButton.setOnAction(e -> {
            File file = mainView.fileChooser.showOpenDialog(null);
            if (file != null) publicXmlField.setText(file.getAbsolutePath());
        });
        publicXmlBox.getChildren().addAll(publicXmlLabel, publicXmlField, browsePublicXmlButton);

        CheckBox cleanMetaCheck = new CheckBox("Clean META-INF");
        cleanMetaCheck.setSelected(refactorConfig.isCleanMeta());

        CheckBox fixTypesCheck = new CheckBox("Fix Resource Types");
        fixTypesCheck.setSelected(refactorConfig.isFixTypes());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));
        grid.add(publicXmlBox, 0, 0, 2, 1);
        grid.add(cleanMetaCheck, 0, 1);
        grid.add(fixTypesCheck, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 150);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                refactorConfig.setPublicXml(publicXmlField.getText());
                refactorConfig.setCleanMeta(cleanMetaCheck.isSelected());
                refactorConfig.setFixTypes(fixTypesCheck.isSelected());
                updateRefactorConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void showProtectOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Protect Configuration");
        dialog.setHeaderText("Configure protection options");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        HBox keepTypeBox = new HBox(10);
        Label keepTypeLabel = new Label("Keep Type:");
        TextField keepTypeField = new TextField(protectConfig.getKeepType());
        keepTypeField.setPromptText("e.g., font (default)");
        keepTypeField.setPrefWidth(150);
        keepTypeBox.getChildren().addAll(keepTypeLabel, keepTypeField);

        CheckBox confuseZipCheck = new CheckBox("Confuse Zip");
        confuseZipCheck.setSelected(protectConfig.isConfuseZip());

        CheckBox skipManifestCheck = new CheckBox("Skip Manifest");
        skipManifestCheck.setSelected(protectConfig.isSkipManifest());

        HBox dicDirBox = new HBox(10);
        Label dicDirLabel = new Label("Dir Names Dict:");
        TextField dicDirField = new TextField(protectConfig.getDicDirNames());
        dicDirField.setPromptText("Text file path");
        dicDirField.setPrefWidth(150);
        dicDirBox.getChildren().addAll(dicDirLabel, dicDirField);

        HBox dicFileBox = new HBox(10);
        Label dicFileLabel = new Label("File Names Dict:");
        TextField dicFileField = new TextField(protectConfig.getDicFileNames());
        dicFileField.setPromptText("Text file path");
        dicFileField.setPrefWidth(150);
        dicFileBox.getChildren().addAll(dicFileLabel, dicFileField);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 25, 15, 15));
        grid.add(keepTypeBox, 0, 0);
        grid.add(confuseZipCheck, 1, 0);
        grid.add(skipManifestCheck, 2, 0);
        grid.add(dicDirBox, 0, 1, 2, 1);
        grid.add(dicFileBox, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(600, 200);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                protectConfig.setKeepType(keepTypeField.getText());
                protectConfig.setConfuseZip(confuseZipCheck.isSelected());
                protectConfig.setSkipManifest(skipManifestCheck.isSelected());
                protectConfig.setDicDirNames(dicDirField.getText());
                protectConfig.setDicFileNames(dicFileField.getText());
                updateProtectConfigSummary(configSummary);

                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    // ========== CONFIG SUMMARY METHODS ==========

    private void updateDecompileConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();
        if (decompileConfig.isDecompileToXml()) {
            summary.append("XML output");
        } else {
            summary.append("JSON output");
        }

        if (decompileConfig.isLoadDex()) {
            summary.append(", Load DEX");
        }

        summary.append(", DEX lib: ").append(decompileConfig.getDexLibrary());

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
    }

    private void updateBuildConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();
        if (buildConfig.isBuildFromXml()) {
            summary.append("XML input");
        } else {
            summary.append("JSON input");
        }

        summary.append(", DEX lib: ").append(buildConfig.getDexLibrary());

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3; -fx-font-weight: bold;");
    }

    private void updateMergeConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();

        if (mergeConfig.getResDir() != null && !mergeConfig.getResDir().isEmpty()) {
            summary.append("Res dir: ").append(mergeConfig.getResDir()).append(", ");
        }

        summary.append("Native libs: ").append(mergeConfig.getExtractNativeLibs());

        if (mergeConfig.isCleanMeta()) summary.append(", Clean META");
        if (mergeConfig.isValidateModules()) summary.append(", Validate modules");
        if (mergeConfig.isVrd()) summary.append(", VRD");

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
    }

    private void updateRefactorConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();

        if (refactorConfig.getPublicXml() != null && !refactorConfig.getPublicXml().isEmpty()) {
            summary.append("Using public.xml, ");
        }

        if (refactorConfig.isCleanMeta()) summary.append("Clean META, ");
        if (refactorConfig.isFixTypes()) summary.append("Fix types");

        if (summary.length() == 0) summary.append("Default settings");

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #9C27B0; -fx-font-weight: bold;");
    }

    private void updateProtectConfigSummary(Label configSummary) {
        StringBuilder summary = new StringBuilder();

        summary.append("Keep type: ").append(protectConfig.getKeepType());

        if (protectConfig.isConfuseZip()) summary.append(", Confuse zip");
        if (protectConfig.isSkipManifest()) summary.append(", Skip manifest");

        configSummary.setText(summary.toString());
        configSummary.setStyle("-fx-font-size: 11px; -fx-text-fill: #F44336; -fx-font-weight: bold;");
    }

    // ========== HELPER METHODS ==========
}