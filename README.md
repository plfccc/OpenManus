# OpenManus Java Replication Project

## 1. Project Introduction

### Project Goal
To re-implement the core functionalities of the OpenManus project (originally Python-based) using Java and the Spring Boot 2.7.x framework. The aim is to create a robust, scalable, and maintainable Java application that mirrors the capabilities of OpenManus, such as AI-driven task management, LLM integration, and tool execution (e.g., browser automation, code execution).

### Scope

#### In Scope:
*   Core agent logic for task intake and basic planning/orchestration.
*   Integration with Large Language Models (LLMs) for understanding, planning, and generation.
*   Implementation of key tools: browser automation and a mechanism for code execution (e.g., for data analysis tasks).
*   Configuration management for API keys, model parameters, etc.
*   RESTful APIs for interacting with the system (e.g., submitting tasks).
*   Comprehensive documentation covering setup, design, and API usage.

#### Out of Scope (Initial Version):
*   Direct replication of all existing Python scripts and specific algorithms if not central to core functionality.
*   Advanced multi-agent features beyond a foundational orchestration capability (unless "MCP" or "flow" implies a core, replicable multi-agent system).
*   Complex UI/frontend (focus is on the backend API and engine).
*   Specific RL (Reinforcement Learning) tuning aspects mentioned in OpenManus-RL, unless a basic version is integral to the core OpenManus.

## 2. Technology Stack Choices & Rationale

*   **Backend Framework:** Spring Boot 2.7.x
    *   *Reason:* As specified. It's a mature and widely-used framework providing rapid application development, embedded servers, and extensive support for microservices and web applications.
*   **Java Version:** JDK 17 (LTS)
    *   *Reason:* Spring Boot 2.7.x is fully compatible with JDK 17. JDK 17 offers significant improvements over JDK 8, including new language features (records, sealed classes, pattern matching for instanceof), performance enhancements (e.g., ZGC, Shenandoah GC improvements), and long-term support. While Spring Boot 2.7 can run on JDK 8, JDK 17 provides a more modern development experience and better future-proofing. For projects starting anew, leveraging a recent LTS version is generally recommended.
    *   *Adaptation Considerations for Spring Boot 2.7:* Ensure all chosen libraries are compatible with JDK 17. Some older libraries might have issues, but the Spring Boot ecosystem itself handles this well. Standard Spring Boot starters and commonly used libraries are generally fine.
*   **Database:**
    *   *Recommendation:* PostgreSQL (e.g., version 14 or newer)
        *   *Reason:* A powerful, open-source object-relational database system with a strong reputation for reliability, feature robustness, and data integrity. It handles complex queries and large datasets well, suitable for an application that might store task details, agent states, or logs.
    *   *Alternative for Development/Testing:* H2 Database
        *   *Reason:* An in-memory Java SQL database, very lightweight and fast. Excellent for local development, running automated tests, and quick prototyping without needing a separate database server. Spring Boot has excellent auto-configuration support for H2.
*   **Build Tool:** Apache Maven
    *   *Reason:* A mature and widely adopted build automation and dependency management tool in the Java ecosystem. It enforces a standard project structure, simplifies the build process, and integrates well with IDEs and CI/CD systems. Gradle is a strong alternative, but Maven's prevalence and declarative XML-based configuration are often preferred for their straightforwardness in many enterprise and open-source projects.
*   **Other Potential Technologies (to be detailed in module design):**
    *   **LLM Integration:** Spring AI (if sufficiently mature and aligned with Spring Boot 2.7) or direct use of LLM provider SDKs (e.g., OpenAI Java SDK).
    *   **Web Automation:** Selenium WebDriver for Java or Playwright for Java.
    *   **Async Processing/Task Queues (Optional, for scalability):** RabbitMQ or Apache Kafka.
    *   **Caching (Optional):** Redis.
    *   **Containerization:** Docker.

## 3. Environment Setup Guide

### 3.1. JDK Installation (JDK 17)
*   Download JDK 17 from a reputable source (e.g., Adoptium Temurin, Oracle OpenJDK).
*   Follow the installation instructions for your operating system.
*   Ensure the `JAVA_HOME` environment variable is set to the JDK 17 installation directory.
*   Verify installation by running `java -version` and `javac -version` in a terminal.

### 3.2. Maven Installation (Latest Stable Version)
*   Download Apache Maven from the official website.
*   Extract the archive to a suitable directory.
*   Add Maven's `bin` directory to your system's `PATH` environment variable.
*   Verify installation by running `mvn -version` in a terminal.

### 3.3. IDE Recommendation
*   *Recommendation:* IntelliJ IDEA (Community or Ultimate Edition)
    *   *Reason:* Excellent support for Spring Boot and Maven, powerful debugging tools, and code assistance features.
*   *Configuration:*
    *   Install the IDE.
    *   Ensure it's configured to use the installed JDK 17.
    *   Import the Maven project once it's created.

### 3.4. Database Setup

#### 3.4.1. PostgreSQL (for Development/Production)
*   Download and install PostgreSQL for your OS.
*   During installation, set a password for the default `postgres` user.
*   Using a tool like `psql` or a GUI (e.g., pgAdmin, DBeaver), create a database for the application (e.g., `openmanus_db`).
*   Note the database URL (e.g., `jdbc:postgresql://localhost:5432/openmanus_db`), username, and password for the Spring Boot configuration.

#### 3.4.2. H2 Database (for quick local development/testing)
*   No separate installation is typically needed if used as an embedded database. Spring Boot, with the `com.h2database` dependency, can auto-configure an in-memory database.
*   To enable the H2 console (for inspecting the database during development), you'll add specific properties to `application.properties` (e.g., `spring.h2.console.enabled=true`, `spring.h2.console.path=/h2-console`).

### 3.5. Other Dependencies (Initial Setup - Optional)
*   If using Docker for managing services like PostgreSQL, Redis, or message queues locally, install Docker Desktop (or Docker Engine on Linux).

## 4. Project Structure and Module Partitioning

*   **4.1. Handling Original Python Code**
    *   If you have the original OpenManus Python codebase, it's recommended to keep it separate from this new Java project. You can place it in a distinct directory, for example, `../openmanus-python-original` or a sub-folder named `legacy/openmanus_python` within your main project directory but ensure it's excluded from the Java build (e.g., via `.gitignore` if it's in a subfolder of the Java project's git repo).
    *   The new Java project should be a clean, from-scratch implementation based on the *core functionalities* of OpenManus, not a direct line-by-line translation.

*   **4.2. Recommended Java Project Structure (Maven Multi-Module)**
    *   A multi-module Maven project is recommended for better organization, separation of concerns, and maintainability as the project grows.
    *   **Parent POM (`openmanus-java/pom.xml`):**
        *   Manages common dependencies, plugins, and properties for all sub-modules.
        *   Packaging type: `pom`.
        *   Modules section lists all sub-modules.

    *   **Sub-Modules:**
        *   **`openmanus-app` (or `openmanus-main`)**:
            *   *Description:* The main application module. Contains the Spring Boot application class (`@SpringBootApplication`) and configuration to wire everything together. This module will depend on all other relevant modules (`api`, `core`, `llm-integration`, `tool-executor`).
            *   *Packaging:* `jar` (executable Spring Boot fat jar).
            *   *Responsibilities:* Application entry point, global configurations, wiring modules.
        *   **`openmanus-api`**:
            *   *Description:* Defines the external RESTful APIs for interacting with the OpenManus Java application.
            *   *Packaging:* `jar`.
            *   *Key Components:* Spring MVC Controllers (`@RestController`), Data Transfer Objects (DTOs) for API requests/responses, API documentation setup (e.g., OpenAPI/Swagger).
            *   *Dependencies:* `openmanus-core`.
        *   **`openmanus-core`**:
            *   *Description:* Contains the central business logic, domain models, service interfaces, and core orchestration logic of the OpenManus agent.
            *   *Packaging:* `jar`.
            *   *Key Components:* Service classes (`@Service`), domain entities/models (POJOs), repository interfaces (if data persistence is needed for core entities like Tasks or Agent State), core algorithms, and workflow definitions.
            *   *Dependencies:* `openmanus-llm-integration`, `openmanus-tool-executor`, persistence-related dependencies (e.g., Spring Data JPA).
        *   **`openmanus-llm-integration`**:
            *   *Description:* Handles all interactions with Large Language Models (LLMs).
            *   *Packaging:* `jar`.
            *   *Key Components:* Clients or services for specific LLM providers (e.g., OpenAI), request/response mapping for LLM interactions, prompt templating/management.
            *   *Dependencies:* Relevant LLM SDKs (e.g., OpenAI Java SDK, Spring AI starters), HTTP client libraries if needed.
        *   **`openmanus-tool-executor`**:
            *   *Description:* Manages and executes various tools that the AI agent can use (e.g., browser automation, code execution).
            *   *Packaging:* `jar`.
            *   *Key Components:* Interfaces for generic tools (`Tool.java`), concrete tool implementations (e.g., `BrowserAutomationTool.java`, `CodeExecutionTool.java`), sandboxing mechanisms if applicable.
            *   *Dependencies:* Libraries for specific tools (e.g., Selenium/Playwright, libraries for process execution).
        *   **`openmanus-config` (Optional, can be part of `openmanus-app` initially):**
            *   *Description:* If configuration becomes complex, this module can centralize configuration loading and management beyond Spring Boot's standard `application.properties/yml`.
            *   *Packaging:* `jar`.
        *   **`openmanus-commons` (Optional):**
            *   *Description:* Utility classes, common constants, or shared DTOs used across multiple modules.
            *   *Packaging:* `jar`.

*   **4.3. Recommended Package Structure (within each module)**
    *   Follow standard Java conventions. Use a consistent base package name, for example, `io.github.yourusername.openmanusjava` (or `com.yourcompany.openmanusjava`).
    *   Within each module, further structure by feature or layer:
        *   `io.github.yourusername.openmanusjava.module.controller` (for API controllers)
        *   `io.github.yourusername.openmanusjava.module.service` (for business logic services)
        *   `io.github.yourusername.openmanusjava.module.service.impl` (for service implementations)
        *   `io.github.yourusername.openmanusjava.module.model` or `domain` (for business objects/entities)
        *   `io.github.yourusername.openmanusjava.module.dto` (for API data transfer objects)
        *   `io.github.yourusername.openmanusjava.module.repository` (for data access interfaces, e.g., Spring Data)
        *   `io.github.yourusername.openmanusjava.module.config` (for module-specific configurations)
        *   `io.github.yourusername.openmanusjava.module.client` (for external service clients, e.g., LLM clients)
        *   `io.github.yourusername.openmanusjava.module.tool` (for tool implementations in `openmanus-tool-executor`)
        *   `io.github.yourusername.openmanusjava.module.exception` (for custom exceptions)
        *   `io.github.yourusername.openmanusjava.module.util` (for utility classes)

*   **4.4. Naming Conventions**
    *   **Packages:** Lowercase, dot-separated (e.g., `io.github.yourusername.openmanusjava.core.service`).
    *   **Classes & Interfaces:** CamelCase, starting with an uppercase letter (e.g., `TaskOrchestrationService`, `LlmClient`).
    *   **Methods & Variables:** camelCase, starting with a lowercase letter (e.g., `executeTask`, `taskDescription`).
    *   **Constants:** All uppercase, words separated by underscores (e.g., `MAX_RETRIES`).
    *   **Modules (Maven Artifact IDs):** Lowercase, hyphen-separated (e.g., `openmanus-api`, `openmanus-tool-executor`).
    *   **Configuration files:** `application.properties` or `application.yml`. Specific profiles: `application-dev.properties`, `application-prod.properties`.

*   **4.5. Initial Directory Structure Example (Illustrative)**
    ```
    openmanus-java/
    ├── pom.xml                   (Parent POM)
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
    └── legacy/ (Optional, for original Python code, add to .gitignore)
        └── openmanus_python/
    ```
