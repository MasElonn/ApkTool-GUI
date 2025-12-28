package org.codex.apktoolgui.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.codex.apktoolgui.services.executor.CommandExecutor;
import org.codex.apktoolgui.utils.UiUtils;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.codex.apktoolgui.utils.StringUtils.isBlank;
import static org.codex.apktoolgui.utils.StringUtils.notBlank;

public class ApkToolService {

    private final LogOutput logOutput;
    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public ApkToolService(LogOutput logOutput, UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.logOutput = logOutput;
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getApkToolPath() {
        try {
            String configured = SettingsManager.getInstance().getSettings().getApktoolPath();
            if (notBlank(configured)) {
                File file = new File(configured);
                if (file.exists()) return file.getAbsolutePath();
            }
        } catch (Exception ignored) {
        }

        File defaultPath = new File("lib/apktool.jar");
        return defaultPath.exists() ? defaultPath.getAbsolutePath() : "";
    }

    public void executeDecode(String apkPath, String outputPath, String frameworkPath,
                              String apiLevel, String jobs, boolean noRes, boolean noSrc,
                              boolean noAssets, boolean onlyManifest, boolean force,
                              boolean noDebug, boolean matchOriginal, boolean keepBroken,
                              boolean onlyMainClasses) {
        if (isBlank(apkPath)) {
            userNotifier.showError("Please select an APK file to decode.");
            return;
        }

        List<String> cmd = buildCommand("d");
        addOptional(cmd, "-o", outputPath);
        addOptional(cmd, "-p", frameworkPath);
        addOptional(cmd, "--api-level", apiLevel);
        addOptional(cmd, "-j", jobs);

        if (force) cmd.add("-f");
        if (noRes) cmd.add("-r");
        if (noSrc) cmd.add("-s");
        if (noAssets) cmd.add("--no-assets");
        if (onlyManifest) cmd.add("--only-manifest");
        if (noDebug) cmd.add("-b");
        if (matchOriginal) cmd.add("-m");
        if (keepBroken) cmd.add("-k");
        if (onlyMainClasses) cmd.add("--only-main-classes");

        cmd.add(apkPath);
        commandExecutor.executeCommand(cmd, "Decoding APK...");
    }

    public void executeBuild(String inputDir, String outputPath, String aaptPath,
                             String frameworkPath, boolean debug, boolean copyOriginal,
                             boolean force, boolean noApk, boolean noCrunch,
                             boolean useAapt1, boolean netSec) {
        if (isBlank(inputDir)) {
            userNotifier.showError("Please select a project directory to build.");
            return;
        }

        List<String> cmd = buildCommand("b");
        addOptional(cmd, "-o", outputPath);
        addOptional(cmd, "-a", aaptPath);
        addOptional(cmd, "-p", frameworkPath);

        if (debug) cmd.add("-d");
        if (copyOriginal) cmd.add("-c");
        if (force) cmd.add("-f");
        if (noApk) cmd.add("-na");
        if (noCrunch) cmd.add("-nc");
        if (useAapt1) cmd.add("--use-aapt1");
        if (netSec) cmd.add("-n");

        cmd.add(inputDir);
        commandExecutor.executeCommand(cmd, "Building APK...");
    }

    public void executeInstallFramework(String frameworkApk, String tag) {
        if (isBlank(frameworkApk)) {
            userNotifier.showError("Please select a framework APK file.");
            return;
        }

        List<String> cmd = buildCommand("if");
        addOptional(cmd, "-t", tag);
        cmd.add(frameworkApk);
        commandExecutor.executeCommand(cmd, "Installing framework...");
    }

    public void executeListFrameworks() {
        commandExecutor.executeCommand(buildCommand("lf"), "Listing frameworks...");
    }

    public void executeEmptyFrameworkDir() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Empty Framework Directory");
        confirm.setContentText("This will delete ALL framework files. Are you sure?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            List<String> cmd = buildCommand("efd", "-f");
            commandExecutor.executeCommand(cmd, "Emptying framework directory...");
        }
    }

    public void executePublicizeResources(String arscPath) {
        if (isBlank(arscPath)) {
            userNotifier.showError("Please select an ARSC file.");
            return;
        }

        List<String> cmd = buildCommand("pr", arscPath);
        commandExecutor.executeCommand(cmd, "Publicizing resources...");
    }

    public void executeVersionCheck() {
        commandExecutor.executeCommand(buildCommand("v"), "Checking version...");
    }

    public void executeHelp() {
        commandExecutor.executeCommand(buildCommand("h"), "Showing help...");
    }

    public void checkApktoolAvailability() {
        File apktoolFile = new File(getApkToolPath());
        if (apktoolFile.exists()) {
            logOutput.append("✅ Apktool found at: " + getApkToolPath());
            return;
        }

        try {
            Process process = new ProcessBuilder("apktool", "version").start();
            if (process.waitFor() == 0) {
                logOutput.append("✅ Apktool found in system PATH");
            } else {
                userNotifier.showError("❌ Apktool not found! Please download apktool.jar and set the path in Settings.");
            }
        } catch (Exception e) {
            userNotifier.showError("❌ Apktool not found! Please download apktool.jar and set the path in Settings.");
        }
    }

    public void saveSettings(String apktoolPath, String defaultDir) {
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

    private List<String> buildCommand(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-jar");
        cmd.add(getApkToolPath());
        for (String arg : args) cmd.add(arg);
        return cmd;
    }

    private void addOptional(List<String> cmd, String flag, String value) {
        if (notBlank(value)) {
            cmd.add(flag);
            cmd.add(value);
        }
    }
}
