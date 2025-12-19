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
            Platform.runLater(() -> {
                if(statusHandler != null) {
                    statusHandler.setProgressVisible(true);
                    statusHandler.setProgress(-1); // Indeterminate
                    statusHandler.setStatus(statusMessage);
                }
                if(logOutput != null) {
                    logOutput.append("> " + String.join(" ", command) + "\n");
                }
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
                        
                        Platform.runLater(() -> {
                            // send to custom consumer if provided
                            if (outputConsumer != null) {
                                outputConsumer.accept(outputLine + "\n");
                                // Also log to main output if consumer is present? 
                                // Original logic said: if (outputConsumer != null) accept else ... wait
                                // Original:
                                // if (outputConsumer != null) accept
                                // MainView.outputArea.appendText (ALWAYS)
                                // Only explicit logOutput if it's not null.
                            }
                            
                            if (logOutput != null) {
                                logOutput.append(outputLine + "\n");
                            }
                        });
                    }
                }

                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    if(statusHandler != null) statusHandler.setProgressVisible(false);
                    
                    if (exitCode == 0) {
                        if(statusHandler != null) statusHandler.setStatus("Command completed successfully");
                        if(logOutput != null) logOutput.append("\n[SUCCESS] Command completed with exit code: " + exitCode + "\n");

                    } else {
                        if(statusHandler != null) statusHandler.setStatus("Command failed with exit code: " + exitCode);
                        if(logOutput != null) logOutput.append("\n[ERROR] Command failed with exit code: " + exitCode + "\n");
                        if (outputConsumer != null) {
                            outputConsumer.accept("\n[ERROR] Command failed with exit code: " + exitCode + "\n");
                        }
                    }
                    if(logOutput != null) logOutput.append("=".repeat(80) + "\n\n");

                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if(statusHandler != null) statusHandler.setProgressVisible(false);
                    if(statusHandler != null) statusHandler.setStatus("Error executing command");
                    if(logOutput != null) logOutput.append("\n[EXCEPTION] " + e.getMessage() + "\n");
                    if (outputConsumer != null) {
                        outputConsumer.accept("\n[EXCEPTION] " + e.getMessage() + "\n");
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}