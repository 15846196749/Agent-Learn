package com.agentlearning.stage1;

public class ToolCall {
    private final String toolName;
    private final String argument;

    public ToolCall(String toolName, String argument) {
        this.toolName = toolName;
        this.argument = argument;
    }

    public String toolName() {
        return toolName;
    }

    public String argument() {
        return argument;
    }
}
