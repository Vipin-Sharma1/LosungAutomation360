package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

public class CreateAndShipOrderTest {

    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;

    // =========================================================
    // TEST DATA
    // =========================================================

    String loginMobile = System.getenv().getOrDefault("LOSUNG_LOGIN_MOBILE", "8824029491");

    String customerMobile = "9876543210";
    String customerName = "Automation Test User";
    String customerAddress = "Jaipur Rajasthan";
    String pincode = "302001";
    String city = "Jaipur";
    String state = "Rajasthan";
    String email = "automation@test.com";

    String sku = "TEST-SKU-001";
    String productName = "Automation Product";
    String unitPrice = "100";
    String discount = "0";

    String deadWeight = "0.20";
    String length = "10";
    String breadth = "10";
    String height = "10";

    String orderNumber = "AUTO-" + System.currentTimeMillis();

    private static final String LOGIN_URL =
            "https://sso.losung360.com/login?referrer=https%3A%2F%2Fapp.losung360.com%2F";

    // =========================================================
    // COURIER PRIORITY
    // =========================================================

    String[] courierPriority = {
            "Delhivery b2c (0.25kg)",
            "AmazonShipping Surface (0.5kg)",
            "Shadowfax Surface (0.5kg)",
            "Xpress Bees SD (0.5kg)",
            "Ekart REGULAR (0.5kg)",
            "Delhivery Special 0.5kg",
            "Bluedart Express (0.5kg)"
    };

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeMethod
    public void setup() {

        System.out.println("\n========== STARTING AUTOMATION ==========");

        ChromeOptions options = new ChromeOptions();

        if (isCiMode()) {
            // GitHub Actions has no interactive desktop Chrome session.
            // Run Chrome headlessly on the CI runner.
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--window-size=1920,1080",
                    "--disable-gpu"
            );

            driver = new ChromeDriver(options);

            System.out.println("CI mode enabled.");
            System.out.println("Started a new headless Chrome session.");

            driver.get(LOGIN_URL);
        } else {
            // Local demo mode: connect to the already logged-in Chrome
            // started with remote debugging on port 9222.
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

            driver = new ChromeDriver(options);

            System.out.println("Local mode enabled.");
            System.out.println("Connected to existing Chrome.");
        }

        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        if (!isCiMode()) {
           // driver.manage().window().maximize();
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        System.out.println("Current URL: " + safeGetUrl());
    }

    // =========================================================
    // MAIN TEST
    // =========================================================

    @Test
    public void createAndShipOrder() {

        try {
            login();
            openAddOrderPage();
            selectPrepaidPayment();
            fillCustomerDetails();
            fillProductDetails();
            fillPackagingDetails();
            fillOtherDetailsAndCreateOrder();
            boolean shipped = searchAndShipOrder();

            Assert.assertTrue(shipped, "Order could not be shipped using any available courier");

        } catch (Exception e) {

            System.out.println("\n===== TEST FAILED =====");
            System.out.println("Current URL: " + safeGetUrl());
            e.printStackTrace();

            takeScreenshot("failure_" + System.currentTimeMillis());

            Assert.fail("Test failed with exception: " + e.getMessage(), e);
        }
    }

    // =========================================================
    // STEP 1 : LOGIN  (with JS-based OTP fill so React state syncs properly)
    // =========================================================

    public void login() {

        System.out.println("\n========== LOGIN CHECK ==========");

        // If the existing Chrome session is already logged in,
        // do NOT ask for mobile number or OTP again.
        String currentUrl = safeGetUrl();

        if (currentUrl.startsWith("https://app.losung360.com/"))  {
            System.out.println("Already logged in to Losung360.");
            System.out.println("Skipping mobile number and OTP.");
            System.out.println("Dashboard URL: " + currentUrl);
            takeScreenshot("03_dashboard_loaded");
            return;
        }

        System.out.println("Login session not available. Starting login flow...");
        System.out.println("\n========== LOGIN ==========");

        WebElement mobileInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[@placeholder='Phone Number or Email']")));

        mobileInput.clear();
        mobileInput.sendKeys(loginMobile);
        System.out.println("Mobile entered: " + loginMobile);

        WebElement getOtpButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Get OTP']")));

        clickWithFallback(getOtpButton);
        System.out.println("Get OTP clicked");

        takeScreenshot("01_otp_screen_loaded");

        // ---------------------------------------------------
        // OTP entry
        // ---------------------------------------------------

        String otp;

        if (isCiMode()) {
            // Never wait for console input in CI.
            // LOSUNG_OTP must be supplied by the CI environment if
            // the QA environment supports a non-interactive test OTP.
            otp = System.getenv("LOSUNG_OTP");

            if (otp == null || otp.isBlank()) {
                Assert.fail(
                        "CI login requires LOSUNG_OTP. " +
                                "Configure a non-interactive QA authentication method " +
                                "before running this test in GitHub Actions."
                );
            }

            otp = otp.trim();
            System.out.println("OTP loaded from CI environment.");
        } else {
            // Local/manual mode keeps the existing interactive OTP flow.
            System.out.print("\nEnter 6 digit OTP: ");
            try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
                otp = scanner.nextLine().trim();
            }
        }