This structure provides a good starting point for a scalable and organized Spring Boot application.

## 5. Core Module Design & Implementation

### 5.1. Task Intake & Orchestration Engine

*   **5.1.1. Functional Description:**
    *   This engine is the brain of the OpenManus Java application. It's responsible for receiving user-defined tasks (or "ideas"), interpreting them (often with the help of the LLM Integration Module), planning a sequence of actions, orchestrating the execution of these actions by invoking appropriate tools or further LLM interactions, and managing the overall task lifecycle and state. It may involve breaking down complex tasks into smaller, manageable sub-tasks.

*   **5.1.2. Module Design (`openmanus-core`):**
    *   **Primary Spring Boot Module:** `openmanus-core`
    *   **Key Classes & Interfaces:**
        *   `TaskRequestDTO` (in `openmanus-api` or `openmanus-commons`): Represents the raw task input from the user (e.g., `{ "description": "Summarize the main points from example.com" }`).
        *   `Task` (Domain Entity): Represents a task within the system.
            *   Attributes: `id`, `description`, `status` (e.g., PENDING, IN_PROGRESS, COMPLETED, FAILED), `creationTimestamp`, `completionTimestamp`, `initialRequest` (TaskRequestDTO), `plannedSteps` (List<Step>), `results` (String or JSON).
            *   Could be a JPA entity if task persistence is desired.
        *   `Step` (Domain Object/Entity): Represents a single step within a task's execution plan.
            *   Attributes: `id`, `taskId`, `description`, `toolName` (e.g., "browser.navigate", "llm.generateText", "code.executePython"), `parameters` (Map<String, Object>), `status`, `result`, `sequenceOrder`.
        *   `TaskController` (in `openmanus-api`):
            *   `POST /api/v1/tasks`: Submits a new task. Accepts `TaskRequestDTO`. Returns `Task` or `TaskId`.
            *   `GET /api/v1/tasks/{taskId}`: Retrieves task status and results.
        *   `TaskOrchestrationService` (Interface in `openmanus-core`):
            *   `Task processNewTask(TaskRequestDTO request)`: Main method to initiate task processing.
            *   `Task getTaskStatus(String taskId)`: Retrieves task status.
            *   `void executeNextStep(String taskId)` (potentially internal or triggered by events).
        *   `TaskOrchestrationServiceImpl` (Implementation in `openmanus-core`):
            *   Coordinates with `PlannerService`, `ToolExecutorService`, and `LlmService`.
            *   Manages task state transitions.
        *   `PlannerService` (Interface & Impl in `openmanus-core`):
            *   `List<Step> createPlan(Task task, String userGoal)`: Interacts with the LLM module to break down the user's goal into executable steps.
        *   `TaskRepository` (Interface extending Spring Data `JpaRepository<Task, String>` if using JPA): For persisting `Task` entities.
        *   `StepRepository` (Optional, if `Step` is an entity): For persisting `Step` entities.
    *   **Data Structures:**
        *   Task states (Enum: `PENDING`, `PLANNING`, `EXECUTING`, `COMPLETED`, `FAILED`, `CANCELED`).
        *   Step states (Enum: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`).
    *   **Workflow:**
        1.  User submits task via API (`TaskController`).
        2.  `TaskOrchestrationService` receives the request, creates a `Task` entity (status PENDING), and persists it.
        3.  It calls `PlannerService` to generate a plan (a list of `Step` objects). This usually involves prompting an LLM via the `LlmIntegrationModule`.
        4.  The plan (steps) is stored. Task status becomes `PLANNING` then `EXECUTING`.
        5.  The `TaskOrchestrationService` iterates through the steps:
            *   For each step, it determines the required tool or action.
            *   It calls the `ToolExecutionModule` (e.g., `BrowserAutomationTool.execute(...)`, `CodeExecutionTool.execute(...)`) or `LlmIntegrationModule`.
            *   Step status is updated based on execution outcome.
            *   If a step fails, the task might be marked as FAILED or a remediation strategy might be attempted.
        6.  Once all steps are completed, the task is marked `COMPLETED` and results are stored.

*   **5.1.3. Technology Selection & Rationale:**
    *   **Core Framework:** Spring Boot (`spring-boot-starter-web` for API, `spring-boot-starter-data-jpa` if persisting tasks).
    *   **Task Persistence (Optional but Recommended):**
        *   *Technology:* Spring Data JPA with PostgreSQL (production) or H2 (development).
        *   *Reason:* To store task definitions, states, and results, allowing for asynchronous processing, recovery, and history.
    *   **Asynchronous Execution:**
        *   *Technology:* Spring's `@Async` annotation, `ThreadPoolTaskExecutor`. For more complex scenarios, consider Spring Integration or a dedicated message queue.
        *   *Reason:* Task planning and execution (especially tool usage like browser automation or long LLM calls) can be time-consuming. Asynchronous processing prevents blocking API requests and allows the system to handle multiple tasks concurrently.
    *   **Workflow/State Management (Initial):** Custom logic within `TaskOrchestrationServiceImpl`.
    *   **Workflow/State Management (Advanced Option):**
        *   *Technology:* Flowable or Camunda (lightweight, embeddable BPMN engines).
        *   *Reason:* For very complex task orchestrations with many steps, conditional logic, human approvals (not in OpenManus's typical scope but possible for extensions), or long-running sagas, a dedicated workflow engine can simplify development and provide better visibility and management. For the initial replication, custom Spring-managed logic is likely sufficient.

*   **5.1.4. Implementation Ideas & Key Logic (Pseudo-code):**

    ```java
    // TaskOrchestrationServiceImpl.java
    @Service
    public class TaskOrchestrationServiceImpl implements TaskOrchestrationService {
        @Autowired private PlannerService plannerService;
        @Autowired private ToolExecutorRouter toolExecutorRouter; // Routes to correct tool service
        @Autowired private TaskRepository taskRepository;
        @Autowired private ApplicationEventPublisher eventPublisher;

        @Override
        @Async // Make this asynchronous
        public Task processNewTask(TaskRequestDTO request) {
            Task task = new Task(request.getDescription());
            task.setStatus(TaskStatus.PENDING);
            task = taskRepository.save(task);

            try {
                task.setStatus(TaskStatus.PLANNING);
                taskRepository.save(task);

                List<Step> steps = plannerService.createPlan(task, request.getDescription());
                task.setPlannedSteps(steps);
                // Persist steps if they are entities

                task.setStatus(TaskStatus.EXECUTING);
                taskRepository.save(task);

                executeTaskSteps(task);

                task.setStatus(TaskStatus.COMPLETED);
                // Aggregate results if necessary
                taskRepository.save(task);
                eventPublisher.publishEvent(new TaskCompletedEvent(this, task.getId()));

            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED);
                task.setResults("Error: " + e.getMessage());
                taskRepository.save(task);
                eventPublisher.publishEvent(new TaskFailedEvent(this, task.getId(), e.getMessage()));
            }
            return task;
        }

        private void executeTaskSteps(Task task) {
            for (Step step : task.getPlannedSteps()) {
                step.setStatus(StepStatus.IN_PROGRESS);
                // Persist step status

                try {
                    ToolExecutionResult result = toolExecutorRouter.executeTool(step.getToolName(), step.getParameters());
                    step.setResult(result.getOutput());
                    step.setStatus(StepStatus.COMPLETED);
                } catch (ToolExecutionException e) {
                    step.setResult("Error: " + e.getMessage());
                    step.setStatus(StepStatus.FAILED);
                    // Persist step status
                    throw new TaskExecutionException("Step failed: " + step.getDescription(), e); // Propagate to mark task as FAILED
                }
                // Persist step status and result
            }
        }
    }

    // PlannerServiceImpl.java
    @Service
    public class PlannerServiceImpl implements PlannerService {
        @Autowired private LlmService llmService; // From LLM Integration module

        @Override
        public List<Step> createPlan(Task task, String userGoal) {
            String prompt = "Given the goal: '" + userGoal + "', break it down into a series of executable steps. Each step should specify a tool and its parameters. Available tools: [browser.navigate(url), browser.extractText(selector), code.executePython(script), llm.ask(question)]. Respond with a JSON list of steps.";
            String llmResponse = llmService.generateText(prompt); // Simplified
            // Parse llmResponse (JSON) into List<Step>
            // Example LLM response:
            // [
            //   { "description": "Navigate to the website", "toolName": "browser.navigate", "parameters": { "url": "https://example.com" } },
            //   { "description": "Extract main content", "toolName": "browser.extractText", "parameters": { "selector": "body" } },
            //   { "description": "Summarize content", "toolName": "llm.ask", "parameters": { "question": "Summarize the following text: ${previous_step_output}" } }
            // ]
            // This parsing logic needs to be robust. Steps might need to reference outputs of previous steps.
            return parseLlmResponseToSteps(llmResponse);
        }
    }
    ```

*   **5.1.5. API Design (`openmanus-api` - TaskController):**
    *   **POST `/api/v1/tasks`**
        *   *Description:* Submits a new task for the agent to perform.
        *   *Request Body:* `TaskRequestDTO`
            ```json
            {
                "description": "What is the current weather in London and summarize today's top news from BBC?",
                "priority": "NORMAL" // Optional
            }
            ```
        *   *Response Body (Success - 202 Accepted):* `Task` (initial state) or `TaskSubmissionResponseDTO`
            ```json
            {
                "taskId": "ts_123xyz",
                "description": "What is the current weather in London and summarize today's top news from BBC?",
                "status": "PENDING",
                "submittedAt": "2024-07-15T10:00:00Z",
                "links": [
                    { "rel": "self", "href": "/api/v1/tasks/ts_123xyz" }
                ]
            }
            ```
    *   **GET `/api/v1/tasks/{taskId}`**
        *   *Description:* Retrieves the status and results of a specific task.
        *   *Path Variable:* `taskId` (string)
        *   *Response Body (Success - 200 OK):* `Task`
            ```json
            {
                "taskId": "ts_123xyz",
                "description": "What is the current weather in London and summarize today's top news from BBC?",
                "status": "COMPLETED", // or IN_PROGRESS, FAILED, etc.
                "submittedAt": "2024-07-15T10:00:00Z",
                "completedAt": "2024-07-15T10:05:00Z", // If completed
                "plannedSteps": [
                    { "description": "Get weather for London", "toolName": "weather.getCurrent", "status": "COMPLETED", "result": "{...weather_data...}"},
                    { "description": "Fetch BBC top news", "toolName": "browser.navigate", "status": "COMPLETED", "result": "Navigated to bbc.com/news"},
                    { "description": "Extract headlines", "toolName": "browser.extractText", "status": "COMPLETED", "result": "[Headline 1, Headline 2]"},
                    { "description": "Summarize news", "toolName": "llm.ask", "status": "COMPLETED", "result": "Summary of news..."}
                ],
                "finalResult": { // Combined or final output
                    "weather": "{...weather_data...}",
                    "newsSummary": "Summary of news..."
                },
                "error": null // Or error details if status is FAILED
            }
            ```
This module will form the backbone of the application, coordinating all other parts to fulfill user requests.

### 5.2. LLM Integration Module

*   **5.2.1. Functional Description:**
    *   This module is responsible for all direct communication with Large Language Models (LLMs). It provides a standardized interface for other modules (like the Task Orchestration Engine or specific tools) to send requests to LLMs and receive responses. It handles API key management, request formatting, and basic response parsing. It should be designed to be potentially extensible to support various LLM providers.

*   **5.2.2. Module Design (`openmanus-llm-integration`):**
    *   **Primary Spring Boot Module:** `openmanus-llm-integration`
    *   **Key Classes & Interfaces:**
        *   `LlmService` (Interface):
            *   `String generateText(String prompt, LlmParameters params)`: For general text generation, planning, etc.
            *   `String chat(List<ChatMessage> messages, LlmParameters params)`: For conversational interactions.
            *   `List<Embedding> embed(List<String> texts)`: (Optional, for future use like RAG) Generates embeddings for texts.
        *   `LlmServiceImpl` (Implementation):
            *   Implements `LlmService`.
            *   Uses a specific `LlmClient` based on configuration.
        *   `LlmClient` (Interface):
            *   Defines methods for interacting with a specific type of LLM provider (e.g., OpenAI, Anthropic, a local model server).
            *   `LlmResponse call(LlmRequest request)`
        *   `OpenAiClient` (Implementation of `LlmClient`):
            *   Uses the OpenAI API (e.g., via OpenAI Java SDK or Spring AI's OpenAI client).
            *   Handles authentication, request building, and response parsing specific to OpenAI.
        *   `LlmParameters` (POJO):
            *   Attributes: `model` (e.g., "gpt-4o", "claude-3-opus"), `temperature`, `maxTokens`, `stopSequences`, etc.
        *   `ChatMessage` (POJO):
            *   Attributes: `role` (Enum: USER, ASSISTANT, SYSTEM), `content` (String).
        *   `LlmRequest`, `LlmResponse` (POJOs): Internal representations for requests/responses to/from `LlmClient`.
        *   `LlmConfigProperties` (Spring `@ConfigurationProperties`):
            *   Attributes: `provider` (e.g., "openai", "anthropic"), `apiKey`, `baseUrl`, `defaultModel`, `timeoutSeconds`.
    *   **Data Structures:**
        *   `ChatMessage` for conversational context.
        *   Enums for roles (`USER`, `ASSISTANT`, `SYSTEM`), LLM providers.
    *   **Workflow:**
        1.  Another service (e.g., `PlannerService`) needs an LLM interaction.
        2.  It calls a method on `LlmService` (e.g., `generateText(prompt, params)`).
        3.  `LlmServiceImpl` retrieves necessary configuration (API key, model) from `LlmConfigProperties`.
        4.  It selects the appropriate `LlmClient` implementation (e.g., `OpenAiClient`).
        5.  The `LlmClient` constructs the provider-specific request and makes the HTTP call.
        6.  The response is parsed, and the relevant content (e.g., generated text) is returned up the chain.

*   **5.2.3. Technology Selection & Rationale:**
    *   **LLM SDKs/Clients:**
        *   **Option 1: Spring AI (Recommended for future flexibility):**
            *   *Technology:* `spring-ai-openai-starter` (or other starters for different models).
            *   *Reason:* Spring AI aims to provide a common abstraction layer for various AI models, simplifying integration. It handles boilerplate for client creation, authentication, and offers features like prompt templating. If Spring Boot 2.7.x has good Spring AI support for the target JDK (17), this is a strong contender. (Need to verify Spring AI's compatibility and maturity with SB 2.7 specifically, as it's a newer project).
        *   **Option 2: Direct SDK Usage (Good for specific provider focus):**
            *   *Technology:* Official OpenAI Java SDK (e.g., `com.openai:openai-client`).
            *   *Reason:* If the primary focus is OpenAI, using their official SDK is straightforward and gives full access to their API features.
        *   **Option 3: Generic HTTP Client:**
            *   *Technology:* `RestTemplate`, `WebClient` (from Spring WebFlux, can be used in MVC too), or Apache HttpClient / OkHttp.
            *   *Reason:* If no suitable SDK exists or maximum control over requests is needed. More boilerplate to implement.
    *   **Configuration Management:**
        *   *Technology:* Spring Boot `@ConfigurationProperties` and `application.properties` / `application.yml`.
        *   *Reason:* Standard Spring Boot way to manage externalized configuration securely (e.g., using environment variables or Vault for API keys in production).
    *   **JSON Processing:**
        *   *Technology:* Jackson (comes with `spring-boot-starter-web`).
        *   *Reason:* For serializing requests and deserializing responses from LLMs.

*   **5.2.4. Implementation Ideas & Key Logic (Pseudo-code):**

    ```java
    // LlmConfigProperties.java
    @ConfigurationProperties(prefix = "openmanus.llm")
    public class LlmConfigProperties {
        private String provider; // "openai", "anthropic", etc.
        private String apiKey;
        private String baseUrl;
        private String defaultModel;
        // getters and setters
    }

    // LlmService.java
    public interface LlmService {
        String generateText(String prompt, LlmParameters params);
        // other methods
    }

    // LlmServiceImpl.java
    @Service
    public class LlmServiceImpl implements LlmService {
        private final LlmClient llmClient;
        private final LlmConfigProperties config;

        // Constructor injection for the specific client based on config
        public LlmServiceImpl(ApplicationContext context, LlmConfigProperties config) {
            this.config = config;
            if ("openai".equalsIgnoreCase(config.getProvider())) {
                this.llmClient = context.getBean(OpenAiClient.class); // Or new OpenAiClient(config)
            } else {
                throw new IllegalArgumentException("Unsupported LLM provider: " + config.getProvider());
            }
        }

        @Override
        public String generateText(String prompt, LlmParameters params) {
            LlmRequest request = new LlmRequest();
            request.setPrompt(prompt);
            request.setModel(params.getModel() != null ? params.getModel() : config.getDefaultModel());
            request.setTemperature(params.getTemperature());
            // ... set other parameters

            LlmResponse response = llmClient.call(request);
            return response.getGeneratedText(); // Simplified
        }
    }

    // OpenAiClient.java (Illustrative, using a hypothetical SDK or direct HTTP)
    @Component
    public class OpenAiClient implements LlmClient {
        private final LlmConfigProperties config;
        // private final OpenAiApi openAiApi; // If using OpenAI SDK

        public OpenAiClient(LlmConfigProperties config) {
            this.config = config;
            // this.openAiApi = new OpenAiApi(config.getApiKey());
        }

        @Override
        public LlmResponse call(LlmRequest manusRequest) {
            // 1. Construct OpenAI specific request from LlmRequest
            //    (e.g., ChatCompletionRequest for chat models)
            // 2. Make the API call using the SDK or an HTTP client
            //    ChatCompletionRequest apiRequest = ChatCompletionRequest.builder()
            //        .model(manusRequest.getModel())
            //        .messages(List.of(new ChatMessage(ChatMessageRole.USER, manusRequest.getPrompt())))
            //        .temperature(manusRequest.getTemperature())
            //        .build();
            //    ChatCompletionResult result = openAiApi.createChatCompletion(apiRequest).join();
            // 3. Parse OpenAI specific response into generic LlmResponse
            //    LlmResponse response = new LlmResponse();
            //    response.setGeneratedText(result.getChoices().get(0).getMessage().getContent());
            //    return response;
            throw new UnsupportedOperationException("Implementation pending"); // Placeholder
        }
    }
    ```

*   **5.2.5. API Design:**
    *   This module primarily provides internal services (`LlmService`) to other modules within the application. It does not typically expose its own public RESTful APIs unless there's a specific need to manage LLM configurations or test LLM connectivity directly via an admin interface. Any such admin APIs would be secondary to its core function of serving other internal modules.
This module is crucial for enabling the "intelligence" of the agent, allowing it to understand tasks, generate plans, and produce human-like text.

### 5.3. Tool Execution Module

*   **5.3.1. Functional Description:**
    *   This module is responsible for executing concrete actions or "tools" that the OpenManus agent can use to interact with its environment or process information. It acts as a registry and executor for various capabilities like web browsing, running code, fetching data, etc. It provides a standardized way for the Orchestration Engine to invoke these tools and receive their outputs.

*   **5.3.2. Module Design (`openmanus-tool-executor`):**
    *   **Primary Spring Boot Module:** `openmanus-tool-executor`
    *   **Key Classes & Interfaces (General Tool Framework):**
        *   `Tool` (Interface):
            *   `String getName()`: Returns a unique name for the tool (e.g., "browser.navigate", "code.executePython").
            *   `ToolExecutionResult execute(Map<String, Object> parameters)`: Executes the tool with given parameters.
            *   `List<ToolParameterDefinition> getParameterDefinitions()`: Describes the parameters the tool expects.
        *   `ToolParameterDefinition` (POJO):
            *   Attributes: `name`, `type`, `description`, `isRequired`.
        *   `ToolExecutionResult` (POJO):
            *   Attributes: `status` (SUCCESS, FAILURE), `output` (String, JSON, or complex object), `error` (String, if failed).
        *   `ToolRegistry` (Service):
            *   `void registerTool(Tool tool)`: Adds a tool to the registry.
            *   `Tool getTool(String toolName)`: Retrieves a tool by its name.
            *   (Often populated at startup by Spring detecting `@Component` tools).
        *   `ToolExecutorService` (Interface, might be part of `TaskOrchestrationServiceImpl` or a dedicated router):
            *   `ToolExecutionResult executeTool(String toolName, Map<String, Object> parameters)`: Finds and executes the specified tool. This could also be named `ToolRouterService`.
        *   `ToolExecutorServiceImpl` (Implementation):
            *   Uses `ToolRegistry` to find the tool.
            *   Invokes the tool's `execute` method.
            *   Handles common pre/post execution logic if any (e.g., logging, basic validation).
    *   **Workflow:**
        1.  The `TaskOrchestrationService` determines that a step requires a specific tool.
        2.  It calls `ToolExecutorService.executeTool("some.tool", params)`.
        3.  `ToolExecutorService` looks up "some.tool" in the `ToolRegistry`.
        4.  If found, it calls the tool's `execute(params)` method.
        5.  The tool performs its action (e.g., navigates a browser, runs a script).
        6.  The tool returns a `ToolExecutionResult`.
        7.  This result is passed back to the `TaskOrchestrationService`.

*   **5.3.3. Technology Selection & Rationale (General Tool Framework):**
    *   **Core Framework:** Plain Java with Spring for dependency injection (`@Component` for tools, `@Service` for registry/executor).
    *   **Reason:** Simplicity and leveraging Spring's existing DI capabilities for managing tool instances.

*   **5.3.4. Implementation Ideas & Key Logic (Pseudo-code - General Tool Framework):**
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
        private Object output; // Could be String, Map, List, etc.
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

        public ToolRegistry(List<Tool> tools) { // Autowire all Tool implementations
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

    // ToolExecutorService.java (or ToolRouterService)
    @Service
    public class ToolExecutorService {
        @Autowired private ToolRegistry toolRegistry;

        public ToolExecutionResult executeTool(String toolName, Map<String, Object> parameters) throws ToolNotFoundException, ToolExecutionException {
            Tool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                throw new ToolNotFoundException("Tool not found: " + toolName);
            }
            // Basic validation of parameters against tool.getParameterDefinitions() could go here.
            return tool.execute(parameters);
        }
    }
    ```

