
package org.apkutility.app.config;

public class ApkEditorRefactorConfig {
    private String publicXml;
    private boolean cleanMeta;
    private boolean fixTypes;

    // Getters and setters
    public String getPublicXml() { return publicXml; }
    public void setPublicXml(String publicXml) { this.publicXml = publicXml; }

    public boolean isCleanMeta() { return cleanMeta; }
    public void setCleanMeta(boolean cleanMeta) { this.cleanMeta = cleanMeta; }

    public boolean isFixTypes() { return fixTypes; }
    public void setFixTypes(boolean fixTypes) { this.fixTypes = fixTypes; }
}