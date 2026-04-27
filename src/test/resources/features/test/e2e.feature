@e2e
Feature: BStackDemo - Add product to cart

  Scenario: Add a product to the cart on bstackdemo.com
    Given I am on "https://www.bstackdemo.com"
    When I add the first product to the cart
    Then the product in the cart should match the product I added
