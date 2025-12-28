package org.apkutility.app.services;

import org.apkutility.app.services.executor.CommandExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apkutility.app.utils.StringUtils.notBlank;

public class ZipAlignService {

    private final CommandExecutor commandExecutor;

    public ZipAlignService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public static String getZipalignPath() {
        try {
            String configured = SettingsManager.getInstance().getSettings().getZipalignPath();
            if (notBlank(configured)) {
                File file = new File(configured);
                if (file.exists()) return file.getAbsolutePath();
            }
        } catch (Exception ignored) {
        }

        File defaultPath = new File("resources/zipalign");
        return defaultPath.exists() ? defaultPath.getAbsolutePath() : "";
    }

    public void alignApk(String apkPath, String outputPath) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getZipalignPath());
        cmd.add("-v");
        cmd.add("4");
        cmd.add(apkPath);
        cmd.add(outputPath);
        commandExecutor.executeCommand(cmd, "Aligning APK...");
    }
}
