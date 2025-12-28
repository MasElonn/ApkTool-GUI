package org.apkutility.app.config;

public class DecompileConfig {
        private boolean noRes = false;
        private boolean noSrc = false;
        private boolean noAssets = false;
        private boolean onlyManifest = false;
        private boolean force = false;
        private boolean noDebug = false;
        private boolean matchOriginal = false;
        private boolean keepBroken = false;
        private boolean onlyMainClasses = false;
        private String apiLevel = "";
        private String jobs = "1";
        private String frameworkPath = "";

        // Getters
        public boolean isNoRes() { return noRes; }
        public boolean isNoSrc() { return noSrc; }
        public boolean isNoAssets() { return noAssets; }
        public boolean isOnlyManifest() { return onlyManifest; }
        public boolean isForce() { return force; }
        public boolean isNoDebug() { return noDebug; }
        public boolean isMatchOriginal() { return matchOriginal; }
        public boolean isKeepBroken() { return keepBroken; }
        public boolean isOnlyMainClasses() { return onlyMainClasses; }
        public String getApiLevel() { return apiLevel; }
        public String getJobs() { return jobs; }
        public String getFrameworkPath() { return frameworkPath; }

        // Setters
        public void setNoRes(boolean value) { noRes = value; }
        public void setNoSrc(boolean value) { noSrc = value; }
        public void setNoAssets(boolean value) { noAssets = value; }
        public void setOnlyManifest(boolean value) { onlyManifest = value; }
        public void setForce(boolean value) { force = value; }
        public void setNoDebug(boolean value) { noDebug = value; }
        public void setMatchOriginal(boolean value) { matchOriginal = value; }
        public void setKeepBroken(boolean value) { keepBroken = value; }
        public void setOnlyMainClasses(boolean value) { onlyMainClasses = value; }
        public void setApiLevel(String value) { apiLevel = value; }
        public void setJobs(String value) { jobs = value; }
        public void setFrameworkPath(String value) { frameworkPath = value; }

}
