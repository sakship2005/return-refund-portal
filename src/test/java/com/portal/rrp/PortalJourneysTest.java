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
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Wireless Mouse", "Damaged on arrival", "799.50", "Asha Rao");

        String row = driver.findElement(
                By.xpath("//tr[td/a[text()='" + orderId + "']]")).getText();
        assertTrue(row.contains("NON_EXISTENT_PRODUCT_FAIL"), "product missing in row: " + row);
        assertTrue(row.contains("Asha Rao"), "customer missing in row: " + row);
        assertTrue(row.contains("799.5"), "amount missing in row: " + row);
        assertTrue(row.contains("REQUESTED"), "new request should be REQUESTED: " + row);
    }

        // J2: search records
    @Test
    void tc2_search_filtersByCustomer() {
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
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Smart Watch", "Defective", "4999", "Meera Shah");
        openRequest(orderId);
        assertEquals("REQUESTED", detailField("Status:"));

        advance("agent");
        waitForStatus("UNDER_REVIEW");

        advance("finance");
        waitForStatus("APPROVED");

        String history = driver.findElement(By.tagName("ul")).getText();
        assertTrue(history.contains("REQUESTED -> UNDER_REVIEW by agent"), history);
        assertTrue(history.contains("UNDER_REVIEW -> APPROVED by finance"), history);
    }

        // J4b: role-based status workflow, rejecting
    @Test
    void tc4b_statusWorkflow_rejectStopsWorkflow() {
        String orderId = uniqueId("ORD");
        createRequest(orderId, "Backpack", "Zip broken", "999", "Neha Joshi");
        openRequest(orderId);

        advance("agent");
        waitForStatus("UNDER_REVIEW");

        // the Reject form has its own required "actor" field, so fill it in first
        driver.findElement(By.xpath(
                "//form[.//button[normalize-space()='Reject request']]//input[@name='actor']"))
                .sendKeys("agent");
        driver.findElement(By.xpath("//button[normalize-space()='Reject request']")).click();
        waitForStatus("REJECTED");

        String history = driver.findElement(By.tagName("ul")).getText();
        assertTrue(history.contains("UNDER_REVIEW -> REJECTED by agent"), history);
        assertTrue(driver.findElements(
                By.xpath("//button[normalize-space()='Advance status']")).isEmpty(),
                "a rejected request must not be advanceable");
    }
    // J5: summary dashboard
    @Test
    void tc5_dashboard_countIncreasesAfterCreate() {
        int before = requestedCountOnDashboard();
        createRequest(uniqueId("ORD"), "Water Bottle", "Leaking", "350", "Karan Mehta");
        int after = requestedCountOnDashboard();
        assertEquals(before + 1, after, "REQUESTED count should go up by one");
    }

    private int requestedCountOnDashboard() {
        driver.get(url("/dashboard"));
        List<WebElement> cells = driver.findElements(
                By.xpath("//tr[td[1][normalize-space()='REQUESTED']]/td[2]"));
        return cells.isEmpty() ? 0 : Integer.parseInt(cells.get(0).getText().trim());
    }
}