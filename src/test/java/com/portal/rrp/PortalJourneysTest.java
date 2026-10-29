package com.portal.rrp;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortalJourneysTest extends BaseSeleniumTest {

    // J1: create a return request
    @Test
    void tc1_createRequest_appearsInList() {
        login(USER, USER_PASSWORD);
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Wireless Mouse", "Damaged on arrival", "799.50", "Asha Rao");

        String row = driver.findElement(
                By.xpath("//tr[td/a[text()='" + orderId + "']]")).getText();
        assertTrue(row.contains("Wireless Mouse"), "product missing in row: " + row);
        assertTrue(row.contains("Asha Rao"), "customer missing in row: " + row);
        assertTrue(row.contains("799.5"), "amount missing in row: " + row);
        assertTrue(row.contains("REQUESTED"), "new request should be REQUESTED: " + row);
    }

        // J2: search records
    @Test
    void tc2_search_filtersByCustomer() {
        login(USER, USER_PASSWORD);
        String orderA = uniqueId("ORD");
        String orderB = uniqueId("ORD");
        String alpha = uniqueId("Alpha");
        String beta = uniqueId("Beta");
        createRequest(orderA, "Headphones", "Not working", "1500", alpha);
        createRequest(orderB, "Charger", "Wrong item", "400", beta);

        driver.get(url("/requests"));
        driver.findElement(By.name("q")).sendKeys(alpha);
        driver.findElement(By.xpath("//button[normalize-space()='Search']")).click();

        // wait for the results page (URL has ?q=) and for the expected row to appear
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("q="));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .textToBePresentInElementLocated(By.tagName("table"), orderA));

        String table = driver.findElement(By.tagName("table")).getText();
        assertTrue(table.contains(orderA), "searched record should be shown");
        assertFalse(table.contains(orderB), "other customer's record should be hidden");
    }
    // J3: view and update a record
    @Test
    void tc3_viewAndUpdate_changesProduct() {
        login(USER, USER_PASSWORD);
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Wireless Mouse", "Damaged", "799.50", "Ravi Kumar");

        openRequest(orderId);
        assertEquals(orderId, detailField("Order ID:"));
        assertEquals("Wireless Mouse", detailField("Product:"));

        driver.findElement(By.linkText("Edit request")).click();
        WebElement product = driver.findElement(By.name("product"));
        product.clear();
        product.sendKeys("Wireless Keyboard");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .urlMatches(".*/requests/\\d+$"));
        assertEquals("Wireless Keyboard", detailField("Product:"));
    }

    // J4a: role-based status workflow, advancing
    @Test
    void tc4a_statusWorkflow_advancesAndLogsHistory() {
        // the customer raises the request ...
        login(USER, USER_PASSWORD);
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Smart Watch", "Defective", "4999", "Meera Shah");
        logout();

        // ... and the admin moves it through the workflow
        login(ADMIN, ADMIN_PASSWORD);
        openRequest(orderId);
        assertEquals("REQUESTED", detailField("Status:"));

        advance();
        waitForStatus("UNDER_REVIEW");

        advance();
        waitForStatus("APPROVED");

        String history = driver.findElement(By.tagName("ul")).getText();
        assertTrue(history.contains("REQUESTED -> UNDER_REVIEW by admin"), history);
        assertTrue(history.contains("UNDER_REVIEW -> APPROVED by admin"), history);
    }

        // J4b: role-based status workflow, rejecting
    @Test
    void tc4b_statusWorkflow_rejectStopsWorkflow() {
        login(USER, USER_PASSWORD);
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Backpack", "Zip broken", "999", "Neha Joshi");
        logout();

        login(ADMIN, ADMIN_PASSWORD);
        openRequest(orderId);

        advance();
        waitForStatus("UNDER_REVIEW");

        driver.findElement(By.xpath("//button[normalize-space()='Reject request']")).click();
        waitForStatus("REJECTED");

        String history = driver.findElement(By.tagName("ul")).getText();
        assertTrue(history.contains("UNDER_REVIEW -> REJECTED by admin"), history);
        assertTrue(driver.findElements(By.cssSelector("form[action$='/advance']")).isEmpty(),
                "a rejected request must not be advanceable");
    }
    // J5: summary dashboard
    @Test
    void tc5_dashboard_countIncreasesAfterCreate() {
        login(ADMIN, ADMIN_PASSWORD);
        int before = requestedCountOnDashboard();
        createRequest(uniqueId("ORD"), "Water Bottle", "Leaking", "350", "Karan Mehta");
        int after = requestedCountOnDashboard();
        assertEquals(before + 1, after, "REQUESTED count should go up by one");
    }

    // J6: login is required, and wrong credentials are refused
    @Test
    void tc6_loginRequired_andWrongPasswordRejected() {
        driver.get(url("/requests"));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));
        assertTrue(driver.findElement(By.name("username")).isDisplayed(),
                "anonymous visitors must be sent to the login form");

        driver.findElement(By.name("username")).sendKeys(USER);
        driver.findElement(By.name("password")).sendKeys("wrong-password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("error"));
        assertTrue(driver.getPageSource().contains("Invalid username or password"),
                "a wrong password must show an error");
    }

    // J7: a customer cannot use admin pages or status controls
    @Test
    void tc7_customer_cannotUseAdminFeatures() {
        login(USER, USER_PASSWORD);

        driver.get(url("/dashboard"));
        assertTrue(driver.getPageSource().contains("Access denied"),
                "the dashboard is admin-only");

        String orderId = uniqueId("ORD");
        createRequest(orderId, "Lamp", "Bulb missing", "250", "Divya Nair");
        openRequest(orderId);
        assertTrue(driver.findElements(By.cssSelector("form[action$='/advance']")).isEmpty(),
                "customers must not see the status buttons");
    }

    // J8: customers only see their own requests
    @Test
    void tc8_customer_seesOnlyOwnRequests() {
        login(USER, USER_PASSWORD);
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Kettle", "Not heating", "1200", "Sanjay Rao");
        openRequest(orderId);
        String detailUrl = driver.getCurrentUrl();
        logout();

        login(USER2, USER_PASSWORD);
        driver.get(url("/requests"));
        assertFalse(driver.getPageSource().contains(orderId),
                "another customer's request must not be listed");
        driver.get(detailUrl);
        assertTrue(driver.getPageSource().contains("Access denied"),
                "opening another customer's request directly must be refused");
    }

    private int requestedCountOnDashboard() {
        driver.get(url("/dashboard"));
        List<WebElement> cells = driver.findElements(
                By.xpath("//tr[td[1][normalize-space()='REQUESTED']]/td[2]"));
        return cells.isEmpty() ? 0 : Integer.parseInt(cells.get(0).getText().trim());
    }
}