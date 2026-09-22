package org.example;

import org.example.base.BaseTest;
import org.example.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    @Test
    public void userCanLogin() {
        new LoginPage(driver, wait, js).login();
        Assert.assertTrue(driver.getCurrentUrl().contains("app.losung360.com"),
                "User was not redirected to the Losung360 application");
    }
}
