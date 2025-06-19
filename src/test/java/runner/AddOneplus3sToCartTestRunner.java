package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features/add_oneplus3s_to_cart.feature",
    glue = "org.example",
    plugin = {"pretty", "html:target/cucumber-reports"},
    monochrome = true
)
public class AddOneplus3sToCartTestRunner extends AbstractTestNGCucumberTests {
    // This class will be used to run the Cucumber tests with TestNG
}

