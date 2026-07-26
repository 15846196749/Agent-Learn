package com.agentlearning.stage1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeModel implements Model {
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(".*计算\\s*(.+?)(?:，|,|。|$).*");
    private static final Pattern CALCULATOR_RESULT_PATTERN = Pattern.compile("(?s).*工具 calculator 返回结果:.*工具结果: (.+?)(?:\\n|$).*");
    private static final Pattern RETRIEVE_RESULT_PATTERN = Pattern.compile("(?s).*原始问题: (.*?)\\n工具 retrieve 返回结果:.*工具结果: (.*?)\\n请基于原始问题和工具结果判断下一步。.*");

    public ModelDecision decide(String observation) {
        Matcher calculatorResultMatcher = CALCULATOR_RESULT_PATTERN.matcher(observation);
        if (calculatorResultMatcher.matches()) {
            String result = calculatorResultMatcher.group(1).trim();
            return ModelDecision.finalAnswer("最终答案是 " + result + "。这是 calculator 工具计算后返回的结果。");
        }

        Matcher retrieveResultMatcher = RETRIEVE_RESULT_PATTERN.matcher(observation);
        if (retrieveResultMatcher.matches()) {
            String originalQuestion = retrieveResultMatcher.group(1).trim();
            String toolResult = retrieveResultMatcher.group(2).trim();
            return ModelDecision.finalAnswer(answerFromRetrievedText(originalQuestion, toolResult));
        }

        Matcher expressionMatcher = EXPRESSION_PATTERN.matcher(observation);
        if (expressionMatcher.matches()) {
            String expression = expressionMatcher.group(1).trim();
            return ModelDecision.toolCall(
                    "我需要调用 calculator 工具计算表达式。",
                    new ToolCall("calculator", expression)
            );
        }

        if (needsRetrieve(observation)) {
            return ModelDecision.toolCall(
                    "我需要调用 retrieve 工具从知识库检索相关规则。",
                    new ToolCall("retrieve", observation)
            );
        }

        return ModelDecision.finalAnswer("我暂时没有识别到需要调用工具的问题。");
    }

    private String answerFromRetrievedText(String originalQuestion, String toolResult) {
        if (originalQuestion.contains("3年") && toolResult.contains("工作已满一年未满十年") && toolResult.contains("年假5天")) {
            return "员工工作 3 年，属于工作已满一年未满十年的范围，因此年假是 5 天。";
        }

        return "根据知识库检索结果回答：" + toolResult;
    }

    private boolean needsRetrieve(String observation) {
        return observation.contains("年假")
                || observation.contains("病假")
                || observation.contains("报销")
                || observation.contains("规则")
                || observation.contains("政策");
    }
}