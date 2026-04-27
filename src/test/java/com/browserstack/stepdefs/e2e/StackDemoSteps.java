package com.browserstack.stepdefs.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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

public class StackDemoSteps {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String selectedProductName;

    private static final String FIRST_PRODUCT_NAME = "//*[@id=\"1\"]/p";
    private static final String FIRST_PRODUCT_ADD_TO_CART = "//*[@id=\"1\"]/div[4]";
    private static final String CART_PANE = ".float-cart__content";
    private static final String PRODUCT_IN_CART = "//*[@id=\"__next\"]/div/div/div[2]/div[2]/div[2]/div/div[3]/p[1]";

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

    @When("I add the first product to the cart")
    public void i_add_the_first_product_to_the_cart() {
        selectedProductName = page.locator(FIRST_PRODUCT_NAME).textContent();
        page.locator(FIRST_PRODUCT_ADD_TO_CART).click();
        page.locator(CART_PANE).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(30000));
    }

    @Then("the product in the cart should match the product I added")
    public void the_product_in_the_cart_should_match_the_product_i_added() {
        String inCart = page.locator(PRODUCT_IN_CART).textContent();
        Assert.assertEquals(inCart, selectedProductName);
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
