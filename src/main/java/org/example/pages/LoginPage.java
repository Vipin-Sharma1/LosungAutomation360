package org.example.pages;

import org.example.utils.ConfigReader;
import org.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.util.List;
import java.util.Scanner;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    public LoginPage(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) {
        this.driver = driver;
        this.wait = wait;
        this.js = js;
    }

    public void login() {
        if (driver.getCurrentUrl().startsWith("https://app.losung360.com/")) {
            return;
        }

        WaitUtils.fill(driver, wait, js, By.xpath("//input[@placeholder='Phone Number or Email']"),
                ConfigReader.loginMobile());
        WaitUtils.click(driver, wait, js,
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Get OTP']"))));

        String otp = readOtp();
        Assert.assertTrue(otp.matches("\\d{6}"), "OTP must contain exactly 6 digits");
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//input[@maxlength='1']"), 6));
        List<WebElement> fields = driver.findElements(By.xpath("//input[@maxlength='1']"));
        for (int index = 0; index < fields.size(); index++) {
            fillOtpBox(fields.get(index), String.valueOf(otp.charAt(index)));
        }

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Continue To Dashboard']")));
        WaitUtils.click(driver, wait, js, continueButton);
        try {
            wait.until(ExpectedConditions.urlContains("app.losung360.com"));
        } catch (TimeoutException exception) {
            js.executeScript("arguments[0].click();", continueButton);
            wait.until(ExpectedConditions.urlContains("app.losung360.com"));
        }
    }

    private String readOtp() {
        if (ConfigReader.isCiMode()) {
            String otp = ConfigReader.otp();
            Assert.assertTrue(otp != null && !otp.isBlank(), "CI login requires LOSUNG_OTP");
            return otp.trim();
        }
        System.out.print("Enter 6 digit OTP: ");
        try (Scanner scanner = new Scanner(System.in)) {
            return scanner.nextLine().trim();
        }
    }

    private void fillOtpBox(WebElement field, String digit) {
        js.executeScript("var el=arguments[0], val=arguments[1];"
                + "var setter=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;"
                + "setter.call(el,val);el.dispatchEvent(new Event('input',{bubbles:true}));"
                + "el.dispatchEvent(new Event('change',{bubbles:true}));", field, digit);
    }
}
