package com.browserstack;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        glue = "com.browserstack.stepdefs.e2e",
        features = "src/test/resources/features/test",
        plugin = {
                "pretty",
                "html:reports/cucumber/cucumber-pretty.html",
                "json:reports/cucumber/cucumber.json"
        }
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {}
