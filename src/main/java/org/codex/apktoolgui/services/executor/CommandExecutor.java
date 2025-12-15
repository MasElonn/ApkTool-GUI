package org.codex.apktoolgui.services.executor;

import javafx.application.Platform;
import org.codex.apktoolgui.views.MainView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static void executeCommand(List<String> command, String statusMessage) {
        executor.submit(() -> {
            Platform.runLater(() -> {
                MainView.progressBar.setVisible(true);
                MainView.progressBar.setProgress(-1); // Indeterminate
                MainView.statusLabel.setText(statusMessage);
                MainView.outputArea.appendText("> " + String.join(" ", command) + "\n\n");
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
                        Platform.runLater(() -> MainView.outputArea.appendText(outputLine + "\n"));
                    }
                }

                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    MainView.progressBar.setVisible(false);
                    if (exitCode == 0) {
                        MainView.statusLabel.setText("Command completed successfully");
                        MainView.outputArea.appendText("\n[SUCCESS] Command completed with exit code: " + exitCode + "\n");
                    } else {
                        MainView.statusLabel.setText("Command failed with exit code: " + exitCode);
                        MainView.outputArea.appendText("\n[ERROR] Command failed with exit code: " + exitCode + "\n");
                    }
                    MainView.outputArea.appendText("=".repeat(80) + "\n\n");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    MainView.progressBar.setVisible(false);
                    MainView.statusLabel.setText("Error executing command");
                    MainView.outputArea.appendText("\n[EXCEPTION] " + e.getMessage() + "\n");
                    e.printStackTrace();
                });
            }
        });
    }

    public static void shutdown() {
        executor.shutdownNow();
    }
}
