package org.apkutility.app.services;

import org.apkutility.app.services.executor.CommandExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.apkutility.app.utils.StringUtils.isBlank;
import static org.apkutility.app.utils.StringUtils.notBlank;

public class ApkEditorService {

    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public ApkEditorService(UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getApkEditorPath() {
        try {
            String configured = SettingsManager.getInstance().getSettings().getApkEditorPath();
            if (notBlank(configured)) {
                File file = new File(configured);
                if (file.exists()) return file.getAbsolutePath();
            }
        } catch (Exception ignored) {
        }

        File defaultPath = new File("resources/APKEditor.jar");
        return defaultPath.exists() ? defaultPath.getAbsolutePath() : "";
    }

    public void executeDecompile(String apkPath, String outputDir, boolean decompileToXml, boolean loadDex, String dexLibrary) {
        if (!requireApk(apkPath, "decompile")) return;

        List<String> cmd = buildCommand("d", "-i", apkPath);
        addOptional(cmd, "-o", outputDir);
        if (decompileToXml) addArgs(cmd, "-t", "xml");
        if (loadDex) addArgs(cmd, "-load-dex", "3");
        addOptional(cmd, "-dex-lib", dexLibrary);

        commandExecutor.executeCommand(cmd, "Decompiling APK...");
    }

    public void executeBuild(String inputDir, String outputApk, boolean buildFromXml, String dexLibrary) {
        if (isBlank(inputDir)) {
            userNotifier.showError("Please select a decompiled directory to build.");
            return;
        }

        List<String> cmd = buildCommand("b", "-i", inputDir);
        addOptional(cmd, "-o", outputApk);
        if (buildFromXml) addArgs(cmd, "-t", "xml");
        addOptional(cmd, "-dex-lib", dexLibrary);

        commandExecutor.executeCommand(cmd, "Building APK...");
    }

    public void executeMerge(String inputPath, String outputApk) {
        if (isBlank(inputPath)) {
            userNotifier.showError("Please select input for merging.");
            return;
        }

        List<String> cmd = buildCommand("m", "-i", inputPath);
        addOptional(cmd, "-o", outputApk);
        commandExecutor.executeCommand(cmd, "Merging APKs...");
    }

    public void executeMergeAdvanced(String inputPath, String outputApk, String resDir, String extractNativeLibs,
                                     boolean cleanMeta, boolean forceDelete, boolean validateModules, boolean vrd) {
        if (isBlank(inputPath)) {
            userNotifier.showError("Please select input for merging.");
            return;
        }

        List<String> cmd = buildCommand("m", "-i", inputPath);
        addOptional(cmd, "-o", outputApk);
        addOptional(cmd, "-res-dir", resDir);
        addOptional(cmd, "-extractNativeLibs", extractNativeLibs);

        if (cleanMeta) cmd.add("-clean-meta");
        if (forceDelete) cmd.add("-f");
        if (validateModules) cmd.add("-validate-modules");
        if (vrd) cmd.add("-vrd");

        commandExecutor.executeCommand(cmd, "Merging APKs...");
    }

    public void executeRefactor(String inputApk, String outputApk, String publicXml,
                                boolean cleanMeta, boolean forceDelete, boolean fixTypes) {
        if (!requireApk(inputApk, "refactor")) return;

        List<String> cmd = buildCommand("x", "-i", inputApk);
        addOptional(cmd, "-o", outputApk);
        addOptional(cmd, "-public-xml", publicXml);

        if (cleanMeta) cmd.add("-clean-meta");
        if (forceDelete) cmd.add("-f");
        if (fixTypes) cmd.add("-fix-types");

        commandExecutor.executeCommand(cmd, "Refactoring APK...");
    }

    public void executeProtect(String inputApk, String outputApk, String keepType,
                               boolean confuseZip, String dicDirNames, String dicFileNames,
                               boolean forceDelete, boolean skipManifest) {
        if (!requireApk(inputApk, "protect")) return;

        List<String> cmd = buildCommand("p", "-i", inputApk);
        addOptional(cmd, "-o", outputApk);
        addOptional(cmd, "-keep-type", keepType);
        addOptional(cmd, "-dic-dir-names", dicDirNames);
        addOptional(cmd, "-dic-file-names", dicFileNames);

        if (confuseZip) cmd.add("-confuse-zip");
        if (forceDelete) cmd.add("-f");
        if (skipManifest) cmd.add("-skip-manifest");

        commandExecutor.executeCommand(cmd, "Protecting APK...");
    }

    public void executeGetInfo(String inputApk, String outputFile, boolean verbose,
                               String filterType, String framework, String frameworkVersion, String resourceId,
                               String xmlStrings, String xmlTree, String outputType,
                               boolean activities, boolean appClass, boolean appIcon, boolean appName,
                               boolean appRoundIcon, boolean configurations, boolean dex, boolean forceDelete,
                               boolean languages, boolean listFiles, boolean listXmlFiles, boolean locales,
                               boolean minSdkVersion, boolean packageInfo, boolean permissions, boolean resources,
                               boolean signatures, boolean signaturesBase64, boolean targetSdkVersion,
                               boolean versionCode, boolean versionName, Consumer<String> outputConsumer) {

        if (!requireApk(inputApk, "get information")) return;

        List<String> cmd = buildCommand("info", "-i", inputApk);
        addOptional(cmd, "-o", outputFile);
        addOptional(cmd, "-filter-type", filterType);
        addOptional(cmd, "-framework", framework);
        addOptional(cmd, "-framework-version", frameworkVersion);
        addOptional(cmd, "-res", resourceId);
        addOptional(cmd, "-xmlstrings", xmlStrings);
        addOptional(cmd, "-xmltree", xmlTree);
        addOptional(cmd, "-t", outputType);

        if (verbose) cmd.add("-v");
        if (activities) cmd.add("-activities");
        if (appClass) cmd.add("-app-class");
        if (appIcon) cmd.add("-app-icon");
        if (appName) cmd.add("-app-name");
        if (appRoundIcon) cmd.add("-app-round-icon");
        if (configurations) cmd.add("-configurations");
        if (dex) cmd.add("-dex");
        if (forceDelete) cmd.add("-f");
        if (languages) cmd.add("-languages");
        if (listFiles) cmd.add("-list-files");
        if (listXmlFiles) cmd.add("-list-xml-files");
        if (locales) cmd.add("-locales");
        if (minSdkVersion) cmd.add("-min-sdk-version");
        if (packageInfo) cmd.add("-package");
        if (permissions) cmd.add("-permissions");
        if (resources) cmd.add("-resources");
        if (signatures) cmd.add("-signatures");
        if (signaturesBase64) cmd.add("-signatures-base64");
        if (targetSdkVersion) cmd.add("-target-sdk-version");
        if (versionCode) cmd.add("-version-code");
        if (versionName) cmd.add("-version-name");

        commandExecutor.executeCommand(cmd, "Getting APK information...", outputConsumer);
    }

    private List<String> buildCommand(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-jar");
        cmd.add(getApkEditorPath());
        for (String arg : args) cmd.add(arg);
        return cmd;
    }

    private void addOptional(List<String> cmd, String flag, String value) {
        if (notBlank(value)) addArgs(cmd, flag, value);
    }

    private void addArgs(List<String> cmd, String... args) {
        for (String arg : args) cmd.add(arg);
    }

    private boolean requireApk(String apkPath, String action) {
        if (isBlank(apkPath)) {
            userNotifier.showError("Please select an APK file to " + action + ".");
            return false;
        }
        return true;
    }
}
