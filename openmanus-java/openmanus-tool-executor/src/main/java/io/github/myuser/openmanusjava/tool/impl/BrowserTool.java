package io.github.myuser.openmanusjava.tool.impl;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import io.github.myuser.openmanusjava.tool.config.ToolConfigProperties;
import io.github.myuser.openmanusjava.tool.spec.Tool;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionException;
import io.github.myuser.openmanusjava.tool.spec.ToolExecutionResult;
import io.github.myuser.openmanusjava.tool.spec.ToolParameterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;

@Component // This makes it a Spring bean, discoverable by ToolRegistry
public class BrowserTool implements Tool {

    private static final Logger logger = LoggerFactory.getLogger(BrowserTool.class);
    private final ToolConfigProperties.Browser browserConfig;

    private Playwright playwright;
    private Browser browser; // Instance of a browser (e.g., Chromium, Firefox)

    public static final String ACTION_NAVIGATE = "NAVIGATE";
    public static final String ACTION_CLICK = "CLICK";
    public static final String ACTION_TYPE = "TYPE";
    public static final String ACTION_EXTRACT_TEXT = "EXTRACT_TEXT";
    public static final String ACTION_EXTRACT_HTML = "EXTRACT_HTML";
    public static final String ACTION_GET_CURRENT_URL = "GET_CURRENT_URL";
    public static final String ACTION_SCREENSHOT = "SCREENSHOT";


