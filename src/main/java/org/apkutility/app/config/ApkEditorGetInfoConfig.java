package org.apkutility.app.config;

public class ApkEditorGetInfoConfig {
        private String filterType;
        private String framework;
        private String frameworkVersion;
        private String resourceId;
        private String xmlStrings;
        private String xmlTree;
        private String outputType;
        private boolean activities = true;
        private boolean appClass=true;
        private boolean appIcon=true;
        private boolean appName=true;
        private boolean appRoundIcon=false;
        private boolean configurations=true;
        private boolean dex=true;
        private boolean forceDelete=false;
        private boolean languages=false;
        private boolean listFiles=false;
        private boolean listXmlFiles=false;
        private boolean locales=false;
        private boolean minSdkVersion=true;
        private boolean packageInfo=true;
        private boolean permissions=true;
        private boolean resources=false;
        private boolean signatures=true;
        private boolean signaturesBase64=true;
        private boolean targetSdkversion=true;
        private boolean versioncode=true;
         private boolean versionName=true;

    //setters
    public void setActivities(boolean value){activities=value;}
    public void setAppClass(boolean value){appClass=value;}
    public void setAppIcon(boolean value){appIcon=value;}
    public void setAppName(boolean value){appName=value;}
    public void setAppRoundIcon(boolean value){appRoundIcon=value;}
    public void setConfigurations(boolean value){configurations=value;}
    public void setDex(boolean value){dex=value;}
    public void setForceDelete(boolean value){forceDelete=value;}
    public void setLanguages(boolean value){languages=value;}
    public void setListFiles(boolean value){listFiles=value;}
    public void setListXmlFiles(boolean value){listXmlFiles=value;}
    public void setLocales(boolean value){locales=value;}
    public void setMinSdkVersion(boolean value){minSdkVersion=value;}
    public void setPackageInfo(boolean value){packageInfo=value;}
    public void setPermissions(boolean value){permissions=value;}
    public void setResources(boolean value){resources=value;}
    public void setSignatures(boolean value){signatures=value;}
    public void setSignaturesBase64(boolean value){signaturesBase64=value;}
    public void setTargetSdkVersion(boolean value){targetSdkversion=value;}
    public void setVersionCode(boolean value){versioncode=value;}
    public void setVersionName(boolean value){versionName=value;}

    //getters
    // getters
    public boolean isActivities() { return activities; }
    public boolean isAppClass() { return appClass; }
    public boolean isAppIcon() { return appIcon; }
    public boolean isAppName() { return appName; }
    public boolean isAppRoundIcon() { return appRoundIcon; }
    public boolean isConfigurations() { return configurations; }
    public boolean isDex() { return dex; }
    public boolean isForceDelete() { return forceDelete; }
    public boolean isLanguages() { return languages; }
    public boolean isListFiles() { return listFiles; }
    public boolean isListXmlFiles() { return listXmlFiles; }
    public boolean isLocales() { return locales; }
    public boolean isMinSdkVersion() { return minSdkVersion; }
    public boolean isPackageInfo() { return packageInfo; }
    public boolean isPermissions() { return permissions; }
    public boolean isResources() { return resources; }
    public boolean isSignatures() { return signatures; }
    public boolean isSignaturesBase64() { return signaturesBase64; }
    public boolean isTargetSdkVersion() { return targetSdkversion; }
    public boolean isVersionCode() { return versioncode; }
    public boolean isVersionName() { return versionName; }

}
