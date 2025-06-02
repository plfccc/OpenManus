# OpenManus Java 重制版项目 (中文)

## 1. 项目简介

### 项目目标
旨在使用 Java 和 Spring Boot 2.7.x 框架重新实现 OpenManus 项目（最初基于 Python）的核心功能。目标是创建一个强大、可扩展且可维护的 Java 应用程序，以反映 OpenManus 的能力，例如 AI 驱动的任务管理、LLM 集成和工具执行（例如，浏览器自动化、代码执行）。

### 范围

#### 范围内：
*   用于任务接收和基本规划/编排的核心代理逻辑。
*   与大型语言模型 (LLM) 集成，用于理解、规划和生成。
*   关键工具的实现：浏览器自动化和代码执行机制（例如，用于数据分析任务）。
*   API 密钥、模型参数等的配置管理。
*   用于与系统交互的 RESTful API（例如，提交任务）。
*   涵盖设置、设计和 API 使用的综合文档。

#### 范围外（初始版本）：
*   直接复制所有现有的 Python 脚本和特定算法，如果它们不是核心功能的核心。
*   超出基础编排能力的高级多代理功能（除非“MCP”或“flow”意味着一个核心的、可复制的多代理系统）。
*   复杂的 UI/前端（重点是后端 API 和引擎）。
*   OpenManus-RL 中提到的特定 RL（强化学习）调整方面，除非基本版本是核心 OpenManus 的组成部分。

## 2. 技术栈选择及基本原理

*   **后端框架：** Spring Boot 2.7.x
    *   *原因：* 按规定。它是一个成熟且广泛使用的框架，提供快速应用程序开发、嵌入式服务器以及对微服务和 Web 应用程序的广泛支持。
*   **Java 版本：** JDK 17 (LTS)
    *   *原因：* Spring Boot 2.7.x 与 JDK 17 完全兼容。JDK 17 相较于 JDK 8 提供了显著改进，包括新的语言特性（记录、密封类、instanceof 的模式匹配）、性能增强（例如 ZGC、Shenandoah GC 改进）和长期支持。虽然 Spring Boot 2.7 可以在 JDK 8 上运行，但 JDK 17 提供了更现代的开发体验和更好的未来保障。对于新开始的项目，通常建议利用最新的 LTS 版本。
    *   *Spring Boot 2.7 的适配注意事项：* 确保所有选定的库都与 JDK 17 兼容。一些较旧的库可能会有问题，但 Spring Boot 生态系统本身很好地处理了这一点。标准的 Spring Boot 启动器和常用库通常没有问题。
*   **数据库：**
    *   *推荐：* PostgreSQL (例如，版本 14 或更新版本)
        *   *原因：* 一个强大、开源的对象关系数据库系统，以其可靠性、功能鲁棒性和数据完整性而享有盛誉。它能很好地处理复杂查询和大型数据集，适用于可能存储任务详细信息、代理状态或日志的应用程序。
    *   *开发/测试替代方案：* H2 数据库
        *   *原因：* 一种内存中的 Java SQL 数据库，非常轻量且快速。非常适合本地开发、运行自动化测试和快速原型设计，无需单独的数据库服务器。Spring Boot 对 H2 有出色的自动配置支持。
*   **构建工具：** Apache Maven
    *   *原因：* Java 生态系统中成熟且广泛采用的构建自动化和依赖管理工具。它强制执行标准的项目结构，简化了构建过程，并与 IDE 和 CI/CD 系统良好集成。Gradle 是一个强大的替代方案，但在许多企业和开源项目中，Maven 的普及性及其基于 XML 的声明式配置因其直接性而通常更受青睐。
*   **其他潜在技术（将在模块设计中详述）：**
    *   **LLM 集成：** Spring AI（如果足够成熟并与 Spring Boot 2.7 对齐）或直接使用 LLM 提供商的 SDK（例如 OpenAI Java SDK）。
    *   **Web 自动化：** Selenium WebDriver for Java 或 Playwright for Java。
    *   **异步处理/任务队列（可选，用于可扩展性）：** RabbitMQ 或 Apache Kafka。
    *   **缓存（可选）：** Redis。
    *   **容器化：** Docker。

## 3. 环境设置指南

### 3.1. JDK 安装 (JDK 17)
*   从信誉良好的来源（例如 Adoptium Temurin, Oracle OpenJDK）下载 JDK 17。
*   遵循您操作系统的安装说明。
*   确保 `JAVA_HOME` 环境变量设置为 JDK 17 的安装目录。
*   通过在终端中运行 `java -version` 和 `javac -version` 来验证安装。

### 3.2. Maven 安装 (最新稳定版)
*   从 Apache Maven 官方网站下载。
*   将压缩包解压到合适的目录。
*   将 Maven 的 `bin` 目录添加到系统的 `PATH` 环境变量中。
*   通过在终端中运行 `mvn -version` 来验证安装。

### 3.3. IDE 推荐
*   *推荐：* IntelliJ IDEA (社区版或旗舰版)
    *   *原因：* 对 Spring Boot 和 Maven 有出色的支持，强大的调试工具和代码辅助功能。
*   *配置：*
    *   安装 IDE。
    *   确保将其配置为使用已安装的 JDK 17。
    *   项目创建后导入 Maven 项目。

### 3.4. 数据库设置

#### 3.4.1. PostgreSQL (用于开发/生产)
*   为您的操作系统下载并安装 PostgreSQL。
*   在安装过程中，为默认的 `postgres` 用户设置密码。
*   使用 `psql` 之类的工具或 GUI（例如 pgAdmin, DBeaver）为应用程序创建一个数据库（例如 `openmanus_db`）。
*   记下数据库 URL (例如 `jdbc:postgresql://localhost:5432/openmanus_db`)、用户名和密码，以用于 Spring Boot 配置。

