package org.example;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AddOneplus3sToCartSteps {
    private WebDriver driver;
    private WebDriverWait wait;
    private String selectedMobileTitle;
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String API_Url = "https://api.openai.com/v1/chat/completions";
    private SelfHealingElementFinder selfHealingFinder;
    private OpenAIClient openAIClient;

    @Given("I am on the Amazon India homepage")
    public void i_am_on_amazon_india_homepage() throws InterruptedException {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        openAIClient = new OpenAIClient(API_KEY, API_Url);
        selfHealingFinder = new SelfHealingElementFinder(driver, openAIClient);
        driver.get("https://www.amazon.in");
        Thread.sleep(2000); // Wait for the page to load

    }

    @When("I search for {string} mobile")
    public void i_search_for_mobile(String mobile) {
        WebElement searchBox = selfHealingFinder.findElement(By.id("twotabsearchtextbox"), "Amazon search box");
        searchBox.sendKeys(mobile);
        selfHealingFinder.findElement(By.id("nav-search-submit-button"), "Amazon search button").click();
    }

    @When("I select the black color mobile if available")
    public void i_select_black_color_mobile_if_available() {
        try {
            WebElement blackMobile = selfHealingFinder.findElement(
                By.xpath("//span[contains(text(),'Black1') or contains(text(),'black')]/ancestor::div[contains(@data-component-type,'s-search-result')][1]//a"),
                "Black color mobile link");
            selectedMobileTitle = blackMobile.getText();
            blackMobile.click();
        } catch (NoSuchElementException | TimeoutException e) {
            // Black color not found, will select first mobile in next step
        }
    }

    @When("if black color is not available I select the first mobile from the list")
    public void if_black_not_available_select_first_mobile() {
        if (selectedMobileTitle == null) {
            WebElement firstMobile = selfHealingFinder.findElement(
                By.cssSelector("div[data-component-type='s-search-result'] h2 a"),
                "First mobile link");
            selectedMobileTitle = firstMobile.getText();
            firstMobile.click();
        }
    }

    @When("I add the selected mobile to the cart")
    public void i_add_selected_mobile_to_cart() {
        for (String winHandle : driver.getWindowHandles()) {
            driver.switchTo().window(winHandle);
        }
        By[] addToCartLocators = new By[] {
            By.id("add-to-cart-button"),
            By.id("submit.add-to-cart"),
            By.cssSelector("input[title='Add to Cart']"),
            By.xpath("//input[@value='Add to Cart' or @aria-labelledby='submit.add-to-cart-announce']"),
            By.xpath("//span[contains(text(),'Add to Cart') or contains(text(),'Add to Basket')]/ancestor::input[1]"),
            By.xpath("//div[@id='a-accordion-auto-6']//input[@id='add-to-cart-button']")
        };
        WebElement addToCartBtn = null;
        for (By locator : addToCartLocators) {
            try {
                addToCartBtn = selfHealingFinder.findElement(locator, "Add to Cart button");
                if (addToCartBtn != null && addToCartBtn.isDisplayed()) {
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (addToCartBtn == null) {
            throw new NoSuchElementException("Add to Cart button not found with known locators");
        }
        addToCartBtn.click();
    }

    @Then("the mobile should be added to the cart page")
    public void mobile_should_be_added_to_cart_page() {
        selfHealingFinder.findElement(By.id("add-to-cart-button"), "Add to Cart button");
        driver.get("https://www.amazon.in/gp/cart/view.html");
        boolean found = driver.getPageSource().contains(selectedMobileTitle);
        driver.quit();
        assert found : "Selected mobile not found in cart!";
    }

    @And("I verify the mobile is present in the smart cart")
    public void i_verify_the_mobile_is_present_in_the_smart_cart() {
        // Navigate to the smart cart page (reference link)
        driver.get("https://www.amazon.in/cart/smart-wagon");
        // Wait for the cart page to load and check for the selected mobile title
        boolean found = driver.getPageSource().toLowerCase().contains(selectedMobileTitle.toLowerCase());
        driver.quit();
        assert found : "Selected mobile not found in smart cart!";
    }
}