**5.3.A. Sub-Component: Browser Automation Tool**

    *   **5.3.A.1. Functional Description:**
        *   Enables the agent to interact with web browsers to perform tasks like navigating to URLs, extracting information from web pages (text, HTML, specific elements), filling out forms, and clicking buttons. This replicates the browser interaction capabilities seen in the original OpenManus (which uses Playwright).

    *   **5.3.A.2. Module Design (within `openmanus-tool-executor`):**
        *   **Key Classes & Interfaces:**
            *   `BrowserAutomationTool` (Implements `Tool`):
                *   Manages a browser instance (e.g., WebDriver).
                *   Provides specific actions as sub-commands or by interpreting parameters.
                *   `getName()`: Could be a generic "browser" and actions are passed as parameters, or specific tools like "browser.navigate", "browser.extractText". Let's assume a more generic `BrowserTool` that takes an action parameter.
            *   `BrowserAction` (Enum or String constants): NAVIGATE, CLICK, TYPE, EXTRACT_TEXT, EXTRACT_HTML, GET_CURRENT_URL, etc.
        *   **Example `Tool` name and parameters:**
            *   Tool Name: `browser.do`
            *   Parameters:
                *   `action`: (String, e.g., "NAVIGATE", "CLICK", "EXTRACT_TEXT")
                *   `url`: (String, for NAVIGATE)
                *   `selector`: (String, for CLICK, TYPE, EXTRACT_TEXT - CSS selector or XPath)
                *   `text`: (String, for TYPE)
                *   `timeoutSeconds`: (Integer, optional)

    *   **5.3.A.3. Technology Selection & Rationale:**
        *   **Option 1: Selenium WebDriver (Mature, Wide Support):**
            *   *Technology:* `org.seleniumhq.selenium:selenium-java`.
            *   *Reason:* Long-standing, well-supported library for browser automation in Java. Works with most major browsers (Chrome, Firefox, Edge, Safari). Large community and extensive documentation.
        *   **Option 2: Playwright for Java (Modern, Good Features):**
            *   *Technology:* `com.microsoft.playwright:playwright`.
            *   *Reason:* More modern API than Selenium, built by Microsoft. Offers features like auto-waits, network interception, and parallel execution. If OpenManus Python used Playwright, this provides closer parity.
            *   *Consideration:* Ensure its Java version is stable and integrates well with the project setup. Might require separate driver downloads managed by Playwright CLI or its API.
        *   **Recommendation:** Start with **Playwright for Java** if parity with the original project is desired and its Java bindings are mature. Otherwise, Selenium is a very safe and robust choice. For this document, let's assume Playwright for Java.

    *   **5.3.A.4. Implementation Ideas & Key Logic (Pseudo-code):**
        ```java
        // BrowserTool.java (Illustrative using Playwright)
        @Component
        public class BrowserTool implements Tool {
            private Playwright playwright;
            private Browser browser;

            // Initialize Playwright & Browser (e.g., in @PostConstruct or on first use)
            // Ensure proper lifecycle management (close browser, playwright)

            @PostConstruct
            public void init() {
                try {
                    playwright = Playwright.create();
                    // Launch options can be configured (headless, browser type)
                    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                } catch (Exception e) {
                    // Handle initialization error
                    throw new RuntimeException("Failed to initialize Playwright browser", e);
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
                // Define parameters: "action", "url", "selector", "text", etc.
                return List.of(
                    new ToolParameterDefinition("action", "String", "Action to perform (NAVIGATE, CLICK, TYPE, EXTRACT_TEXT, GET_URL)", true),
                    new ToolParameterDefinition("url", "String", "URL for NAVIGATE action", false),
                    new ToolParameterDefinition("selector", "String", "CSS Selector or XPath for element interaction", false),
                    new ToolParameterDefinition("text", "String", "Text to type for TYPE action", false)
                    // ... other params
                );
            }

            @Override
            public ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException {
                String action = (String) parameters.get("action");
                Page page = null; // Manage page lifecycle carefully

                try {
                    page = browser.newPage(); // Each execution might use a new page/context for isolation
                    ToolExecutionResult result = new ToolExecutionResult();
                    result.setStatus("SUCCESS");

                    switch (action.toUpperCase()) {
                        case "NAVIGATE":
                            String url = (String) parameters.get("url");
                            page.navigate(url);
                            result.setOutput("Navigated to: " + url);
                            break;
                        case "EXTRACT_TEXT":
                            String selector = (String) parameters.get("selector");
                            result.setOutput(page.textContent(selector));
                            break;
                        case "CLICK":
                            String clickSelector = (String) parameters.get("selector");
                            page.click(clickSelector);
                            result.setOutput("Clicked element: " + clickSelector);
                            break;
                        case "TYPE":
                            String typeSelector = (String) parameters.get("selector");
                            String textToType = (String) parameters.get("text");
                            page.fill(typeSelector, textToType); // or page.type for individual keystrokes
                            result.setOutput("Typed into element: " + typeSelector);
                            break;
                        case "GET_URL":
                            result.setOutput(page.url());
                            break;
                        default:
                            throw new ToolExecutionException("Unsupported browser action: " + action);
                    }
                    return result;
                } catch (PlaywrightException e) {
                    throw new ToolExecutionException("Browser action '" + action + "' failed: " + e.getMessage(), e);
                } finally {
                    if (page != null) {
                        page.close();
                    }
                }
            }
        }
        ```
