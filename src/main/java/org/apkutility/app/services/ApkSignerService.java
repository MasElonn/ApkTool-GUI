package org.apkutility.app.services;

import org.apkutility.app.services.executor.CommandExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ApkSignerService {

    private static final String TEST_KEYSTORE_NAME = "debug.keystore";
    private static final String TEST_KEYSTORE_PASSWORD = "android";
    private static final String TEST_KEY_ALIAS = "androiddebugkey";
    private static final String TEST_KEY_PASSWORD = "android";

    private final LogOutput logOutput;
    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public ApkSignerService(LogOutput logOutput, UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.logOutput = logOutput;
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getApkSignerPath() {
        try {
            String configuredPath = SettingsManager.getInstance().getSettings().getApksignerPath();
            if (!isBlank(configuredPath)) {
                File signerFile = new File(configuredPath);
                if (signerFile.exists()) {
                    return signerFile.getAbsolutePath();
                }
            }
        } catch (Exception ignored) {
        }

        File defaultPath = new File("resources/apksigner.jar");
        return defaultPath.exists() ? defaultPath.getAbsolutePath() : "";
    }

    public String getOrCreateTestKeystore() {
        Path keystorePath = Path.of(System.getProperty("user.home"), ".apktool-gui", TEST_KEYSTORE_NAME);

        if (Files.exists(keystorePath)) {
            return keystorePath.toString();
        }

        try {
            Files.createDirectories(keystorePath.getParent());
        } catch (Exception e) {
            logOutput.append("❌ Failed to create keystore directory: " + e.getMessage());
            return null;
        }

        logOutput.append("🔑 Generating test keystore...");

        List<String> cmd = new ArrayList<>();
        cmd.add("keytool");
        cmd.add("-genkeypair");
        cmd.add("-v");
        cmd.add("-keystore");
        cmd.add(keystorePath.toString());
        cmd.add("-storepass");
        cmd.add(TEST_KEYSTORE_PASSWORD);
        cmd.add("-alias");
        cmd.add(TEST_KEY_ALIAS);
        cmd.add("-keypass");
        cmd.add(TEST_KEY_PASSWORD);
        cmd.add("-keyalg");
        cmd.add("RSA");
        cmd.add("-keysize");
        cmd.add("2048");
        cmd.add("-validity");
        cmd.add("10000");
        cmd.add("-dname");
        cmd.add("CN=Android Debug,O=Android,C=US");

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            int exitCode = pb.start().waitFor();

            if (exitCode == 0 && Files.exists(keystorePath)) {
                logOutput.append("✅ Test keystore created at: " + keystorePath);
                return keystorePath.toString();
            }
            logOutput.append("❌ Failed to create test keystore");
            return null;
        } catch (Exception e) {
            logOutput.append("❌ Failed to create test keystore: " + e.getMessage());
            return null;
        }
    }

    public String getTestKeystorePassword() {
        return TEST_KEYSTORE_PASSWORD;
    }

    public String getTestKeyAlias() {
        return TEST_KEY_ALIAS;
    }

    public String getTestKeyPassword() {
        return TEST_KEY_PASSWORD;
    }

    public void signApk(String inputApk, String outputApk, String keystorePath,
                        String keystorePassword, String keyAlias, String keyPassword,
                        boolean v1, boolean v2, boolean v3, boolean v4, boolean useTestKey) {

        if (isBlank(inputApk)) {
            userNotifier.showError("Please select an APK file to sign.");
            return;
        }

        if (useTestKey || isBlank(keystorePath)) {
            keystorePath = getOrCreateTestKeystore();
            if (keystorePath == null) {
                userNotifier.showError("Failed to create test keystore.");
                return;
            }
            keystorePassword = TEST_KEYSTORE_PASSWORD;
            keyAlias = TEST_KEY_ALIAS;
            keyPassword = TEST_KEY_PASSWORD;
            logOutput.append("🔑 Using test key for signing");
        }

        if (isBlank(outputApk)) {
            outputApk = inputApk.replace(".apk", "_signed.apk");
        }

        List<String> cmd = buildBaseCommand("sign");
        cmd.add("--v1-signing-enabled");
        cmd.add(String.valueOf(v1));
        cmd.add("--v2-signing-enabled");
        cmd.add(String.valueOf(v2));
        cmd.add("--v3-signing-enabled");
        cmd.add(String.valueOf(v3));
        cmd.add("--v4-signing-enabled");
        cmd.add(String.valueOf(v4));
        addKeystoreArgs(cmd, keystorePath, keystorePassword, keyAlias, keyPassword);
        cmd.add("--out");
        cmd.add(outputApk);
        cmd.add(inputApk);

        commandExecutor.executeCommand(cmd, "Signing APK...");
    }

    public void quickSignWithTestKey(String inputApk, String outputApk) {
        signApk(inputApk, outputApk, null, null, null, null, true, true, true, false, true);
    }

    public void verifyApk(String apkPath, boolean verbose, boolean printCerts) {
        if (isBlank(apkPath)) {
            userNotifier.showError("Please select an APK file to verify.");
            return;
        }

        List<String> cmd = buildBaseCommand("verify");
        if (verbose) cmd.add("-v");
        if (printCerts) cmd.add("--print-certs");
        cmd.add(apkPath);

        commandExecutor.executeCommand(cmd, "Verifying APK signature...");
    }

    public void getVersion() {
        commandExecutor.executeCommand(buildBaseCommand("version"), "Getting apksigner version...");
    }

    public void rotateSigningKey(String inputApk, String outputApk,
                                  String oldKsPath, String oldKsPass, String oldAlias, String oldKeyPass,
                                  String newKsPath, String newKsPass, String newAlias, String newKeyPass) {

        if (isBlank(inputApk)) {
            userNotifier.showError("Please select an APK file.");
            return;
        }

        if (oldKsPath == null || newKsPath == null) {
            userNotifier.showError("Both old and new keystores are required for key rotation.");
            return;
        }

        logOutput.append("🔄 Creating signing certificate lineage...");

        List<String> cmd = buildBaseCommand("lineage");
        cmd.add("--old-signer");
        cmd.add("--ks");
        cmd.add(oldKsPath);
        cmd.add("--ks-pass");
        cmd.add("pass:" + oldKsPass);
        cmd.add("--ks-key-alias");
        cmd.add(oldAlias);
        cmd.add("--new-signer");
        cmd.add("--ks");
        cmd.add(newKsPath);
        cmd.add("--ks-pass");
        cmd.add("pass:" + newKsPass);
        cmd.add("--ks-key-alias");
        cmd.add(newAlias);

        commandExecutor.executeCommand(cmd, "Rotating signing key...");
    }

    private List<String> buildBaseCommand(String subCommand) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getJavaPath());
        cmd.add("-jar");
        cmd.add(getApkSignerPath());
        cmd.add(subCommand);
        return cmd;
    }

    private void addKeystoreArgs(List<String> cmd, String ksPath, String ksPass, String alias, String keyPass) {
        cmd.add("--ks");
        cmd.add(ksPath);
        cmd.add("--ks-pass");
        cmd.add("pass:" + ksPass);
        cmd.add("--ks-key-alias");
        cmd.add(alias);
        cmd.add("--key-pass");
        cmd.add("pass:" + keyPass);
    }

    private static String getJavaPath() {
        return SettingsManager.getInstance().getSettings().getJavaPath();
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
