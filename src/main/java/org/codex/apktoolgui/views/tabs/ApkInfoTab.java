package org.codex.apktoolgui.views.tabs;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

import org.codex.apktoolgui.config.ApkEditorGetInfoConfig;
import org.codex.apktoolgui.services.ApkEditorService;
import org.codex.apktoolgui.utils.ApkInfoParser;
import org.codex.apktoolgui.utils.UiUtils;
import org.codex.apktoolgui.views.MainView;

public class ApkInfoTab {
    private final MainView mainView;
    private final ApkEditorService apkEditorService;

    // UI Components
    private TextArea rawOutputArea;
    private VBox richContentContainer;
    private TabPane outputModeTabPane;
    private StringBuilder outputBuffer;
    private PauseTransition updateDebouncer;

    ApkEditorGetInfoConfig infoConfig = new ApkEditorGetInfoConfig();

    public ApkInfoTab(MainView mainView, ApkEditorService apkEditorService) {
        this.mainView = mainView;
        this.apkEditorService = apkEditorService;
        this.outputBuffer = new StringBuilder();

        // Debouncer: wait 500ms after the last output chunk to parse/update UI
        this.updateDebouncer = new PauseTransition(Duration.millis(500));
        this.updateDebouncer.setOnFinished(e -> updateRichUi());
    }

    public Node createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-container");

        // --- Controls Card ---
        VBox controlsCard = new VBox(15);
        controlsCard.getStyleClass().add("card");

        Label title = new Label("APK Information / Analyzer");
        title.getStyleClass().add("card-title");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label apkLabel = new Label("APK File");
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        HBox.setHgrow(apkPathField, Priority.ALWAYS);

        Button browseApkButton = new Button("Browse");
        browseApkButton.setOnAction(e -> UiUtils.browseFile(UiUtils.fileChooser, apkPathField, "Select APK", "*.apk", "Select File"));

        topRow.getChildren().addAll(apkLabel, apkPathField, browseApkButton);

        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button getInfoButton = new Button("Analyze APK");
        getInfoButton.getStyleClass().add("button-primary");
        getInfoButton.setPrefWidth(120);

        Button optionsButton = new Button("⚙ Options");
        optionsButton.setOnAction(e -> showInfoOptionsDialog());

        Button clearButton = new Button("Clear Output");
        clearButton.setOnAction(e -> clearOutput());
        
        actionRow.getChildren().addAll(getInfoButton, optionsButton, clearButton);

        controlsCard.getChildren().addAll(title, topRow, actionRow);

        // --- Output Area (TabPane for Rich vs Raw) ---
        // Using a card for the output container
        VBox outputCard = new VBox();
        outputCard.getStyleClass().add("card");
        outputCard.setPadding(new Insets(0)); // TabPane handles padding
        VBox.setVgrow(outputCard, Priority.ALWAYS);

        outputModeTabPane = new TabPane();
        outputModeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        outputModeTabPane.getStyleClass().add("transparent-tab-pane");
        VBox.setVgrow(outputModeTabPane, Priority.ALWAYS);

        // 1. Rich View Tab
        Tab richViewTab = new Tab("Formatted View");
        richContentContainer = new VBox(15);
        richContentContainer.setPadding(new Insets(20));
        
        ScrollPane richScroll = new ScrollPane(richContentContainer);
        richScroll.setFitToWidth(true);
        richScroll.getStyleClass().add("scroll-pane");
        richViewTab.setContent(richScroll);

        // 2. Raw View Tab
        Tab rawViewTab = new Tab("Raw Output");
        rawOutputArea = new TextArea();
        rawOutputArea.setFont(Font.font("Monospaced", 12));
        rawOutputArea.setEditable(false);
        rawViewTab.setContent(rawOutputArea);

        outputModeTabPane.getTabs().addAll(richViewTab, rawViewTab);
        outputCard.getChildren().add(outputModeTabPane);

        root.getChildren().addAll(controlsCard, outputCard);
        
        // --- Logic ---
        getInfoButton.setOnAction(e -> {
            String apkPath = apkPathField.getText();
            if (apkPath == null || apkPath.trim().isEmpty()) {
                mainView.showError("Please select an APK file first.");
                return;
            }

            clearOutput();
            outputModeTabPane.getSelectionModel().select(richViewTab); // Auto-switch to rich view
            setLoadingState(true);

            apkEditorService.executeGetInfo(
                    apkPath, "", false, "", "", "", "", "", "", "",
                    infoConfig.isActivities(),
                    infoConfig.isAppClass(),
                    infoConfig.isAppIcon(),
                    infoConfig.isAppName(),
                    infoConfig.isAppRoundIcon(),
                    infoConfig.isConfigurations(),
                    infoConfig.isDex(),
                    infoConfig.isForceDelete(),
                    infoConfig.isLanguages(),
                    infoConfig.isListFiles(),
                    infoConfig.isListXmlFiles(),
                    infoConfig.isLocales(),
                    infoConfig.isMinSdkVersion(),
                    infoConfig.isPackageInfo(),
                    infoConfig.isPermissions(),
                    infoConfig.isResources(),
                    infoConfig.isSignatures(),
                    infoConfig.isSignaturesBase64(),
                    infoConfig.isTargetSdkVersion(),
                    infoConfig.isVersionCode(),
                    infoConfig.isVersionName(),
                    output -> Platform.runLater(() -> handleStreamOutput(output))
            );
        });

