package org.apkutility.app.config;

public class ApkEditorBuildConfig {
    private boolean buildFromXml;
    private String dexLibrary;

    public ApkEditorBuildConfig() {
        this.dexLibrary = "jf";
    }

    // Getters and setters
    public boolean isBuildFromXml() { return buildFromXml; }
    public void setBuildFromXml(boolean buildFromXml) { this.buildFromXml = buildFromXml; }

    public String getDexLibrary() { return dexLibrary; }
    public void setDexLibrary(String dexLibrary) { this.dexLibrary = dexLibrary; }
}