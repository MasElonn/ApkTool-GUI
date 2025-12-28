// models/ApkEditorProtectConfig.java
package org.apkutility.app.config;

public class ApkEditorProtectConfig {
    private String keepType;
    private boolean confuseZip;
    private String dicDirNames;
    private String dicFileNames;
    private boolean skipManifest;

    public ApkEditorProtectConfig() {
        this.keepType = "font";
    }

    // Getters and setters
    public String getKeepType() { return keepType; }
    public void setKeepType(String keepType) { this.keepType = keepType; }

    public boolean isConfuseZip() { return confuseZip; }
    public void setConfuseZip(boolean confuseZip) { this.confuseZip = confuseZip; }

    public String getDicDirNames() { return dicDirNames; }
    public void setDicDirNames(String dicDirNames) { this.dicDirNames = dicDirNames; }

    public String getDicFileNames() { return dicFileNames; }
    public void setDicFileNames(String dicFileNames) { this.dicFileNames = dicFileNames; }

    public boolean isSkipManifest() { return skipManifest; }
    public void setSkipManifest(boolean skipManifest) { this.skipManifest = skipManifest; }
}