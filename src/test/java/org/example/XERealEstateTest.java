package org.example;

import org.openqa.selenium.By;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class XERealEstateTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver; // JavaScript Executor for handling dynamic elements
    }

    @Test(priority = 1)
    public void searchRentalProperties() {
        driver.get("https://xe.gr/");

        clickElement(By.xpath("//*[@id='qc-cmp2-ui']/div[2]/div/button[3]/span")); // Accept Cookies

        sendKeysToElement(By.name("geo_place_id"), "Παγκράτι");
        clickElement(By.xpath("//button[contains(text(),'Παγκράτι, Αθήνα, Ελλάδα')]")); // Select location

        clickElement(By.xpath("//button[contains(@class,'area-tag-button')]")); // Confirm selection
        clickElement(By.xpath("//input[@value='Αναζήτηση']")); // Search button

        clickElement(By.xpath("//body/main[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/button[1]")); // Open filter menu

        sendKeysToElement(By.name("minimum_price"), "200");
        sendKeysToElement(By.name("maximum_price"), "700");

        clickElement(By.xpath("//button[contains(text(),'Τετραγωνικά')]")); // Square meters filter
        sendKeysToElement(By.name("minimum_size"), "75");
        sendKeysToElement(By.name("maximum_size"), "150");

        // ✅ Validate Property Prices & Sizes
        validatePropertyPrices(200, 700);
        validatePropertySizes(75, 150);
        validateMaxImagesInAd(30);
        validateDescendingPriceSorting();
    }

    /**
     * ✅ Validates property prices fall within the defined range.
     */
    private void validatePropertyPrices(int minPrice, int maxPrice) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='property-ad-price']")));
        List<WebElement> priceElements = driver.findElements(By.xpath("//span[@class='property-ad-price']"));

        for (WebElement priceElement : priceElements) {
            try {
                String priceText = priceElement.getText().replaceAll("[^0-9]", "").trim(); // Extract numbers only
                if (!priceText.isEmpty()) {
                    int priceValue = Integer.parseInt(priceText);
                    Assert.assertTrue(priceValue >= minPrice && priceValue <= maxPrice,
                            "❌ Price out of range! Found: " + priceValue);
                    System.out.println("✅ Validated Price: " + priceValue);
                } else {
                    System.out.println("⚠️ Price is empty or not found.");
                }
            } catch (NumberFormatException e) {
                System.err.println("❌ Error parsing price: " + priceElement.getText());
            }
        }
    }

    /**
     * ✅ Validates property sizes (square meters) fall within the defined range.
     */
    private void validatePropertySizes(int minSize, int maxSize) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//h3[contains(@data-testid, 'property-ad-title')]")));
        List<WebElement> sizeElements = driver.findElements(By.xpath("//h3[contains(@data-testid, 'property-ad-title')]"));

        for (WebElement sizeElement : sizeElements) {
            try {
                String sizeText = sizeElement.getText().replaceAll("[^0-9]", "").trim(); // Extract numbers only
                if (!sizeText.isEmpty()) {
                    int sizeValue = Integer.parseInt(sizeText);
                    Assert.assertTrue(sizeValue >= minSize && sizeValue <= maxSize,
                            "❌ Size out of range! Found: " + sizeValue + " m²");
                    System.out.println("✅ Validated Size: " + sizeValue + " m²");
                } else {
                    System.out.println("⚠️ Size is empty or not found.");
                }
            } catch (NumberFormatException e) {
                System.err.println("❌ Error parsing size: " + sizeElement.getText());
            }
        }
    }

    /**
     * ✅ Validates that no property ad contains more than the specified max number of images.
     */
    private void validateMaxImagesInAd(int maxImages) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//button[contains(@class, 'slick-arrow')]")));

        List<WebElement> propertyAds = driver.findElements(By.xpath("//button[contains(@class, 'slick-arrow')]"));

        for (WebElement ad : propertyAds) {
            List<WebElement> images = ad.findElements(By.xpath("//button[contains(@class, 'slick-arrow')]")); // Get all images inside carousel

            int imageCount = images.size();
            Assert.assertTrue(imageCount <= maxImages,
                    "❌ Ad contains more than " + maxImages + " images! Found: " + imageCount);
            System.out.println("✅ Validated Image Count: " + imageCount);
        }
    }

    /**
     * ✅ Validates that the ads are correctly sorted by descending price.
     */
    private void validateDescendingPriceSorting() {
        // Click on the sorting button to select "Descending Price"
        clickElement(By.xpath("//body/main[1]/div[1]/div[1]/div[1]/div[3]/div[1]/div[1]/div[1]/button[1]"));

        // Click on the sorting button to select "Descending Price"
        clickElement(By.xpath("//button[contains(text(),'Τιμή (φθίνουσα)')]"));

        // Wait for prices to be loaded after sorting
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='property-ad-price']")));

        // Extract all property prices
        List<WebElement> priceElements = driver.findElements(By.xpath("//span[@class='property-ad-price']"));

        // Convert price text to integer values
        List<Integer> extractedPrices = priceElements.stream()
                .map(e -> e.getText().replaceAll("[^0-9]", "").trim()) // Remove non-numeric characters
                .filter(text -> !text.isEmpty()) // Ignore empty price fields
                .map(Integer::parseInt) // Convert to integer
                .collect(Collectors.toList());

        // Create a sorted copy in descending order for comparison
        List<Integer> sortedPrices = extractedPrices.stream()
                .sorted((a, b) -> Integer.compare(b, a)) // Sort in descending order
                .collect(Collectors.toList());

        // Validate that extracted prices are correctly sorted
        Assert.assertEquals(extractedPrices, sortedPrices, "❌ Prices are not sorted in descending order!");

        System.out.println("✅ Prices are correctly sorted in descending order.");
    }

    /**
     * ✅ Validates that the contact phone in each ad is hidden initially
     *    and only shown after clicking the reveal button.
     */
    private void validateContactPhoneVisibility() {
        // Wait for the ads to be loaded
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@class,'property-ad')]")));

        // Locate all property ads
        List<WebElement> propertyAds = driver.findElements(By.xpath("//div[contains(@class,'property-ad')]"));

        for (WebElement ad : propertyAds) {
            try {
                // ✅ Ensure the phone number is initially hidden
                List<WebElement> phoneElements = ad.findElements(By.xpath(".//span[contains(@class, 'contact-phone')]"));
                Assert.assertTrue(phoneElements.isEmpty(), "❌ Phone number is visible before clicking the button!");

                // ✅ Ensure the clickable button exists
                WebElement revealButton = ad.findElement(By.xpath(".//button[contains(text(), 'Τηλέφωνο')]"));
                Assert.assertTrue(revealButton.isDisplayed(), "❌ Phone reveal button is missing!");

                // ✅ Click the button to reveal the phone number
                revealButton.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'phone-popup')]")));

                // ✅ Verify that the phone number appears inside the pop-up
                WebElement phonePopup = driver.findElement(By.xpath("//div[contains(@class,'phone-popup')]"));
                WebElement phoneText = phonePopup.findElement(By.xpath(".//span[contains(@class, 'contact-phone')]"));
                Assert.assertTrue(phoneText.isDisplayed(), "❌ Phone number did not appear after clicking the button!");

                System.out.println("✅ Phone number is correctly hidden and revealed upon clicking.");

            } catch (Exception e) {
                System.out.println("⚠️ Skipping an ad due to missing phone button.");
            }
        }
    }

    /**
     * ✅ Clicks an element after waiting for it to be clickable.
     */
    private void clickElement(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        } catch (Exception e) {
            System.out.println("⚠️ Click failed, trying JavaScript click...");
            WebElement element = driver.findElement(locator);
            js.executeScript("arguments[0].click();", element);
        }
    }

    /**
     * ✅ Sends keys to an input field and clears it properly.
     */
    private void sendKeysToElement(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].value='';", element); // Clear input using JavaScript
        element.sendKeys(value);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
