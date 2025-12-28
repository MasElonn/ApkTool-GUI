<div align="center">

# 📱 ApkTool GUI

**A modern, feature-rich GUI wrapper for common Android reverse engineering tools.**  
*Built with JavaFX and Passion*

![Java](https://img.shields.io/badge/Java-21+-orange?style=flat-square&logo=openjdk)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-green?style=flat-square)

</div>


---

## ✨ Features

<div align="center">

| 📦 **APK Decompilation** | 🔑 **APK Signing** |
|:---|:---|
| • Decode APKs with `apktool`<br>• Rebuild APKs from source<br>• Framework file management<br>• Custom decode options | • Sign with auto-generated debug keys<br>• Custom keystore support<br>• v1, v2, v3, v4 signature schemes<br>• Verify signatures |

| 🔧 **APK Editor** | 📱 **ADB Tools** |
|:---|:---|
| • Merge split APKs<br>• Refactor obfuscated resources<br>• Protect/Obfuscate resources | • Device connection manager<br>• Install/Uninstall apps<br>• Pull APKs directly from device<br>• Built-in shell terminal |

| 📄 **AAPT / AAPT2** | 🛠️ **Utilities** |
|:---|:---|
| • Dump badging & permissions<br>• Explore XML trees<br>• List APK contents | • `ZipAlign` integration<br>• Dark & Light themes<br>• Configurable tool paths |

</div>

## 🖼️ Screenshots

<div align="center">
  <img src="images/gui-dark.png" alt="Dark Theme" width="45%" border="1" />
  <img src="images/gui-light.png" alt="Light Theme" width="45%" border="1" />
  <br>
  <i>Experience both Dark and Light themes</i>
</div>

## 🚀 Releases

> [!NOTE]
> **Coming Soon**  
>
## 📋 Requirements

- **Java Runtime**: Java 21 or higher
- **External Tools** (Optional - placed in `resources/` or configured via Settings):
  - `apktool.jar`
  - `APKEditor.jar`
  - `apksigner.jar`
  - `aapt` / `aapt2`
  - `zipalign`
  - `platform-tools/adb`

## 🔨 Build & Run

**Build from source:**
```bash
./mvnw clean package
```
*Creates `target/ApktoolGui-1.0-SNAPSHOT-shaded.jar`*

**Run the application:**
```bash
java -jar target/ApktoolGui-1.0-SNAPSHOT-shaded.jar
```

---
## 📄 License

This project is licensed under the **MIT License**.  
See the [LICENSE](LICENSE) file for details.