This provides a foundation for browser-based interactions. The specific actions and parameter handling can be expanded significantly.

**5.3.B. Sub-Component: Code Execution Tool**

    *   **5.3.B.1. Functional Description:**
        *   Enables the agent to execute arbitrary code snippets in a controlled environment. This is crucial for tasks requiring complex computation, data analysis (e.g., using Python libraries like Pandas, NumPy), or accessing functionalities available in other programming languages. The initial focus will be on executing Python code, similar to capabilities implied by the "DataAnalysis Agent" in OpenManus. Secure sandboxing is a critical consideration.

    *   **5.3.B.2. Module Design (within `openmanus-tool-executor`):**
        *   **Key Classes & Interfaces:**
            *   `CodeExecutionTool` (Implements `Tool`):
                *   `getName()`: e.g., "code.executePython", "code.executeJavascript". For now, focus on "code.executePython".
                *   Manages the execution environment (e.g., Docker container, local sandboxed process).
            *   **Parameters for `execute` method:**
                *   `language`: (String, e.g., "python", "javascript") - initially "python".
                *   `script`: (String) The actual code to execute.
                *   `inputData`: (String or JSON, optional) Data to be passed to the script (e.g., via stdin or a temporary file).
                *   `timeoutSeconds`: (Integer, optional) Maximum execution time.
            *   **`ToolExecutionResult` would contain:**
                *   `stdout`: Standard output from the script.
                *   `stderr`: Standard error from the script.
                *   `exitCode`: Exit code of the script.
                *   `artifacts`: (Map<String, String> or List<String>, optional) Paths to files generated by the script that should be made available (e.g., charts, data files).

        *   **Security Considerations:**
            *   **Sandboxing is paramount.** Untrusted code execution is a major security risk.
            *   **Option 1: Docker Containers:** Launch a new Docker container for each execution.
                *   *Pros:* Strong isolation. Environment can be precisely defined with necessary libraries.
                *   *Cons:* Overhead (startup time, resource usage). Docker must be available.
            *   **Option 2: Process-based Sandboxing with OS-level controls:** Use `ProcessBuilder` with restricted permissions, user accounts, and potentially tools like `nsjail` (Linux) or equivalent.
                *   *Pros:* Lighter than Docker if well-configured.
                *   *Cons:* Harder to set up correctly and ensure cross-platform consistency. True isolation is challenging.
            *   **Option 3: Language-specific Sandboxes/Interpreters:** Some languages offer embedded interpreters with sandboxing capabilities (e.g., GraalVM for some languages).
                *   *Pros:* Can be efficient.
                *   *Cons:* May not be available for all desired languages (e.g., general Python C libraries). Sandboxing capabilities vary.
            *   **Recommendation:** Docker-based sandboxing is the most robust and commonly accepted approach for executing untrusted code with external library dependencies.

    *   **5.3.B.3. Technology Selection & Rationale:**
        *   **Code Execution Runtime:**
            *   **Python:** Requires a Python interpreter accessible to the execution environment.
        *   **Sandboxing Mechanism:**
            *   *Technology (Recommended):* Docker. Use a Java Docker client library.
                *   `com.github.docker-java:docker-java` and its transport/connector dependencies (e.g., `docker-java-transport-httpclient5`).
            *   *Reason:* Provides strong isolation and environment control. A pre-built Docker image with Python and common data analysis libraries (Pandas, NumPy, Matplotlib, Scikit-learn) should be used.
        *   **Alternative (Simpler, Less Secure, for trusted scripts only or very early dev):** `java.lang.ProcessBuilder`
            *   *Reason:* Simple for invoking local scripts. Not suitable for untrusted code from users or LLMs due to security risks.
        *   **File Handling:** Java NIO for managing temporary script files, input/output data files, and artifact collection.

    *   **5.3.B.4. Implementation Ideas & Key Logic (Pseudo-code - Docker-based Python execution):**
        ```java
        // CodeExecutionTool.java
        @Component
        public class CodeExecutionTool implements Tool {
            // DockerClient setup (potentially configured and injected)
            private final DockerClient dockerClient;
            private final String pythonDockerImage = "python:3.10-slim"; // Or a custom image with libraries

            public CodeExecutionTool(/* DockerClient injected */) {
                // Initialize DockerClient, e.g., DockerClientConfig.createDefaultConfigBuilder().build()
                // this.dockerClient = DockerClientBuilder.getInstance(config).build();
                this.dockerClient = null; // Placeholder for actual initialization
                if (this.dockerClient == null) {
                     System.err.println("Warning: DockerClient not initialized for CodeExecutionTool. Executions will fail.");
                }
            }

            @Override
            public String getName() { return "code.executePython"; }

            @Override
            public List<ToolParameterDefinition> getParameterDefinitions() {
                return List.of(
                    new ToolParameterDefinition("script", "String", "Python script to execute", true),
                    new ToolParameterDefinition("inputData", "String", "Input data as a JSON string (optional)", false),
                    new ToolParameterDefinition("timeoutSeconds", "Integer", "Execution timeout in seconds (optional, default 60)", false)
                );
            }

            @Override
            public ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException {
                if (dockerClient == null) {
                    throw new ToolExecutionException("DockerClient not available. Cannot execute code.");
                }

                String scriptContent = (String) parameters.get("script");
                String inputDataJson = (String) parameters.get("inputData"); // Optional
                int timeout = (Integer) parameters.getOrDefault("timeoutSeconds", 60);

                // Create a temporary host directory for script, input, output
                Path hostTempDir = Files.createTempDirectory("code_exec_");
                Path scriptFile = hostTempDir.resolve("script.py");
                Files.writeString(scriptFile, scriptContent);

                String containerScriptPath = "/sandbox/script.py";
                String containerInputDir = "/sandbox/input";
                String containerOutputDir = "/sandbox/output"; // For artifacts

                CreateContainerCmd containerCmd = dockerClient.createContainerCmd(pythonDockerImage)
                    .withWorkingDir("/sandbox")
                    .withCmd("python", containerScriptPath)
                    .withHostConfig(new HostConfig().withBinds(
                        new Bind(hostTempDir.toAbsolutePath().toString(), new Volume("/sandbox"))
                    ))
                    .withNetworkDisabled(true); // Essential security: disable network unless specifically needed

                // Handle inputData: write to a file in hostTempDir/input, map to containerInputDir
                if (inputDataJson != null) {
                    Path hostInputDataFile = hostTempDir.resolve("input_data.json");
                    Files.writeString(hostInputDataFile, inputDataJson);
                    // Script needs to know to read from /sandbox/input/input_data.json
                }

                String containerId = containerCmd.exec().getId();
                // Assuming ToolExecutionResult has an internal Map<String, Object> attributes
                ToolExecutionResult toolResult = new ToolExecutionResult();

                try {
                    dockerClient.startContainerCmd(containerId).exec();

                    // Wait for completion with timeout
                    WaitContainerResultCallback callback = new WaitContainerResultCallback();
                    dockerClient.waitContainerCmd(containerId).exec(callback);
                    int exitCode = callback.awaitStatusCode(timeout, TimeUnit.SECONDS); // Timeout handling

                    // Collect logs (stdout, stderr)
                    StringBuilder stdout = new StringBuilder();
                    StringBuilder stderr = new StringBuilder();
                    LogContainerCmd logCmd = dockerClient.logContainerCmd(containerId)
                        .withStdOut(true).withStdErr(true).withTimestamps(false);
                    logCmd.exec(new LogContainerTestCallback() { // LogContainerTestCallback is illustrative
                        @Override public void onNext(Frame item) {
                            if (StreamType.STDOUT.equals(item.getStreamType())) stdout.append(new String(item.getPayload()));
                            if (StreamType.STDERR.equals(item.getStreamType())) stderr.append(new String(item.getPayload()));
                        }
                    }).awaitCompletion();


                    toolResult.addAttribute("stdout", stdout.toString());
                    toolResult.addAttribute("stderr", stderr.toString());
                    toolResult.addAttribute("exitCode", exitCode);

                    if (exitCode == 0) {
                        toolResult.setStatus("SUCCESS");
                        // Example for primary output, could be stdout or a specific artifact
                        toolResult.setOutput(stdout.toString());
                        // Collect artifacts from hostTempDir/output if any
                        // toolResult.addAttribute("artifacts", collectArtifacts(hostTempDir.resolve("output")));
                    } else {
                        toolResult.setStatus("FAILURE");
                        toolResult.setError("Script exited with code: " + exitCode + "\nStderr: " + stderr.toString());
                        toolResult.setOutput(stdout.toString()); // Still provide stdout
                    }

                } catch (Exception e) { // InterruptedException, DockerException etc.
                    throw new ToolExecutionException("Error executing Python script in Docker: " + e.getMessage(), e);
                } finally {
                    // Cleanup: remove container and temporary directory
                    try { dockerClient.removeContainerCmd(containerId).withForce(true).exec(); } catch (Exception e) { /* log */ }
                    // Recursively delete hostTempDir
                    // FileUtils.deleteDirectory(hostTempDir.toFile()); // Using Apache Commons IO, or write custom
                }
                return toolResult;
            }
        }
        ```
        *   **Note on `ToolExecutionResult` fields:** The `ToolExecutionResult` class from 5.3.2 should be used. It has `status`, `output` (for primary result, e.g. stdout), and `error` fields. Additional details like `exitCode`, `stderr`, and `artifacts` can be added as attributes to a flexible map within `ToolExecutionResult` or as specific fields if they are common to many tools. The pseudo-code above uses `addAttribute` for this, assuming such a mechanism.

    *   **Security Enhancements (Further Considerations):**
        *   Use a minimal Docker image.
        *   Run the process inside the container as a non-root user.
        *   Strictly control container capabilities (e.g., no network access by default).
        *   Implement resource limits (CPU, memory) for the container.
        *   Scan the script for obviously malicious patterns (limited effectiveness).
        *   Ensure the Docker daemon itself is secured.
