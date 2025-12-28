package org.apkutility.app.config;

public class RecompileConfig {
        private boolean debug = false;
        private boolean copyOriginal = false;
        private boolean force = false;
        private boolean noApk = false;
        private boolean noCrunch = false;
        private boolean useAapt1 = false;
        private boolean netSec = false;
        private String aaptPath = "";
        private String frameworkPath = "";

        // Getters
        public boolean isDebug() { return debug; }
        public boolean isCopyOriginal() { return copyOriginal; }
        public boolean isForce() { return force; }
        public boolean isNoApk() { return noApk; }
        public boolean isNoCrunch() { return noCrunch; }
        public boolean isUseAapt1() { return useAapt1; }
        public boolean isNetSec() { return netSec; }
        public String getAaptPath() { return aaptPath; }
        public String getFrameworkPath() { return frameworkPath; }

        // Setters
        public void setDebug(boolean value) { debug = value; }
        public void setCopyOriginal(boolean value) { copyOriginal = value; }
        public void setForce(boolean value) { force = value; }
        public void setNoApk(boolean value) { noApk = value; }
        public void setNoCrunch(boolean value) { noCrunch = value; }
        public void setUseAapt1(boolean value) { useAapt1 = value; }
        public void setNetSec(boolean value) { netSec = value; }
        public void setAaptPath(String value) { aaptPath = value; }
        public void setFrameworkPath(String value) { frameworkPath = value; }

}
