package org.codex.apktoolgui.services;

import org.codex.apktoolgui.services.executor.CommandExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.codex.apktoolgui.utils.StringUtils.isBlank;
import static org.codex.apktoolgui.utils.StringUtils.notBlank;

public class AdbService {

    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public AdbService(UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        this.userNotifier = userNotifier;
    }

    public static String getAdbPath() {
        try {
            String configured = SettingsManager.getInstance().getSettings().getAdbPath();
            if (notBlank(configured)) {
                File adbFile = new File(configured);
                if (adbFile.exists()) return adbFile.getAbsolutePath();
                return configured;
            }
        } catch (Exception ignored) {
        }

        String filename = System.getProperty("os.name").toLowerCase().contains("win") ? "adb.exe" : "adb";
        File defaultFile = new File("lib/platform-tools/" + filename);
        return defaultFile.exists() ? defaultFile.getAbsolutePath() : "adb";
    }

    public void getConnectedDevices(Consumer<List<String>> callback) {
        new Thread(() -> {
            List<String> devices = new ArrayList<>();
            try {
                ProcessBuilder pb = new ProcessBuilder(getAdbPath(), "devices");
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty() || line.startsWith("List of devices")) continue;
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2 && "device".equals(parts[1])) {
                            devices.add(parts[0]);
                        }
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            javafx.application.Platform.runLater(() -> callback.accept(devices));
        }).start();
    }

    public void install(String deviceId, String apkPath) {
        if (isBlank(apkPath)) {
            userNotifier.showError("Please select an APK file to install.");
            return;
        }
        if (!requireDevice(deviceId)) return;

        List<String> cmd = buildDeviceCommand(deviceId, "install", "-r", apkPath);
        commandExecutor.executeCommand(cmd, "Installing APK on " + deviceId + "...");
    }

    public void uninstall(String deviceId, String packageName) {
        if (isBlank(packageName)) {
            userNotifier.showError("Please enter a package name.");
            return;
        }
        if (!requireDevice(deviceId)) return;

        List<String> cmd = buildDeviceCommand(deviceId, "uninstall", packageName);
        commandExecutor.executeCommand(cmd, "Uninstalling " + packageName + " from " + deviceId + "...");
    }

    public void pair(String ip, String port, String code) {
        if (ip.isEmpty() || port.isEmpty() || code.isEmpty()) {
            userNotifier.showError("Please enter IP, Port, and Code.");
            return;
        }
        List<String> cmd = buildCommand("pair", ip + ":" + port, code);
        commandExecutor.executeCommand(cmd, "Pairing with " + ip + ":" + port + "...");
    }

    public void connect(String ip, String port) {
        if (ip.isEmpty() || port.isEmpty()) {
            userNotifier.showError("Please enter IP and Port.");
            return;
        }
        List<String> cmd = buildCommand("connect", ip + ":" + port);
        commandExecutor.executeCommand(cmd, "Connecting to " + ip + ":" + port + "...");
    }

    public void executeShellCommand(String deviceId, String shellCommand, Consumer<String> outputConsumer) {
        if (!requireDevice(deviceId)) {
            if (outputConsumer != null) outputConsumer.accept("Error: No device selected.");
            return;
        }

        List<String> cmd = buildDeviceCommand(deviceId);
        for (String part : shellCommand.split("\\s+")) cmd.add(part);
        commandExecutor.executeCommand(cmd, "Executing: " + shellCommand, outputConsumer);
    }

    public void pullApk(String deviceId, String packageName, String destDir) {
        if (deviceId == null || packageName.isEmpty() || destDir == null) {
            userNotifier.showError("Invalid parameters for Pull APK.");
            return;
        }

        List<String> pathCmd = List.of(getAdbPath(), "-s", deviceId, "shell", "pm", "path", packageName);
        commandExecutor.executeCommand(pathCmd, "Finding APK path for " + packageName + "...", output -> {
            if (output.startsWith("package:")) {
                String remotePath = output.substring("package:".length()).trim();
                List<String> pullCmd = buildDeviceCommand(deviceId, "pull", remotePath, destDir + File.separator + packageName + ".apk");
                javafx.application.Platform.runLater(() ->
                    commandExecutor.executeCommand(pullCmd, "Pulling " + packageName + ".apk...")
                );
            }
        });
    }

    public void dumpPackage(String deviceId, String packageName, Consumer<String> output) {
        if (deviceId == null || packageName.isEmpty()) return;
        List<String> cmd = List.of(getAdbPath(), "-s", deviceId, "shell", "dumpsys", "package", packageName);
        commandExecutor.executeCommand(cmd, "Dumping package info...", output);
    }

    private List<String> buildCommand(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getAdbPath());
        for (String arg : args) cmd.add(arg);
        return cmd;
    }

    private List<String> buildDeviceCommand(String deviceId, String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getAdbPath());
        cmd.add("-s");
        cmd.add(deviceId);
        for (String arg : args) cmd.add(arg);
        return cmd;
    }

    private boolean requireDevice(String deviceId) {
        if (isBlank(deviceId)) {
            userNotifier.showError("Please select a device.");
            return false;
        }
        return true;
    }
}