This tool is powerful but requires careful implementation to prevent security vulnerabilities.

### 5.4. Configuration Module

*   **5.4.1. Functional Description:**
    *   This module is responsible for managing all application configurations, with a special focus on externalized parameters, security-sensitive data (like API keys), and settings for various components like the LLM Integration and Tool Execution modules. It leverages Spring Boot's built-in configuration mechanisms but also provides a clear structure for OpenManus-specific settings.

*   **5.4.2. Module Design (Primarily leveraging Spring Boot; can be part of `openmanus-app` or a dedicated `openmanus-config` module if it grows complex):**
    *   **Primary Location:** Spring Boot's `application.properties` or `application.yml` files. For type-safe configuration, `@ConfigurationProperties` classes are used.
    *   **Key Classes & Structures:**
        *   `LlmConfigProperties` (already defined in `openmanus-llm-integration`):
            *   Example: `openmanus.llm.provider`, `openmanus.llm.apiKey`, `openmanus.llm.defaultModel`, `openmanus.llm.baseUrl`, `openmanus.llm.timeoutSeconds`.
        *   `ToolConfigProperties` (New, likely in `openmanus-tool-executor` or `openmanus-app`):
            *   Could have generic settings or tool-specific sub-classes.
            *   Example: `openmanus.tools.codeExecution.dockerImage` (e.g., "python:3.10-slim-custom"), `openmanus.tools.codeExecution.defaultTimeoutSeconds`.
            *   Example: `openmanus.tools.browser.defaultHeadless` (e.g., true), `openmanus.tools.browser.defaultTimeoutSeconds`.
        *   `TaskOrchestrationConfigProperties` (New, in `openmanus-core` or `openmanus-app`):
            *   Example: `openmanus.orchestration.defaultMaxRetries`, `openmanus.orchestration.planner.defaultLlmModel`.
        *   `DatabaseConfig`: Managed by Spring Boot's `spring.datasource.*` properties.
        *   `SecurityConfigProperties` (Optional, for security-specific settings beyond Spring Security defaults):
            *   Example: `openmanus.security.cors.allowedOrigins`.

    *   **Configuration Files Structure:**
        *   `application.properties` (or `application.yml`): Default settings, often suitable for local development.
        *   `application-dev.properties`, `application-prod.properties`, `application-test.properties`: Profile-specific configurations.
        *   Externalized configuration: Using environment variables, command-line arguments, or configuration servers (like Spring Cloud Config) for production environments, especially for sensitive data.

*   **5.4.3. Technology Selection & Rationale:**
    *   **Core Mechanism:** Spring Boot Configuration.
        *   *Reason:* Robust, flexible, well-documented. Supports properties files, YAML files, environment variables, command-line arguments, and profiles.
    *   **Type-Safe Configuration:** Spring Boot's `@ConfigurationProperties` annotation.
        *   *Reason:* Allows grouping related properties into POJOs, providing type safety and better organization.
    *   **Sensitive Data Management (Production):**
        *   *Recommendation:* Environment variables, or a dedicated secrets management tool (e.g., HashiCorp Vault, AWS Secrets Manager, Azure Key Vault) integrated with Spring Boot.
        *   *Reason:* Avoids hardcoding API keys or passwords in source code or packaged artifacts. Spring Boot has good support for externalizing these. For LLM API keys, environment variables are a common starting point.
            ```properties
            # Example: For OpenAI API Key
            OPENMANUS_LLM_API_KEY=your_actual_api_key_here # Set as an environment variable
            ```
            And in `LlmConfigProperties`:
            ```java
            @Value("${openmanus.llm.apiKey:${OPENAI_API_KEY}}") // Allow override by specific env var
            private String apiKey;
            ```

*   **5.4.4. Implementation Ideas & Key Logic:**
    *   Define clear prefixes for all custom configurations (e.g., `openmanus.llm.*`, `openmanus.tools.*`).
    *   Use `@ConfigurationProperties` classes extensively for structured access to properties.
    *   Leverage Spring Profiles (`dev`, `prod`, `test`) to manage environment-specific settings.
    *   Prioritize externalized configuration for sensitive values.

    ```java
    // Example: ToolConfigProperties.java
    // Typically placed in the module that owns the configuration, or a shared config module.
    // If in openmanus-app, it can configure beans for other modules.
    package io.github.yourusername.openmanusjava.app.config; // Or ...tool.config;

    import org.springframework.boot.context.properties.ConfigurationProperties;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.validation.annotation.Validated;

    import javax.validation.constraints.NotEmpty; // Example validation
    import javax.validation.constraints.Positive;

    @Configuration
    @ConfigurationProperties(prefix = "openmanus.tools")
    @Validated // Enable JSR 303 validation
    public class ToolConfigProperties {

        private final CodeExecution codeExecution = new CodeExecution();
        private final Browser browser = new Browser();

        public CodeExecution getCodeExecution() { return codeExecution; }
        public Browser getBrowser() { return browser; }

        public static class CodeExecution {
            @NotEmpty
            private String pythonDockerImage = "python:3.10-slim";
            @Positive
            private int defaultTimeoutSeconds = 60;
            // getters and setters
        }

        public static class Browser {
            private boolean defaultHeadless = true;
            @Positive
            private int defaultTimeoutSeconds = 30;
            // getters and setters
        }
    }

    // Usage in a tool (e.g., CodeExecutionTool)
    // @Autowired
    // private ToolConfigProperties toolConfig;
    // String image = toolConfig.getCodeExecution().getPythonDockerImage();
    ```

    *   **Example `application.yml` structure:**
        ```yaml
        spring:
          application:
            name: openmanus-java
          datasource:
            url: jdbc:postgresql://localhost:5432/openmanus_db
            username: user
            password: password # Use env vars for prod
            driver-class-name: org.postgresql.Driver
          jpa:
            hibernate:
              ddl-auto: update # Or validate for prod
            show-sql: true # For dev

        openmanus:
          llm:
            provider: openai
            # apiKey: ${OPENMANUS_LLM_API_KEY} # Loaded from env var
            baseUrl: https://api.openai.com/v1
            defaultModel: gpt-4o
            timeoutSeconds: 120
          tools:
            codeExecution:
              pythonDockerImage: "yourcustomrepo/openmanus-python-runner:latest"
              defaultTimeoutSeconds: 120
            browser:
              defaultHeadless: true
              defaultTimeoutSeconds: 45
          orchestration:
            defaultMaxRetries: 1
            planner:
              defaultLlmModel: gpt-4o # Specific model for planning if different

        # Logging, Server port, etc.
        server:
          port: 8080

        logging:
          level:
            io.github.yourusername.openmanusjava: DEBUG
            org.springframework.web: INFO
        ```

*   **5.4.5. API Design:**
    *   Generally, this module does not expose public APIs. However, an admin API endpoint (e.g., under `/actuator/configprops` if Spring Boot Actuator is used, or a custom admin controller) could be useful for inspecting current configuration values in a running application (excluding sensitive data).

Proper configuration management is key for flexibility, security, and maintainability, especially as the application moves between different environments.

## 6. Error Handling Mechanism

*   **6.1. Overview:**
    *   A robust error handling strategy is crucial for building a reliable application. The goal is to handle exceptions gracefully, provide meaningful feedback to users (or client systems), log appropriate information for debugging, and maintain system stability. This section outlines global and specific error handling approaches.

*   **6.2. Global Exception Handling:**
    *   **Technology:** Spring Boot's `@ControllerAdvice` and `@ExceptionHandler` annotations.
    *   **Approach:**
        *   Create a centralized class (e.g., `GlobalExceptionHandler.java` in the `openmanus-api` module or a shared commons module) annotated with `@ControllerAdvice`.
        *   Within this class, define methods annotated with `@ExceptionHandler` to handle specific types of exceptions that may propagate to the controller layer.
        *   These handlers will convert exceptions into standardized JSON error responses for API clients.
    *   **Standard Error Response Structure:**
        ```json
        {
            "timestamp": "2024-07-15T12:30:45.123Z",
            "status": 500, // HTTP Status Code
            "error": "Internal Server Error", // HTTP Status Message
            "message": "A detailed, user-friendly error message.",
            "errorCode": "APP_SPECIFIC_ERROR_CODE_001", // Optional: Application-specific error code
            "path": "/api/v1/tasks", // Request path
            "details": [] // Optional: For validation errors, list of field errors
        }
        ```
    *   **Example (`GlobalExceptionHandler.java`):**
        ```java
        package io.github.yourusername.openmanusjava.api.exception; // Or a common exception package

        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.MethodArgumentNotValidException;
        import org.springframework.web.bind.annotation.ControllerAdvice;
        import org.springframework.web.bind.annotation.ExceptionHandler;
        import org.springframework.web.context.request.WebRequest;
        import java.time.LocalDateTime;
        import java.util.List;
        import java.util.stream.Collectors;

        @ControllerAdvice
        public class GlobalExceptionHandler {

            @ExceptionHandler(ResourceNotFoundException.class) // Custom exception
            public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase(),
                    ex.getMessage(),
                    "RESOURCE_NOT_FOUND", // Example app-specific code
                    request.getDescription(false).replace("uri=", ""),
                    null
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            @ExceptionHandler(MethodArgumentNotValidException.class) // For @Valid validation failures
            public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
                List<String> details = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
                ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Validation Failed",
                    "VALIDATION_ERROR",
                    request.getDescription(false).replace("uri=", ""),
                    details
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            @ExceptionHandler(ToolExecutionException.class) // Custom from tool executor
            public ResponseEntity<ErrorResponse> handleToolExecutionException(ToolExecutionException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), // Or BAD_GATEWAY if tool is external
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    "Error during tool execution: " + ex.getMessage(),
                    "TOOL_EXECUTION_FAILURE",
                    request.getDescription(false).replace("uri=", ""),
                    null
                );
                // Log the full exception for debugging
                // log.error("Tool execution failed:", ex);
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @ExceptionHandler(LlmInteractionException.class) // Custom from LLM integration
            public ResponseEntity<ErrorResponse> handleLlmInteractionException(LlmInteractionException ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.SERVICE_UNAVAILABLE.value(), // Or specific codes based on LLM error
                    HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                    "Error interacting with LLM: " + ex.getMessage(),
                    "LLM_INTERACTION_FAILURE",
                    request.getDescription(false).replace("uri=", ""),
                    null
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
            }

            @ExceptionHandler(Exception.class) // Generic fallback handler
            public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    "An unexpected error occurred: " + ex.getMessage(),
                    "UNEXPECTED_ERROR",
                    request.getDescription(false).replace("uri=", ""),
                    null
                );
                // Log the full exception for debugging
                // log.error("Unexpected error caught by global handler:", ex);
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // ErrorResponse POJO (records are good for this in JDK 16+)
            // public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String errorCode, String path, List<String> details) {}
            // For JDK < 16, a regular class:
            public static class ErrorResponse {
                public LocalDateTime timestamp;
                public int status;
                public String error;
                public String message;
                public String errorCode;
                public String path;
                public List<String> details;

                public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String errorCode, String path, List<String> details) {
                    this.timestamp = timestamp;
                    this.status = status;
                    this.error = error;
                    this.message = message;
                    this.errorCode = errorCode;
                    this.path = path;
                    this.details = details;
                }
                // Add getters if not using records or Lombok
            }
        }
        ```

