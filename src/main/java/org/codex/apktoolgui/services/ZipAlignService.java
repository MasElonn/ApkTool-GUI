package org.codex.apktoolgui.services;


import java.util.List;
import java.util.ArrayList;
import org.codex.apktoolgui.services.executor.CommandExecutor;
import java.io.File;

public class ZipAlignService {
    
    private final CommandExecutor commandExecutor;

    public ZipAlignService(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public static String getZipalighPath(){
        File zipalignPath = new File("lib/zipalign");
        if(zipalignPath.exists()){
            return zipalignPath.getAbsolutePath();
        }
        return "";
    }

    public void alighApk(String apkPath, String outputPath){
        List<String> command = new ArrayList<>();
        command.add(getZipalighPath());
        command.add("-v");
        command.add("4");
        command.add(apkPath);
        command.add(outputPath);
        commandExecutor.executeCommand(command, "Aligning APK...");
    }

}
