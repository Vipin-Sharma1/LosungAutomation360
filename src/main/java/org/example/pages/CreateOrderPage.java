package org.example.pages;

import org.example.constants.TestData;
import org.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateOrderPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public CreateOrderPage(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.js = js;
    }

    public void open() {
        WaitUtils.click(driver, wait, js, wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Add an order']"))));
        wait.until(ExpectedConditions.urlContains("/orders/create"));
    }

    public void selectPrepaidPayment() {
        WaitUtils.click(driver, wait, js, wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[normalize-space()='Prepaid']"))));
    }

    public void fillCustomerDetails() {
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='customerDetails.mobile']"), TestData.CUSTOMER_MOBILE);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='customerDetails.fullName']"), TestData.CUSTOMER_NAME);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='customerDetails.address']"), TestData.CUSTOMER_ADDRESS);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='customerDetails.pincode']"), TestData.PINCODE);
        wait.until(driver -> hasValue(By.xpath("//input[@placeholder='City']"))
            && hasValue(By.xpath("//input[@placeholder='State']")));
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='customerDetails.email']"), TestData.EMAIL);
        WebElement billing = wait.until(ExpectedConditions.elementToBeClickable(By.id("billing-same")));
        if (!"true".equalsIgnoreCase(billing.getAttribute("aria-checked"))) {
            WaitUtils.click(driver, wait, js, billing);
        }
    }

    public void fillProductDetails() {
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@placeholder='SKU']"), TestData.SKU);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='orderItems.0.name']"), TestData.PRODUCT_NAME);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='orderItems.0.unitPrice']"), TestData.UNIT_PRICE);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='orderItems.0.discount']"), TestData.DISCOUNT);
    }

    public void fillPackagingDetails() {
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='packageDetails.deadWeight']"), TestData.DEAD_WEIGHT);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='packageDetails.length']"), TestData.LENGTH);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='packageDetails.breadth']"), TestData.BREADTH);
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@name='packageDetails.height']"), TestData.HEIGHT);
    }

    public String createOrder() {
        String orderNumber = "AUTO-" + System.currentTimeMillis();
        WaitUtils.fill(driver, wait, js, By.xpath("//input[@placeholder='Order Number']"), orderNumber);
        WebElement create = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Create Order']")));
        WaitUtils.click(driver, wait, js, create);
        try {
            wait.until(ExpectedConditions.urlContains("/manage-orders"));
        } catch (TimeoutException exception) {
            js.executeScript("arguments[0].click();", create);
            wait.until(ExpectedConditions.urlContains("/manage-orders"));
        }
        return orderNumber;
    }

    private boolean hasValue(By locator) {
        String value = driver.findElement(locator).getAttribute("value");
        return value != null && !value.isBlank();
    }
}
