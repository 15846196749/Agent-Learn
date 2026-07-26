# Stage 1 Java Agent

这个文件夹是 Stage 1 的 Java 项目，用来实现一个最小 Agent Loop。

当前版本支持两种模式：

- 未配置 `OPENAI_API_KEY`：使用 `FakeModel` 离线模拟模型决策。
- 已配置 `OPENAI_API_KEY`：使用真实 OpenAI-compatible 模型接口。
- 注册一个 `calculator` 工具。
- 演示 `observe -> think -> act -> observe` 循环。

## 项目结构

```text
stage1-java-agent/
  .env.example
  .env
  pom.xml
  README.md
  src/main/java/com/agentlearning/stage1/
    Main.java
    AgentLoop.java
    Model.java
    AppConfig.java
    FakeModel.java
    OpenAiCompatibleModel.java
    JsonUtil.java
    Tool.java
    CalculatorTool.java
    ToolCall.java
    ModelDecision.java
```

## 配置 API Key

推荐使用本地 `.env` 文件。这个文件已经被项目根目录的 `.gitignore` 忽略，不应该提交。

编辑 `.env`：

```text
OPENAI_API_KEY=你的 API Key
OPENAI_MODEL=deepseek-v4-pro
OPENAI_BASE_URL=https://api.deepseek.com/v1
```

也可以用 PowerShell 临时配置：

```powershell
$env:OPENAI_API_KEY="你的 API Key"
$env:OPENAI_MODEL="deepseek-v4-pro"
$env:OPENAI_BASE_URL="https://api.deepseek.com/v1"
```

优先级：

```text
系统环境变量 > .env 文件 > 默认值
```

## 运行

```bash
mvn package
java -jar target/stage1-java-agent-1.0.0-SNAPSHOT.jar
```

也可以传入自己的问题：

```powershell
java -jar target/stage1-java-agent-1.0.0-SNAPSHOT.jar "帮我计算 (120 + 35) / 5，然后解释过程"
```

## Stage 1 学习目标

完成这个阶段后，你应该能解释：

- 什么是 tool registry
- 模型如何决定调用工具
- 程序如何执行工具
- 工具结果如何回到 agent loop
- 为什么 agent loop 需要最大步数和错误处理

## Stage 2 最小 RAG 扩展

当前项目已经增加了一个最小检索工具：

- `RetrieveTool`：从本地知识库检索相关规则。
- `src/main/resources/docs/knowledge.txt`：本地知识库。
- `AgentLoop`：会把原始问题、工具名、工具参数和工具结果一起回灌给模型。

示例问题：

```text
员工已经工作了3年,他的年假是多少天?
```

最小数据流：

```text
用户问题 -> LLM/FakeModel 决定调用 retrieve -> Java 检索知识库 -> 检索结果回灌给模型 -> 输出最终答案
```