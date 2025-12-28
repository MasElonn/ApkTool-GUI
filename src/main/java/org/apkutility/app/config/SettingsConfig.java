package org.apkutility.app.config;

import java.io.File;

import static org.apkutility.app.utils.StringUtils.notBlank;

public class SettingsConfig {

    public static final String DEFAULT_APKTOOL_PATH = "resources/apktool.jar";
    public static final String DEFAULT_APKEDITOR_PATH = "resources/APKEditor.jar";
    public static final String DEFAULT_ADB_PATH = isWindows() ? "resources/platform-tools/adb.exe" : "resources/platform-tools/adb";
    public static final String DEFAULT_ZIPALIGN_PATH = "resources/zipalign";
    public static final String DEFAULT_APKSIGNER_PATH = "resources/apksigner.jar";
    public static final String DEFAULT_AAPT_PATH = "resources/aapt";
    public static final String DEFAULT_AAPT2_PATH = "resources/aapt2";
    public static final String DEFAULT_JAVA_PATH = "java";
    public static final String DEFAULT_FRAMEWORK_PATH = System.getProperty("user.home") + "/.apktool/framework";
    public static final String DEFAULT_WORKING_DIR = System.getProperty("user.home") + "/apktool-workspace";
    public static final boolean DEFAULT_DARK_MODE = true;
    public static final boolean DEFAULT_AUTO_SAVE = false;

    private String apktoolPath = DEFAULT_APKTOOL_PATH;
    private String apkEditorPath = DEFAULT_APKEDITOR_PATH;
    private String adbPath = DEFAULT_ADB_PATH;
    private String zipalignPath = DEFAULT_ZIPALIGN_PATH;
    private String apksignerPath = DEFAULT_APKSIGNER_PATH;
    private String aaptPath = DEFAULT_AAPT_PATH;
    private String aapt2Path = DEFAULT_AAPT2_PATH;
    private String javaPath = DEFAULT_JAVA_PATH;
    private String frameworkPath = DEFAULT_FRAMEWORK_PATH;
    private String defaultWorkingDir = DEFAULT_WORKING_DIR;
    private boolean darkMode = DEFAULT_DARK_MODE;
    private boolean autoSave = DEFAULT_AUTO_SAVE;

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // Getters
    public String getApktoolPath() { return valueOr(apktoolPath, DEFAULT_APKTOOL_PATH); }
    public String getApkEditorPath() { return valueOr(apkEditorPath, DEFAULT_APKEDITOR_PATH); }
    public String getAdbPath() { return valueOr(adbPath, DEFAULT_ADB_PATH); }
    public String getZipalignPath() { return valueOr(zipalignPath, DEFAULT_ZIPALIGN_PATH); }
    public String getApksignerPath() { return valueOr(apksignerPath, DEFAULT_APKSIGNER_PATH); }
    public String getAaptPath() { return valueOr(aaptPath, DEFAULT_AAPT_PATH); }
    public String getAapt2Path() { return valueOr(aapt2Path, DEFAULT_AAPT2_PATH); }
    public String getJavaPath() { return valueOr(javaPath, DEFAULT_JAVA_PATH); }
    public String getFrameworkPath() { return valueOr(frameworkPath, DEFAULT_FRAMEWORK_PATH); }
    public String getDefaultWorkingDir() { return valueOr(defaultWorkingDir, DEFAULT_WORKING_DIR); }
    public boolean isDarkMode() { return darkMode; }
    public boolean isAutoSave() { return autoSave; }

    // Setters
    public void setApktoolPath(String v) { apktoolPath = v; }
    public void setApkEditorPath(String v) { apkEditorPath = v; }
    public void setAdbPath(String v) { adbPath = v; }
    public void setZipalignPath(String v) { zipalignPath = v; }
    public void setApksignerPath(String v) { apksignerPath = v; }
    public void setAaptPath(String v) { aaptPath = v; }
    public void setAapt2Path(String v) { aapt2Path = v; }
    public void setJavaPath(String v) { javaPath = v; }
    public void setFrameworkPath(String v) { frameworkPath = v; }
    public void setDefaultWorkingDir(String v) { defaultWorkingDir = v; }
    public void setDarkMode(boolean v) { darkMode = v; }
    public void setAutoSave(boolean v) { autoSave = v; }

    // Validators
    public boolean validateApktoolPath() { return fileExists(getApktoolPath()); }
    public boolean validateApkEditorPath() { return fileExists(getApkEditorPath()); }
    public boolean validateZipalignPath() { return fileExists(getZipalignPath()); }
    public boolean validateApksignerPath() { return fileExists(getApksignerPath()); }
    public boolean validateAaptPath() { return fileExists(getAaptPath()); }
    public boolean validateAapt2Path() { return fileExists(getAapt2Path()); }
    public boolean validateFrameworkPath() { return dirExists(getFrameworkPath()); }

    public boolean validateAdbPath() {
        if (fileExists(getAdbPath())) return true;
        return canExecute(getAdbPath(), "version");
    }

    public boolean validateJavaPath() {
        return canExecute(getJavaPath(), "-version");
    }

    private String valueOr(String value, String defaultValue) {
        return notBlank(value) ? value : defaultValue;
    }

    private boolean fileExists(String path) {
        return new File(path).exists();
    }

    private boolean dirExists(String path) {
        File dir = new File(path);
        return dir.exists() && dir.isDirectory();
    }

    private boolean canExecute(String... command) {
        try {
            return new ProcessBuilder(command).start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
