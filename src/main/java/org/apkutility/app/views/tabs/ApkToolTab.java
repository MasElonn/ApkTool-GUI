package org.apkutility.app.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apkutility.app.config.DecompileConfig;
import org.apkutility.app.config.RecompileConfig;
import org.apkutility.app.services.ApkToolService;
import org.apkutility.app.utils.UiUtils;
import org.apkutility.app.views.MainView;

import java.io.File;

public class ApkToolTab {
    private final MainView mainView;
    private final ApkToolService apkToolService;

    // Instance variables
    private final DecompileConfig decompileConfig = new DecompileConfig();
    private final RecompileConfig recompileConfig = new RecompileConfig();

    public ApkToolTab(MainView mainView, ApkToolService apkToolService) {
        this.mainView = mainView;
        this.apkToolService = apkToolService;
    }

    public Node createContent() {
        // Create main container with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.getStyleClass().add("root-container");

        // ========== DECOMPILE CARD ==========
        VBox decompileCard = createDecompileSection();
        decompileCard.getStyleClass().add("card");

        // ========== RECOMPILE CARD ==========
        VBox recompileCard = createRecompileSection();
        recompileCard.getStyleClass().add("card");

        mainBox.getChildren().addAll(decompileCard, recompileCard);
        scrollPane.setContent(mainBox);

        return scrollPane;
    }

    private VBox createDecompileSection() {
        VBox decompileSection = new VBox(15);
        
        Label decodeTitle = new Label("Decompile / Decode");
        decodeTitle.getStyleClass().add("card-title");

        // File Selection Grid
        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label apkLabel = new Label("APK File");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file to decode...");
        apkPathField.setPrefWidth(400);

        Label outputLabel = new Label("Output Dir");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [apkname]_decompiled");

        Button browseApkButton = new Button("Browse");
        browseApkButton.setOnAction(e -> {
            File file = UiUtils.fileChooser.showOpenDialog(null);
            if (file != null) {
                apkPathField.setText(file.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputDirectoryPath(file.getAbsolutePath(), "_decompiled"));
            }
        });
        
        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> {
            File dir = UiUtils.directoryChooser.showDialog(null);
            if (dir != null) {
                outputPathField.setText(dir.getAbsolutePath());
            }
        });

