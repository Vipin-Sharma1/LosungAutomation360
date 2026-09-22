package org.example.pages;

import org.example.constants.TestData;
import org.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ManageOrdersPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public ManageOrdersPage(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.js = js;
    }

    public boolean searchAndShip(String orderNumber) {
        WaitUtils.fill(driver, wait, js,
                By.xpath("//input[contains(@placeholder,'Search items, AWB, Order No, Phone No')]"), orderNumber);
        WaitUtils.click(driver, wait, js, wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Search']"))));
        WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space()=" + xpathLiteral(orderNumber) + "]")));
        String orderRow = "//*[normalize-space()=" + xpathLiteral(orderNumber)
                + "]/ancestor::div[contains(@class,'relative')]";
        WaitUtils.click(driver, wait, js, wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(orderRow + "//button[normalize-space()='Ship']"))));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'AmazonShipping')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Delhivery')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Shadowfax')]"))));

        for (String courier : TestData.COURIER_PRIORITY) {
            if (tryCourier(courier)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryCourier(String courierName) {
        try {
            WebElement courierText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[normalize-space()=" + xpathLiteral(courierName) + "]")));
            WebElement card = courierText.findElement(By.xpath(
                    "./ancestor::div[.//button[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SHIP')]][1]"));
            WebElement ship = card.findElement(By.xpath(".//button[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SHIP')][1]"));
            WaitUtils.click(driver, wait, js, ship);
            return waitForShippingResult();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean waitForShippingResult() {
        try {
            return new WebDriverWait(driver, java.time.Duration.ofSeconds(15)).until(currentDriver -> {
                String success = "//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SHIPMENT CREATED') or contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SHIPMENT SUCCESS') or contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SUCCESSFULLY SHIPPED')]";
                String failure = "//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'FAILED') or contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ERROR') or contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'UNABLE TO SHIP')]";
                if (!currentDriver.findElements(By.xpath(success)).isEmpty()) return true;
                if (!currentDriver.findElements(By.xpath(failure)).isEmpty()) return false;
                return null;
            });
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        return "concat('" + value.replace("'", "',\"'\",'") + "')";
    }
}
