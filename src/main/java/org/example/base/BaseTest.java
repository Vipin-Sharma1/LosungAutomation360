package org.example.base;

import org.example.utils.ConfigReader;
import org.example.utils.ScreenshotUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (ConfigReader.isCiMode()) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                    "--window-size=1920,1080", "--disable-gpu");
            driver = new ChromeDriver(options);
            driver.get(ConfigReader.loginUrl());
        } else {
            options.setExperimentalOption("debuggerAddress", ConfigReader.debuggerAddress());
            driver = new ChromeDriver(options);
        }

        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.timeoutSeconds()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.timeoutSeconds()));
    }

    @AfterMethod
    public void tearDown() {
        if (ConfigReader.isCiMode() && driver != null) {
            driver.quit();
        }
    }

    protected void captureFailure(String name) {
        if (driver != null) {
            ScreenshotUtils.capture(driver, name);
        }
    }
}
