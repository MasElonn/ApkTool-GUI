package org.codex.apktoolgui.services.executor;

import javafx.application.Platform;
import org.codex.apktoolgui.services.LogOutput;
import org.codex.apktoolgui.services.StatusHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CommandExecutor {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LogOutput logOutput;
    private final StatusHandler statusHandler;

    public CommandExecutor(LogOutput logOutput, StatusHandler statusHandler) {
        this.logOutput = logOutput;
        this.statusHandler = statusHandler;
    }

    public void executeCommand(List<String> command, String statusMessage) {
        executeCommand(command, statusMessage, null);
    }

    public void executeCommand(List<String> command, String statusMessage, Consumer<String> outputConsumer) {
        executor.submit(() -> {
            runOnUi(() -> {
                if (statusHandler != null) {
                    statusHandler.setProgressVisible(true);
                    statusHandler.setProgress(-1);
                    statusHandler.setStatus(statusMessage);
                }
                if (logOutput != null) {
                    logOutput.append("> " + String.join(" ", command) + "\n");
                }
            });

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String outputLine = line;
                        runOnUi(() -> {
                            if (outputConsumer != null) outputConsumer.accept(outputLine + "\n");
                            if (logOutput != null) logOutput.append(outputLine + "\n");
                        });
                    }
                }

                int exitCode = process.waitFor();
                runOnUi(() -> handleCompletion(exitCode, outputConsumer));

            } catch (Exception e) {
                runOnUi(() -> handleError(e, outputConsumer));
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private void handleCompletion(int exitCode, Consumer<String> outputConsumer) {
        if (statusHandler != null) statusHandler.setProgressVisible(false);

        if (exitCode == 0) {
            if (statusHandler != null) statusHandler.setStatus("Command completed successfully");
            if (logOutput != null) logOutput.append("\n[SUCCESS] Command completed with exit code: " + exitCode + "\n");
        } else {
            String error = "\n[ERROR] Command failed with exit code: " + exitCode + "\n";
            if (statusHandler != null) statusHandler.setStatus("Command failed with exit code: " + exitCode);
            if (logOutput != null) logOutput.append(error);
            if (outputConsumer != null) outputConsumer.accept(error);
        }

        if (logOutput != null) logOutput.append("=".repeat(80) + "\n\n");
    }

    private void handleError(Exception e, Consumer<String> outputConsumer) {
        String error = "\n[EXCEPTION] " + e.getMessage() + "\n";
        if (statusHandler != null) {
            statusHandler.setProgressVisible(false);
            statusHandler.setStatus("Error executing command");
        }
        if (logOutput != null) logOutput.append(error);
        if (outputConsumer != null) outputConsumer.accept(error);
        e.printStackTrace();
    }

    private void runOnUi(Runnable action) {
        Platform.runLater(action);
    }
}