    @Autowired
    public BrowserTool(ToolConfigProperties toolConfigProperties) {
        this.browserConfig = toolConfigProperties.getBrowser();
    }

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing Playwright BrowserTool...");
            playwright = Playwright.create();
            // For now, hardcoding Chromium. Could be made configurable.
            BrowserType browserType = playwright.chromium();
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(browserConfig.isDefaultHeadless());
            // You might need to specify browser path if not found automatically
            // launchOptions.setExecutablePath(Paths.get("/path/to/your/browser/executable"));
            browser = browserType.launch(launchOptions);
            logger.info("Playwright BrowserTool initialized successfully with browser: {}", browser.browserType().name());
        } catch (PlaywrightException e) {
            logger.error("Failed to initialize Playwright browser: {}", e.getMessage(), e);
            // Depending on policy, either throw a runtime exception to prevent app startup
            // or allow it to fail gracefully when execute is called.
            // For now, log and playwright will be null, execute will fail.
            if (playwright != null) { // Partial cleanup if playwright created but browser launch failed
                playwright.close();
                playwright = null;
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up Playwright BrowserTool...");
        if (browser != null && browser.isConnected()) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        logger.info("Playwright BrowserTool cleaned up.");
    }

    @Override
    public String getName() {
        return "browser.action"; // A single tool name, action specified in parameters
    }

    @Override
    public List<ToolParameterDefinition> getParameterDefinitions() {
        return Arrays.asList(
            new ToolParameterDefinition("action", "String", "Action to perform (e.g., NAVIGATE, CLICK, TYPE, EXTRACT_TEXT, EXTRACT_HTML, GET_CURRENT_URL, SCREENSHOT)", true),
            new ToolParameterDefinition("url", "String", "URL for NAVIGATE action", false),
            new ToolParameterDefinition("selector", "String", "CSS Selector or XPath for element interaction (CLICK, TYPE, EXTRACT_TEXT, EXTRACT_HTML)", false),
            new ToolParameterDefinition("text", "String", "Text to type for TYPE action", false),
            new ToolParameterDefinition("path", "String", "File path for SCREENSHOT action (e.g., /path/to/screenshot.png)", false),
            new ToolParameterDefinition("timeout", "Integer", "Timeout in milliseconds for the action (optional)", false)
        );
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> parameters) throws ToolExecutionException {
        if (playwright == null || browser == null || !browser.isConnected()) {
            logger.error("BrowserTool is not initialized or browser is not connected. Cannot execute action.");
            throw new ToolExecutionException("BrowserTool not initialized or browser disconnected.");
        }

        String action = (String) parameters.get("action");
        if (action == null || action.trim().isEmpty()) {
            throw new ToolExecutionException("Missing required parameter: 'action'");
        }

        // Use a new page for each execution for isolation, though context reuse can be more efficient
        Page page = browser.newPage();
        // Set default timeout from config, can be overridden by parameter
        page.setDefaultTimeout(browserConfig.getDefaultTimeoutSeconds() * 1000.0);
        if (parameters.containsKey("timeout")) {
            try {
                page.setDefaultTimeout(((Number)parameters.get("timeout")).doubleValue());
            } catch (ClassCastException e) {
                logger.warn("Invalid timeout parameter format, using default. Error: {}", e.getMessage());
            }
        }

        try {
            Object resultOutput = null;
            String logMessage = "Action completed.";

            switch (action.toUpperCase()) {
                case ACTION_NAVIGATE:
                    String url = (String) parameters.get("url");
                    if (url == null) throw new ToolExecutionException("Missing 'url' for NAVIGATE action.");
                    page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
                    resultOutput = "Navigated to: " + url;
                    logMessage = "Navigated to: " + url;
                    break;

                case ACTION_CLICK:
                    String clickSelector = (String) parameters.get("selector");
                    if (clickSelector == null) throw new ToolExecutionException("Missing 'selector' for CLICK action.");
                    page.click(clickSelector);
                    resultOutput = "Clicked element: " + clickSelector;
                    logMessage = "Clicked: " + clickSelector;
                    break;

                case ACTION_TYPE:
                    String typeSelector = (String) parameters.get("selector");
                    String textToType = (String) parameters.get("text");
                    if (typeSelector == null || textToType == null) throw new ToolExecutionException("Missing 'selector' or 'text' for TYPE action.");
                    page.fill(typeSelector, textToType); // 'fill' is often better than 'type'
                    resultOutput = "Typed '" + textToType + "' into element: " + typeSelector;
                    logMessage = "Typed into: " + typeSelector;
                    break;

                case ACTION_EXTRACT_TEXT:
                    String textSelector = (String) parameters.get("selector");
                    if (textSelector == null) throw new ToolExecutionException("Missing 'selector' for EXTRACT_TEXT action.");
                    resultOutput = page.textContent(textSelector);
                    logMessage = "Extracted text from: " + textSelector;
                    break;

                case ACTION_EXTRACT_HTML:
                    String htmlSelector = (String) parameters.get("selector");
                    if (htmlSelector == null) { // Extract whole page HTML if no selector
                        resultOutput = page.content();
                        logMessage = "Extracted HTML of the whole page.";
                    } else {
                        resultOutput = page.innerHTML(htmlSelector);
                        logMessage = "Extracted inner HTML from: " + htmlSelector;
                    }
                    break;

                case ACTION_GET_CURRENT_URL:
                    resultOutput = page.url();
                    logMessage = "Retrieved current URL.";
                    break;

                case ACTION_SCREENSHOT:
                    String path = (String) parameters.get("path");
                    if (path == null) throw new ToolExecutionException("Missing 'path' for SCREENSHOT action.");
                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(path)).setFullPage(true));
                    resultOutput = "Screenshot saved to: " + path;
                    logMessage = "Screenshot saved to: " + path;
                    break;

                default:
                    throw new ToolExecutionException("Unsupported browser action: " + action);
            }
            logger.info(logMessage);
            return new ToolExecutionResult("SUCCESS", resultOutput);

        } catch (PlaywrightException e) {
            logger.error("PlaywrightException during action '{}': {}", action, e.getMessage(), e);
            throw new ToolExecutionException("Browser action '" + action + "' failed: " + e.getMessage(), e);
        } catch (ToolExecutionException e) { // Re-throw specific exceptions
            throw e;
        } catch (Exception e) { // Catch any other unexpected error
            logger.error("Unexpected error during browser action '{}': {}", action, e.getMessage(), e);
            throw new ToolExecutionException("Unexpected error in browser action '" + action + "': " + e.getMessage(), e);
        } finally {
            if (page != null && !page.isClosed()) {
                page.close();
            }
        }
    }
}
