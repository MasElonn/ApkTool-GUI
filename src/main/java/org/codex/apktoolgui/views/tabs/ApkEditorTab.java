package org.codex.apktoolgui.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.codex.apktoolgui.config.*;
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

    public Node createContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        // Create main container with sections
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("root-container");

        // ========== DECOMPILE SECTION ==========
        VBox decompileSection = createDecompileSection();
        decompileSection.getStyleClass().add("card");

        // ========== BUILD SECTION ==========
        VBox buildSection = createBuildSection();
        buildSection.getStyleClass().add("card");

        // ========== MERGE SECTION ==========
        VBox mergeSection = createMergeSection();
        mergeSection.getStyleClass().add("card");

        // ========== REFACTOR SECTION ==========
        VBox refactorSection = createRefactorSection();
        refactorSection.getStyleClass().add("card");

        // ========== PROTECT SECTION ==========
        VBox protectSection = createProtectSection();
        protectSection.getStyleClass().add("card");

        mainBox.getChildren().addAll(
                decompileSection,
                buildSection,
                mergeSection,
                refactorSection,
                protectSection
        );

        scrollPane.setContent(mainBox);
        return scrollPane;
    }

    private VBox createDecompileSection() {
        VBox section = new VBox(15);
        
        Label title = new Label("1. Decompile APK");
        title.getStyleClass().add("card-title");

        // File Selection Grid
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label apkLabel = new Label("APK File");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        Label outputLabel = new Label("Output Dir");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [apkname]_decompiled");
        GridPane.setHgrow(outputPathField, javafx.scene.layout.Priority.ALWAYS);

        Button browseApkButton = new Button("Browse");
        browseApkButton.setOnAction(e -> {
            File file = UiUtils.fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputDirectoryPath(file.getAbsolutePath(), "_decompiled"));
            }
        });

        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> UiUtils.browseDirectory(UiUtils.directoryChooser, outputPathField));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration summary
        Label configSummary = new Label("Default settings (JSON output)");
        configSummary.getStyleClass().add("label-dim");

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = new Button("⚙ Options");
        configButton.setOnAction(e -> showDecompileOptionsDialog(configSummary));

        Button executeButton = new Button("Decompile");
        executeButton.getStyleClass().add("button-primary");
        executeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath.isEmpty()) {
                mainView.showError("Please select an APK file first.");
                return;
            }

            String finalOutput = outputPathField.getText();
            if (finalOutput.isEmpty()) {
                finalOutput = UiUtils.generateOutputDirectoryPath(apkPath, "_decompiled");
            }

            apkEditorService.executeDecompile(
                    apkPath,
                    finalOutput,
                    decompileConfig.isDecompileToXml(),
                    decompileConfig.isLoadDex(),
                    decompileConfig.getDexLibrary()
            );
        });

        buttonBox.getChildren().addAll(executeButton, configButton);

        section.getChildren().addAll(title, fileGrid, configSummary, buttonBox);
        return section;
    }

    private VBox createBuildSection() {
        VBox section = new VBox(15);

        Label title = new Label("2. Build APK");
        title.getStyleClass().add("card-title");

        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label inputLabel = new Label("Project Dir");
        TextField inputPathField = new TextField();
        inputPathField.setPromptText("Select decompiled directory...");
        Label outputLabel = new Label("Output APK");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [project_dir].apk");
        GridPane.setHgrow(outputPathField, javafx.scene.layout.Priority.ALWAYS);

        Button browseInputButton = new Button("Browse");
        browseInputButton.setOnAction(e -> {
            File dir = UiUtils.directoryChooser.showDialog(null);
            if (dir != null) {
                inputPathField.setText(dir.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputFilePath(dir.getAbsolutePath(), ".apk"));
            }
        });

        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(UiUtils.fileChooser, outputPathField, "Save APK", "*.apk", "Select File"));

        fileGrid.add(inputLabel, 0, 0);
        fileGrid.add(inputPathField, 1, 0);
        fileGrid.add(browseInputButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        Label configSummary = new Label("Default settings (JSON input)");
        configSummary.getStyleClass().add("label-dim");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = new Button("⚙ Options");
        configButton.setOnAction(e -> showBuildOptionsDialog(configSummary));

        Button executeButton = new Button("Build APK");
        executeButton.getStyleClass().add("button-primary");
        executeButton.setOnAction(e -> {
            String inputPath = inputPathField.getText();
            if (inputPath.isEmpty()) {
                mainView.showError("Please select a project directory first.");
                return;
            }

            String finalOutput = outputPathField.getText();
            if (finalOutput.isEmpty()) {
                 finalOutput = UiUtils.generateOutputFilePath(inputPath, ".apk");
            }

            apkEditorService.executeBuild(
                    inputPath,
                    finalOutput,
                    buildConfig.isBuildFromXml(),
                    buildConfig.getDexLibrary()
            );
        });

        buttonBox.getChildren().addAll(executeButton, configButton);

        section.getChildren().addAll(title, fileGrid, configSummary, buttonBox);
        return section;
    }

    private VBox createMergeSection() {
        VBox section = new VBox(15);
        
        Label title = new Label("3. Merge Split APKs");
        title.getStyleClass().add("card-title");

        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label inputLabel = new Label("Input Path");
        TextField inputPathField = new TextField();
        inputPathField.setPromptText("Directory or XAPK/APKM/APKS file...");
        Label outputLabel = new Label("Output APK");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [name]_merged.apk");
        GridPane.setHgrow(outputPathField, javafx.scene.layout.Priority.ALWAYS);

        Button browseInputButton = new Button("Browse");
        browseInputButton.setOnAction(e -> {
            File file = UiUtils.directoryChooser.showDialog(null);
            if (file != null) {
                inputPathField.setText(file.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputDirectoryPath(file.getAbsolutePath(), "_merged.apk"));
            }
        });

        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(UiUtils.fileChooser, outputPathField, "Save Merged APK", "*.apk", "Select File"));

        fileGrid.add(inputLabel, 0, 0);
        fileGrid.add(inputPathField, 1, 0);
        fileGrid.add(browseInputButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        Label configSummary = new Label("Default settings");
        configSummary.getStyleClass().add("label-dim");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = new Button("⚙ Options");
        configButton.setOnAction(e -> showMergeOptionsDialog(configSummary));

        Button executeButton = new Button("Merge APKs");
        executeButton.getStyleClass().add("button-primary");
        executeButton.setOnAction(e -> {
            String inputPath = inputPathField.getText();
            if (inputPath.isEmpty()) {
                mainView.showError("Please select input path first.");
                return;
            }

            String finalOutput = outputPathField.getText();
            if (finalOutput.isEmpty()) {
                finalOutput = UiUtils.generateOutputDirectoryPath(inputPath, "_merged.apk");
            }

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

        buttonBox.getChildren().addAll(executeButton, configButton);

        section.getChildren().addAll(title, fileGrid, configSummary, buttonBox);
        return section;
    }

    private VBox createRefactorSection() {
        VBox section = new VBox(15);

        Label title = new Label("4. Refactor Obfuscated Resources");
        title.getStyleClass().add("card-title");

        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label apkLabel = new Label("APK File");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        Label outputLabel = new Label("Output APK");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [apkname]_refactored.apk");
        GridPane.setHgrow(outputPathField, javafx.scene.layout.Priority.ALWAYS);

        Button browseApkButton = new Button("Browse");
        browseApkButton.setOnAction(e -> {
            File file = UiUtils.fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputFilePath(file.getAbsolutePath(), "_refactored"));
            }
        });

        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(UiUtils.fileChooser, outputPathField, "Save Refactored APK", "*.apk", "Select File"));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        Label configSummary = new Label("Default settings");
        configSummary.getStyleClass().add("label-dim");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = new Button("⚙ Options");
        configButton.setOnAction(e -> showRefactorOptionsDialog(configSummary));

        Button executeButton = new Button("Refactor APK");
        executeButton.getStyleClass().add("button-primary");
        executeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath.isEmpty()) {
                mainView.showError("Please select an APK file first.");
                return;
            }

            String finalOutput = outputPathField.getText();
            if (finalOutput.isEmpty()) {
                finalOutput = UiUtils.generateOutputFilePath(apkPath, "_refactored");
            }

            apkEditorService.executeRefactor(
                    apkPath,
                    finalOutput,
                    refactorConfig.getPublicXml(),
                    refactorConfig.isCleanMeta(),
                    false,
                    refactorConfig.isFixTypes()
            );
        });

        buttonBox.getChildren().addAll(executeButton, configButton);

        section.getChildren().addAll(title, fileGrid, configSummary, buttonBox);
        return section;
    }

    private VBox createProtectSection() {
        VBox section = new VBox(15);

        Label title = new Label("5. Protect/Obfuscate APK");
        title.getStyleClass().add("card-title");

        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label apkLabel = new Label("APK File");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        Label outputLabel = new Label("Output APK");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [apkname]_protected.apk");
        GridPane.setHgrow(outputPathField, javafx.scene.layout.Priority.ALWAYS);

        Button browseApkButton = new Button("Browse");
        browseApkButton.setOnAction(e -> {
            File file = UiUtils.fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputFilePath(file.getAbsolutePath(), "_protected"));
            }
        });

        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> UiUtils.browseSaveFile(UiUtils.fileChooser, outputPathField, "Save Protected APK", "*.apk", "Select File"));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);
        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        Label configSummary = new Label("Default settings (keep font resources)");
        configSummary.getStyleClass().add("label-dim");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button configButton = new Button("⚙ Options");
        configButton.setOnAction(e -> showProtectOptionsDialog(configSummary));

        Button executeButton = new Button("Protect APK");
        executeButton.getStyleClass().add("button-primary");
        executeButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath.isEmpty()) {
                mainView.showError("Please select an APK file first.");
                return;
            }

            String finalOutput = outputPathField.getText();
            if (finalOutput.isEmpty()) {
                finalOutput = UiUtils.generateOutputFilePath(apkPath, "_protected");
            }

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

        buttonBox.getChildren().addAll(executeButton, configButton);

        section.getChildren().addAll(title, fileGrid, configSummary, buttonBox);
        return section;
    }

    // ========== DIALOG METHODS (Dialog styling added) ==========

    private void showDecompileOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Decompile Configuration");
        dialog.setHeaderText("Configure decompilation options");
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

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
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

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
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

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
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        HBox publicXmlBox = new HBox(10);
        Label publicXmlLabel = new Label("Public XML:");
        TextField publicXmlField = new TextField(refactorConfig.getPublicXml());
        publicXmlField.setPrefWidth(200);
        Button browsePublicXmlButton = new Button("Browse");
        browsePublicXmlButton.setOnAction(e -> {
            File file = UiUtils.fileChooser.showOpenDialog(null);
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
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

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
    }

    private void updateMergeConfigSummary(Label configSummary) {
        int count = 0;
        if(mergeConfig.isCleanMeta()) count++;
        if(mergeConfig.isValidateModules()) count++;
        if(mergeConfig.isVrd()) count++;
        
        configSummary.setText(count + " options selected");
    }

    private void updateRefactorConfigSummary(Label configSummary) {
        int count = 0;
        if(refactorConfig.isCleanMeta()) count++;
        if(refactorConfig.isFixTypes()) count++;
        
        configSummary.setText(count + " options selected");
    }

    private void updateProtectConfigSummary(Label configSummary) {
        int count = 0;
        if(protectConfig.isConfuseZip()) count++;
        if(protectConfig.isSkipManifest()) count++;
        
        configSummary.setText(count + " options selected (Keep: " + protectConfig.getKeepType() + ")");
    }
}