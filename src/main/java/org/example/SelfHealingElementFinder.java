package org.example;

import org.openqa.selenium.*;

public class SelfHealingElementFinder {
    private final WebDriver driver;
    private final OpenAIClient openAIClient;

    public SelfHealingElementFinder(WebDriver driver, OpenAIClient openAIClient) {
        this.driver = driver;
        this.openAIClient = openAIClient;
    }

    public WebElement findElement(By by, String elementDescription) {
        try {
            return driver.findElement(by);
        } catch (NoSuchElementException e) {
            System.err.println("[SelfHealing] Original locator failed: " + by);
            try {
                String prompt = String.format("The locator '%s' for '%s' failed. Suggest a new Selenium locator (in Java By format) for this element on the current page. Respond ONLY with a valid By statement.", by.toString(), elementDescription);
                String suggestion = openAIClient.getLocatorSuggestion(prompt);
                System.out.println("[SelfHealing] OpenAI suggestion: " + suggestion);
                By newBy = parseByFromSuggestion(suggestion);
                if (newBy != null) {
                    System.out.println("[SelfHealing] Trying suggested locator: " + newBy);
                    try {
                        return driver.findElement(newBy);
                    } catch (NoSuchElementException ex) {
                        System.err.println("[SelfHealing] Suggested locator also failed: " + newBy);
                    }
                } else {
                    System.err.println("[SelfHealing] No valid locator could be parsed from OpenAI suggestion.");
                }
            } catch (RuntimeException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("429")) {
                    System.err.println("[SelfHealing] OpenAI API rate limit reached (429). Skipping self-healing for this locator.");
                } else {
                    System.err.println("[SelfHealing] OpenAI API error: " + ex.getMessage());
                }
            } catch (Exception ex) {
                System.err.println("[SelfHealing] Unexpected error during self-healing: " + ex.getMessage());
            }
            throw e;
        }
    }

    private By parseByFromSuggestion(String suggestion) {
        // Simple parser for By statements in the OpenAI response
        if (suggestion == null) return null;
        suggestion = suggestion.trim();
        int idx = suggestion.indexOf("By.");
        if (idx == -1) return null;
        String byLine = suggestion.substring(idx);
        byLine = byLine.split("[;\n\r]")[0];
        try {
            if (byLine.startsWith("By.id")) {
                String val = byLine.substring(byLine.indexOf('"')+1, byLine.lastIndexOf('"'));
                return By.id(val);
            } else if (byLine.startsWith("By.xpath")) {
                String val = byLine.substring(byLine.indexOf('"')+1, byLine.lastIndexOf('"'));
                return By.xpath(val);
            } else if (byLine.startsWith("By.cssSelector")) {
                String val = byLine.substring(byLine.indexOf('"')+1, byLine.lastIndexOf('"'));
                return By.cssSelector(val);
            } else if (byLine.startsWith("By.name")) {
                String val = byLine.substring(byLine.indexOf('"')+1, byLine.lastIndexOf('"'));
                return By.name(val);
            }
        } catch (Exception e) {
            System.err.println("[SelfHealing] Failed to parse By from suggestion: " + byLine);
        }
        return null;
    }
}

