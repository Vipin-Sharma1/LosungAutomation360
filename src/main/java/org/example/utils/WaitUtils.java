package org.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class WaitUtils {
    private WaitUtils() {
    }

    public static WebElement visible(WebDriverWait wait, By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static void fill(WebDriver driver, WebDriverWait wait, JavascriptExecutor js,
                            By locator, String value) {
        WebElement element = visible(wait, locator);
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(js, element);
        element.clear();
        element.sendKeys(value);
    }

    public static void click(WebDriver driver, WebDriverWait wait, JavascriptExecutor js,
                             WebElement element) {
        try {
            scrollIntoView(js, element);
            element.click();
        } catch (ElementClickInterceptedException | StaleElementReferenceException exception) {
            js.executeScript("arguments[0].click();", element);
        }
    }

    public static void scrollIntoView(JavascriptExecutor js, WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }
}
