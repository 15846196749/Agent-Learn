package com.agentlearning.stage1;

public class ModelDecision {
    private final String reasoning;
    private final ToolCall toolCall;
    private final String finalAnswer;

    private ModelDecision(String reasoning, ToolCall toolCall, String finalAnswer) {
        this.reasoning = reasoning;
        this.toolCall = toolCall;
        this.finalAnswer = finalAnswer;
    }

    public static ModelDecision toolCall(String reasoning, ToolCall toolCall) {
        return new ModelDecision(reasoning, toolCall, null);
    }

    public static ModelDecision finalAnswer(String finalAnswer) {
        return new ModelDecision("已经可以输出最终答案。", null, finalAnswer);
    }

    public static ModelDecision finalAnswer(String finalAnswer, String reasoning) {
        return new ModelDecision(reasoning, null, finalAnswer);
    }

    public String reasoning() {
        return reasoning;
    }

    public ToolCall toolCall() {
        return toolCall;
    }

    public boolean isFinalAnswer() {
        return finalAnswer != null;
    }

    public String finalAnswer() {
        return finalAnswer;
    }
}
