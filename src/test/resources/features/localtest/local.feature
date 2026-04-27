@local
Feature: BrowserStack Local - Reach localhost via tunnel

  Scenario: Reach a page served on localhost through BrowserStack Local
    Given I am on "http://bs-local.com:45454/"
    Then the page title should contain "BrowserStack Local"