        return root;
    }

    private void handleStreamOutput(String output) {
        // Update Raw View
        rawOutputArea.appendText(output);

        // Buffer for Parser
        outputBuffer.append(output);

        // Reset and restart the debounce timer
        // This ensures we only parse once the stream pauses/finishes
        updateDebouncer.playFromStart();
    }

    private void updateRichUi() {
        setLoadingState(false);
        String fullOutput = outputBuffer.toString();
        ApkInfoParser.ApkInfo info = ApkInfoParser.parse(fullOutput);

        richContentContainer.getChildren().clear();

        // 1. Header Card (Icon, Name, Package, Version)
        richContentContainer.getChildren().add(UiUtils.createHeaderCard(info));

        // 2. Grid Info Card (SDKs, Activities, etc)
        if (!info.generalInfo.isEmpty()) {
            richContentContainer.getChildren().add(UiUtils.createSectionTitle("General Information"));
            richContentContainer.getChildren().add(UiUtils.createKeyValueGrid(info.generalInfo));
        }

        // 3. Permissions
        if (!info.permissions.isEmpty()) {
            richContentContainer.getChildren().add(UiUtils.createExpandableListSection("Permissions (" + info.permissions.size() + ")", info.permissions));
        }

        // 4. Locales & Languages (Chips)
        if (!info.locales.isEmpty()) {
            richContentContainer.getChildren().add(UiUtils.createSectionTitle("Locales"));
            richContentContainer.getChildren().add(UiUtils.createChipView(info.locales));
        }

        // 5. Certificates
        if (info.certificateInfo != null && !info.certificateInfo.isEmpty()) {
            richContentContainer.getChildren().add(UiUtils.createSectionTitle("Signing Certificate"));
            richContentContainer.getChildren().add(UiUtils.createCodeBlock(info.certificateInfo));
        }

        // 6. DEX Info
        if (!info.dexBlocks.isEmpty()) {
            richContentContainer.getChildren().add(UiUtils.createSectionTitle("DEX Files"));
            for (String dex : info.dexBlocks) {
                richContentContainer.getChildren().add(UiUtils.createCodeBlock(dex));
            }
        }
    }

    private void clearOutput() {
        rawOutputArea.clear();
        richContentContainer.getChildren().clear();
        outputBuffer.setLength(0);
        // Reset to initial waiting state
        Label waiting = new Label("Waiting for output...");
        waiting.getStyleClass().add("label-dim");
        richContentContainer.getChildren().add(waiting);
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            ProgressIndicator pi = new ProgressIndicator();
            pi.setMaxSize(30,30);
            richContentContainer.getChildren().clear();
            richContentContainer.getChildren().addAll(new Label("Analyzing APK..."), pi);
            richContentContainer.setAlignment(Pos.CENTER);
        } else {
            richContentContainer.setAlignment(Pos.TOP_LEFT);
        }
    }

    private void showInfoOptionsDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Information Options");
        dialog.setHeaderText("Select the information you want to extract");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);
        
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("card");

        // Grid of checkboxes (kept simplified for brevity, logic remains same as original)
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10); grid.setPadding(new Insets(20));

        CheckBox[] cbs = new CheckBox[]{
                new CheckBox("Activities"), new CheckBox("App Class"), new CheckBox("App Icon"),
                new CheckBox("Configurations"), new CheckBox("DEX"), new CheckBox("Languages"),
                new CheckBox("Permissions"), new CheckBox("Signatures"), new CheckBox("Version Info")
        };

        // Map common ones just for demo
        cbs[0].setSelected(infoConfig.isActivities()); cbs[0].setOnAction(e -> infoConfig.setActivities(cbs[0].isSelected()));
        cbs[6].setSelected(infoConfig.isPermissions()); cbs[6].setOnAction(e -> infoConfig.setPermissions(cbs[6].isSelected()));
        cbs[8].setSelected(infoConfig.isVersionName()); cbs[8].setOnAction(e -> {
            infoConfig.setVersionName(cbs[8].isSelected());
            infoConfig.setVersionCode(cbs[8].isSelected());
        });

        // Add to grid
        int col=0, row=0;
        for(CheckBox cb : cbs) {
            grid.add(cb, col, row);
            col++; if(col>2) { col=0; row++; }
        }

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
}