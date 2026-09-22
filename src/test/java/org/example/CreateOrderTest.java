package org.example;

import org.example.base.BaseTest;
import org.example.pages.CreateOrderPage;
import org.example.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateOrderTest extends BaseTest {
    @Test
    public void userCanCreateOrder() {
        try {
            new LoginPage(driver, wait, js).login();
            CreateOrderPage createOrderPage = new CreateOrderPage(driver, wait, js);
            createOrderPage.open();
            createOrderPage.selectPrepaidPayment();
            createOrderPage.fillCustomerDetails();
            createOrderPage.fillProductDetails();
            createOrderPage.fillPackagingDetails();
            String orderNumber = createOrderPage.createOrder();
            Assert.assertTrue(orderNumber.startsWith("AUTO-"));
            Assert.assertTrue(driver.getCurrentUrl().contains("manage-orders"));
        } catch (Exception exception) {
            captureFailure("create-order-failure");
            throw exception;
        }
    }
}