#### 3.4.2. H2 数据库 (用于快速本地开发/测试)
*   如果用作嵌入式数据库，通常不需要单独安装。Spring Boot 配合 `com.h2database` 依赖项，可以自动配置内存数据库。
*   要启用 H2 控制台（用于在开发过程中检查数据库），您需要在 `application.properties` 中添加特定属性（例如 `spring.h2.console.enabled=true`，`spring.h2.console.path=/h2-console`）。

### 3.5. 其他依赖项 (初始设置 - 可选)
*   如果使用 Docker 在本地管理 PostgreSQL、Redis 或消息队列等服务，请安装 Docker Desktop（或 Linux 上的 Docker Engine）。

## 4. 项目结构和模块划分

*   **4.1. 处理原始 Python 代码**
    *   如果您有原始的 OpenManus Python 代码库，建议将其与这个新的 Java 项目分开存放。您可以将其放置在一个单独的目录中，例如 `../openmanus-python-original`，或者在主项目目录内的一个名为 `legacy/openmanus_python` 的子文件夹中，但要确保它被排除在 Java 构建之外（例如，如果它位于 Java 项目的 git 仓库的子文件夹中，则通过 `.gitignore` 排除）。
    *   新的 Java 项目应该是基于 OpenManus 的*核心功能*进行全新、从零开始的实现，而不是直接逐行翻译。

*   **4.2. 推荐的 Java 项目结构 (Maven 多模块)**
    *   推荐使用多模块 Maven 项目，以便随着项目的增长实现更好的组织、关注点分离和可维护性。
    *   **父 POM (`openmanus-java/pom.xml`):**
        *   管理所有子模块的通用依赖项、插件和属性。
        *   打包类型: `pom`。
        *   `<modules>` 部分列出所有子模块。

    *   **子模块:**
        *   **`openmanus-app` (或 `openmanus-main`)**:
            *   *描述:* 主应用程序模块。包含 Spring Boot 应用程序类 (`@SpringBootApplication`) 和用于将所有内容连接在一起的配置。该模块将依赖于所有其他相关模块 (`api`, `core`, `llm-integration`, `tool-executor`)。
            *   *打包:* `jar` (可执行的 Spring Boot fat jar)。
            *   *职责:* 应用程序入口点、全局配置、模块装配。
        *   **`openmanus-api`**:
            *   *描述:* 定义用于与 OpenManus Java 应用程序交互的外部 RESTful API。
            *   *打包:* `jar`。
            *   *关键组件:* Spring MVC 控制器 (`@RestController`)、用于 API 请求/响应的数据传输对象 (DTO)、API 文档设置 (例如 OpenAPI/Swagger)。
            *   *依赖:* `openmanus-core`。
        *   **`openmanus-core`**:
            *   *描述:* 包含 OpenManus 代理的核心业务逻辑、领域模型、服务接口和核心编排逻辑。
            *   *打包:* `jar`。
            *   *关键组件:* 服务类 (`@Service`)、领域实体/模型 (POJO)、仓库接口 (如果核心实体如任务或代理状态需要数据持久化)、核心算法和工作流定义。
            *   *依赖:* `openmanus-llm-integration`, `openmanus-tool-executor`, 持久化相关依赖 (例如 Spring Data JPA)。
        *   **`openmanus-llm-integration`**:
            *   *描述:* 处理与大型语言模型 (LLM) 的所有交互。
            *   *打包:* `jar`。
            *   *关键组件:* 特定 LLM 提供商的客户端或服务 (例如 OpenAI)、LLM 交互的请求/响应映射、提示模板/管理。
            *   *依赖:* 相关的 LLM SDK (例如 OpenAI Java SDK, Spring AI 启动器)、HTTP 客户端库 (如果需要)。
        *   **`openmanus-tool-executor`**:
            *   *描述:* 管理和执行 AI 代理可以使用的各种工具 (例如浏览器自动化、代码执行)。
            *   *打包:* `jar`。
            *   *关键组件:* 通用工具接口 (`Tool.java`)、具体工具实现 (例如 `BrowserAutomationTool.java`, `CodeExecutionTool.java`)、沙箱机制 (如果适用)。
            *   *依赖:* 特定工具的库 (例如 Selenium/Playwright, 用于进程执行的库)。
        *   **`openmanus-config` (可选, 初期可作为 `openmanus-app` 的一部分):**
            *   *描述:* 如果配置变得复杂，此模块可以集中管理配置加载和管理，超越 Spring Boot 标准的 `application.properties/yml`。
            *   *打包:* `jar`。
        *   **`openmanus-commons` (可选):**
            *   *描述:* 跨多个模块使用的实用工具类、通用常量或共享 DTO。
            *   *打包:* `jar`。

*   **4.3. 推荐的包结构 (每个模块内部)**
    *   遵循标准的 Java 约定。使用一致的基础包名，例如 `io.github.yourusername.openmanusjava` (或 `com.yourcompany.openmanusjava`)。
    *   在每个模块内部，按功能或层进一步组织结构：
        *   `io.github.yourusername.openmanusjava.module.controller` (用于 API 控制器)
        *   `io.github.yourusername.openmanusjava.module.service` (用于业务逻辑服务)
        *   `io.github.yourusername.openmanusjava.module.service.impl` (用于服务实现)
        *   `io.github.yourusername.openmanusjava.module.model` 或 `domain` (用于业务对象/实体)
        *   `io.github.yourusername.openmanusjava.module.dto` (用于 API 数据传输对象)
        *   `io.github.yourusername.openmanusjava.module.repository` (用于数据访问接口，例如 Spring Data)
        *   `io.github.yourusername.openmanusjava.module.config` (用于模块特定配置)
        *   `io.github.yourusername.openmanusjava.module.client` (用于外部服务客户端，例如 LLM 客户端)
        *   `io.github.yourusername.openmanusjava.module.tool` (用于 `openmanus-tool-executor` 中的工具实现)
        *   `io.github.yourusername.openmanusjava.module.exception` (用于自定义异常)
        *   `io.github.yourusername.openmanusjava.module.util` (用于实用工具类)

