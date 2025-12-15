
package org.codex.apktoolgui.models;

public class ApkEditorDecompileConfig {
    private boolean decompileToXml;
    private boolean loadDex;
    private String dexLibrary;

    public ApkEditorDecompileConfig() {
        this.dexLibrary = "jf";
    }

    // Getters and setters
    public boolean isDecompileToXml() { return decompileToXml; }
    public void setDecompileToXml(boolean decompileToXml) { this.decompileToXml = decompileToXml; }

    public boolean isLoadDex() { return loadDex; }
    public void setLoadDex(boolean loadDex) { this.loadDex = loadDex; }

    public String getDexLibrary() { return dexLibrary; }
    public void setDexLibrary(String dexLibrary) { this.dexLibrary = dexLibrary; }
}