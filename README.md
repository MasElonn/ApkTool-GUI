# ApkTool GUI

A GUI wrapper for common Android reverse engineering tools. Built with JavaFX.

## Features

**APK Decompilation**
- Decode APKs with apktool (with options like no-resources, no-sources, force, etc.)
- Rebuild APKs from decompiled projects
- Manage framework files

**APK Signing**
- Sign with a debug key (auto-generated) or your own keystore
- Supports v1, v2, v3, v4 signature schemes
- Verify existing signatures

**APK Editor**
- Merge split APKs into a single APK
- Refactor obfuscated resources
- Protect/obfuscate resources

**AAPT/AAPT2**
- Dump badging, permissions, resources
- Dump XML tree
- List APK contents

**ADB**
- Connect to devices (USB and wireless)
- Install/uninstall apps
- Pull APKs from device
- Simple terminal for shell commands

**Other**
- ZipAlign support
- Dark and light themes
- Configurable tool paths

## Requirements

- Java 21 or higher

Optional tools (place in `lib/` or configure paths in Settings):
- `apktool.jar`
- `APKEditor.jar`
- `apksigner.jar`
- `aapt` / `aapt2`
- `zipalign`
- `platform-tools/adb`

## Build

```bash
./mvnw clean package
```

This creates `target/ApktoolGui-1.0-SNAPSHOT-shaded.jar`

## Run

```bash
java -jar target/ApktoolGui-1.0-SNAPSHOT-shaded.jar
```
