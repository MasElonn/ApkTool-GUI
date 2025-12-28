package org.apkutility.app.services;

import org.apkutility.app.config.SettingsConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Settings manager service for loading and saving application settings
 * Implements singleton pattern to ensure single settings instance across the app
 */
public class SettingsManager {
    
    private static SettingsManager instance;
    private SettingsConfig settings;
    private final Path configPath;
    
    private static final String CONFIG_FILE = ".apktool-gui.properties";
    
    // Property keys
    private static final String KEY_APKTOOL_PATH = "apktool.path";
    private static final String KEY_APKEDITOR_PATH = "apkeditor.path";
    private static final String KEY_ADB_PATH = "adb.path";
    private static final String KEY_ZIPALIGN_PATH = "zipalign.path";
    private static final String KEY_APKSIGNER_PATH = "apksigner.path";
    private static final String KEY_AAPT_PATH = "aapt.path";
    private static final String KEY_AAPT2_PATH = "aapt2.path";
    private static final String KEY_JAVA_PATH = "java.path";
    private static final String KEY_FRAMEWORK_PATH = "framework.path";
    private static final String KEY_DEFAULT_DIR = "default.dir";
    private static final String KEY_DARK_MODE = "dark.mode";
    private static final String KEY_AUTO_SAVE = "auto.save";
    
    private SettingsManager() {
        this.configPath = Path.of(System.getProperty("user.home"), CONFIG_FILE);
        this.settings = new SettingsConfig();
        loadSettings();
    }
    
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    
    public SettingsConfig getSettings() {
        return settings;
    }
    
    /**
     * Load settings from properties file
     * If file doesn't exist, use default values
     */
    public void loadSettings() {
        if (!Files.exists(configPath)) {
            settings = new SettingsConfig(); // Use defaults
            return;
        }
        
        try (InputStream in = Files.newInputStream(configPath)) {
            Properties props = new Properties();
            props.load(in);
            
            settings.setApktoolPath(props.getProperty(KEY_APKTOOL_PATH, SettingsConfig.DEFAULT_APKTOOL_PATH));
            settings.setApkEditorPath(props.getProperty(KEY_APKEDITOR_PATH, SettingsConfig.DEFAULT_APKEDITOR_PATH));
            settings.setAdbPath(props.getProperty(KEY_ADB_PATH, SettingsConfig.DEFAULT_ADB_PATH));
            settings.setZipalignPath(props.getProperty(KEY_ZIPALIGN_PATH, SettingsConfig.DEFAULT_ZIPALIGN_PATH));
            settings.setApksignerPath(props.getProperty(KEY_APKSIGNER_PATH, SettingsConfig.DEFAULT_APKSIGNER_PATH));
            settings.setAaptPath(props.getProperty(KEY_AAPT_PATH, SettingsConfig.DEFAULT_AAPT_PATH));
            settings.setAapt2Path(props.getProperty(KEY_AAPT2_PATH, SettingsConfig.DEFAULT_AAPT2_PATH));
            settings.setJavaPath(props.getProperty(KEY_JAVA_PATH, SettingsConfig.DEFAULT_JAVA_PATH));
            settings.setFrameworkPath(props.getProperty(KEY_FRAMEWORK_PATH, SettingsConfig.DEFAULT_FRAMEWORK_PATH));
            settings.setDefaultWorkingDir(props.getProperty(KEY_DEFAULT_DIR, SettingsConfig.DEFAULT_WORKING_DIR));
            settings.setDarkMode(Boolean.parseBoolean(props.getProperty(KEY_DARK_MODE, String.valueOf(SettingsConfig.DEFAULT_DARK_MODE))));
            settings.setAutoSave(Boolean.parseBoolean(props.getProperty(KEY_AUTO_SAVE, String.valueOf(SettingsConfig.DEFAULT_AUTO_SAVE))));
            
        } catch (IOException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            settings = new SettingsConfig(); // Fallback to defaults
        }

        // Validate and fix paths
        validateAndFixPaths();
    }

    private void validateAndFixPaths() {
        if (!settings.validateApktoolPath()) settings.setApktoolPath(SettingsConfig.DEFAULT_APKTOOL_PATH);
        if (!settings.validateApkEditorPath()) settings.setApkEditorPath(SettingsConfig.DEFAULT_APKEDITOR_PATH);
        if (!settings.validateAdbPath()) settings.setAdbPath(SettingsConfig.DEFAULT_ADB_PATH);
        if (!settings.validateZipalignPath()) settings.setZipalignPath(SettingsConfig.DEFAULT_ZIPALIGN_PATH);
        if (!settings.validateApksignerPath()) settings.setApksignerPath(SettingsConfig.DEFAULT_APKSIGNER_PATH);
        if (!settings.validateAaptPath()) settings.setAaptPath(SettingsConfig.DEFAULT_AAPT_PATH);
        if (!settings.validateAapt2Path()) settings.setAapt2Path(SettingsConfig.DEFAULT_AAPT2_PATH);
    }
    
    /**
     * Save current settings to properties file
     */
    public void saveSettings() throws IOException {
        Properties props = new Properties();
        
        props.setProperty(KEY_APKTOOL_PATH, settings.getApktoolPath());
        props.setProperty(KEY_APKEDITOR_PATH, settings.getApkEditorPath());
        props.setProperty(KEY_ADB_PATH, settings.getAdbPath());
        props.setProperty(KEY_ZIPALIGN_PATH, settings.getZipalignPath());
        props.setProperty(KEY_APKSIGNER_PATH, settings.getApksignerPath());
        props.setProperty(KEY_AAPT_PATH, settings.getAaptPath());
        props.setProperty(KEY_AAPT2_PATH, settings.getAapt2Path());
        props.setProperty(KEY_JAVA_PATH, settings.getJavaPath());
        props.setProperty(KEY_FRAMEWORK_PATH, settings.getFrameworkPath());
        props.setProperty(KEY_DEFAULT_DIR, settings.getDefaultWorkingDir());
        props.setProperty(KEY_DARK_MODE, String.valueOf(settings.isDarkMode()));
        props.setProperty(KEY_AUTO_SAVE, String.valueOf(settings.isAutoSave()));
        
        try (OutputStream out = Files.newOutputStream(configPath)) {
            props.store(out, "ApkTool GUI Settings");
        }
    }
    
    /**
     * Reset settings to default values
     */
    public void resetToDefaults() {
        settings = new SettingsConfig();
    }
    
    /**
     * Validate all configured tool paths
     * @return true if all paths are valid, false otherwise
     */
    public boolean validateAllPaths() {
        return settings.validateApktoolPath() &&
               settings.validateApkEditorPath() &&
               settings.validateAdbPath() &&
               settings.validateZipalignPath() &&
                settings.validateApksignerPath() &&
                settings.validateAaptPath() &&
                settings.validateAapt2Path() &&
               settings.validateJavaPath() &&
               settings.validateFrameworkPath();
    }
    
    public Path getConfigPath() {
        return configPath;
    }
}
