Feature: Add OnePlus 3S mobile to cart on Amazon India

  @Test
  Scenario: Search and add OnePlus 3S mobile to cart
    Given I am on the Amazon India homepage
    When I search for "oneplus 3s" mobile
    And I select the black color mobile if available
    And if black color is not available I select the first mobile from the list
    And I add the selected mobile to the cart
    And I verify the mobile is present in the smart cart
