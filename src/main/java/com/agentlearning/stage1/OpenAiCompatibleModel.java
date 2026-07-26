package com.agentlearning.stage1;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAiCompatibleModel implements Model {
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public OpenAiCompatibleModel(String apiKey, String model, String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public static OpenAiCompatibleModel fromConfig(AppConfig config) {
        String apiKey = config.get("OPENAI_API_KEY");
        String model = config.getOrDefault("OPENAI_MODEL", "gpt-4o-mini");
        String baseUrl = config.getOrDefault("OPENAI_BASE_URL", "https://api.openai.com/v1");
        return new OpenAiCompatibleModel(apiKey, model, baseUrl);
    }

    public static boolean hasApiKey(AppConfig config) {
        return config.has("OPENAI_API_KEY");
    }

    public String modelName() {
        return model;
    }

    public ModelDecision decide(String observation) throws Exception {
        String content = callChatCompletions(observation);
        String json = JsonUtil.extractFirstJsonObject(content);
        if (json == null) {
            throw new IllegalStateException("模型没有返回 JSON: " + content);
        }

        String type = JsonUtil.extractStringField(json, "type");
        String reasoning = JsonUtil.extractStringField(json, "reasoning");
        if ("tool_call".equals(type)) {
            String toolName = JsonUtil.extractStringField(json, "tool_name");
            String argument = JsonUtil.extractStringField(json, "argument");
            return ModelDecision.toolCall(reasoning, new ToolCall(toolName, argument));
        }

        if ("final_answer".equals(type)) {
            String answer = JsonUtil.extractStringField(json, "answer");
            return ModelDecision.finalAnswer(answer, reasoning);
        }

        throw new IllegalStateException("未知模型决策类型: " + type + ", 原始内容: " + content);
    }

    private String callChatCompletions(String observation) throws Exception {
        URL url = new URL(baseUrl + "/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(60_000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);

        String body = buildRequestBody(observation);
        logRequest(url, body);

        OutputStream output = connection.getOutputStream();
        output.write(body.getBytes(StandardCharsets.UTF_8));
        output.close();

        int statusCode = connection.getResponseCode();
        String responseBody = readAll(statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
        logResponse(statusCode, responseBody);

        if (statusCode >= 400) {
            throw new IllegalStateException("模型 API 请求失败，HTTP " + statusCode + ": " + responseBody);
        }

        String content = JsonUtil.extractStringField(responseBody, "content");
        if (content == null) {
            throw new IllegalStateException("模型响应里没有 content 字段: " + responseBody);
        }
        logModelContent(content);
        return content;
    }

    private void logRequest(URL url, String body) {
        System.out.println();
        System.out.println("========== LLM REQUEST ==========");
        System.out.println("POST " + url);
        System.out.println("Content-Type: application/json; charset=UTF-8");
        System.out.println("Authorization: Bearer ***");
        System.out.println();
        System.out.println(body);
        System.out.println("======== END LLM REQUEST ========");
        System.out.println();
    }

    private void logResponse(int statusCode, String responseBody) {
        System.out.println();
        System.out.println("========== LLM RESPONSE =========");
        System.out.println("HTTP " + statusCode);
        System.out.println();
        System.out.println(responseBody);
        System.out.println("======== END LLM RESPONSE =======");
        System.out.println();
    }

    private void logModelContent(String content) {
        System.out.println();
        System.out.println("========== LLM CONTENT ==========");
        System.out.println(content);
        System.out.println("======== END LLM CONTENT ========");
        System.out.println();
    }

    private String buildRequestBody(String observation) {
        String systemPrompt =
                "你是一个最小 Agent Loop 的决策模型。"
                        + "你每次只做一步决策，并且只能返回一个 JSON 对象，不要返回 Markdown。"
                        + "可用工具有两个："
                        + "1. calculator：用于计算数学表达式，argument 必须是纯数学表达式。"
                        + "2. retrieve：用于从本地知识库检索政策、规则、资料，argument 应该是用户问题中的关键词或原始问题。"
                        + "如果需要调用工具，只能返回："
                        + "{\"type\":\"tool_call\",\"reasoning\":\"为什么需要工具\",\"tool_name\":\"calculator或retrieve\",\"argument\":\"工具参数\"}。"
                        + "如果当前观察包含工具结果，请结合原始问题、工具参数和工具结果回答。"
                        + "如果可以结束，只能返回："
                        + "{\"type\":\"final_answer\",\"reasoning\":\"为什么可以结束\",\"answer\":\"最终答案\"}。"
                        + "不要编造知识库里没有的信息；如果检索结果不足，请在 answer 中说明无法确定。";

        return "{"
                + "\"model\":\"" + JsonUtil.escape(model) + "\","
                + "\"temperature\":0,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + JsonUtil.escape(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"当前观察: " + JsonUtil.escape(observation) + "\"}"
                + "]"
                + "}";
    }

    private static String readAll(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "https://api.openai.com/v1";
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
