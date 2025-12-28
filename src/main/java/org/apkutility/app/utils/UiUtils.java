package org.apkutility.app.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Map;



public final class UiUtils {
    public static boolean darkMode = true;

    public static final FileChooser fileChooser = new FileChooser();
    public static final DirectoryChooser directoryChooser = new DirectoryChooser();

    private UiUtils() {
    }

    public static Node createHeaderCard(ApkInfoParser.ApkInfo info) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: -fx-control-inner-background; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        card.setAlignment(Pos.CENTER_LEFT);

        // Icon Placeholder (Visual only)
        Label iconLabel = new Label("APK");
        iconLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 10;");
        iconLabel.setMinWidth(64);
        iconLabel.setMinHeight(64);
        iconLabel.setAlignment(Pos.CENTER);

        VBox details = new VBox(5);
        Label appName = new Label(info.generalInfo.getOrDefault("AppName", "Unknown App"));
        appName.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label pkgName = new Label(info.generalInfo.getOrDefault("package", "Unknown Package"));
        pkgName.setStyle("-fx-text-fill: derive(-fx-text-base-color, 30%); -fx-font-size: 14px;");

        String verName = info.generalInfo.getOrDefault("VersionName", "?");
        String verCode = info.generalInfo.getOrDefault("VersionCode", "?");
        Label version = new Label("v" + verName + " (" + verCode + ")");
        version.setStyle("-fx-text-fill: derive(-fx-text-base-color, 30%);");

        details.getChildren().addAll(appName, pkgName, version);
        card.getChildren().addAll(iconLabel, details);
        return card;
    }

    public static Node createKeyValueGrid(Map<String, String> data) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: -fx-control-inner-background; -fx-padding: 15; -fx-background-radius: 5;");

        int row = 0;
        int col = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            // Skip header fields we already showed
            if (entry.getKey().matches("AppName|package|VersionName|VersionCode")) continue;

            VBox field = new VBox(2);
            Label key = new Label(formatKey(entry.getKey()));
            key.setStyle("-fx-font-size: 10px; -fx-text-fill: derive(-fx-text-base-color, 40%); -fx-font-weight: bold;");

            Label val = new Label(entry.getValue());
            val.setWrapText(true);
            val.setStyle("-fx-font-size: 13px;");

            field.getChildren().addAll(key, val);

            grid.add(field, col, row);

            col++;
            if (col > 2) { // 3 columns
                col = 0;
                row++;
            }
        }
        return grid;
    }

    public static Node createExpandableListSection(String title, java.util.List<String> items) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setExpanded(false);

        ListView<String> list = new ListView<>();
        list.getItems().addAll(items);
        list.setPrefHeight(200);

        pane.setContent(list);
        return pane;
    }

    public static Node createChipView(java.util.List<String> items) {
        FlowPane flow = new FlowPane();
        flow.setHgap(10);
        flow.setVgap(10);

        for (String item : items) {
            Label chip = new Label(item);
            chip.setStyle("-fx-background-color: derive(-fx-base, -10%); -fx-padding: 5 10 5 10; -fx-background-radius: 15; -fx-font-size: 11px;");
            flow.getChildren().add(chip);
        }
        return flow;
    }

    public static Node createCodeBlock(String content) {
        TextArea area = new TextArea(content);
        area.setFont(Font.font("Monospaced", 12));
        area.setEditable(false);
        area.setPrefRowCount(10);
        return area;
    }

    public static Node createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        return label;
    }
    private static String formatKey(String rawKey) {
        // Splits CamelCase or hyphen-case to nicer text
        return rawKey.replaceAll("([a-z])([A-Z])", "$1 $2").replace("-", " ").toUpperCase();
    }

    public static void browseFile(FileChooser fileChooser, TextField field, String title, String extension, String resetTitle) {
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Files", extension)
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            field.setText(file.getAbsolutePath());
        }
        fileChooser.setTitle(resetTitle);
    }

    public static void browseSaveFile(FileChooser fileChooser, TextField field, String title, String extension, String resetTitle) {
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Files", extension)
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            field.setText(file.getAbsolutePath());
        }
        fileChooser.setTitle(resetTitle);
    }

    public static void browseDirectory(DirectoryChooser directoryChooser, TextField field) {
        File dir = directoryChooser.showDialog(null);
        if (dir != null) {
            field.setText(dir.getAbsolutePath());
        }
    }

    public static String generateOutputFilePath(String inputPath, String suffix) {
        File inputFile = new File(inputPath);
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? name : name.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : name.substring(dotIndex);
        return inputFile.getParent() + File.separator + baseName + suffix + extension;
    }

    public static String generateOutputDirectoryPath(String inputPath, String suffix) {
        File inputFile = new File(inputPath);
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? name : name.substring(0, dotIndex);
        return inputFile.getParent() + File.separator + baseName + suffix;
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static Button createStyledButton(String text, String style) {
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

    public static Label createIcon(String emoji) {
        Label icon = new Label(emoji);
        icon.getStyleClass().add("icon");
        return icon;
    }

    // Theme Switching
    public static void switchTheme(Scene scene, boolean dark) {
        darkMode = dark;
        if (scene == null) return;
        
        scene.getStylesheets().clear();
        String theme = dark ? "dark-theme.css" : "light-theme.css";
        String cssPath = "/org/apkutility/app/" + theme;
        
        java.net.URL resource = UiUtils.class.getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("Theme not found: " + cssPath);
        }
        
        // Save settings
        try {
            org.apkutility.app.services.SettingsManager.getInstance().getSettings().setDarkMode(dark);
            org.apkutility.app.services.SettingsManager.getInstance().saveSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
