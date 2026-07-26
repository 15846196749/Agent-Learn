package com.agentlearning.stage1;

public class Main {
    public static void main(String[] args) {
        AppConfig config = AppConfig.load();

        Model model;
        if (OpenAiCompatibleModel.hasApiKey(config)) {
            OpenAiCompatibleModel realModel = OpenAiCompatibleModel.fromConfig(config);
            model = realModel;
            System.out.println("使用真实模型: " + realModel.modelName());
        } else {
            model = new FakeModel();
            System.out.println("未配置 OPENAI_API_KEY，使用 FakeModel 离线演示。");
        }

        AgentLoop agent = new AgentLoop(model, 5);
        agent.registerTool(new CalculatorTool());
        try {
            agent.registerTool(new RetrieveTool());
        } catch (Exception e) {
            System.out.println("注册检索工具失败: " + e.getMessage());
        }

        String userInput = args.length > 0
                ? joinArgs(args)
                // : "帮我计算 (23 + 19) * 7，然后解释过程";
                : "员工已经工作了3年,他的年假是多少天?";
        String answer = agent.run(userInput);

        System.out.println();
        System.out.println("final answer: " + answer);
    }

    private static String joinArgs(String[] args) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append(args[i]);
        }
        return out.toString();
    }
}
