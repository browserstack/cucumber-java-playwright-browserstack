package com.browserstack.stepdefs.local;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.json.JSONObject;
import org.testng.Assert;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class StackLocalSteps {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @Before
    public void setUp(Scenario scenario) {
        playwright = Playwright.create();
        BrowserType browserType = playwright.chromium();

        Map<String, Object> config = loadBrowserStackYaml();
        String userName = envOrYaml("BROWSERSTACK_USERNAME", config, "userName");
        String accessKey = envOrYaml("BROWSERSTACK_ACCESS_KEY", config, "accessKey");

        HashMap<String, String> caps = new HashMap<>();
        caps.put("browserstack.user", userName);
        caps.put("browserstack.key", accessKey);
        caps.put("browserstack.source", "cucumber-java-playwright:sample-master:v1.0");
        caps.put("browser", "chrome");
        caps.put("browserstack.local", "true");
        caps.put("sessionName", scenario.getName());

        String encoded = URLEncoder.encode(new JSONObject(caps).toString(), StandardCharsets.UTF_8);
        String wsEndpoint = "wss://cdp.browserstack.com/playwright?caps=" + encoded;

        browser = browserType.connect(wsEndpoint);
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
        page = context.newPage();
    }

    @Given("I am on {string}")
    public void i_am_on(String url) {
        page.navigate(url);
    }

    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String expected) {
        Assert.assertTrue(page.title().contains(expected),
                "expected title to contain '" + expected + "' but was '" + page.title() + "'");
    }

    @After
    public void tearDown() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    private Map<String, Object> loadBrowserStackYaml() {
        File file = new File(System.getProperty("user.dir") + "/browserstack.yml");
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return new Yaml().load(in);
        } catch (Exception e) {
            throw new RuntimeException("Could not read browserstack.yml: " + e.getMessage(), e);
        }
    }

    private String envOrYaml(String envKey, Map<String, Object> yaml, String yamlKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isEmpty()) return fromEnv;
        Object fromYaml = yaml.get(yamlKey);
        return fromYaml == null ? null : fromYaml.toString();
    }
}
