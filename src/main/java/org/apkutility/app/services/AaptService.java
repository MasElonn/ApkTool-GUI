package org.apkutility.app.services;

import org.apkutility.app.services.executor.CommandExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apkutility.app.utils.StringUtils.isBlank;
import static org.apkutility.app.utils.StringUtils.notBlank;

public class AaptService {

    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public AaptService(LogOutput logOutput, UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getAaptPath() {
        return resolvePath(
            () -> SettingsManager.getInstance().getSettings().getAaptPath(),
            "resources/aapt", "aapt"
        );
    }

    public static String getAapt2Path() {
        return resolvePath(
            () -> SettingsManager.getInstance().getSettings().getAapt2Path(),
            "resources/aapt2", "aapt2"
        );
    }

    // AAPT dump commands
    public void dumpBadging(String apkPath) {
        executeAaptDump(apkPath, "badging", "Dumping APK badging...");
    }

    public void dumpPermissions(String apkPath) {
        executeAaptDump(apkPath, "permissions", "Dumping APK permissions...");
    }

    public void dumpResources(String apkPath) {
        executeAaptDump(apkPath, "resources", "Dumping APK resources...");
    }

    public void dumpConfigurations(String apkPath) {
        executeAaptDump(apkPath, "configurations", "Dumping APK configurations...");
    }

    public void dumpXmlTree(String apkPath, String assetPath) {
        if (!requireApk(apkPath)) return;

        List<String> cmd = buildAaptCommand("dump", "xmltree");
        cmd.add(apkPath);
        cmd.add(isBlank(assetPath) ? "AndroidManifest.xml" : assetPath);

        commandExecutor.executeCommand(cmd, "Dumping XML tree...");
    }

    public void listContents(String apkPath, boolean verbose) {
        if (!requireApk(apkPath)) return;

        List<String> cmd = buildAaptCommand("list");
        if (verbose) cmd.add("-v");
        cmd.add(apkPath);

        commandExecutor.executeCommand(cmd, "Listing APK contents...");
    }

    public void getVersion() {
        commandExecutor.executeCommand(buildAaptCommand("version"), "Getting AAPT version...");
    }

    // AAPT2 dump commands
    public void aapt2DumpBadging(String apkPath) {
        executeAapt2Dump(apkPath, "badging", "AAPT2: Dumping APK badging...");
    }

    public void aapt2DumpPermissions(String apkPath) {
        executeAapt2Dump(apkPath, "permissions", "AAPT2: Dumping permissions...");
    }

    public void aapt2DumpResources(String apkPath) {
        executeAapt2Dump(apkPath, "resources", "AAPT2: Dumping resources...");
    }

    public void aapt2DumpConfigurations(String apkPath) {
        executeAapt2Dump(apkPath, "configurations", "AAPT2: Dumping configurations...");
    }

    public void aapt2DumpStrings(String apkPath) {
        executeAapt2Dump(apkPath, "strings", "AAPT2: Dumping strings...");
    }

    public void aapt2DumpXmlTree(String apkPath, String assetPath) {
        if (!requireApk(apkPath)) return;

        List<String> cmd = buildAapt2Command("dump", "xmltree", "--file");
        cmd.add(isBlank(assetPath) ? "AndroidManifest.xml" : assetPath);
        cmd.add(apkPath);

        commandExecutor.executeCommand(cmd, "AAPT2: Dumping XML tree...");
    }

    public void getAapt2Version() {
        commandExecutor.executeCommand(buildAapt2Command("version"), "Getting AAPT2 version...");
    }

    public void aapt2Compile(String resourcePath, String outputDir) {
        if (isBlank(resourcePath)) {
            userNotifier.showError("Please select a resource file.");
            return;
        }
        if (isBlank(outputDir)) {
            userNotifier.showError("Please select an output directory.");
            return;
        }

        List<String> cmd = buildAapt2Command("compile", "-o", outputDir);
        cmd.add(resourcePath);
        commandExecutor.executeCommand(cmd, "AAPT2: Compiling resource...");
    }

    public void aapt2CompileDir(String resourceDir, String outputDir) {
        if (isBlank(resourceDir)) {
            userNotifier.showError("Please select a resource directory.");
            return;
        }
        if (isBlank(outputDir)) {
            userNotifier.showError("Please select an output directory.");
            return;
        }

        List<String> cmd = buildAapt2Command("compile", "--dir", resourceDir, "-o", outputDir + "/compiled_resources.zip");
        commandExecutor.executeCommand(cmd, "AAPT2: Compiling resources directory...");
    }

    public void aapt2Link(String compiledResources, String manifestPath, String outputApk,
                          String minSdk, String targetSdk, String androidJar) {
        if (compiledResources == null || manifestPath == null || outputApk == null) {
            userNotifier.showError("Missing required parameters for linking.");
            return;
        }

        List<String> cmd = buildAapt2Command("link", "-o", outputApk, "--manifest", manifestPath);

        if (notBlank(minSdk)) {
            cmd.add("--min-sdk-version");
            cmd.add(minSdk);
        }
        if (notBlank(targetSdk)) {
            cmd.add("--target-sdk-version");
            cmd.add(targetSdk);
        }
        if (notBlank(androidJar)) {
            cmd.add("-I");
            cmd.add(androidJar);
        }

        cmd.add(compiledResources);
        commandExecutor.executeCommand(cmd, "AAPT2: Linking resources...");
    }

    // Helper methods
    private void executeAaptDump(String apkPath, String dumpType, String statusMessage) {
        if (!requireApk(apkPath)) return;

        List<String> cmd = buildAaptCommand("dump", dumpType);
        cmd.add(apkPath);
        commandExecutor.executeCommand(cmd, statusMessage);
    }

    private void executeAapt2Dump(String apkPath, String dumpType, String statusMessage) {
        if (!requireApk(apkPath)) return;

        List<String> cmd = buildAapt2Command("dump", dumpType);
        cmd.add(apkPath);
        commandExecutor.executeCommand(cmd, statusMessage);
    }

    private List<String> buildAaptCommand(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getAaptPath());
        for (String arg : args) cmd.add(arg);
        return cmd;
    }

    private List<String> buildAapt2Command(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getAapt2Path());
        for (String arg : args) cmd.add(arg);
        return cmd;
    }

    private boolean requireApk(String apkPath) {
        if (isBlank(apkPath)) {
            userNotifier.showError("Please select an APK file.");
            return false;
        }
        return true;
    }

    private static String resolvePath(java.util.function.Supplier<String> configGetter, String defaultPath, String fallback) {
        try {
            String configured = configGetter.get();
            if (notBlank(configured)) {
                File file = new File(configured);
                if (file.exists()) return file.getAbsolutePath();
            }
        } catch (Exception ignored) {
        }

        File defaultFile = new File(defaultPath);
        return defaultFile.exists() ? defaultFile.getAbsolutePath() : fallback;
    }
}