*   **6.3. Custom Business Exceptions:**
    *   Define specific custom exceptions for different business logic errors. This makes error handling more granular and meaningful.
    *   These custom exceptions should ideally extend a common base exception (e.g., `OpenManusException`).
    *   Examples:
        *   `ResourceNotFoundException(String message)`: For when a specific entity (e.g., Task) isn't found.
        *   `TaskPlanningException(String message)`: If the LLM fails to generate a valid plan.
        *   `ToolConfigurationException(String message)`: If a required tool is misconfigured.
        *   `ToolExecutionException(String message, Throwable cause)`: General exception from the tool execution module.
        *   `LlmInteractionException(String message, Throwable cause)`: For errors during LLM API calls (network issues, API errors).
        *   `InvalidTaskRequestException(String message)`: For invalid user input for a task.
    *   These custom exceptions will be caught by the `GlobalExceptionHandler` or by specific try-catch blocks within service layers if internal recovery or specific logging is needed.

*   **6.4. Error Codes (Optional but Recommended):**
    *   Define a system of application-specific error codes (e.g., `OM_TASK_001`, `OM_LLM_002`, `OM_TOOL_BROWSER_003`).
    *   These codes can be included in the JSON error response.
    *   *Benefits:*
        *   Provides a stable way for client applications to identify errors programmatically, even if human-readable messages change.
        *   Useful for looking up more detailed error information in documentation.
    *   Error codes can be managed as enums or constants.

*   **6.5. Logging Errors:**
    *   Crucially, all significant exceptions (especially those caught by the global handler or representing unexpected issues) must be logged with sufficient detail (stack trace, relevant context like Task ID or User ID). This will be covered more in the "Logging" section.

*   **6.6. Specific Module Error Handling:**
    *   **Task Orchestration Engine:** Must handle failures from `PlannerService`, `ToolExecutorService`. Implement retry logic for transient errors if appropriate (e.g., network glitch calling an LLM). Mark tasks as FAILED if unrecoverable.
    *   **LLM Integration Module:** Handle API errors from LLM providers (e.g., rate limits, authentication failures, server errors from LLM provider). Convert these into `LlmInteractionException`.
    *   **Tool Execution Module:** Each `Tool` implementation should handle its own specific errors and wrap them in `ToolExecutionException`. The `CodeExecutionTool`, for example, needs to report script errors, timeouts, and sandboxing issues.
This structured approach to error handling will improve the application's robustness and diagnosability.

## 8. Testing Strategy

*   **8.1. Overview:**
    *   A comprehensive testing strategy is essential to ensure the quality, correctness, and robustness of the OpenManus Java application. This involves multiple levels of testing, from individual units of code to the integration of various components. The goal is to detect bugs early, facilitate refactoring, and provide confidence in the application's behavior.

*   **8.2. Testing Levels & Scope:**
    *   **8.2.1. Unit Testing:**
        *   *Scope:* Individual classes and methods in isolation. Focus on testing the smallest pieces of logic.
        *   *Modules:* All modules (`openmanus-core`, `openmanus-api`, `openmanus-llm-integration`, `openmanus-tool-executor`, etc.).
        *   *Examples:* Testing a specific calculation in a service, validating input in a controller method, checking the logic of a utility class.
    *   **8.2.2. Integration Testing:**
        *   *Scope:* Testing the interaction between components or modules. This can range from testing service interactions with repositories to testing API endpoints and their underlying service calls, or even interactions with external systems (like databases or mocked LLMs/tools).
        *   *Examples:*
            *   Service-layer integration: Test if `TaskOrchestrationService` correctly interacts with `PlannerService` and `ToolExecutorService` (using mocks for downstream dependencies if needed, or real instances for deeper integration).
            *   Repository integration: Test Spring Data JPA repositories against an in-memory database (like H2) or a test-containerized real database.
            *   API endpoint testing: Test `TaskController` endpoints using `MockMvc` or `WebTestClient` to ensure correct request handling, response generation, and interaction with service layers.
            *   Tool integration: Testing the `ToolExecutorModule`'s ability to correctly invoke specific tools (e.g., `BrowserAutomationTool` against a test webpage, `CodeExecutionTool` with a simple script in a Docker container).
    *   **8.2.3. Contract Testing (Optional, for Microservices):**
        *   *Scope:* If OpenManus were to evolve into a system with multiple independent microservices, contract testing (e.g., using Spring Cloud Contract) would ensure that services can communicate with each other correctly based on defined contracts. Not a primary focus for the initial monolithic replication.
    *   **8.2.4. End-to-End (E2E) Testing (Limited Scope for Backend Focus):**
        *   *Scope:* Testing the entire application flow from the API endpoint down to interactions with real external services (if feasible and stable for automated tests).
        *   *Considerations:* E2E tests can be complex, slow, and brittle. For this backend-focused project, API integration tests that mock external dependencies (like actual LLM provider APIs or complex web interactions) might provide a better balance of coverage and reliability. True E2E might involve a separate test suite.

