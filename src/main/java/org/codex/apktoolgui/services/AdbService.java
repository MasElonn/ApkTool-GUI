package org.codex.apktoolgui.services;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.codex.apktoolgui.services.executor.CommandExecutor;
import java.util.function.Consumer;


public class AdbService {
    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public AdbService(UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        this.userNotifier = userNotifier;
    }
    
    public static String getAdbPath() {
        // Check local lib folder first
        File adbFile = new File("lib/platform-tools/adb");
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            adbFile = new File("lib/platform-tools/adb.exe");
        }
        
        if (adbFile.exists()) {
            return adbFile.getAbsolutePath();
        }
        // Fallback to system ADB
        return "adb";
    }

    public void getConnectedDevices(Consumer<List<String>> callback) {
        new Thread(() -> {
            List<String> devices = new ArrayList<>();
            try {
                ProcessBuilder pb = new ProcessBuilder(getAdbPath(), "devices");
                Process process = pb.start();
                
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
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
        if (apkPath == null || apkPath.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to install.");
            return;
        }
        if (deviceId == null || deviceId.isEmpty()) {
            userNotifier.showError("Please select a device.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add(getAdbPath());
        command.add("-s");
        command.add(deviceId);
        command.add("install");
        command.add("-r"); // Reinstall if exists
        command.add(apkPath);
        commandExecutor.executeCommand(command, "Installing APK on " + deviceId + "...");
    }

    public void uninstall(String deviceId, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            userNotifier.showError("Please enter a package name.");
            return;
        }
        if (deviceId == null || deviceId.isEmpty()) {
            userNotifier.showError("Please select a device.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add(getAdbPath());
        command.add("-s");
        command.add(deviceId);
        command.add("uninstall");
        command.add(packageName);
        commandExecutor.executeCommand(command, "Uninstalling " + packageName + " from " + deviceId + "...");
    }

    public void pair(String ip, String port, String code) {
        if (ip.isEmpty() || port.isEmpty() || code.isEmpty()) {
            userNotifier.showError("Please enter IP, Port, and Code.");
            return;
        }
        List<String> command = new ArrayList<>();
        command.add(getAdbPath());
        command.add("pair");
        command.add(ip + ":" + port);
        command.add(code);
        commandExecutor.executeCommand(command, "Pairing with " + ip + ":" + port + "...");
    }

    public void connect(String ip, String port) {
         if (ip.isEmpty() || port.isEmpty()) {
            userNotifier.showError("Please enter IP and Port.");
            return;
        }
        List<String> command = new ArrayList<>();
        command.add(getAdbPath());
        command.add("connect");
        command.add(ip + ":" + port);
        commandExecutor.executeCommand(command, "Connecting to " + ip + ":" + port + "...");
    }

    public void executeShellCommand(String deviceId, String shellCommand, Consumer<String> outputConsumer) {
         if (deviceId == null || deviceId.isEmpty()) {
            if (outputConsumer != null) outputConsumer.accept("Error: No device selected.");
            return;
        }
        List<String> command = new ArrayList<>();
        command.add(getAdbPath());
        command.add("-s");
        command.add(deviceId);
        command.addAll(List.of(shellCommand.split("\\s+"))); // Basic split, might need better parsing for quotes

        commandExecutor.executeCommand(command, "Executing: " + shellCommand, outputConsumer);
    }

    public void pullApk(String deviceId, String packageName, String destDir) {
         if (deviceId == null || packageName.isEmpty() || destDir == null) {
             userNotifier.showError("Invalid parameters for Pull APK.");
             return;
         }
         
         // 1. Get Path
         List<String> pathCmd = List.of(getAdbPath(), "-s", deviceId, "shell", "pm", "path", packageName);
         
         // We need to capture the output of this command to get the path
         commandExecutor.executeCommand(pathCmd, "Finding APK path for " + packageName + "...", output -> {
             if (output.startsWith("package:")) {
                 String remotePath = output.substring("package:".length()).trim();
                 
                 // 2. Pull
                 List<String> pullCmd = new ArrayList<>();
                 pullCmd.add(getAdbPath());
                 pullCmd.add("-s");
                 pullCmd.add(deviceId);
                 pullCmd.add("pull");
                 pullCmd.add(remotePath);
                 pullCmd.add(destDir + File.separator + packageName + ".apk");
                 
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
}