        // Add to grid
        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);

        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        // Configuration Summary
        Label configSummary = new Label("Default options selected");
        configSummary.getStyleClass().add("label-dim");

        // Actions Bar
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button optionsButton = new Button("⚙ Options");
        optionsButton.setOnAction(e -> showDecompileOptionsDialog(configSummary));

        Button actionButton = new Button("Decode APK");
        actionButton.getStyleClass().add("button-primary");
        actionButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();

            if (apkPath == null || apkPath.trim().isEmpty()) {
                mainView.showError("Please select an APK file first.");
                return;
            }

            String outputPath = outputPathField.getText();
            if (outputPath.isEmpty()) {
                outputPath = UiUtils.generateOutputDirectoryPath(apkPath, "_decompiled");
            }

            executeDecompile(apkPath, outputPath);
        });

        actions.getChildren().addAll(actionButton, optionsButton);

        decompileSection.getChildren().addAll(decodeTitle, fileGrid, configSummary, actions);
        return decompileSection;
    }

    private VBox createRecompileSection() {
        VBox recompileSection = new VBox(15);

        Label buildTitle = new Label("Recompile / Build");
        buildTitle.getStyleClass().add("card-title");

        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);

        Label inputLabel = new Label("Project Dir");
        TextField inputPathField = new TextField();
        inputPathField.setPromptText("Select directory to build...");
        inputPathField.setPrefWidth(400);

        Label outputLabel = new Label("Output APK");
        TextField outputPathField = new TextField();
        outputPathField.setPromptText("(Optional) Default: [project_dir].apk");

        Button browseInputButton = new Button("Browse");
        browseInputButton.setOnAction(e -> {
            File dir = UiUtils.directoryChooser.showDialog(null);
            if (dir != null) {
                inputPathField.setText(dir.getAbsolutePath());
                outputPathField.setText(UiUtils.generateOutputFilePath(dir.getAbsolutePath(), ".apk")); 
               
            }
        });

        Button browseOutputButton = new Button("Browse");
        browseOutputButton.setOnAction(e -> {
            UiUtils.fileChooser.setTitle("Save APK");
            File file = UiUtils.fileChooser.showSaveDialog(null);
            if (file != null) {
                outputPathField.setText(file.getAbsolutePath());
            }
            UiUtils.fileChooser.setTitle("Select APK File");
        });

        fileGrid.add(inputLabel, 0, 0);
        fileGrid.add(inputPathField, 1, 0);
        fileGrid.add(browseInputButton, 2, 0);

        fileGrid.add(outputLabel, 0, 1);
        fileGrid.add(outputPathField, 1, 1);
        fileGrid.add(browseOutputButton, 2, 1);

        Label configSummary = new Label("Default options selected");
        configSummary.getStyleClass().add("label-dim");

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button optionsButton = new Button("⚙ Options");
        optionsButton.setOnAction(e -> showRecompileOptionsDialog(configSummary));

        Button actionButton = new Button("Build APK");
        actionButton.getStyleClass().add("button-primary");
        actionButton.setOnAction(e -> {
            String inputPath = inputPathField.getText();

            if (inputPath == null || inputPath.trim().isEmpty()) {
                mainView.showError("Please select a project directory first.");
                return;
            }

            String outputPath = outputPathField.getText();
            if (outputPath.isEmpty()) {
                 outputPath = UiUtils.generateOutputFilePath(inputPath, ".apk");
            }

            executeRecompile(inputPath, outputPath);
        });

        actions.getChildren().addAll(actionButton, optionsButton);

        recompileSection.getChildren().addAll(buildTitle, fileGrid, configSummary, actions);
        return recompileSection;
    }

    // ... Helper methods (Dialogs/Executors) stay largely the same but with improved dialog styling if needed.
    // Minimizing changes to logic, focusing on structure for now.
    
    private void showDecompileOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Decompile Configuration");
        dialog.setHeaderText("Configure decompilation options");
        
        // Add style to dialog
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org.apkutility.app/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        // ... (Keep existing checkbox logic, just layout tweaked)
        CheckBox noResCheck = new CheckBox("No Resources (-r)"); noResCheck.setSelected(decompileConfig.isNoRes());
        CheckBox noSrcCheck = new CheckBox("No Sources (-s)"); noSrcCheck.setSelected(decompileConfig.isNoSrc());
        CheckBox noAssetsCheck = new CheckBox("No Assets"); noAssetsCheck.setSelected(decompileConfig.isNoAssets());
        CheckBox onlyManifestCheck = new CheckBox("Only Manifest"); onlyManifestCheck.setSelected(decompileConfig.isOnlyManifest());
        CheckBox forceCheck = new CheckBox("Force Delete (-f)"); forceCheck.setSelected(decompileConfig.isForce());
        CheckBox noDebugCheck = new CheckBox("No Debug Info (-b)"); noDebugCheck.setSelected(decompileConfig.isNoDebug());
        CheckBox matchOriginalCheck = new CheckBox("Match Original (-m)"); matchOriginalCheck.setSelected(decompileConfig.isMatchOriginal());
        CheckBox keepBrokenCheck = new CheckBox("Keep Broken Resources (-k)"); keepBrokenCheck.setSelected(decompileConfig.isKeepBroken());
        CheckBox onlyMainClassesCheck = new CheckBox("Only Main Classes"); onlyMainClassesCheck.setSelected(decompileConfig.isOnlyMainClasses());

        // Simple Grid for options
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(noResCheck, 0, 0);
        grid.add(noSrcCheck, 0, 1);
        grid.add(noAssetsCheck, 0, 2);
        
        grid.add(forceCheck, 1, 0);
        grid.add(noDebugCheck, 1, 1);
        grid.add(matchOriginalCheck, 1, 2);

        grid.add(keepBrokenCheck, 2, 0);
        grid.add(onlyMainClassesCheck, 2, 1);
        grid.add(onlyManifestCheck, 2, 2);
        
        // Advanced
        TextField apiField = new TextField(decompileConfig.getApiLevel()); apiField.setPromptText("API Level");
        TextField jobsField = new TextField(decompileConfig.getJobs()); jobsField.setPromptText("Jobs");
        TextField frameworkField = new TextField(decompileConfig.getFrameworkPath()); frameworkField.setPromptText("Framework Path");

        grid.add(new Label("Advanced:"), 0, 3);
        grid.add(apiField, 0, 4);
        grid.add(jobsField, 1, 4);
        grid.add(frameworkField, 2, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
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

                updateDecompileConfigSummary(configSummary);
                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void showRecompileOptionsDialog(Label configSummary) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recompile Configuration");
        dialog.setHeaderText("Configure recompilation options");
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org.apkutility.app/dark-theme.css").toExternalForm());

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);

        CheckBox debugCheck = new CheckBox("Debuggable (-d)"); debugCheck.setSelected(recompileConfig.isDebug());
        CheckBox copyOriginalCheck = new CheckBox("Copy Original (-c)"); copyOriginalCheck.setSelected(recompileConfig.isCopyOriginal());
        CheckBox forceCheck = new CheckBox("Force Build (-f)"); forceCheck.setSelected(recompileConfig.isForce());
        CheckBox noApkCheck = new CheckBox("No APK (-na)"); noApkCheck.setSelected(recompileConfig.isNoApk());
        CheckBox noCrunchCheck = new CheckBox("No Crunch (-nc)"); noCrunchCheck.setSelected(recompileConfig.isNoCrunch());
        CheckBox useAapt1Check = new CheckBox("Use AAPT1"); useAapt1Check.setSelected(recompileConfig.isUseAapt1());
        CheckBox netSecCheck = new CheckBox("Net Sec Config (-n)"); netSecCheck.setSelected(recompileConfig.isNetSec());

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(debugCheck, 0, 0);
        grid.add(copyOriginalCheck, 0, 1);
        grid.add(forceCheck, 0, 2);

        grid.add(noApkCheck, 1, 0);
        grid.add(noCrunchCheck, 1, 1);
        grid.add(useAapt1Check, 1, 2);
        grid.add(netSecCheck, 0, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK || dialogButton == ButtonType.APPLY) {
                recompileConfig.setDebug(debugCheck.isSelected());
                recompileConfig.setCopyOriginal(copyOriginalCheck.isSelected());
                recompileConfig.setForce(forceCheck.isSelected());
                recompileConfig.setNoApk(noApkCheck.isSelected());
                recompileConfig.setNoCrunch(noCrunchCheck.isSelected());
                recompileConfig.setUseAapt1(useAapt1Check.isSelected());
                recompileConfig.setNetSec(netSecCheck.isSelected());

                updateRecompileConfigSummary(configSummary);
                if (dialogButton == ButtonType.APPLY) return null;
            }
            return dialogButton;
        });

        dialog.showAndWait();
    }

    private void updateDecompileConfigSummary(Label configSummary) {
        // Simple count summary
        int count = 0;
        if (decompileConfig.isNoRes()) count++;
        if (decompileConfig.isNoSrc()) count++;
        if (decompileConfig.isForce()) count++;
        // ... count others if needed
        configSummary.setText(count > 0 ? count + " options enabled" : "Default options");
    }

    private void updateRecompileConfigSummary(Label configSummary) {
        int count = 0;
        if (recompileConfig.isDebug()) count++;
        if (recompileConfig.isForce()) count++;
        configSummary.setText(count > 0 ? count + " options enabled" : "Default options");
    }

    private void executeDecompile(String apkPath, String outputPath) {
        apkToolService.executeDecode(apkPath, outputPath, 
                decompileConfig.getFrameworkPath(), decompileConfig.getApiLevel(), decompileConfig.getJobs(),
                decompileConfig.isNoRes(), decompileConfig.isNoSrc(), decompileConfig.isNoAssets(), 
                decompileConfig.isOnlyManifest(), decompileConfig.isForce(), decompileConfig.isNoDebug(),
                decompileConfig.isMatchOriginal(), decompileConfig.isKeepBroken(), decompileConfig.isOnlyMainClasses());
    }

    private void executeRecompile(String inputPath, String outputPath) {
        apkToolService.executeBuild(inputPath, outputPath, 
                recompileConfig.getAaptPath(), recompileConfig.getFrameworkPath(),
                recompileConfig.isDebug(), recompileConfig.isCopyOriginal(), recompileConfig.isForce(),
                recompileConfig.isNoApk(), recompileConfig.isNoCrunch(), recompileConfig.isUseAapt1(),
                recompileConfig.isNetSec());
    }
}
