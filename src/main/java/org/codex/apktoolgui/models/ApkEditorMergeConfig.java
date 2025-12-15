package org.codex.apktoolgui.models;

public class ApkEditorMergeConfig {
    private String resDir;
    private String extractNativeLibs;
    private boolean cleanMeta;
    private boolean validateModules;
    private boolean vrd;

    public ApkEditorMergeConfig() {
        this.extractNativeLibs = "manifest";
    }

    // Getters and setters
    public String getResDir() { return resDir; }
    public void setResDir(String resDir) { this.resDir = resDir; }

    public String getExtractNativeLibs() { return extractNativeLibs; }
    public void setExtractNativeLibs(String extractNativeLibs) { this.extractNativeLibs = extractNativeLibs; }

    public boolean isCleanMeta() { return cleanMeta; }
    public void setCleanMeta(boolean cleanMeta) { this.cleanMeta = cleanMeta; }

    public boolean isValidateModules() { return validateModules; }
    public void setValidateModules(boolean validateModules) { this.validateModules = validateModules; }

    public boolean isVrd() { return vrd; }
    public void setVrd(boolean vrd) { this.vrd = vrd; }
}