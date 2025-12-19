package org.codex.apktoolgui.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.codex.apktoolgui.services.executor.CommandExecutor;
import java.util.function.Consumer;

public class ApkEditorService {

    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public ApkEditorService(UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getApkEditorPath() {
        File apkEditorPath = new File("lib/APKEditor.jar");
        if (apkEditorPath.exists()) {
            return apkEditorPath.getAbsolutePath();
        }
        return "";
    }

    // 1. Decompile APK
    public void executeDecompile(String apkPath, String outputDir, boolean decompileToXml, boolean loadDex, String dexLibrary) {
        if (apkPath == null || apkPath.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to decompile.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("d");
        command.add("-i");
        command.add(apkPath);

        if (outputDir != null && !outputDir.trim().isEmpty()) {
            command.add("-o");
            command.add(outputDir);
        }

        if (decompileToXml) {
            command.add("-t");
            command.add("xml");
        }

        if (loadDex) {
            command.add("-load-dex");
            command.add("3"); // Based on example: -load-dex = 3
        }

        if (dexLibrary != null && !dexLibrary.trim().isEmpty()) {
            command.add("-dex-lib");
            command.add(dexLibrary); // Based on example: -dex-lib = jf
        }

        commandExecutor.executeCommand(command, "Decompiling APK...");
    }

    // 2. Build APK from decompiled files
    public void executeBuild(String inputDir, String outputApk, boolean buildFromXml, String dexLibrary) {
        if (inputDir == null || inputDir.trim().isEmpty()) {
            userNotifier.showError("Please select a decompiled directory to build.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("b");
        command.add("-i");
        command.add(inputDir);

        if (outputApk != null && !outputApk.trim().isEmpty()) {
            command.add("-o");
            command.add(outputApk);
        }

        if (buildFromXml) {
            command.add("-t");
            command.add("xml");
        }

        if (dexLibrary != null && !dexLibrary.trim().isEmpty()) {
            command.add("-dex-lib");
            command.add(dexLibrary);
        }

        commandExecutor.executeCommand(command, "Building APK...");
    }

    // 3. Merge split APKs - Simple version (existing)
    public void executeMerge(String inputPath, String outputApk) {
        if (inputPath == null || inputPath.trim().isEmpty()) {
            userNotifier.showError("Please select input for merging.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("m");
        command.add("-i");
        command.add(inputPath);

        if (outputApk != null && !outputApk.trim().isEmpty()) {
            command.add("-o");
            command.add(outputApk);
        }

        commandExecutor.executeCommand(command, "Merging APKs...");
    }

    // 3. Merge split APKs - Advanced version (with more options)
    public void executeMergeAdvanced(String inputPath, String outputApk, String resDir, String extractNativeLibs,
                                     boolean cleanMeta, boolean forceDelete, boolean validateModules, boolean vrd) {
        if (inputPath == null || inputPath.trim().isEmpty()) {
            userNotifier.showError("Please select input for merging.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("m");
        command.add("-i");
        command.add(inputPath);

        if (outputApk != null && !outputApk.trim().isEmpty()) {
            command.add("-o");
            command.add(outputApk);
        }

        if (resDir != null && !resDir.trim().isEmpty()) {
            command.add("-res-dir");
            command.add(resDir);
        }

        if (extractNativeLibs != null && !extractNativeLibs.trim().isEmpty()) {
            command.add("-extractNativeLibs");
            command.add(extractNativeLibs);
        }

        if (cleanMeta) {
            command.add("-clean-meta");
        }

        if (forceDelete) {
            command.add("-f");
        }

        if (validateModules) {
            command.add("-validate-modules");
        }

        if (vrd) {
            command.add("-vrd");
        }

        commandExecutor.executeCommand(command, "Merging APKs...");
    }

    // 4. Refactor obfuscated resources
    public void executeRefactor(String inputApk, String outputApk, String publicXml,
                                boolean cleanMeta, boolean forceDelete, boolean fixTypes) {
        if (inputApk == null || inputApk.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to refactor.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("x");
        command.add("-i");
        command.add(inputApk);

        if (outputApk != null && !outputApk.trim().isEmpty()) {
            command.add("-o");
            command.add(outputApk);
        }

        if (publicXml != null && !publicXml.trim().isEmpty()) {
            command.add("-public-xml");
            command.add(publicXml);
        }

        if (cleanMeta) {
            command.add("-clean-meta");
        }

        if (forceDelete) {
            command.add("-f");
        }

        if (fixTypes) {
            command.add("-fix-types");
        }

        commandExecutor.executeCommand(command, "Refactoring APK...");
    }

    // 5. Protect/Obfuscate APK resources
    public void executeProtect(String inputApk, String outputApk, String keepType,
                               boolean confuseZip, String dicDirNames, String dicFileNames,
                               boolean forceDelete, boolean skipManifest) {
        if (inputApk == null || inputApk.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to protect.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("p");
        command.add("-i");
        command.add(inputApk);

        if (outputApk != null && !outputApk.trim().isEmpty()) {
            command.add("-o");
            command.add(outputApk);
        }

        if (keepType != null && !keepType.trim().isEmpty()) {
            command.add("-keep-type");
            command.add(keepType);
        }

        if (confuseZip) {
            command.add("-confuse-zip");
        }

        if (dicDirNames != null && !dicDirNames.trim().isEmpty()) {
            command.add("-dic-dir-names");
            command.add(dicDirNames);
        }

        if (dicFileNames != null && !dicFileNames.trim().isEmpty()) {
            command.add("-dic-file-names");
            command.add(dicFileNames);
        }

        if (forceDelete) {
            command.add("-f");
        }

        if (skipManifest) {
            command.add("-skip-manifest");
        }

        commandExecutor.executeCommand(command, "Protecting APK...");
    }

    // 6. Get APK information
    public void executeGetInfo(String inputApk, String outputFile,boolean verbose,
                               String filterType, String framework, String frameworkVersion, String resourceId,
                               String xmlStrings, String xmlTree,
                               String outputType,boolean activities, boolean appClass,
                               boolean appIcon, boolean appName, boolean appRoundIcon,
                               boolean configurations, boolean dex, boolean forceDelete,
                               boolean languages, boolean listFiles, boolean listXmlFiles,
                               boolean locales, boolean minSdkVersion, boolean packageInfo,
                               boolean permissions, boolean resources, boolean signatures,
                               boolean signaturesBase64,  boolean targetSdkVersion,
                               boolean versionCode, boolean versionName,Consumer<String> outputConsumer) {

        if (inputApk == null || inputApk.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to get information.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getApkEditorPath());
        command.add("info");
        command.add("-i");
        command.add(inputApk);

        if (outputFile != null && !outputFile.trim().isEmpty()) {
            command.add("-o");
            command.add(outputFile);
        }

        if (filterType != null && !filterType.trim().isEmpty()) {
            command.add("-filter-type");
            command.add(filterType);
        }

        if (framework != null && !framework.trim().isEmpty()) {
            command.add("-framework");
            command.add(framework);
        }

        if (frameworkVersion != null && !frameworkVersion.trim().isEmpty()) {
            command.add("-framework-version");
            command.add(frameworkVersion);
        }

        if (resourceId != null && !resourceId.trim().isEmpty()) {
            command.add("-res");
            command.add(resourceId);
        }

        if (xmlStrings != null && !xmlStrings.trim().isEmpty()) {
            command.add("-xmlstrings");
            command.add(xmlStrings);
        }

        if (xmlTree != null && !xmlTree.trim().isEmpty()) {
            command.add("-xmltree");
            command.add(xmlTree);
        }

        if (outputType != null && !outputType.trim().isEmpty()) {
            command.add("-t");
            command.add(outputType);
        }

        // Flags
        if (verbose) command.add("-v");
        if (activities) command.add("-activities");
        if (appClass) command.add("-app-class");
        if (appIcon) command.add("-app-icon");
        if (appName) command.add("-app-name");
        if (appRoundIcon) command.add("-app-round-icon");
        if (configurations) command.add("-configurations");
        if (dex) command.add("-dex");
        if (forceDelete) command.add("-f");
        if (languages) command.add("-languages");
        if (listFiles) command.add("-list-files");
        if (listXmlFiles) command.add("-list-xml-files");
        if (locales) command.add("-locales");
        if (minSdkVersion) command.add("-min-sdk-version");
        if (packageInfo) command.add("-package");
        if (permissions) command.add("-permissions");
        if (resources) command.add("-resources");
        if (signatures) command.add("-signatures");
        if (signaturesBase64) command.add("-signatures-base64");
        if (targetSdkVersion) command.add("-target-sdk-version");
        if (versionCode) command.add("-version-code");
        if (versionName) command.add("-version-name");

        commandExecutor.executeCommand(command, "Getting APK information...",outputConsumer);
    }

}
