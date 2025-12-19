package org.codex.apktoolgui.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.codex.apktoolgui.utils.UiUtils;
import org.codex.apktoolgui.services.executor.CommandExecutor;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApkToolService {

    private final LogOutput logOutput;
    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public ApkToolService(LogOutput logOutput, UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.logOutput = logOutput;
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getApkToolPath(){
        File apktoolPath = new File("lib/apktool.jar");
        if(apktoolPath.exists()){
            return apktoolPath.getAbsolutePath();
        }
        return "";
    }

    public void executeDecode(String apkPath, String outputPath, String frameworkPath,
                              String apiLevel, String jobs, boolean noRes, boolean noSrc,
                              boolean noAssets, boolean onlyManifest, boolean force,
                              boolean noDebug, boolean matchOriginal, boolean keepBroken,
                              boolean onlyMainClasses) {
        if (apkPath == null || apkPath.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to decode.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
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

        commandExecutor.executeCommand(command, "Decoding APK...");
    }


    public void executeBuild(String inputDir, String outputPath, String aaptPath,
                             String frameworkPath, boolean debug, boolean copyOriginal,
                             boolean force, boolean noApk, boolean noCrunch,
                             boolean useAapt1, boolean netSec) {
        if (inputDir == null || inputDir.trim().isEmpty()) {
            userNotifier.showError("Please select a project directory to build.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
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

        commandExecutor.executeCommand(command, "Building APK...");
    }

    public void executeInstallFramework(String frameworkApk, String tag) {
        if (frameworkApk == null || frameworkApk.trim().isEmpty()) {
            userNotifier.showError("Please select a framework APK file.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
        command.add("if");

        if (tag != null && !tag.trim().isEmpty()) {
            command.add("-t");
            command.add(tag);
        }

        command.add(frameworkApk);

        commandExecutor.executeCommand(command, "Installing framework...");
    }

    public void executeListFrameworks() {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
        command.add("lf");

        commandExecutor.executeCommand(command, "Listing frameworks...");
    }

    public void executeEmptyFrameworkDir() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Empty Framework Directory");
        confirm.setContentText("This will delete ALL framework files. Are you sure?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(getApkToolPath());
            command.add("efd");
            command.add("-f");

            commandExecutor.executeCommand(command, "Emptying framework directory...");
        }
    }

    public  void executePublicizeResources(String arscPath) {
        if (arscPath == null || arscPath.trim().isEmpty()) {
            userNotifier.showError("Please select an ARSC file.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
        command.add("pr");
        command.add(arscPath);

        commandExecutor.executeCommand(command, "Publicizing resources...");
    }

    public void executeVersionCheck() {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
        command.add("v");

        commandExecutor.executeCommand(command, "Checking version...");
    }

    public void executeHelp() {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkToolPath());
        command.add("h");

        commandExecutor.executeCommand(command, "Showing help...");
    }

    // Utility Methods
    public void checkApktoolAvailability() {
        File apktoolFile = new File(getApkToolPath());
        if (apktoolFile.exists()) {
            logOutput.append("✅ Apktool found at: " + getApkToolPath());
        } else {
            try {
                Process process = new ProcessBuilder("apktool", "version").start();
                if (process.waitFor() == 0) {
                    // This seems redundant or just checking if it runs? 
                    // Keeping original logic structure basically.
                    getApkToolPath().isEmpty(); 
                    logOutput.append("✅ Apktool found in system PATH");
                } else {
                    userNotifier.showError("❌ Apktool not found! Please download apktool.jar and set the path in Settings.");
                }
            } catch (Exception e) {
                userNotifier.showError("❌ Apktool not found! Please download apktool.jar and set the path in Settings.");
            }
        }
    }

    public void saveSettings(String ApktoolPath, String defaultDir) {
        // Param ApktoolPath is confusingly named/unused effectively in original, but keeping signature for now if needed?
        // Actually I should probably clean that up too but let's stick to the main goal first.
        
        try {
            Properties props = new Properties();
            props.setProperty("apktool.path", getApkToolPath());
            props.setProperty("default.dir", defaultDir);
            props.setProperty("dark.mode", String.valueOf(UiUtils.darkMode));

            Path configPath = Path.of(System.getProperty("user.home"), ".apktool-gui.properties");
            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "Apktool GUI Settings");
            }

            logOutput.append("✅ Settings saved to: " + configPath);
        } catch (Exception e) {
            userNotifier.showError("Failed to save settings: " + e.getMessage());
        }
    }
}
