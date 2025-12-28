package org.apkutility.app.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkInfoParser {

    public static class ApkInfo {
        public Map<String, String> generalInfo = new LinkedHashMap<>();
        public List<String> permissions = new ArrayList<>();
        public List<String> configurations = new ArrayList<>();
        public List<String> languages = new ArrayList<>();
        public List<String> locales = new ArrayList<>();
        public List<String> dexBlocks = new ArrayList<>();
        public String certificateInfo = "";
    }

    // Regex patterns
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([^=\\s]+)=\"([^\"]*)\"$");
    private static final Pattern LIST_HEADER_PATTERN = Pattern.compile("^(.+)\\s+\\[\\s*count\\s+(\\d+)\\s*\\]$");
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("^\\s+\\d+\\)\\s+(.+)$");
    private static final Pattern DEX_HEADER_PATTERN = Pattern.compile("^Name=\"(.+\\.dex)\"$");
    private static final Pattern CERT_HEADER_PATTERN = Pattern.compile("^Certificates=\"(\\d+)\"$");

    public static ApkInfo parse(String rawOutput) {
        ApkInfo info = new ApkInfo();
        if (rawOutput == null || rawOutput.isEmpty()) return info;

        String[] lines = rawOutput.split("\\r?\\n");

        // State parsing
        String currentSection = null;
        StringBuilder currentBlock = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // 1. Check for specific block headers that change state
            Matcher dexMatcher = DEX_HEADER_PATTERN.matcher(line.trim()); // trim again just in case
            if (dexMatcher.matches()) {
                saveBlock(info, currentSection, currentBlock);
                currentSection = "DEX";
                currentBlock.setLength(0);
                currentBlock.append(line).append("\n");
                continue;
            }

            Matcher certMatcher = CERT_HEADER_PATTERN.matcher(line.trim());
            if (certMatcher.matches()) {
                saveBlock(info, currentSection, currentBlock);
                currentSection = "CERT";
                currentBlock.setLength(0);
                currentBlock.append(line).append("\n");
                continue;
            }

            // 2. Check for List Headers (e.g., uses-permission [ count 10])
            Matcher listHeaderMatcher = LIST_HEADER_PATTERN.matcher(line.trim());
            if (listHeaderMatcher.matches()) {
                saveBlock(info, currentSection, currentBlock);
                currentSection = listHeaderMatcher.group(1).trim(); // e.g., "uses-permission"
                currentBlock.setLength(0);
                continue;
            }

            // 3. Handle Content based on current section
            if (currentSection != null) {
                switch (currentSection) {
                    case "uses-permission":
                        extractListItem(line, info.permissions);
                        break;
                    case "configurations":
                        extractListItem(line, info.configurations);
                        break;
                    case "languages":
                        extractListItem(line, info.languages);
                        break;
                    case "locales":
                        extractListItem(line, info.locales);
                        break;
                    case "DEX":
                    case "CERT":
                        currentBlock.append(line).append("\n");
                        break;
                    default:
                        // Unknown list or block
                        break;
                }
            } else {
                // 4. Default / General Info (Key="Value")
                Matcher kvMatcher = KEY_VALUE_PATTERN.matcher(trimmed);
                if (kvMatcher.matches()) {
                    info.generalInfo.put(kvMatcher.group(1), kvMatcher.group(2));
                }
            }
        }

        // Save any remaining block at the end
        saveBlock(info, currentSection, currentBlock);

        return info;
    }

    private static void saveBlock(ApkInfo info, String section, StringBuilder block) {
        if (section == null || block.length() == 0) return;

        if (section.equals("DEX")) {
            info.dexBlocks.add(block.toString());
        } else if (section.equals("CERT")) {
            info.certificateInfo = block.toString();
        }
    }

    private static void extractListItem(String line, List<String> targetList) {
        Matcher m = LIST_ITEM_PATTERN.matcher(line);
        if (m.matches()) {
            targetList.add(m.group(1).trim());
        }
    }
}