*   **4.4. 命名约定**
    *   **包:** 小写，点分隔 (例如 `io.github.yourusername.openmanusjava.core.service`)。
    *   **类和接口:** 驼峰命名法，首字母大写 (例如 `TaskOrchestrationService`, `LlmClient`)。
    *   **方法和变量:** 小驼峰命名法，首字母小写 (例如 `executeTask`, `taskDescription`)。
    *   **常量:** 全大写，单词间用下划线分隔 (例如 `MAX_RETRIES`)。
    *   **模块 (Maven Artifact ID):** 小写，连字符分隔 (例如 `openmanus-api`, `openmanus-tool-executor`)。
    *   **配置文件:** `application.properties` 或 `application.yml`。特定配置文件: `application-dev.properties`, `application-prod.properties`。

*   **4.5. 初始目录结构示例 (说明性)**
    ```
    openmanus-java/
    ├── pom.xml                   (父 POM)
    ├── openmanus-app/
    │   ├── pom.xml
    │   └── src/
    │       └── main/
    │           ├── java/
    │           │   └── io/github/yourusername/openmanusjava/app/
    │           │       └── OpenManusApplication.java
    │           └── resources/
    │               └── application.properties
    ├── openmanus-api/
    │   ├── pom.xml
    │   └── src/main/java/io/github/yourusername/openmanusjava/api/
    │       ├── controller/
    │       └── dto/
    ├── openmanus-core/
    │   ├── pom.xml
    │   └── src/main/java/io/github/yourusername/openmanusjava/core/
    │       ├── service/
    │       ├── model/
    │       └── repository/
    ├── openmanus-llm-integration/
    │   ├── pom.xml
    │   └── src/main/java/io/github/yourusername/openmanusjava/llm/
    │       ├── client/
    │       └── config/
    ├── openmanus-tool-executor/
    │   ├── pom.xml
    │   └── src/main/java/io/github/yourusername/openmanusjava/tool/
    │       ├── impl/
    │       └── definition/
    └── legacy/ (可选, 用于原始 Python 代码, 添加到 .gitignore)
        └── openmanus_python/
    ```
此结构为可扩展和有组织的 Spring Boot 应用程序提供了一个良好的起点。

## 5. 核心模块设计与实现

### 5.1. 任务接收与编排引擎

*   **5.1.1. 功能描述:**
    *   该引擎是 OpenManus Java 应用程序的大脑。它负责接收用户定义的任务（或“想法”），解释它们（通常借助 LLM 集成模块），规划一系列操作，通过调用适当的工具或进一步的 LLM 交互来编排这些操作的执行，并管理整个任务生命周期和状态。它可能涉及将复杂任务分解为更小、可管理的子任务。

