package org.codex.apktoolgui.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.codex.apktoolgui.models.DecompileConfig;
import org.codex.apktoolgui.models.RecompileConfig;
import org.codex.apktoolgui.views.MainView;
import org.codex.apktoolgui.services.ApkToolService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApkToolTab {
    private final MainView mainView;
    private final ApkToolService apkToolService;

    public ApkToolTab(MainView mainView, ApkToolService apkToolService) {
        this.mainView = mainView;
        this.apkToolService = apkToolService;
    }

    public Tab createAPKToolTab() {
        Tab apkToolTab = new Tab("APKTOOL");
        apkToolTab.setClosable(false);
        apkToolTab.setGraphic(mainView.createIcon("🔨"));

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

        Button browseApkButton = mainView.createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> {
            File file = mainView.fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
            }
        });

        Label outputLabel = new Label("Output Directory:");
        outputLabel.getStyleClass().add("dark-label");

        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: apk.out");
        outputPathField.getStyleClass().add("dark-text-field");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> {
            File dir = mainView.directoryChooser.showDialog(null);
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

        Button decompileOptionsButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        decompileOptionsButton.setPrefWidth(180);
        decompileOptionsButton.setOnAction(e -> showDecompileOptionsDialog(decompileConfigSummary));

        Button decodeButton = mainView.createStyledButton("▶ Decompile APK", "large-primary");
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

        Button browseInputButton = mainView.createStyledButton("Browse", "primary");
        browseInputButton.setOnAction(e -> {
            File dir = mainView.directoryChooser.showDialog(null);
            if (dir != null) {
                inputPathField.setText(dir.getAbsolutePath());
            }
        });

        Label outputLabel = new Label("Output APK:");
        outputLabel.getStyleClass().add("dark-label");

        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: dist/name.apk");
        outputPathField.getStyleClass().add("dark-text-field");

        Button browseOutputButton = mainView.createStyledButton("Browse", "secondary");
        browseOutputButton.setOnAction(e -> {
            mainView.fileChooser.setTitle("Save APK");
            File file = mainView.fileChooser.showSaveDialog(null);
            if (file != null) {
                outputPathField.setText(file.getAbsolutePath());
            }
            mainView.fileChooser.setTitle("Select APK File");
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

        Button recompileOptionsButton = mainView.createStyledButton("⚙ Configure Options", "secondary");
        recompileOptionsButton.setPrefWidth(180);
        recompileOptionsButton.setOnAction(e -> showRecompileOptionsDialog(recompileConfigSummary));

        Button buildButton = mainView.createStyledButton("▶ Build APK", "large-primary");
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
    //TODO making the framework as selection
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
            File dir = mainView.directoryChooser.showDialog(null);
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
            File dir = mainView.directoryChooser.showDialog(null);
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
        apkToolService.executeDecode(
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
        apkToolService.executeBuild(
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

}
