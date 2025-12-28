package org.apkutility.app.views.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apkutility.app.services.AdbService;
import org.apkutility.app.utils.UiUtils;
import org.apkutility.app.views.MainView;

import java.io.File;

public class AdbTab {

    private final MainView mainView;
    private final AdbService adbService;
    private ComboBox<String> deviceCombo;
    private TextArea adbTerminalArea;

    public AdbTab(MainView mainView, AdbService adbService) {
        this.mainView = mainView;
        this.adbService = adbService;
    }

    public Node createContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-container");

        // Use a shared package name field for RE tasks
        TextField sharedPkgField = new TextField();
        sharedPkgField.setPromptText("Package Name (e.g. com.example.app)");

        // --- 1. Wireless Pairing/Connection ---
        root.getChildren().add(createWirelessCard());

        // --- 2. Connected Device Control ---
        root.getChildren().add(createDeviceSelectionCard());

        // --- 3. Install APK ---
        root.getChildren().add(createInstallCard());

        // --- 4. Reverse Engineering / Package Ops ---
        root.getChildren().add(createPackageOpsCard(sharedPkgField));

        // --- 5. ADB Terminal ---
        root.getChildren().add(createTerminalCard());

        scrollPane.setContent(root);
        
        // Initial Refresh
        refreshDevices();

        return scrollPane;
    }

    private VBox createWirelessCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Wireless Debugging");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField ipField = new TextField(); ipField.setPromptText("IP Address");
        TextField portField = new TextField(); portField.setPromptText("Port");
        TextField codeField = new TextField(); codeField.setPromptText("Pairing Code");
        
        Button pairBtn = new Button("Pair");
        pairBtn.setOnAction(e -> adbService.pair(ipField.getText(), portField.getText(), codeField.getText()));

        Button connectBtn = new Button("Connect");
        connectBtn.setOnAction(e -> adbService.connect(ipField.getText(), portField.getText()));

        grid.add(new Label("IP:"), 0, 0); grid.add(ipField, 1, 0);
        grid.add(new Label("Port:"), 2, 0); grid.add(portField, 3, 0);
        grid.add(new Label("Code:"), 0, 1); grid.add(codeField, 1, 1);
        
        HBox actions = new HBox(10, pairBtn, connectBtn);
        grid.add(actions, 1, 2, 3, 1);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createDeviceSelectionCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Selected Device");
        title.getStyleClass().add("card-title");

        HBox box = new HBox(10);
        deviceCombo = new ComboBox<>();
        deviceCombo.setPromptText("Select Device");
        deviceCombo.setPrefWidth(250);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshDevices());

        box.getChildren().addAll(deviceCombo, refreshBtn);
        card.getChildren().addAll(title, box);
        return card;
    }

    private VBox createInstallCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Install APK");
        title.getStyleClass().add("card-title");

        HBox box = new HBox(10);
        TextField apkField = new TextField();
        apkField.setPromptText("Select APK...");
        HBox.setHgrow(apkField, Priority.ALWAYS);
        
        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> UiUtils.browseFile(UiUtils.fileChooser, apkField, "Select APK", "*.apk", "APK Files"));
        
        Button installBtn = new Button("Install");
        installBtn.getStyleClass().add("button-primary");
        installBtn.setOnAction(e -> {
            String dev = deviceCombo.getValue();
            if (dev != null) adbService.install(dev, apkField.getText());
            else mainView.showError("Select a device");
        });

        box.getChildren().addAll(apkField, browseBtn, installBtn);
        card.getChildren().addAll(title, box);
        return card;
    }

    private VBox createPackageOpsCard(TextField pkgField) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Package Operations");
        title.getStyleClass().add("card-title");

        HBox pkgBox = new HBox(10);
        pkgBox.getChildren().addAll(new Label("Package:"), pkgField);
        HBox.setHgrow(pkgField, Priority.ALWAYS);

        // Grid of actions
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        Button pullBtn = new Button("Pull APK");
        pullBtn.setOnAction(e -> {
            String dev = deviceCombo.getValue();
            if (dev == null) { mainView.showError("Select a device"); return; }
            if (pkgField.getText().isEmpty()) { mainView.showError("Enter package name"); return; }
            
            File dest = UiUtils.directoryChooser.showDialog(null);
            if (dest != null) {
                adbService.pullApk(dev, pkgField.getText(), dest.getAbsolutePath());
            }
        });

        Button dumpBtn = new Button("Dump Info");
        dumpBtn.setOnAction(e -> {
            String dev = deviceCombo.getValue();
            if (dev != null) adbService.dumpPackage(dev, pkgField.getText(), this::appendTerminal);
            else mainView.showError("Select a device");
        });

        Button uninstallBtn = new Button("Uninstall");
        uninstallBtn.setStyle("-fx-base: #e74c3c;");
        uninstallBtn.setOnAction(e -> {
            String dev = deviceCombo.getValue();
            if (dev != null) adbService.uninstall(dev, pkgField.getText());
            else mainView.showError("Select a device");
        });

        grid.add(pullBtn, 0, 0);
        grid.add(dumpBtn, 1, 0);
        grid.add(uninstallBtn, 2, 0);

        card.getChildren().addAll(title, pkgBox, grid);
        return card;
    }

    private VBox createTerminalCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("ADB Terminal");
        title.getStyleClass().add("card-title");

        adbTerminalArea = new TextArea();
        adbTerminalArea.setEditable(false);
        adbTerminalArea.setPrefHeight(200);
        adbTerminalArea.getStyleClass().add("terminal-text-area");

        TextField cmdField = new TextField();
        cmdField.setPromptText("Enter ADB shell command (e.g. pm list packages)...");
        cmdField.setOnAction(e -> {
            String cmd = cmdField.getText();
            if (cmd.isEmpty()) return;
            String dev = deviceCombo.getValue();
            
            appendTerminal("> adb -s " + (dev==null?"?":dev) + " shell " + cmd);
            cmdField.clear();
            
            if (dev != null) {
                adbService.executeShellCommand(dev, cmd, this::appendTerminal);
            } else {
                appendTerminal("Error: No device selected.");
            }
        });

        card.getChildren().addAll(title, adbTerminalArea, cmdField);
        return card;
    }

    private void appendTerminal(String text) {
        Platform.runLater(() -> {
            adbTerminalArea.appendText(text + "\n");
            adbTerminalArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void refreshDevices() {
        adbService.getConnectedDevices(devices -> {
            Platform.runLater(() -> {
                String current = deviceCombo.getValue();
                deviceCombo.getItems().clear();
                deviceCombo.getItems().addAll(devices);
                if (current != null && devices.contains(current)) {
                    deviceCombo.setValue(current);
                } else if (!devices.isEmpty()) {
                    deviceCombo.getSelectionModel().select(0);
                }
            });
        });
    }
}