*   **5.1.2. 模块设计 (`openmanus-core`):**
    *   **主要 Spring Boot 模块:** `openmanus-core`
    *   **关键类与接口:**
        *   `TaskRequestDTO` (在 `openmanus-api` 或 `openmanus-commons` 中): 代表来自用户的原始任务输入 (例如 `{ "description": "总结 example.com 的主要内容" }`)。
        *   `Task` (领域实体): 代表系统中的一个任务。
            *   属性: `id`, `description`, `status` (例如 PENDING, IN_PROGRESS, COMPLETED, FAILED), `creationTimestamp`, `completionTimestamp`, `initialRequest` (TaskRequestDTO), `plannedSteps` (List<Step>), `results` (String 或 JSON)。
            *   如果需要任务持久化，可以是一个 JPA 实体。
        *   `Step` (领域对象/实体): 代表任务执行计划中的单个步骤。
            *   属性: `id`, `taskId`, `description`, `toolName` (例如 "browser.navigate", "llm.generateText", "code.executePython"), `parameters` (Map<String, Object>), `status`, `result`, `sequenceOrder`。
        *   `TaskController` (在 `openmanus-api` 中):
            *   `POST /api/v1/tasks`: 提交一个新任务。接受 `TaskRequestDTO`。返回 `Task` 或 `TaskId`。
            *   `GET /api/v1/tasks/{taskId}`: 检索任务状态和结果。
        *   `TaskOrchestrationService` (在 `openmanus-core` 中的接口):
            *   `Task processNewTask(TaskRequestDTO request)`: 初始化任务处理的主要方法。
            *   `Task getTaskStatus(String taskId)`: 检索任务状态。
            *   `void executeNextStep(String taskId)` (可能是内部的或由事件触发)。
        *   `TaskOrchestrationServiceImpl` (在 `openmanus-core` 中的实现):
            *   与 `PlannerService`, `ToolExecutorService`, 和 `LlmService` 协调。
            *   管理任务状态转换。
        *   `PlannerService` (在 `openmanus-core` 中的接口和实现):
            *   `List<Step> createPlan(Task task, String userGoal)`: 与 LLM 模块交互，将用户目标分解为可执行步骤。
        *   `TaskRepository` (接口，如果使用 JPA，则扩展 Spring Data `JpaRepository<Task, String>`): 用于持久化 `Task` 实体。
        *   `StepRepository` (可选, 如果 `Step` 是一个实体): 用于持久化 `Step` 实体。
    *   **数据结构:**
        *   任务状态 (枚举: `PENDING`, `PLANNING`, `EXECUTING`, `COMPLETED`, `FAILED`, `CANCELED`)。
        *   步骤状态 (枚举: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`)。
    *   **工作流程:**
        1.  用户通过 API (`TaskController`) 提交任务。
        2.  `TaskOrchestrationService` 接收请求，创建一个 `Task` 实体 (状态 PENDING)，并将其持久化。
        3.  它调用 `PlannerService` 生成一个计划 (一个 `Step` 对象列表)。这通常涉及通过 `LlmIntegrationModule` 向 LLM 发出提示。
        4.  计划 (步骤) 被存储。任务状态变为 `PLANNING` 然后是 `EXECUTING`。
        5.  `TaskOrchestrationService` 遍历这些步骤：
            *   对于每个步骤，它确定所需的工具或操作。
            *   它调用 `ToolExecutionModule` (例如 `BrowserAutomationTool.execute(...)`, `CodeExecutionTool.execute(...)`) 或 `LlmIntegrationModule`。
            *   步骤状态根据执行结果更新。
            *   如果一个步骤失败，任务可能会被标记为 FAILED，或者可能会尝试补救策略。
        6.  一旦所有步骤完成，任务将被标记为 `COMPLETED` 并存储结果。

*   **5.1.3. 技术选型及基本原理:**
    *   **核心框架:** Spring Boot (`spring-boot-starter-web` 用于 API, `spring-boot-starter-data-jpa` 如果持久化任务)。
    *   **任务持久化 (可选但推荐):**
        *   *技术:* Spring Data JPA 与 PostgreSQL (生产环境) 或 H2 (开发环境)。
        *   *原因:* 存储任务定义、状态和结果，允许异步处理、恢复和历史记录。
    *   **异步执行:**
        *   *技术:* Spring 的 `@Async` 注解, `ThreadPoolTaskExecutor`。对于更复杂的场景，可以考虑 Spring Integration 或专用的消息队列。
        *   *原因:* 任务规划和执行 (特别是像浏览器自动化或长时间的 LLM 调用这样的工具使用) 可能非常耗时。异步处理可以防止阻塞 API 请求，并允许系统并发处理多个任务。
    *   **工作流/状态管理 (初始):** `TaskOrchestrationServiceImpl` 内部的自定义逻辑。
    *   **工作流/状态管理 (高级选项):**
        *   *技术:* Flowable 或 Camunda (轻量级、可嵌入的 BPMN 引擎)。
        *   *原因:* 对于具有许多步骤、条件逻辑、人工批准 (通常不在 OpenManus 的范围内，但可能用于扩展) 或长时间运行的Saga的非常复杂的任务编排，专用的工作流引擎可以简化开发并提供更好的可见性和管理。对于初始复制，自定义的 Spring 管理逻辑可能就足够了。

*   **5.1.4. 实现思路与关键逻辑 (伪代码):**
    ```java
    // TaskOrchestrationServiceImpl.java
    @Service
    public class TaskOrchestrationServiceImpl implements TaskOrchestrationService {
        @Autowired private PlannerService plannerService;
        @Autowired private ToolExecutorRouter toolExecutorRouter; // 路由到正确的工具服务
        @Autowired private TaskRepository taskRepository;
        @Autowired private ApplicationEventPublisher eventPublisher;

        @Override
        @Async // 使其异步
        public Task processNewTask(TaskRequestDTO request) {
            Task task = new Task(request.getDescription());
            task.setStatus(TaskStatus.PENDING);
            task = taskRepository.save(task);

            try {
                task.setStatus(TaskStatus.PLANNING);
                taskRepository.save(task);

                List<Step> steps = plannerService.createPlan(task, request.getDescription());
                task.setPlannedSteps(steps);
                // 如果步骤是实体，则持久化步骤

                task.setStatus(TaskStatus.EXECUTING);
                taskRepository.save(task);

                executeTaskSteps(task);

                task.setStatus(TaskStatus.COMPLETED);
                // 如有必要，聚合结果
                taskRepository.save(task);
                eventPublisher.publishEvent(new TaskCompletedEvent(this, task.getId()));

            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED);
                task.setResults("错误: " + e.getMessage());
                taskRepository.save(task);
                eventPublisher.publishEvent(new TaskFailedEvent(this, task.getId(), e.getMessage()));
            }
            return task;
        }

        private void executeTaskSteps(Task task) {
            for (Step step : task.getPlannedSteps()) {
                step.setStatus(StepStatus.IN_PROGRESS);
                // 持久化步骤状态

                try {
                    ToolExecutionResult result = toolExecutorRouter.executeTool(step.getToolName(), step.getParameters());
                    step.setResult(result.getOutput());
                    step.setStatus(StepStatus.COMPLETED);
                } catch (ToolExecutionException e) {
                    step.setResult("错误: " + e.getMessage());
                    step.setStatus(StepStatus.FAILED);
                    // 持久化步骤状态
                    throw new TaskExecutionException("步骤失败: " + step.getDescription(), e); // 传播以将任务标记为 FAILED
                }
                // 持久化步骤状态和结果
            }
        }
    }

    // PlannerServiceImpl.java
    @Service
    public class PlannerServiceImpl implements PlannerService {
        @Autowired private LlmService llmService; // 来自 LLM 集成模块

        @Override
        public List<Step> createPlan(Task task, String userGoal) {
            String prompt = "给定目标: '" + userGoal + "',将其分解为一系列可执行步骤。每个步骤应指定一个工具及其参数。可用工具: [browser.navigate(url), browser.extractText(selector), code.executePython(script), llm.ask(question)]。请使用 JSON 步骤列表进行响应。";
            String llmResponse = llmService.generateText(prompt); // 简化
            // 将 llmResponse (JSON) 解析为 List<Step>
            // LLM 响应示例:
            // [
            //   { "description": "导航到网站", "toolName": "browser.navigate", "parameters": { "url": "https://example.com" } },
            //   { "description": "提取主要内容", "toolName": "browser.extractText", "parameters": { "selector": "body" } },
            //   { "description": "总结内容", "toolName": "llm.ask", "parameters": { "question": "总结以下文本: ${previous_step_output}" } }
            // ]
            // 此解析逻辑需要健壮。步骤可能需要引用先前步骤的输出。
            return parseLlmResponseToSteps(llmResponse);
        }
    }
    ```

*   **5.1.5. API 设计 (`openmanus-api` - TaskController):**
    *   **POST `/api/v1/tasks`**
        *   *描述:* 提交一个新任务供代理执行。
        *   *请求体:* `TaskRequestDTO`
            ```json
            {
                "description": "伦敦现在天气如何？并从 BBC 总结今天的头条新闻。",
                "priority": "NORMAL" // 可选
            }
            ```
        *   *响应体 (成功 - 202 Accepted):* `Task` (初始状态) 或 `TaskSubmissionResponseDTO`
            ```json
            {
                "taskId": "ts_123xyz",
                "description": "伦敦现在天气如何？并从 BBC 总结今天的头条新闻。",
                "status": "PENDING",
                "submittedAt": "2024-07-15T10:00:00Z",
                "links": [
                    { "rel": "self", "href": "/api/v1/tasks/ts_123xyz" }
                ]
            }
            ```
    *   **GET `/api/v1/tasks/{taskId}`**
        *   *描述:* 检索特定任务的状态和结果。
        *   *路径变量:* `taskId` (字符串)
        *   *响应体 (成功 - 200 OK):* `Task`
            ```json
            {
                "taskId": "ts_123xyz",
                "description": "伦敦现在天气如何？并从 BBC 总结今天的头条新闻。",
                "status": "COMPLETED", // 或 IN_PROGRESS, FAILED 等
                "submittedAt": "2024-07-15T10:00:00Z",
                "completedAt": "2024-07-15T10:05:00Z", // 如果已完成
                "plannedSteps": [
                    { "description": "获取伦敦天气", "toolName": "weather.getCurrent", "status": "COMPLETED", "result": "{...天气数据...}"},
                    { "description": "访问 BBC 头条新闻", "toolName": "browser.navigate", "status": "COMPLETED", "result": "已导航到 bbc.com/news"},
                    { "description": "提取标题", "toolName": "browser.extractText", "status": "COMPLETED", "result": "[标题1, 标题2]"},
                    { "description": "总结新闻", "toolName": "llm.ask", "status": "COMPLETED", "result": "新闻摘要..."}
                ],
                "finalResult": { // 合并的或最终的输出
                    "weather": "{...天气数据...}",
                    "newsSummary": "新闻摘要..."
                },
                "error": null // 如果状态为 FAILED，则为错误详情
            }
            ```
此模块将构成应用程序的骨干，协调所有其他部分以满足用户请求。

### 5.2. LLM 集成模块

*   **5.2.1. 功能描述:**
    *   此模块负责与大型语言模型 (LLM) 的所有直接通信。它为其他模块（如任务编排引擎或特定工具）提供了一个标准化接口，以便向 LLM 发送请求并接收响应。它处理 API 密钥管理、请求格式化和基本响应解析。其设计应具有可扩展性，以支持各种 LLM 提供商。

*   **5.2.2. 模块设计 (`openmanus-llm-integration`):**
    *   **主要 Spring Boot 模块:** `openmanus-llm-integration`
    *   **关键类与接口:**
        *   `LlmService` (接口):
            *   `String generateText(String prompt, LlmParameters params)`: 用于通用文本生成、规划等。
            *   `String chat(List<ChatMessage> messages, LlmParameters params)`: 用于会话式交互。
            *   `List<Embedding> embed(List<String> texts)`: (可选，未来用于 RAG 等) 为文本生成嵌入。
        *   `LlmServiceImpl` (实现):
            *   实现 `LlmService`。
            *   根据配置使用特定的 `LlmClient`。
        *   `LlmClient` (接口):
            *   定义与特定类型 LLM 提供商（例如 OpenAI、Anthropic、本地模型服务器）交互的方法。
            *   `LlmResponse call(LlmRequest request)`
        *   `OpenAiClient` (`LlmClient` 的实现):
            *   使用 OpenAI API (例如，通过 OpenAI Java SDK 或 Spring AI 的 OpenAI 客户端)。
            *   处理 OpenAI 特定的身份验证、请求构建和响应解析。
        *   `LlmParameters` (POJO):
            *   属性: `model` (例如 "gpt-4o", "claude-3-opus"), `temperature`, `maxTokens`, `stopSequences` 等。
        *   `ChatMessage` (POJO):
            *   属性: `role` (枚举: USER, ASSISTANT, SYSTEM), `content` (String)。
        *   `LlmRequest`, `LlmResponse` (POJO): 用于与 `LlmClient` 交互的请求/响应的内部表示。
        *   `LlmConfigProperties` (Spring `@ConfigurationProperties`):
            *   属性: `provider` (例如 "openai", "anthropic"), `apiKey`, `baseUrl`, `defaultModel`, `timeoutSeconds`。
    *   **数据结构:**
        *   `ChatMessage` 用于会话上下文。
        *   用于角色 (`USER`, `ASSISTANT`, `SYSTEM`)、LLM 提供商的枚举。
    *   **工作流程:**
        1.  另一个服务 (例如 `PlannerService`) 需要 LLM 交互。
        2.  它调用 `LlmService` 上的方法 (例如 `generateText(prompt, params)`)。
        3.  `LlmServiceImpl` 从 `LlmConfigProperties` 检索必要的配置 (API 密钥, 模型)。
        4.  它选择适当的 `LlmClient` 实现 (例如 `OpenAiClient`)。
        5.  `LlmClient` 构建特定于提供商的请求并发起 HTTP 调用。
        6.  解析响应，并将相关内容 (例如生成的文本) 返回给调用链。

*   **5.2.3. 技术选型及基本原理:**
    *   **LLM SDK/客户端:**
        *   **选项 1: Spring AI (推荐，以实现未来灵活性):**
            *   *技术:* `spring-ai-openai-starter` (或其他针对不同模型的启动器)。
            *   *原因:* Spring AI 旨在为各种 AI 模型提供一个通用的抽象层，从而简化集成。它处理客户端创建、身份验证的样板代码，并提供提示模板等功能。如果 Spring Boot 2.7.x 对目标 JDK (17) 的 Spring AI 支持良好，这是一个强有力的竞争者。(需要验证 Spring AI 与 SB 2.7 的兼容性和成熟度，因为它是一个较新的项目)。
        *   **选项 2: 直接使用 SDK (适用于特定提供商的重点):**
            *   *技术:* 官方 OpenAI Java SDK (例如 `com.openai:openai-client`)。
            *   *原因:* 如果主要关注 OpenAI，使用其官方 SDK 会很直接，并可以完全访问其 API 功能。
        *   **选项 3: 通用 HTTP 客户端:**
            *   *技术:* `RestTemplate`, `WebClient` (来自 Spring WebFlux, 也可在 MVC 中使用), 或 Apache HttpClient / OkHttp。
            *   *原因:* 如果没有合适的 SDK 或需要对请求进行最大程度的控制。需要实现更多样板代码。
    *   **配置管理:**
        *   *技术:* Spring Boot `@ConfigurationProperties` 和 `application.properties` / `application.yml`。
        *   *原因:* Spring Boot 管理外部化配置的标准方式，安全可靠 (例如，在生产中使用环境变量或 Vault 管理 API 密钥)。
    *   **JSON 处理:**
        *   *技术:* Jackson (随 `spring-boot-starter-web` 提供)。
        *   *原因:* 用于序列化来自 LLM 的请求和反序列化响应。

*   **5.2.4. 实现思路与关键逻辑 (伪代码):**
    ```java
    // LlmConfigProperties.java
    @ConfigurationProperties(prefix = "openmanus.llm")
    public class LlmConfigProperties {
        private String provider; // "openai", "anthropic", 等
        private String apiKey;
        private String baseUrl;
        private String defaultModel;
        // getters 和 setters
    }

    // LlmService.java
    public interface LlmService {
        String generateText(String prompt, LlmParameters params);
        // 其他方法
    }

    // LlmServiceImpl.java
    @Service
    public class LlmServiceImpl implements LlmService {
        private final LlmClient llmClient;
        private final LlmConfigProperties config;

        // 基于配置的特定客户端的构造函数注入
        public LlmServiceImpl(ApplicationContext context, LlmConfigProperties config) {
            this.config = config;
            if ("openai".equalsIgnoreCase(config.getProvider())) {
                this.llmClient = context.getBean(OpenAiClient.class); // 或 new OpenAiClient(config)
            } else {
                throw new IllegalArgumentException("不支持的 LLM 提供商: " + config.getProvider());
            }
        }

        @Override
        public String generateText(String prompt, LlmParameters params) {
            LlmRequest request = new LlmRequest();
            request.setPrompt(prompt);
            request.setModel(params.getModel() != null ? params.getModel() : config.getDefaultModel());
            request.setTemperature(params.getTemperature());
            // ... 设置其他参数

            LlmResponse response = llmClient.call(request);
            return response.getGeneratedText(); // 简化
        }
    }

    // OpenAiClient.java (说明性，使用假设的 SDK 或直接 HTTP)
    @Component
    public class OpenAiClient implements LlmClient {
        private final LlmConfigProperties config;
        // private final OpenAiApi openAiApi; // 如果使用 OpenAI SDK

        public OpenAiClient(LlmConfigProperties config) {
            this.config = config;
            // this.openAiApi = new OpenAiApi(config.getApiKey());
        }

        @Override
        public LlmResponse call(LlmRequest manusRequest) {
            // 1. 从 LlmRequest 构建 OpenAI 特定请求
            //    (例如，聊天模型的 ChatCompletionRequest)
            // 2. 使用 SDK 或 HTTP 客户端进行 API 调用
            //    ChatCompletionRequest apiRequest = ChatCompletionRequest.builder()
            //        .model(manusRequest.getModel())
            //        .messages(List.of(new ChatMessage(ChatMessageRole.USER, manusRequest.getPrompt())))
            //        .temperature(manusRequest.getTemperature())
            //        .build();
            //    ChatCompletionResult result = openAiApi.createChatCompletion(apiRequest).join();
            // 3. 将 OpenAI 特定响应解析为通用 LlmResponse
            //    LlmResponse response = new LlmResponse();
            //    response.setGeneratedText(result.getChoices().get(0).getMessage().getContent());
            //    return response;
            throw new UnsupportedOperationException("实现待定"); // 占位符
        }
    }
    ```

*   **5.2.5. API 设计:**
    *   此模块主要为应用程序内的其他模块提供内部服务 (`LlmService`)。除非有特定需求通过管理界面直接管理 LLM 配置或测试 LLM 连接性，否则它通常不公开自己的公共 RESTful API。任何此类管理 API 都将次于其为其他内部模块提供服务的核心功能。
此模块对于实现代理的“智能”至关重要，使其能够理解任务、生成计划并产生类似人类的文本。

### 5.3. 工具执行模块

*   **5.3.1. 功能描述:**
    *   此模块负责执行 OpenManus 代理可用于与其环境交互或处理信息的具体操作或“工具”。它充当各种功能（如 Web 浏览、代码运行、数据获取等）的注册中心和执行器。它为编排引擎提供了一种标准化的方式来调用这些工具并接收其输出。

*   **5.3.2. 模块设计 (`openmanus-tool-executor`):**
    *   **主要 Spring Boot 模块:** `openmanus-tool-executor`
    *   **关键类与接口 (通用工具框架):**
        *   `Tool` (接口):
            *   `String getName()`: 返回工具的唯一名称 (例如 "browser.navigate", "code.executePython")。
            *   `ToolExecutionResult execute(Map<String, Object> parameters)`: 使用给定参数执行工具。
            *   `List<ToolParameterDefinition> getParameterDefinitions()`: 描述工具期望的参数。
        *   `ToolParameterDefinition` (POJO):
            *   属性: `name`, `type`, `description`, `isRequired`。
        *   `ToolExecutionResult` (POJO):
            *   属性: `status` (SUCCESS, FAILURE), `output` (String, JSON, 或复杂对象), `error` (String, 如果失败)。
        *   `ToolRegistry` (服务):
            *   `void registerTool(Tool tool)`: 将工具添加到注册表。
            *   `Tool getTool(String toolName)`: 按名称检索工具。
            *   (通常在启动时由 Spring 检测 `@Component` 工具来填充)。
        *   `ToolExecutorService` (接口, 可能是 `TaskOrchestrationServiceImpl` 的一部分或专用路由器):
            *   `ToolExecutionResult executeTool(String toolName, Map<String, Object> parameters)`: 查找并执行指定的工具。这也可以命名为 `ToolRouterService`。
        *   `ToolExecutorServiceImpl` (实现):
            *   使用 `ToolRegistry` 查找工具。
            *   调用工具的 `execute` 方法。
            *   处理任何通用的执行前/后逻辑 (例如日志记录、基本验证)。
    *   **工作流程:**
        1.  `TaskOrchestrationService` 确定某个步骤需要特定工具。
        2.  它调用 `ToolExecutorService.executeTool("some.tool", params)`。
        3.  `ToolExecutorService` 在 `ToolRegistry` 中查找 "some.tool"。
        4.  如果找到，它将调用工具的 `execute(params)` 方法。
        5.  工具执行其操作 (例如导航浏览器、运行脚本)。
        6.  工具返回 `ToolExecutionResult`。
        7.  此结果将传递回 `TaskOrchestrationService`。

*   **5.3.3. 技术选型及基本原理 (通用工具框架):**
    *   **核心框架:** 普通 Java 与 Spring 用于依赖注入 (`@Component` 用于工具, `@Service` 用于注册表/执行器)。
    *   *原因:* 简单性并利用 Spring 现有的 DI 功能来管理工具实例。

*   **5.3.4. 实现思路与关键逻辑 (伪代码 - 通用工具框架):**
    ```java
    // Tool.java
    public interface Tool {
        String getName();
        ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException;
        List<ToolParameterDefinition> getParameterDefinitions();
    }

    // ToolExecutionResult.java
    public class ToolExecutionResult {
        // SUCCESS, FAILURE
        private String status;
        private Object output; // 可以是 String, Map, List 等
        private String error;
        // getters/setters
    }

    // ToolExecutionException.java
    public class ToolExecutionException extends Exception {
        public ToolExecutionException(String message, Throwable cause) { super(message, cause); }
        public ToolExecutionException(String message) { super(message); }
    }

    // ToolRegistry.java
    @Service
    public class ToolRegistry {
        private final Map<String, Tool> toolMap = new ConcurrentHashMap<>();

        public ToolRegistry(List<Tool> tools) { // 自动装配所有 Tool 实现
            for (Tool tool : tools) {
                registerTool(tool);
            }
        }

        public void registerTool(Tool tool) {
            toolMap.put(tool.getName().toLowerCase(), tool);
        }

        public Tool getTool(String toolName) {
            return toolMap.get(toolName.toLowerCase());
        }
    }

    // ToolExecutorService.java (或 ToolRouterService)
    @Service
    public class ToolExecutorService {
        @Autowired private ToolRegistry toolRegistry;

        public ToolExecutionResult executeTool(String toolName, Map<String, Object> parameters) throws ToolNotFoundException, ToolExecutionException {
            Tool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                throw new ToolNotFoundException("未找到工具: " + toolName);
            }
            // 此处可以添加根据 tool.getParameterDefinitions() 对参数进行基本验证的逻辑。
            return tool.execute(parameters);
        }
    }
    ```

**5.3.A. 子组件: 浏览器自动化工具**

    *   **5.3.A.1. 功能描述:**
        *   使代理能够与 Web 浏览器交互以执行任务，例如导航到 URL、从网页中提取信息（文本、HTML、特定元素）、填写表单和点击按钮。这复制了原始 OpenManus（使用 Playwright）中看到的浏览器交互功能。

    *   **5.3.A.2. 模块设计 (在 `openmanus-tool-executor` 内):**
        *   **关键类与接口:**
            *   `BrowserAutomationTool` (实现 `Tool`):
                *   管理浏览器实例 (例如 WebDriver)。
                *   通过子命令或解释参数来提供特定操作。
                *   `getName()`: 可以是一个通用的 "browser"，并通过参数传递操作；或者像 "browser.navigate"、"browser.extractText" 这样的特定工具。我们假设使用一个更通用的 `BrowserTool`，它接受一个操作参数。
            *   `BrowserAction` (枚举或字符串常量): NAVIGATE, CLICK, TYPE, EXTRACT_TEXT, EXTRACT_HTML, GET_CURRENT_URL 等。
        *   **示例 `Tool` 名称和参数:**
            *   工具名称: `browser.do`
            *   参数:
                *   `action`: (字符串, 例如 "NAVIGATE", "CLICK", "EXTRACT_TEXT")
                *   `url`: (字符串, 用于 NAVIGATE)
                *   `selector`: (字符串, 用于 CLICK, TYPE, EXTRACT_TEXT - CSS 选择器或 XPath)
                *   `text`: (字符串, 用于 TYPE)
                *   `timeoutSeconds`: (整数, 可选)

    *   **5.3.A.3. 技术选型及基本原理:**
        *   **选项 1: Selenium WebDriver (成熟，广泛支持):**
            *   *技术:* `org.seleniumhq.selenium:selenium-java`。
            *   *原因:* Java 中用于浏览器自动化的历史悠久、支持良好的库。适用于大多数主流浏览器 (Chrome, Firefox, Edge, Safari)。拥有庞大的社区和广泛的文档。
        *   **选项 2: Playwright for Java (现代，功能良好):**
            *   *技术:* `com.microsoft.playwright:playwright`。
            *   *原因:* 比 Selenium 更现代的 API，由微软构建。提供自动等待、网络拦截和并行执行等功能。如果 OpenManus Python 使用了 Playwright，这能提供更接近的对等性。
            *   *考虑因素:* 确保其 Java 版本稳定并且与项目设置良好集成。可能需要由 Playwright CLI 或其 API 管理单独的驱动程序下载。
        *   **推荐:** 如果希望与原始项目保持对等性并且其 Java 绑定成熟，则从 **Playwright for Java** 开始。否则，Selenium 是一个非常安全和强大的选择。在本文档中，我们假设使用 Playwright for Java。

    *   **5.3.A.4. 实现思路与关键逻辑 (伪代码):**
        ```java
        // BrowserTool.java (使用 Playwright 的示例)
        @Component
        public class BrowserTool implements Tool {
            private Playwright playwright;
            private Browser browser;

            // 初始化 Playwright 和浏览器 (例如，在 @PostConstruct 中或首次使用时)
            // 确保正确的生命周期管理 (关闭浏览器、playwright)

            @PostConstruct
            public void init() {
                try {
                    playwright = Playwright.create();
                    // 可以配置启动选项 (无头模式、浏览器类型)
                    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                } catch (Exception e) {
                    // 处理初始化错误
                    throw new RuntimeException("初始化 Playwright 浏览器失败", e);
                }
            }

            @PreDestroy
            public void cleanup() {
                if (browser != null) browser.close();
                if (playwright != null) playwright.close();
            }

            @Override
            public String getName() { return "browser.action"; }

            @Override
            public List<ToolParameterDefinition> getParameterDefinitions() {
                // 定义参数: "action", "url", "selector", "text" 等
                return List.of(
                    new ToolParameterDefinition("action", "String", "要执行的操作 (NAVIGATE, CLICK, TYPE, EXTRACT_TEXT, GET_URL)", true),
                    new ToolParameterDefinition("url", "String", "NAVIGATE 操作的 URL", false),
                    new ToolParameterDefinition("selector", "String", "用于元素交互的 CSS 选择器或 XPath", false),
                    new ToolParameterDefinition("text", "String", "TYPE 操作要输入的文本", false)
                    // ... 其他参数
                );
            }

            @Override
            public ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException {
                String action = (String) parameters.get("action");
                Page page = null; // 仔细管理页面生命周期

                try {
                    page = browser.newPage(); // 每次执行可能使用新的页面/上下文以实现隔离
                    ToolExecutionResult result = new ToolExecutionResult();
                    result.setStatus("SUCCESS");

                    switch (action.toUpperCase()) {
                        case "NAVIGATE":
                            String url = (String) parameters.get("url");
                            page.navigate(url);
                            result.setOutput("已导航至: " + url);
                            break;
                        case "EXTRACT_TEXT":
                            String selector = (String) parameters.get("selector");
                            result.setOutput(page.textContent(selector));
                            break;
                        case "CLICK":
                            String clickSelector = (String) parameters.get("selector");
                            page.click(clickSelector);
                            result.setOutput("已点击元素: " + clickSelector);
                            break;
                        case "TYPE":
                            String typeSelector = (String) parameters.get("selector");
                            String textToType = (String) parameters.get("text");
                            page.fill(typeSelector, textToType); // 或 page.type 用于模拟单个按键
                            result.setOutput("已在元素中输入: " + typeSelector);
                            break;
                        case "GET_URL":
                            result.setOutput(page.url());
                            break;
                        default:
                            throw new ToolExecutionException("不支持的浏览器操作: " + action);
                    }
                    return result;
                } catch (PlaywrightException e) {
                    throw new ToolExecutionException("浏览器操作 '" + action + "' 失败: " + e.getMessage(), e);
                } finally {
                    if (page != null) {
                        page.close();
                    }
                }
            }
        }
        ```
这为基于浏览器的交互提供了基础。具体的操作和参数处理可以显著扩展。
