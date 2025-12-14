package org.codex.apktoolgui;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;


public class Zipalign {
    private static String zipalignPath = "zipalign";

    public static void alighApk(String apkPath, String outputPath){
        List<String> command = new ArrayList<>();
        command.add(zipalignPath);
        command.add("-v");
        command.add("4");
        command.add(apkPath);
        command.add(outputPath);
        executeCommand(command, "Aligning APK...");
    }


    public static void executeCommand(List<String> command, String statusMessage) {
        ApktoolGUI.executor.submit(() -> {
            Platform.runLater(() -> {
                ApktoolGUI.progressBar.setVisible(true);
                ApktoolGUI.progressBar.setProgress(-1); // Indeterminate
                ApktoolGUI.statusLabel.setText(statusMessage);
                ApktoolGUI.outputArea.appendText("> " + String.join(" ", command) + "\n\n");
            });

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String outputLine = line;
                        Platform.runLater(() -> ApktoolGUI.outputArea.appendText(outputLine + "\n"));
                    }
                }

                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    ApktoolGUI.progressBar.setVisible(false);
                    if (exitCode == 0) {
                        ApktoolGUI.statusLabel.setText("Command completed successfully");
                        ApktoolGUI.outputArea.appendText("\n[SUCCESS] Command completed with exit code: " + exitCode + "\n");
                    } else {
                        ApktoolGUI.statusLabel.setText("Command failed with exit code: " + exitCode);
                        ApktoolGUI.outputArea.appendText("\n[ERROR] Command failed with exit code: " + exitCode + "\n");
                    }
                    ApktoolGUI.outputArea.appendText("=".repeat(80) + "\n\n");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    ApktoolGUI.progressBar.setVisible(false);
                    ApktoolGUI.statusLabel.setText("Error executing command");
                    ApktoolGUI.outputArea.appendText("\n[EXCEPTION] " + e.getMessage() + "\n");
                    e.printStackTrace();
                });
            }
        });
    }
}
