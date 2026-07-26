package com.agentlearning.stage1;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class CalculatorTool implements Tool {
    private final ScriptEngine engine;

    public CalculatorTool() {
        this.engine = new ScriptEngineManager().getEngineByName("JavaScript");
    }

    public String name() {
        return "calculator";
    }

    public String description() {
        return "计算简单的数学表达式，例如 (23 + 19) * 7";
    }

    public String execute(String argument) throws Exception {
        if (argument == null || argument.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }

        Object value = engine.eval(argument);
        return String.valueOf(value);
    }
}
