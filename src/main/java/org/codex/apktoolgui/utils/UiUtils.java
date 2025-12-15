package org.codex.apktoolgui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

public final class UiUtils {
    private UiUtils() {
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

    public static String getDefaultOutputPath(String inputPath, String suffix) {
        File inputFile = new File(inputPath);
        String name = inputFile.getName();
        String baseName = name.substring(0, name.lastIndexOf('.'));
        String extension = name.substring(name.lastIndexOf('.'));
        return inputFile.getParent() + File.separator + baseName + suffix + extension;
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