        Assert.assertTrue(otp.matches("\\d{6}"), "OTP must contain exactly 6 digits");

        wait.until(ExpectedConditions.numberOfElementsToBe(
                By.xpath("//input[@maxlength='1']"), 6));

        List<WebElement> otpFields = driver.findElements(By.xpath("//input[@maxlength='1']"));

        for (int i = 0; i < 6; i++) {
            fillOtpBoxViaJs(otpFields.get(i), String.valueOf(otp.charAt(i)));
        }

        System.out.println("OTP entered via JS (state-safe)");

        // Give React a moment to process state + enable Continue button
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        takeScreenshot("02_otp_filled");

        // ---------------------------------------------------
        // Continue To Dashboard
        // ---------------------------------------------------

        WebElement dashboardButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Continue To Dashboard']")));

        clickWithFallback(dashboardButton);
        System.out.println("Continue To Dashboard clicked");

        try {
            wait.until(ExpectedConditions.urlContains("app.losung360.com"));
        } catch (TimeoutException e) {
            System.out.println("URL did not change after click — retrying with JS click");
            js.executeScript("arguments[0].click();", dashboardButton);
            wait.until(ExpectedConditions.urlContains("app.losung360.com"));
        }

        System.out.println("Dashboard loaded: " + driver.getCurrentUrl());
        takeScreenshot("03_dashboard_loaded");
    }

    // =========================================================
    // STEP 2 : ADD ORDER
    // =========================================================

    public void openAddOrderPage() {
        System.out.println("CURRENT URL: " + driver.getCurrentUrl());
        System.out.println("PAGE TITLE: " + driver.getTitle());
        System.out.println("PAGE SOURCE LENGTH: " + driver.getPageSource().length());

        WebElement addOrderButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Add an order']")));

        clickWithFallback(addOrderButton);
        System.out.println("Add an order clicked");

        wait.until(ExpectedConditions.urlContains("/orders/create"));
        System.out.println("Create Order page loaded");
        takeScreenshot("04_create_order_page");
    }

    // =========================================================
    // STEP 3 : PAYMENT METHOD
    // =========================================================

    public void selectPrepaidPayment() {

        WebElement prepaid = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[normalize-space()='Prepaid']")));

        clickWithFallback(prepaid);
        System.out.println("Prepaid selected");
    }

    // =========================================================
    // STEP 4 : CUSTOMER DETAILS  (locators verified from DevTools)
    // =========================================================

    public void fillCustomerDetails() {

        System.out.println("Entering Customer Details");

        fill(By.xpath("//input[@name='customerDetails.mobile']"), customerMobile);
        fill(By.xpath("//input[@name='customerDetails.fullName']"), customerName);
        fill(By.xpath("//input[@name='customerDetails.address']"), customerAddress);

        // Pincode ke baad application City aur State automatically populate karti hai.
        // Isliye City/State ko click/sendKeys nahi karna hai.
        WebElement pincodeInput = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//input[@name='customerDetails.pincode']")));
        scrollToElement(pincodeInput);
        pincodeInput.clear();
        pincodeInput.sendKeys(pincode);

        // Pincode lookup complete hone tak City/State ke values ka wait.
        wait.until(driver -> {
            try {
                WebElement cityField = driver.findElement(
                        By.xpath("//input[@placeholder='City']"));
                WebElement stateField = driver.findElement(
                        By.xpath("//input[@placeholder='State']"));

                String cityValue = cityField.getAttribute("value");
                String stateValue = stateField.getAttribute("value");

                return cityValue != null && !cityValue.trim().isEmpty()
                        && stateValue != null && !stateValue.trim().isEmpty();
            } catch (Exception e) {
                return false;
            }
        });

        System.out.println(
                "City auto-populated: " +
                        driver.findElement(By.xpath("//input[@placeholder='City']")).getAttribute("value")
        );

        System.out.println(
                "State auto-populated: " +
                        driver.findElement(By.xpath("//input[@placeholder='State']")).getAttribute("value")
        );

        fill(By.xpath("//input[@name='customerDetails.email']"), email);

        takeScreenshot("05_customer_details_filled");

        // Billing address same as delivery checkbox
        WebElement billingCheckbox = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("billing-same")));

        String checkedState = billingCheckbox.getAttribute("aria-checked");
        if (checkedState == null || checkedState.equals("false")) {
            clickWithFallback(billingCheckbox);
        }

        System.out.println("Billing address same as delivery selected");
    }

    // =========================================================
    // STEP 5 : PRODUCT DETAILS
    // =========================================================

    public void fillProductDetails() {

        System.out.println("Entering Product Details");

        fill(By.xpath("//input[@placeholder='SKU']"), sku);
        fill(By.xpath("//input[@name='orderItems.0.name']"), productName);
        fill(By.xpath("//input[@name='orderItems.0.unitPrice']"), unitPrice);
        fill(By.xpath("//input[@name='orderItems.0.discount']"), discount);

        System.out.println("Product details entered");
        takeScreenshot("06_product_details_filled");
    }

    // =========================================================
    // STEP 6 : PACKAGING DETAILS
    // =========================================================

    public void fillPackagingDetails() {

        System.out.println("Entering Packaging Details");

        fill(By.xpath("//input[@name='packageDetails.deadWeight']"), deadWeight);
        fill(By.xpath("//input[@name='packageDetails.length']"), length);
        fill(By.xpath("//input[@name='packageDetails.breadth']"), breadth);
        fill(By.xpath("//input[@name='packageDetails.height']"), height);

        System.out.println("Packaging details entered");
        takeScreenshot("07_packaging_details_filled");
    }

    // =========================================================
    // STEP 7 : OTHER DETAILS + CREATE ORDER
    // =========================================================

    public void fillOtherDetailsAndCreateOrder() {

        System.out.println("Entering Other Details");

        fill(By.xpath("//input[@placeholder='Order Number']"), orderNumber);
        System.out.println("Order Number: " + orderNumber);

        WebElement createOrderButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Create Order']")));

        scrollToElement(createOrderButton);
        takeScreenshot("08_before_create_order_click");

        clickWithFallback(createOrderButton);
        System.out.println("Create Order button clicked");

        try {
            wait.until(ExpectedConditions.urlContains("/manage-orders"));
        } catch (TimeoutException e) {
            System.out.println("URL did not change after Create Order click — retrying with JS click");
            js.executeScript("arguments[0].click();", createOrderButton);
            wait.until(ExpectedConditions.urlContains("/manage-orders"));
        }

        System.out.println("Order created successfully");
        takeScreenshot("09_order_created");
    }

    // =========================================================
    // STEP 8 : SEARCH + SHIP ORDER
    // =========================================================

    public boolean searchAndShipOrder() {

        System.out.println("Searching created order...");

        WebElement searchBox = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[contains(@placeholder,'Search items, AWB, Order No, Phone No')]")));

        searchBox.clear();
        searchBox.sendKeys(orderNumber);

        WebElement searchButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Search']")));

        clickWithFallback(searchButton);
        System.out.println("Order search performed");

        WebElement createdOrder = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[normalize-space()='" + orderNumber + "']")));

        Assert.assertTrue(createdOrder.isDisplayed(), "Created order was not found");
        System.out.println("Created order found: " + orderNumber);

        WebElement shipButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[normalize-space()='" + orderNumber +
                                "']/ancestor::div[contains(@class,'relative')]//button[normalize-space()='Ship']")));

        scrollToElement(shipButton);
        clickWithFallback(shipButton);
        System.out.println("Ship button clicked");

        waitForCourierScreen();
        System.out.println("\n========== COURIER SELECTION ==========");

        boolean orderShipped = false;

        for (String courier : courierPriority) {
            boolean result = tryCourier(courier);

            if (result) {
                orderShipped = true;
                System.out.println("\n====================================");
                System.out.println("ORDER SUCCESSFULLY SHIPPED");
                System.out.println("Courier: " + courier);
                System.out.println("Order: " + orderNumber);
                System.out.println("====================================");
                break;
            }
            System.out.println("Trying next courier...");
        }

        return orderShipped;
    }

    // =========================================================
    // TRY COURIER
    // =========================================================

    public boolean tryCourier(String courierName) {

        try {
            System.out.println("\nTrying courier: " + courierName);

            WebElement courierText = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[normalize-space()='" + courierName + "']")));

            WebElement courierCard = findCourierCard(courierText);

            if (courierCard == null) {
                System.out.println("Courier card not found: " + courierName);
                return false;
            }

            WebElement shipButton = courierCard.findElement(
                    By.xpath(".//button[contains(" +
                            "translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
                            "'SHIP')]"));

            wait.until(ExpectedConditions.elementToBeClickable(shipButton));
            scrollToElement(shipButton);
            clickWithFallback(shipButton);

            System.out.println(courierName + " -> SHIP clicked");

            boolean success = waitForShippingResult();

            if (success) {
                System.out.println("SUCCESS -> " + courierName);
                return true;
            }

            System.out.println("FAILED -> " + courierName);
            return false;

        } catch (Exception e) {
            System.out.println("Error with " + courierName + " : " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    // FIND COURIER CARD
    // =========================================================

    private WebElement findCourierCard(WebElement courierText) {

        String[] cardXpaths = {
                "./ancestor::div[contains(@class,'grid')][1]",
                "./ancestor::div[contains(@class,'card')][1]",
                "./ancestor::div[contains(@class,'rounded')][1]",
                "./ancestor::div[.//button[contains(" +
                        "translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
                        "'SHIP')]][1]"
        };

        for (String xpath : cardXpaths) {
            try {
                WebElement card = courierText.findElement(By.xpath(xpath));

                List<WebElement> buttons = card.findElements(
                        By.xpath(".//button[contains(" +
                                "translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')," +
                                "'SHIP')]"));

                if (!buttons.isEmpty()) {
                    return card;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // =========================================================
    // WAIT FOR COURIER SCREEN
    // =========================================================

    private void waitForCourierScreen() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'AmazonShipping')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Delhivery')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Shadowfax')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Xpress Bees')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(),'Ekart')]"))
        ));
        System.out.println("Courier selection screen loaded");
    }

    // =========================================================
    // WAIT FOR SHIPPING RESULT
    // =========================================================

    private boolean waitForShippingResult() {

        WebDriverWait resultWait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            return resultWait.until(driver -> {

                if (isVisible(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SHIPMENT CREATED')]"))) return true;
                if (isVisible(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SHIPMENT SUCCESS')]"))) return true;
                if (isVisible(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SUCCESSFULLY SHIPPED')]"))) return true;

                if (isVisible(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'FAILED')]"))) return false;
                if (isVisible(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'ERROR')]"))) return false;
                if (isVisible(By.xpath("//*[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'UNABLE TO SHIP')]"))) return false;

                if (isVisible(By.xpath("//*[normalize-space()='Ship']"))) return false;

                return null;
            });
        } catch (Exception e) {
            System.out.println("Shipping result timeout.");
            return false;
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isVisible(By locator) {
        try {
            for (WebElement element : driver.findElements(locator)) {
                if (element.isDisplayed()) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Reusable text field filler with wait + scroll */
    public void fill(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollToElement(element);
        element.clear();
        element.sendKeys(value);
    }

    /**
     * Fills a single-character OTP box by dispatching a real 'input' event,
     * so React-controlled OTP components register the value properly.
     * This is the fix for "click had to be done manually" issue —
     * sendKeys() alone sometimes leaves React state out of sync with the DOM,
     * making the Continue button's internal validation silently fail.
     */
    private void fillOtpBoxViaJs(WebElement field, String digit) {
        js.executeScript(
                "var el = arguments[0]; var val = arguments[1];" +
                        "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "nativeInputValueSetter.call(el, val);" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));",
                field, digit
        );
    }

    /**
     * Clicks a WebElement, falling back to a JS click if the native click
     * throws (element intercepted by an overlay) or silently does nothing.
     */
    private void clickWithFallback(WebElement element) {
        try {
            scrollToElement(element);
            element.click();
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            System.out.println("Native click failed (" + e.getClass().getSimpleName() + "), retrying via JS click");
            js.executeScript("arguments[0].click();", element);
        }
    }

    private void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    private String safeGetUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private void takeScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destDir = new File("screenshots");
            if (!destDir.exists()) destDir.mkdirs();
            File dest = new File(destDir, name + ".png");
            Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + dest.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
    }

    // =========================================================
    // TEARDOWN
    // =========================================================

    @AfterMethod
    public void tearDown() {

        if (isCiMode()) {
            // CI runners must release the browser/processes after the test.
            if (driver != null) {
                driver.quit();
            }
            System.out.println("CI browser session closed.");
        } else {
            // Local browser intentionally remains open for inspection/demo.
            System.out.println("Local browser left open for inspection.");
        }
    }

    // =========================================================
    // CI MODE
    // =========================================================

    private boolean isCiMode() {
        return "true".equalsIgnoreCase(System.getenv("CI"))
                || "true".equalsIgnoreCase(System.getenv("CI_MODE"));
    }
}