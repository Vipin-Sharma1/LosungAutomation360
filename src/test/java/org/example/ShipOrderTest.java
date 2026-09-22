package org.example;

import org.example.base.BaseTest;
import org.example.pages.CreateOrderPage;
import org.example.pages.LoginPage;
import org.example.pages.ManageOrdersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ShipOrderTest extends BaseTest {
    @Test
    public void userCanCreateAndShipOrder() {
        try {
            new LoginPage(driver, wait, js).login();
            CreateOrderPage createOrderPage = new CreateOrderPage(driver, wait, js);
            createOrderPage.open();
            createOrderPage.selectPrepaidPayment();
            createOrderPage.fillCustomerDetails();
            createOrderPage.fillProductDetails();
            createOrderPage.fillPackagingDetails();
            String orderNumber = createOrderPage.createOrder();

            boolean shipped = new ManageOrdersPage(driver, wait, js).searchAndShip(orderNumber);
            Assert.assertTrue(shipped, "Order could not be shipped using any available courier");
        } catch (Exception exception) {
            captureFailure("ship-order-failure");
            throw exception;
        }
    }
}
