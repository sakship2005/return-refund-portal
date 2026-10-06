package com.portal.rrp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseSeleniumTest {

    @LocalServerPort
    protected int port;

    protected WebDriver driver;
    protected WebDriverWait wait;

    // Failure screenshot mechanism: runs when a test throws, BEFORE the browser is closed.
    // Screenshots are saved to target/screenshots/<Class>_<method>.png
    @RegisterExtension
    final TestExecutionExceptionHandler screenshotOnFailure = (context, throwable) -> {
        takeScreenshot(context.getRequiredTestClass().getSimpleName()
                + "_" + context.getRequiredTestMethod().getName());
        throw throwable;
    };

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        // headless by default; run "mvn test -Dheadless=false" to watch the browser
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1280,900", "--no-sandbox", "--disable-gpu");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- helpers shared by all tests ----------

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected String uniqueId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected void createRequest(String orderId, String product, String reason,
                                 String amount, String customer) {
        driver.get(url("/requests/new"));
        driver.findElement(By.name("orderId")).sendKeys(orderId);
        driver.findElement(By.name("product")).sendKeys(product);
        driver.findElement(By.name("reason")).sendKeys(reason);
        driver.findElement(By.name("amount")).sendKeys(amount);
        driver.findElement(By.name("customerName")).sendKeys(customer);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlMatches(".*/requests$"));
    }

    protected void openRequest(String orderId) {
        driver.get(url("/requests"));
        driver.findElement(By.linkText(orderId)).click();
        wait.until(ExpectedConditions.urlMatches(".*/requests/\\d+$"));
    }

    protected String detailField(String label) {
        return driver.findElement(
                By.xpath("//p[b[normalize-space()='" + label + "']]/span")).getText();
    }

    protected void waitForStatus(String expected) {
        wait.until(ExpectedConditions.textToBe(
                By.xpath("//p[b[normalize-space()='Status:']]/span"), expected));
    }

    protected void advance(String actor) {
        driver.findElement(By.name("actor")).sendKeys(actor);
        driver.findElement(By.xpath("//button[normalize-space()='Advance status']")).click();
    }

    private void takeScreenshot(String name) {
        try {
            if (driver instanceof TakesScreenshot ts) {
                Path dir = Path.of("target", "screenshots");
                Files.createDirectories(dir);
                Path tmp = ts.getScreenshotAs(OutputType.FILE).toPath();
                Files.copy(tmp, dir.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("Could not save screenshot: " + e.getMessage());
        }
    }
}