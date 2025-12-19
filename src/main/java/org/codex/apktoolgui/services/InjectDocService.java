package org.codex.apktoolgui.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.codex.apktoolgui.services.executor.CommandExecutor;

public class InjectDocService {

    private final UserNotifier userNotifier;
    private final CommandExecutor commandExecutor;

    public InjectDocService(UserNotifier userNotifier, CommandExecutor commandExecutor) {
        this.userNotifier = userNotifier;
        this.commandExecutor = commandExecutor;
    }

    public static String getInjectDocPath(){
        File injectDocPath = new File("lib/InjectDocumentProvider.jar");
        if (injectDocPath.exists()) {
            return injectDocPath.getAbsolutePath();
        }
        return "";
    }

    public void executeInjectDoc(String apkPath){
        if (apkPath == null || apkPath.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to inject document.");
            return;
        }
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(getInjectDocPath());
        command.add(apkPath);

        commandExecutor.executeCommand(command, "Injecting document Provider...");
    }

}
