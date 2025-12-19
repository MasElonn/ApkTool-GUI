package org.codex.apktoolgui.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codex.apktoolgui.services.ApkToolService;
import org.codex.apktoolgui.services.ApkEditorService;
import org.codex.apktoolgui.services.InjectDocService;
import org.codex.apktoolgui.services.ZipAlignService;
import org.codex.apktoolgui.services.LogOutput;
import org.codex.apktoolgui.services.UserNotifier;
import org.codex.apktoolgui.services.StatusHandler;
import org.codex.apktoolgui.services.executor.CommandExecutor;
import org.codex.apktoolgui.utils.UiUtils;
import org.codex.apktoolgui.views.tabs.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import static org.codex.apktoolgui.services.ApkToolService.getApkToolPath;
import org.codex.apktoolgui.services.AdbService;

public class MainView implements LogOutput, UserNotifier, StatusHandler {
    
    // Services
    private ApkToolService apkToolService;
    private ApkEditorService apkEditorService;
    private InjectDocService injectDocService;
    private ZipAlignService zipAlignService;
    private AdbService adbService;
    private CommandExecutor commandExecutor;

    // UI Components
    public TextArea outputArea;
    public ProgressBar progressBar;
    public Label statusLabel;
    
    private BorderPane rootLayout;
    private StackPane contentArea;
    private VBox sidebar;

    // Views (Nodes)
    private Node apkToolView;
    private Node apkEditorView;
    private Node apkInfoView;
    private Node utilitiesView;
    private Node adbView;

    private Stage primaryStage;
    String apktoolPath = getApkToolPath();

    public MainView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void stop() {
        if (commandExecutor != null) {
            commandExecutor.shutdown();
        }
    }

    public void initialize(Stage primaryStage) {
        primaryStage.setTitle("Apktool GUI v2.12.0");

        // Initialize Services & Components
        initializeUIComponents();
        initializeServices();
        initializeFileChoosers();

        // Initialize Views
        apkToolView = new ApkToolTab(this, apkToolService).createContent();
        apkEditorView = new ApkEditorTab(this, apkEditorService).createContent();
        apkInfoView = new ApkInfoTab(this, apkEditorService).createContent();
        utilitiesView = new UtilitiesTab(this, injectDocService, zipAlignService).createContent();
        adbView = new AdbTab(this, adbService).createContent();

        // Create Main Layout
        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("root-container");

        // Sidebar
        sidebar = createSidebar();
        rootLayout.setLeft(sidebar);

        // Content Area
        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_LEFT);
        contentArea.setPadding(new Insets(0));
        rootLayout.setCenter(contentArea);

        // Bottom Panel (Terminal)
        rootLayout.setBottom(createBottomPanel());

        // Set default view
        switchView(apkToolView, (Button) sidebar.getChildren().get(1)); // 0 is label, 1 is first btn

        Scene scene = new Scene(rootLayout, 1000, 700);

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

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Label appTitle = new Label("APKTOOL GUI");
        appTitle.getStyleClass().add("sidebar-title");

        Button apktoolBtn = createSidebarButton("🔨 Apktool", apkToolView);
        Button apkEditorBtn = createSidebarButton("⚡ ApkEditor", apkEditorView);
        Button infoBtn = createSidebarButton("🔍 Info", apkInfoView);
        Button utilsBtn = createSidebarButton("🔧 Utilities", utilitiesView);
        Button adbBtn = createSidebarButton("📱 ADB Ops", adbView);

        sidebar.getChildren().addAll(appTitle, apktoolBtn, apkEditorBtn, infoBtn, utilsBtn, adbBtn);
        
        // Spacer to push bottom items down
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);
        
        // Settings/Theme toggle could go here
        Button themeBtn = createSidebarButton("🌗 Toggle Theme", null);
        themeBtn.setOnAction(e -> {
             // Simple toggle logic for demo, usually needs CSS reload
             UiUtils.switchTheme(!UiUtils.darkMode);
        });
        sidebar.getChildren().add(themeBtn);

        return sidebar;
    }

    private Button createSidebarButton(String text, Node view) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        
        if (view != null) {
            btn.setOnAction(e -> switchView(view, btn));
        }
        return btn;
    }

    private void switchView(Node view, Button activeBtn) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
        
        // Update active state in sidebar
        sidebar.getChildren().forEach(n -> n.getStyleClass().remove("active"));
        activeBtn.getStyleClass().add("active");
    }

    private void initializeUIComponents() {
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(150);
        outputArea.getStyleClass().add("terminal-text-area");

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-text-fill: #8b949e;");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("progress-bar");
    }

    private void initializeServices() {
        commandExecutor = new CommandExecutor(this, this);
        apkToolService = new ApkToolService(this, this, commandExecutor);
        apkEditorService = new ApkEditorService(this, commandExecutor);
        injectDocService = new InjectDocService(this, commandExecutor);
        zipAlignService = new ZipAlignService(commandExecutor);
        adbService = new AdbService(this, commandExecutor);
    }

    public void initializeFileChoosers() {
        UiUtils.fileChooser.setTitle("Select APK File");
        UiUtils.fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("APK Files", "*.apk"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        UiUtils.directoryChooser.setTitle("Select Directory");
    }

    // Interface Implementations
    @Override
    public void append(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text + "\n");
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/org/codex/apktoolgui/dark-theme.css").toExternalForm());
            dialogPane.getStyleClass().add("card");

            alert.showAndWait();
        });
    }

    @Override
    public void setStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    @Override
    public void setProgressVisible(boolean visible) {
        Platform.runLater(() -> progressBar.setVisible(visible));
    }

    @Override
    public void setProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    private VBox createBottomPanel() {
        VBox bottomBox = new VBox(0);
        bottomBox.getStyleClass().add("terminal-drawer");
        
        // Header (Status + Clear)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("terminal-header");
        
        Label terminalTitle = new Label("TERMINAL OUTPUT");
        terminalTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #8b949e;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearBtn = new Button("Clear"); // Icon could go here
        clearBtn.getStyleClass().addAll("button-icon");
        clearBtn.setOnAction(e -> outputArea.clear());
        
        header.getChildren().addAll(terminalTitle, spacer, progressBar, statusLabel, clearBtn);

        bottomBox.getChildren().addAll(header, outputArea);
        return bottomBox;
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
                UiUtils.darkMode = Boolean.parseBoolean(props.getProperty("dark.mode", "true"));
                append("✅ Settings loaded.");
            }
        } catch (Exception e) {
            // Use defaults
        }
    }

    /* Use append() instead of appendOutput */
}