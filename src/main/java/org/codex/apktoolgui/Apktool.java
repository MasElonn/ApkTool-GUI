package org.codex.apktoolgui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class Apktool {
    private  final Logger LOGGER = Logger.getLogger(Apktool.class.getName());
    private static String apktoolPath= "apktool";

    // Command Execution Methods (same as before, but with dark theme styling)
    public static void executeDecode(String apkPath, String outputPath, String frameworkPath,
                               String apiLevel, String jobs, boolean noRes, boolean noSrc,
                               boolean noAssets, boolean onlyManifest, boolean force,
                               boolean noDebug, boolean matchOriginal, boolean keepBroken,
                               boolean onlyMainClasses) {
        if (apkPath == null || apkPath.trim().isEmpty()) {
            ApktoolGUI.showError("Please select an APK file to decode.");
            return;
        }

        List<String> command = buildDecodeCommand(apkPath, outputPath, frameworkPath,
                apiLevel, jobs, noRes, noSrc, noAssets, onlyManifest, force,
                noDebug, matchOriginal, keepBroken, onlyMainClasses);

        executeCommand(command, "Decoding APK...");
    }

    private static List<String> buildDecodeCommand(String apkPath, String outputPath, String frameworkPath,
                                            String apiLevel, String jobs, boolean noRes, boolean noSrc,
                                            boolean noAssets, boolean onlyManifest, boolean force,
                                            boolean noDebug, boolean matchOriginal, boolean keepBroken,
                                            boolean onlyMainClasses) {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("d");

        if (outputPath != null && !outputPath.trim().isEmpty()) {
            command.add("-o");
            command.add(outputPath);
        }

        if (frameworkPath != null && !frameworkPath.trim().isEmpty()) {
            command.add("-p");
            command.add(frameworkPath);
        }

        if (apiLevel != null && !apiLevel.trim().isEmpty()) {
            command.add("--api-level");
            command.add(apiLevel);
        }

        if (jobs != null && !jobs.trim().isEmpty()) {
            command.add("-j");
            command.add(jobs);
        }

        if (force) command.add("-f");
        if (noRes) command.add("-r");
        if (noSrc) command.add("-s");
        if (noAssets) command.add("--no-assets");
        if (onlyManifest) command.add("--only-manifest");
        if (noDebug) command.add("-b");
        if (matchOriginal) command.add("-m");
        if (keepBroken) command.add("-k");
        if (onlyMainClasses) command.add("--only-main-classes");

        command.add(apkPath);
        return command;
    }


    public static void executeBuild(String inputDir, String outputPath, String aaptPath,
                              String frameworkPath, boolean debug, boolean copyOriginal,
                              boolean force, boolean noApk, boolean noCrunch,
                              boolean useAapt1, boolean netSec) {
        if (inputDir == null || inputDir.trim().isEmpty()) {
            ApktoolGUI.showError("Please select a project directory to build.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("b");

        if (outputPath != null && !outputPath.trim().isEmpty()) {
            command.add("-o");
            command.add(outputPath);
        }

        if (aaptPath != null && !aaptPath.trim().isEmpty()) {
            command.add("-a");
            command.add(aaptPath);
        }

        if (frameworkPath != null && !frameworkPath.trim().isEmpty()) {
            command.add("-p");
            command.add(frameworkPath);
        }

        if (debug) command.add("-d");
        if (copyOriginal) command.add("-c");
        if (force) command.add("-f");
        if (noApk) command.add("-na");
        if (noCrunch) command.add("-nc");
        if (useAapt1) command.add("--use-aapt1");
        if (netSec) command.add("-n");

        command.add(inputDir);

        executeCommand(command, "Building APK...");
    }

    public static void executeInstallFramework(String frameworkApk, String tag) {
        if (frameworkApk == null || frameworkApk.trim().isEmpty()) {
            ApktoolGUI.showError("Please select a framework APK file.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("if");

        if (tag != null && !tag.trim().isEmpty()) {
            command.add("-t");
            command.add(tag);
        }

        command.add(frameworkApk);

        executeCommand(command, "Installing framework...");
    }

    public static void executeListFrameworks() {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("lf");

        executeCommand(command, "Listing frameworks...");
    }

    public static void executeEmptyFrameworkDir() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Empty Framework Directory");
        confirm.setContentText("This will delete ALL framework files. Are you sure?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(apktoolPath);
            command.add("efd");
            command.add("-f");

            executeCommand(command, "Emptying framework directory...");
        }
    }

    public static void executePublicizeResources(String arscPath) {
        if (arscPath == null || arscPath.trim().isEmpty()) {
            ApktoolGUI.showError("Please select an ARSC file.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("pr");
        command.add(arscPath);

        executeCommand(command, "Publicizing resources...");
    }

    public static void executeVersionCheck() {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("v");

        executeCommand(command, "Checking version...");
    }

    public static void executeHelp() {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(apktoolPath);
        command.add("h");

        executeCommand(command, "Showing help...");
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


    // Utility Methods
    public static void checkApktoolAvailability() {
        File apktoolFile = new File(apktoolPath);
        if (apktoolFile.exists()) {
            ApktoolGUI.appendOutput("✅ Apktool found at: " + apktoolPath);
        } else {
            try {
                Process process = new ProcessBuilder("apktool", "version").start();
                if (process.waitFor() == 0) {
                    apktoolPath = "apktool";
                    ApktoolGUI.appendOutput("✅ Apktool found in system PATH");
                } else {
                    ApktoolGUI.showError("❌ Apktool not found! Please download apktool.jar and set the path in Settings.");
                }
            } catch (Exception e) {
                ApktoolGUI.showError("❌ Apktool not found! Please download apktool.jar and set the path in Settings.");
            }
        }
    }

    public static void saveSettings(String ApktoolPath, String defaultDir) {
        ApktoolPath = apktoolPath;

        try {
            Properties props = new Properties();
            props.setProperty("apktool.path", apktoolPath);
            props.setProperty("default.dir", defaultDir);
            props.setProperty("dark.mode", String.valueOf(ApktoolGUI.darkMode));

            Path configPath = Path.of(System.getProperty("user.home"), ".apktool-gui.properties");
            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "Apktool GUI Settings");
            }

            ApktoolGUI.appendOutput("✅ Settings saved to: " + configPath);
        } catch (Exception e) {
            ApktoolGUI.showError("Failed to save settings: " + e.getMessage());
        }
    }
}
