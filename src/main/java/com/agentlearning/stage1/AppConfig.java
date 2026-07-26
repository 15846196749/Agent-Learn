package com.agentlearning.stage1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AppConfig {
    private final Map<String, String> dotEnvValues;

    private AppConfig(Map<String, String> dotEnvValues) {
        this.dotEnvValues = dotEnvValues;
    }

    public static AppConfig load() {
        return new AppConfig(loadDotEnv(new File(".env")));
    }

    public String get(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        String dotEnvValue = dotEnvValues.get(key);
        if (dotEnvValue != null && !dotEnvValue.trim().isEmpty()) {
            return dotEnvValue.trim();
        }

        return null;
    }

    public String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    public boolean has(String key) {
        String value = get(key);
        return value != null && !value.trim().isEmpty();
    }

    private static Map<String, String> loadDotEnv(File file) {
        Map<String, String> values = new HashMap<String, String>();
        if (!file.exists() || !file.isFile()) {
            return values;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, values);
            }
        } catch (Exception ex) {
            System.out.println("读取 .env 失败，将继续使用系统环境变量: " + ex.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }

        return values;
    }

    private static void parseLine(String line, Map<String, String> values) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        int equalsIndex = trimmed.indexOf('=');
        if (equalsIndex <= 0) {
            return;
        }

        String key = stripBom(trimmed.substring(0, equalsIndex).trim());
        String value = trimmed.substring(equalsIndex + 1).trim();
        values.put(key, stripQuotes(value));
    }

    private static String stripBom(String value) {
        if (value != null && value.startsWith("\uFEFF")) {
            return value.substring(1);
        }
        return value;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
