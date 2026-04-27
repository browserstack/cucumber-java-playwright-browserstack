package com.browserstack;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        glue = "com.browserstack.stepdefs.local",
        features = "src/test/resources/features/localtest",
        plugin = {
                "pretty",
                "html:reports/cucumber/cucumber-pretty.html",
                "json:reports/cucumber/cucumber.json"
        }
)
public class RunCucumberLocalTest extends AbstractTestNGCucumberTests {}