*   **8.3. Recommended Testing Frameworks & Libraries:**
    *   **Unit & Integration Testing Core:**
        *   *Technology:* JUnit 5 (Jupiter)
        *   *Reason:* The de-facto standard Java testing framework, providing a modern API with annotations, assertions, and extension model. Spring Boot has excellent integration with JUnit 5 (`spring-boot-starter-test`).
    *   **Mocking:**
        *   *Technology:* Mockito
        *   *Reason:* A popular and powerful mocking framework for creating test doubles (mocks, spies) to isolate units of code and simulate dependencies. Integrates well with JUnit 5 and Spring.
    *   **Assertions:**
        *   *Technology:* AssertJ
        *   *Reason:* Provides fluent, readable assertions that are often more expressive than JUnit's built-in assertions. `spring-boot-starter-test` includes it.
    *   **Spring Boot Testing Utilities:**
        *   *Technology:* `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `MockMvc`, `WebTestClient`, `TestRestTemplate`.
        *   *Reason:* Spring Boot provides specialized annotations and utilities to simplify testing different slices of the application (e.g., web layer, data layer, full application context).
    *   **Testcontainers (for integration with external services):**
        *   *Technology:* Testcontainers (`org.testcontainers:postgresql`, `org.testcontainers:docker-java-api`, etc.)
        *   *Reason:* Allows running real services (like PostgreSQL, Docker for code execution tests) in Docker containers managed by the test framework. Provides a high-fidelity testing environment for integrations.
        *   *Example:* Using Testcontainers to spin up a PostgreSQL instance for repository tests, or a Docker daemon for testing the `CodeExecutionTool`.

*   **8.4. Testing Strategy by Module:**
    *   **`openmanus-api` (e.g., `TaskController`):**
        *   Use `@WebMvcTest` to test controllers in isolation, mocking service layer dependencies.
        *   Verify request mapping, input validation (`@Valid`), DTO serialization/deserialization, and response status codes using `MockMvc`.
    *   **`openmanus-core` (e.g., `TaskOrchestrationService`, `PlannerService`):**
        *   Unit tests with Mockito to mock collaborators (other services, repositories).
        *   Integration tests using `@SpringBootTest` (potentially with a sliced context) to test interactions between core services. If using JPA, `@DataJpaTest` (for repositories) or `@SpringBootTest` with an H2/Testcontainerized DB.
    *   **`openmanus-llm-integration` (e.g., `LlmServiceImpl`, `OpenAiClient`):**
        *   Unit tests for logic within the module (e.g., request/response mapping).
        *   Integration tests against mocked LLM provider endpoints (e.g., using WireMock or MockWebServer) to simulate API calls without actual external calls. This is crucial to avoid costs and flakiness.
    *   **`openmanus-tool-executor` (e.g., `ToolRegistry`, `BrowserTool`, `CodeExecutionTool`):**
        *   Unit tests for individual tools, mocking external dependencies (e.g., mock Playwright `Browser` object for `BrowserTool` unit tests).
        *   Integration tests:
            *   `BrowserTool`: Test against actual local HTML files or simple, stable websites.
            *   `CodeExecutionTool`: Use Testcontainers to run a Docker daemon and execute simple, harmless Python scripts to verify the entire execution flow (container creation, script running, log retrieval, cleanup).
    *   **Persistence Layer (Repositories):**
        *   Use `@DataJpaTest` with an embedded H2 database or Testcontainers (PostgreSQL) to verify query methods and custom repository logic.

*   **8.5. Best Practices:**
    *   **Test Naming:** Clear and descriptive test method names (e.g., `givenX_whenY_thenZ`, `shouldReturnZ_whenXAndY`).
    *   **Arrange-Act-Assert (AAA):** Structure tests clearly.
    *   **Independence:** Tests should be independent and runnable in any order. Avoid shared mutable state between tests.
    *   **Speed:** Unit tests should be fast. Integration tests can be slower but should still be optimized.
    *   **Coverage:** Aim for good test coverage, especially for critical business logic. Use code coverage tools (e.g., JaCoCo) to measure.
    *   **CI/CD Integration:** Automate test execution in the CI/CD pipeline to catch regressions early.
    *   **Mock External Services:** For most integration tests, mock external dependencies (LLMs, third-party APIs) to ensure test stability, speed, and avoid costs. Use Testcontainers for infrastructure dependencies like databases or Docker itself.
    *   **Test Data Management:** Use well-defined and consistent test data. For JPA tests, use `src/test/resources/import.sql` or programmatic setup.

A robust testing strategy is an ongoing effort, evolving with the application. Starting with these principles will lay a strong foundation.

## 7. Logging Norms

*   **7.1. Overview:**
    *   Comprehensive logging is essential for monitoring application behavior, diagnosing issues, and auditing activities. This section defines the standards and best practices for logging within the OpenManus Java application.

*   **7.2. Recommended Logging Framework:**
    *   **Technology:** SLF4J (Simple Logging Facade for Java) with Logback as the underlying implementation.
    *   **Reason:**
        *   SLF4J provides an abstraction layer, allowing the underlying logging framework to be changed if needed.
        *   Logback is the native successor to Log4j 1.x, offering significant improvements, rich configuration options, and good performance. It's the default choice with Spring Boot starters (`spring-boot-starter-logging` includes it).

*   **7.3. Log Levels:**
    *   Adhere to standard SLF4J/Logback log levels:
        *   **TRACE:** Highly detailed information, typically only useful for deep debugging of specific components. Should be disabled in production by default.
        *   **DEBUG:** Fine-grained information useful for debugging application behavior. Can be enabled in development or temporarily in production for specific troubleshooting.
        *   **INFO:** Informational messages highlighting the progress of the application at a coarse-grained level (e.g., service startup, task lifecycle events, significant operations completed). This should be the default active level in production.
        *   **WARN:** Potentially harmful situations or unexpected events that are not critical errors but should be monitored (e.g., API deprecation warnings, recoverable errors, unusual system state).
        *   **ERROR:** Error events that might still allow the application to continue running but indicate a significant problem (e.g., failed execution of a task step, inability to connect to a non-critical external service after retries). All caught exceptions that are not re-thrown should generally be logged at ERROR or WARN.

*   **7.4. Log Format:**
    *   A structured and consistent log format is crucial for readability and parsing by log analysis tools.
    *   **Recommended Log Pattern (for `logback-spring.xml`):**
        ```xml
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %X{taskId} %X{userId} - %msg%n%throwable</pattern>
        ```
    *   **Explanation of Pattern Elements:**
        *   `%d{yyyy-MM-dd HH:mm:ss.SSS}`: Timestamp.
        *   `[%thread]`: Name of the thread that generated the log event.
        *   `%-5level`: Log level (INFO, DEBUG, etc.), left-justified in 5 characters.
        *   `%logger{36}`: Name of the logger (usually the class name), abbreviated to 36 characters.
        *   `%X{taskId}`: MDC (Mapped Diagnostic Context) field for tracking Task ID.
        *   `%X{userId}`: MDC field for tracking User ID (if applicable).
        *   `%msg`: The log message.
        *   `%n`: Newline character.
        *   `%throwable`: Stack trace for exceptions (if an exception is logged).
    *   **Structured Logging (Optional but Recommended for Production):**
        *   Consider logging in JSON format (e.g., using `LogstashLogbackEncoder`) for easier ingestion and analysis by log management systems like ELK Stack (Elasticsearch, Logstash, Kibana) or Splunk.
        ```xml
        <!-- Example for JSON output in logback-spring.xml -->
        <appender name="STDOUT_JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
        </appender>
        ```

*   **7.5. Mapped Diagnostic Context (MDC):**
    *   Use MDC to include contextual information in log messages without passing it explicitly through every method call.
    *   **Key Contextual Data:**
        *   `taskId`: The ID of the current task being processed.
        *   `userId`: The ID of the user who initiated the request (if applicable).
        *   `correlationId`: A unique ID for tracking a single request across multiple services (if microservices are used).
    *   **Implementation:**
        *   Set MDC values at the beginning of a request or task processing (e.g., in a Spring MVC interceptor, a servlet filter, or at the start of `TaskOrchestrationService.processNewTask`).
        *   Clear MDC values after the request or task is processed to prevent data leakage to other threads.
        ```java
        // Example of setting MDC
        MDC.put("taskId", task.getId());
        try {
            // ... business logic ...
            logger.info("Processing step for task."); // taskId will be automatically included
        } finally {
            MDC.remove("taskId");
        }
        ```

*   **7.6. What to Log:**
    *   **INFO:**
        *   Application startup and shutdown.
        *   Key lifecycle events of major components (e.g., Task received, Task planning started, Task completed, Task failed).
        *   Entry and exit of significant service methods (optional, can be DEBUG).
        *   Configuration values at startup (be careful not to log secrets).
        *   External service calls (URL, success/failure, duration - can be DEBUG).
    *   **DEBUG:**
        *   Detailed parameters for method calls.
        *   Intermediate states or values within complex algorithms.
        *   Steps within a tool execution.
        *   Payloads of external requests/responses (be careful with sensitive data).
    *   **WARN:**
        *   Recoverable errors (e.g., retries for external calls).
        *   Use of deprecated APIs or configurations.
        *   Unusual but non-critical conditions (e.g., high resource usage if monitored).
    *   **ERROR:**
        *   All caught exceptions that are not handled locally or re-thrown (include stack traces).
        *   Failures in critical operations (e.g., inability to connect to the database, LLM API returning consistent errors).
        *   Task processing failures with reasons.

*   **7.7. What NOT to Log (or Log with Extreme Caution):**
    *   **Sensitive Data:** Passwords, API keys, personal identifiable information (PII), financial data, raw LLM prompts if they contain sensitive user input.
        *   If necessary to log parts of such data for debugging, ensure it's masked or tokenized.
    *   **Verbose Data in Production INFO Logs:** Avoid logging large objects or frequent, high-volume messages at INFO level in production, as it can impact performance and log storage costs. Use DEBUG or TRACE for these.

*   **7.8. Configuration (`logback-spring.xml`):**
    *   Place `logback-spring.xml` in `src/main/resources`. Spring Boot will automatically pick it up.
    *   Define appenders (e.g., console, file, rolling file, JSON appender).
    *   Set root log level and specific log levels for application packages and libraries.
    *   **Example `logback-spring.xml`:**
        ```xml
        <configuration>
            <springProperty scope="context" name="LOG_LEVEL" source="logging.level.root" defaultValue="INFO"/>
            <springProperty scope="context" name="APP_LOG_LEVEL" source="logging.level.io.github.yourusername.openmanusjava" defaultValue="INFO"/>

            <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
                <encoder>
                    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %X{taskId} %X{userId} - %msg%n%throwable</pattern>
                </encoder>
            </appender>

            <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
                <file>logs/openmanus-java.log</file>
                <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                    <fileNamePattern>logs/openmanus-java.%d{yyyy-MM-dd}.log</fileNamePattern>
                    <maxHistory>30</maxHistory> <!-- Keep 30 days of history -->
                </rollingPolicy>
                <encoder>
                    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %X{taskId} %X{userId} - %msg%n%throwable</pattern>
                </encoder>
            </appender>

            <logger name="io.github.yourusername.openmanusjava" level="${APP_LOG_LEVEL}" additivity="false">
                <appender-ref ref="STDOUT"/>
                <appender-ref ref="FILE"/>
            </logger>

            <root level="${LOG_LEVEL}">
                <appender-ref ref="STDOUT"/>
                <appender-ref ref="FILE"/>
            </root>
        </configuration>
        ```
    *   This allows controlling log levels via `application.properties` (e.g., `logging.level.io.github.yourusername.openmanusjava=DEBUG`).

By following these logging norms, the application will be easier to monitor, troubleshoot, and maintain.

## 9. Deployment Guide

*   **9.1. Overview:**
    *   This section provides guidance on packaging the OpenManus Java application and deploying it to various environments, from local development to production servers.

*   **9.2. Building the Application:**
    *   **Build Tool:** Apache Maven (as per earlier recommendation).
    *   **Command:**
        ```bash
        mvn clean package
        # or to skip tests during build (not recommended for release builds)
        # mvn clean package -DskipTests
        ```
    *   **Output:**
        *   The primary output will be an executable JAR file (fat JAR) located in the `openmanus-app/target/` directory (e.g., `openmanus-app-0.0.1-SNAPSHOT.jar`). This JAR contains all necessary dependencies and an embedded web server (Tomcat by default).
        *   Other modules will produce standard JARs in their respective `target/` directories.

*   **9.3. Running Locally (Development/Testing):**
    *   **From IDE:** Most IDEs (IntelliJ IDEA, Eclipse) allow running Spring Boot applications directly by right-clicking the main application class (`OpenManusApplication.java`) and selecting "Run".
    *   **From Command Line:**
        ```bash
        java -jar openmanus-app/target/openmanus-app-0.0.1-SNAPSHOT.jar
        ```
        *   You can override configuration properties via command-line arguments:
            ```bash
            java -jar openmanus-app/target/openmanus-app-0.0.1-SNAPSHOT.jar --server.port=8081 --openmanus.llm.apiKey=your_env_key_here
            ```
        *   Or using Spring Boot's JSON application properties:
            ```bash
            java -Dspring.application.json='{"openmanus.llm.apiKey":"your_key"}' -jar openmanus-app/target/openmanus-app-0.0.1-SNAPSHOT.jar
            ```

*   **9.4. Deployment Environments & Strategies:**

    *   **9.4.1. Traditional Server Deployment (Linux Server):**
        *   **Prerequisites:**
            *   Java 17 (JDK or JRE) installed on the server.
            *   Database (e.g., PostgreSQL) accessible from the server.
            *   (If using CodeExecutionTool with Docker) Docker installed and configured on the server.
        *   **Steps:**
            1.  Transfer the executable JAR to the server (e.g., using `scp`).
            2.  Run the application, preferably as a system service (e.g., using `systemd` or `init.d`) for process management and auto-restart.
                *   **Example `systemd` service file (`openmanus.service`):**
                    ```ini
                    [Unit]
                    Description=OpenManus Java Application
                    After=network.target docker.service postgresql.service
                    Requires=docker.service postgresql.service # If Docker/Postgres are local

                    [Service]
                    User=openmanus_user # Run as a non-root user
                    WorkingDirectory=/opt/openmanus
                    ExecStart=/usr/bin/java -jar openmanus-app.jar \
                                 --spring.config.location=file:/opt/openmanus/config/application-prod.properties \
                                 --openmanus.llm.apiKey=${OPENMANUS_LLM_API_KEY} # Load sensitive data from env
                    SuccessExitStatus=143 # For graceful shutdown
                    Restart=on-failure
                    RestartSec=10

                    [Install]
                    WantedBy=multi-user.target
                    ```
                *   Place this file in `/etc/systemd/system/`.
                *   Run `sudo systemctl daemon-reload`, `sudo systemctl enable openmanus`, `sudo systemctl start openmanus`.
            3.  Configure environment-specific properties (e.g., database URLs, API keys) using `application-prod.properties` and environment variables. Sensitive data should *always* be managed via environment variables or a secrets management system, not hardcoded in JARs or property files committed to VCS.
            4.  Configure a reverse proxy (e.g., Nginx, Apache HTTPD) in front of the Spring Boot application for SSL termination, load balancing (if multiple instances), and serving static content if needed.

    *   **9.4.2. Docker Container Deployment (Recommended for Portability & Scalability):**
        *   **`Dockerfile` (in `openmanus-app` module or project root for multi-module build):**
            ```dockerfile
            # Stage 1: Build the application using Maven
            FROM eclipse-temurin:17-jdk-jammy AS builder
            WORKDIR /build
            # Copy parent pom.xml and .mvn directory first for dependency resolution
            COPY .mvn/ .mvn
            COPY mvnw pom.xml ./
            # Copy module-specific pom.xml files
            COPY openmanus-commons/pom.xml ./openmanus-commons/
            COPY openmanus-config/pom.xml ./openmanus-config/
            COPY openmanus-llm-integration/pom.xml ./openmanus-llm-integration/
            COPY openmanus-tool-executor/pom.xml ./openmanus-tool-executor/
            COPY openmanus-core/pom.xml ./openmanus-core/
            COPY openmanus-api/pom.xml ./openmanus-api/
            COPY openmanus-app/pom.xml ./openmanus-app/
            # Download dependencies
            RUN ./mvnw dependency:go-offline -B
            # Copy source code for all modules
            COPY openmanus-commons/src ./openmanus-commons/src
            COPY openmanus-config/src ./openmanus-config/src
            COPY openmanus-llm-integration/src ./openmanus-llm-integration/src
            COPY openmanus-tool-executor/src ./openmanus-tool-executor/src
            COPY openmanus-core/src ./openmanus-core/src
            COPY openmanus-api/src ./openmanus-api/src
            COPY openmanus-app/src ./openmanus-app/src
            # Build the application (specifically the app module)
            RUN ./mvnw clean package -pl openmanus-app -am -DskipTests

            # Stage 2: Create the final lightweight image
            FROM eclipse-temurin:17-jre-jammy
            WORKDIR /app
            # Copy the executable JAR from the builder stage
            COPY --from=builder /build/openmanus-app/target/openmanus-app-*.jar app.jar

            EXPOSE 8080

            # Environment variables for configuration (examples to be set at runtime)
            # ENV SPRING_PROFILES_ACTIVE=prod
            # ENV OPENMANUS_LLM_API_KEY=""
            # ENV SPRING_DATASOURCE_URL=""
            # ENV SPRING_DATASOURCE_USERNAME=""
            # ENV SPRING_DATASOURCE_PASSWORD=""

            ENTRYPOINT ["java", "-jar", "/app/app.jar"]
            ```
            *   **Note:** This Dockerfile assumes it's placed in the root directory of the multi-module Maven project (`openmanus-java`). It uses a multi-stage build to keep the final image small.
        *   **Building the Docker Image:**
            ```bash
            docker build -t yourusername/openmanus-java:latest .
            ```
        *   **Running the Docker Container:**
            ```bash
            docker run -p 8080:8080 \
                 -e SPRING_PROFILES_ACTIVE=prod \
                 -e OPENMANUS_LLM_API_KEY="your_actual_api_key" \
                 -e SPRING_DATASOURCE_URL="jdbc:postgresql://your_db_host:5432/openmanus_db" \
                 -e SPRING_DATASOURCE_USERNAME="your_db_user" \
                 -e SPRING_DATASOURCE_PASSWORD="your_db_password" \
                 yourusername/openmanus-java:latest
            ```
        *   **Docker Compose (for local multi-container setups, e.g., app + database):**
            ```yaml
            # docker-compose.yml
            version: '3.8'
            services:
              openmanus-app:
                image: yourusername/openmanus-java:latest # Or build context: build: .
                ports:
                  - "8080:8080"
                environment:
                  - SPRING_PROFILES_ACTIVE=prod # Or 'docker'
                  - OPENMANUS_LLM_API_KEY=${OPENMANUS_LLM_API_KEY} # Reads from .env file or shell
                  - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/openmanus_db
                  - SPRING_DATASOURCE_USERNAME=user
                  - SPRING_DATASOURCE_PASSWORD=secret
                  # For CodeExecutionTool with Docker-in-Docker (if needed and security implications understood)
                  # - DOCKER_HOST=tcp://docker-in-docker:2375
                depends_on:
                  db:
                    condition: service_healthy # Wait for DB to be healthy
                # volumes: # If CodeExecutionTool needs Docker socket
                  # - /var/run/docker.sock:/var/run/docker.sock # Use with extreme caution
              db:
                image: postgres:14
                ports:
                  - "54320:5432" # Expose Postgres on host port 54320
                environment:
                  - POSTGRES_DB=openmanus_db
                  - POSTGRES_USER=user
                  - POSTGRES_PASSWORD=secret
                volumes:
                  - postgres_data:/var/lib/postgresql/data
                healthcheck:
                  test: ["CMD-SHELL", "pg_isready -U user -d openmanus_db"]
                  interval: 10s
                  timeout: 5s
                  retries: 5
            volumes:
              postgres_data:
            ```
            *   **Note on Docker for CodeExecutionTool:** If the `CodeExecutionTool` uses Docker itself, running it inside a Docker container (the application) requires a Docker-in-Docker setup or mounting the host's Docker socket. Mounting the Docker socket has significant security implications and should be handled with extreme care.

    *   **9.4.3. Kubernetes (for Production-Grade Orchestration):**
        *   *Scope:* For scalable, resilient production deployments.
        *   *Approach:* Package the application as a Docker container (see above). Create Kubernetes Deployments, Services, ConfigMaps (for non-sensitive config), and Secrets (for sensitive data). Use Horizontal Pod Autoscalers for scaling.
        *   *Considerations:* Requires a Kubernetes cluster. Configuration can be complex but offers significant operational benefits. This is an advanced deployment option. Key Kubernetes objects:
            *   `Deployment`: Manages stateless application instances (pods).
            *   `Service`: Provides a stable network endpoint (IP address, DNS name) for accessing the application pods.
            *   `ConfigMap`: For non-sensitive configuration data.
            *   `Secret`: For sensitive data like API keys and database passwords.
            *   `Ingress`: (Optional) Manages external access to services, often providing SSL termination and path-based routing.
            *   `PersistentVolumeClaim`: If the application or its tools require persistent storage beyond container lifecycles.

*   **9.5. Production Configuration & Best Practices:**
    *   **Externalize Configuration:** Never hardcode environment-specific (DB URLs, API keys) or sensitive configurations in the JAR. Use Spring Boot's externalized configuration mechanisms (profile-specific properties, environment variables, config servers).
    *   **Secrets Management:** Use environment variables passed to Docker/systemd, or integrate with secrets management tools like HashiCorp Vault, AWS Secrets Manager, Azure Key Vault.
    *   **Logging:** Configure appropriate log levels for production (INFO typically). Ensure logs are written to a persistent location or aggregated by a log management system (e.g., ELK, Splunk).
    *   **Monitoring & Health Checks:** Use Spring Boot Actuator (`spring-boot-starter-actuator`) to expose health (`/actuator/health`), metrics (`/actuator/metrics`), and other operational endpoints. Integrate with monitoring systems (e.g., Prometheus, Grafana).
    *   **Database Migrations:** Use a database migration tool like Flyway or Liquibase to manage schema changes in a controlled, versioned manner. This should be part of the CI/CD pipeline.
    *   **Resource Allocation:** Ensure sufficient CPU and memory resources for the application, especially if it involves intensive tasks like LLM processing or code execution. Define requests and limits in containerized environments.
    *   **Security:** Apply standard security best practices (HTTPS, secure API keys, input validation, rate limiting, principle of least privilege for service accounts/Docker users).

This guide provides a starting point for deploying the OpenManus Java application. The choice of deployment strategy will depend on the specific environment and operational requirements.

## 10. Extensibility & Maintainability Considerations

*   **10.1. Overview:**
    *   Building a system that is easy to extend with new features and maintain over time is crucial. This section outlines design principles and practices to promote these qualities in the OpenManus Java application.

*   **10.2. Extensibility:**

    *   **10.2.1. Pluggable Tools:**
        *   The `Tool` interface and `ToolRegistry` (Section 5.3) are designed for easy addition of new tools.
        *   *To add a new tool:*
            1.  Create a new class implementing the `Tool` interface.
            2.  Implement `getName()`, `execute()`, and `getParameterDefinitions()`.
            3.  Annotate the class with `@Component` (or define as a bean). Spring will automatically pick it up and register it via constructor injection in `ToolRegistry`.
            4.  The LLM/Planner can then be updated or prompted to utilize this new tool by its name.
    *   **10.2.2. LLM Abstraction:**
        *   The `LlmService` and `LlmClient` interfaces (Section 5.2) allow for adding support for new LLM providers.
        *   *To add a new LLM provider:*
            1.  Create a new class implementing `LlmClient` (e.g., `AnthropicClient`).
            2.  Implement the logic to interact with the new provider's API.
            3.  Update `LlmServiceImpl`'s constructor or a factory method to select the appropriate client based on configuration (`openmanus.llm.provider`).
    *   **10.2.3. Agent Capabilities/Personas (Future Extension):**
        *   The system could be extended to support different "agent types" or "personas" with varying sets of tools, default LLMs, or planning strategies.
        *   This might involve new configuration options or specialized `TaskOrchestrationService` implementations.
    *   **10.2.4. Event-Driven Architecture (for Decoupling):**
        *   Using Spring Application Events (or a message queue for larger systems) for communication between components can improve decoupling.
        *   *Example:* When a task completes, `TaskOrchestrationService` could publish a `TaskCompletedEvent`. Other services (e.g., a notification service, a results archiving service) could listen for this event without direct coupling to the orchestration logic.
            ```java
            // In TaskOrchestrationServiceImpl
            // eventPublisher.publishEvent(new TaskCompletedEvent(this, task.getId()));

            // Separate listener component
            // @Component
            // public class TaskEventListener {
            //     @EventListener
            //     public void handleTaskCompleted(TaskCompletedEvent event) {
            //         // ... react to task completion ...
            //     }
            // }
            ```
    *   **10.2.5. Configurable Workflows/Strategies:**
        *   For advanced scenarios, consider making parts of the task planning or execution strategy configurable (e.g., different planning prompts for different task types, different retry strategies).

*   **10.3. Maintainability:**

    *   **10.3.1. Clean Code Practices:**
        *   **Readability:** Write clear, concise, and self-documenting code. Use meaningful names for variables, methods, and classes.
        *   **Simplicity (KISS - Keep It Simple, Stupid):** Avoid unnecessary complexity.
        *   **DRY (Don't Repeat Yourself):** Factor out common code into reusable methods or utility classes.
        *   **SOLID Principles:**
            *   **S**ingle Responsibility Principle: Each class/method should have one primary responsibility.
            *   **O**pen/Closed Principle: Software entities should be open for extension but closed for modification. (Interfaces and abstract classes help here).
            *   **L**iskov Substitution Principle: Subtypes must be substitutable for their base types.
            *   **I**nterface Segregation Principle: Clients should not be forced to depend on interfaces they do not use.
            *   **D**ependency Inversion Principle: Depend on abstractions, not concretions. (Achieved via interfaces and dependency injection).
    *   **10.3.2. Modular Design:**
        *   The recommended multi-module Maven structure (Section 4.2) promotes separation of concerns and reduces coupling between different parts of the application. This makes it easier to understand, test, and modify individual modules without impacting others.
    *   **10.3.3. Consistent Coding Style:**
        *   Adopt and enforce a consistent code style (e.g., using Google Java Style Guide, Spring Java Format).
        *   Use IDE code formatting tools and consider static analysis tools (e.g., Checkstyle, PMD, SonarLint) to enforce style and detect potential issues.
    *   **10.3.4. Comprehensive Documentation:**
        *   **Code Comments:** Add Javadoc comments for public APIs (interfaces, methods) and explanatory comments for complex or non-obvious logic.
        *   **This Document:** Keep this overall design document updated as the system evolves.
        *   **API Documentation:** Maintain accurate API documentation (e.g., using OpenAPI/Swagger, Section 5.1.5).
    *   **10.3.5. Effective Logging and Error Handling:**
        *   As detailed in Sections 6 and 7, good logging and error handling are critical for diagnosing and fixing issues quickly.
    *   **10.3.6. Robust Testing Strategy:**
        *   A comprehensive suite of unit and integration tests (Section 8) is vital for catching regressions and enabling confident refactoring.
    *   **10.3.7. Dependency Management:**
        *   Keep dependencies up-to-date to leverage new features and security patches.
        *   Use Maven's dependency management features effectively (e.g., `dependencyManagement` in parent POM). Regularly review and remove unused dependencies.
    *   **10.3.8. Version Control (Git):**
        *   Use Git effectively: meaningful commit messages, feature branches, pull requests for code reviews.
    *   **10.3.9. Configuration Management:**
        *   Keep configuration externalized and well-organized (Section 5.4 and 9.5).

*   **10.4. Performance Optimization (Preliminary Suggestions):**
    *   **Asynchronous Operations:** Utilize `@Async` or message queues for long-running operations (LLM calls, tool executions) to keep API endpoints responsive (as discussed in Section 5.1.3).
    *   **Efficient Database Interaction:**
        *   Optimize JPA queries (e.g., avoid N+1 problems, use projections if fetching partial data).
        *   Use connection pooling (Spring Boot configures this by default).
        *   Consider caching for frequently accessed, rarely changing data (e.g., using Spring Cache with Redis or Caffeine).
    *   **Resource Management for Tools:**
        *   Ensure resources used by tools (e.g., Playwright browsers, Docker containers for code execution) are properly released after use.
        *   Optimize Docker images for size and startup time for the `CodeExecutionTool`.
    *   **LLM Interaction Optimization:**
        *   Use smaller/faster LLM models if appropriate for certain tasks.
        *   Optimize prompts for conciseness and clarity to reduce token usage and processing time.
        *   Batch requests to LLMs if multiple similar, independent requests need to be made.
    *   **Profiling:** If performance issues arise, use a profiler (e.g., VisualVM, JProfiler) to identify bottlenecks before attempting optimizations.

By adhering to these considerations, the OpenManus Java application can be developed into a flexible, robust, and maintainable system that can evolve to meet future requirements.
