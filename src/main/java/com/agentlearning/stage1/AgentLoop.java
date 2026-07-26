package com.agentlearning.stage1;

import java.util.HashMap;
import java.util.Map;

public class AgentLoop {
    private final Model model;
    private final Map<String, Tool> tools;
    private final int maxSteps;

    public AgentLoop(Model model, int maxSteps) {
        this.model = model;
        this.maxSteps = maxSteps;
        this.tools = new HashMap<String, Tool>();
    }

    public void registerTool(Tool tool) {
        tools.put(tool.name(), tool);
    }

    public String run(String userInput) {
        String originalInput = userInput;
        String observation = userInput;

        for (int step = 1; step <= maxSteps; step++) {
            System.out.println("[step " + step + "] observe: " + observation);

            ModelDecision decision;
            try {
                decision = model.decide(observation);
            } catch (Exception ex) {
                return "模型调用失败: " + ex.getMessage();
            }

            System.out.println("[step " + step + "] think: " + decision.reasoning());

            if (decision.isFinalAnswer()) {
                return decision.finalAnswer();
            }

            ToolCall toolCall = decision.toolCall();
            Tool tool = tools.get(toolCall.toolName());
            if (tool == null) {
                return "工具不存在: " + toolCall.toolName();
            }

            try {
                System.out.println("[step " + step + "] act: call " + toolCall.toolName() + "(" + toolCall.argument() + ")");
                String result = tool.execute(toolCall.argument());
                observation = "原始问题: " + originalInput + "\n"
                        + "工具 " + toolCall.toolName() + " 返回结果:\n"
                        + "工具参数: " + toolCall.argument() + "\n"
                        + "工具结果: " + result + "\n"
                        + "请基于原始问题和工具结果判断下一步。";
            } catch (Exception ex) {
                observation = "原始问题: " + originalInput + "\n"
                        + "工具 " + toolCall.toolName() + " 执行失败:\n"
                        + "工具参数: " + toolCall.argument() + "\n"
                        + "错误信息: " + ex.getMessage() + "\n"
                        + "请判断是否需要修正参数、换工具，或返回失败说明。";
            }
        }

        return "达到最大循环次数，任务未完成。";
    }
}