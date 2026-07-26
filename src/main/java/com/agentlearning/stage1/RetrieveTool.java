package com.agentlearning.stage1;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RetrieveTool implements Tool {
    private static final String KNOWLEDGE_RESOURCE = "/docs/knowledge.txt";

    private final List<String> documents;

    public RetrieveTool() {
        this.documents = loadDocuments();
    }

    public String name() {
        return "retrieve";
    }

    public String description() {
        return "从知识库检索和用户问题相关的信息，例如年假、病假、报销规则。";
    }

    public String execute(String argument) {
        if (argument == null || argument.trim().isEmpty()) {
            throw new IllegalArgumentException("检索问题不能为空");
        }

        System.out.println("检索信息：" + argument);

        SearchResult best = null;
        for (String document : documents) {
            int score = score(document, argument);
            if (best == null || score > best.score) {
                best = new SearchResult(document, score);
            }
        }

        if (best == null || best.score <= 0) {
            return "未找到相关信息。检索问题: " + argument;
        }

        return "检索问题: " + argument + "\n"
                + "命中分数: " + best.score + "\n"
                + "来源: " + KNOWLEDGE_RESOURCE + "\n"
                + "内容: " + best.text;
    }

    private static List<String> loadDocuments() {
        InputStream inputStream = RetrieveTool.class.getResourceAsStream(KNOWLEDGE_RESOURCE);
        if (inputStream == null) {
            throw new IllegalStateException("无法读取知识库资源: " + KNOWLEDGE_RESOURCE);
        }

        List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.add(trimmed);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("读取知识库失败: " + ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }

        if (lines.isEmpty()) {
            throw new IllegalStateException("知识库为空: " + KNOWLEDGE_RESOURCE);
        }

        return lines;
    }

    private static int score(String document, String query) {
        int score = 0;
        String normalizedDocument = normalize(document);
        String normalizedQuery = normalize(query);

        for (String token : extractTokens(normalizedQuery)) {
            if (normalizedDocument.contains(token)) {
                score += token.length() >= 2 ? 2 : 1;
            }
        }

        Integer years = extractYears(normalizedQuery);
        if (years != null && normalizedDocument.contains("年")) {
            score += 3;
        }

        if (normalizedQuery.contains("年假") && normalizedDocument.contains("年假")) {
            score += 5;
        }
        if (normalizedQuery.contains("病假") && normalizedDocument.contains("病假")) {
            score += 5;
        }
        if (normalizedQuery.contains("报销") && normalizedDocument.contains("报销")) {
            score += 5;
        }

        return score;
    }

    private static List<String> extractTokens(String value) {
        List<String> tokens = new ArrayList<String>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c) || isChinese(c)) {
                current.append(c);
            } else {
                addToken(tokens, current);
            }
        }
        addToken(tokens, current);

        return tokens;
    }

    private static void addToken(List<String> tokens, StringBuilder current) {
        if (current.length() == 0) {
            return;
        }

        String token = current.toString();
        if (token.length() >= 2 && !isStopWord(token)) {
            tokens.add(token);
        }
        current.setLength(0);
    }

    private static boolean isStopWord(String token) {
        return "员工".equals(token)
                || "已经".equals(token)
                || "多少".equals(token)
                || "是什么".equals(token)
                || "请问".equals(token);
    }

    private static Integer extractYears(String query) {
        for (int i = 0; i < query.length(); i++) {
            if (!Character.isDigit(query.charAt(i))) {
                continue;
            }

            int start = i;
            while (i < query.length() && Character.isDigit(query.charAt(i))) {
                i++;
            }

            if (i < query.length() && query.charAt(i) == '年') {
                return Integer.parseInt(query.substring(start, i));
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fff';
    }

    private static class SearchResult {
        private final String text;
        private final int score;

        private SearchResult(String text, int score) {
            this.text = text;
            this.score = score;
        }
    }
}