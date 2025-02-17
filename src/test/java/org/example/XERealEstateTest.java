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
import java.util.ArrayList;
import org.openqa.selenium.interactions.Actions;
import java.util.stream.Collectors;
import java.util.*;


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

        // Accept Cookies
        clickElement(By.xpath("//*[@id='qc-cmp2-ui']/div[2]/div/button[3]/span"));

        // Type initial search to trigger dropdown
        sendKeysToElement(By.name("geo_place_id"), "Παγκράτι");

        // Wait for the dropdown container to appear
        WebElement dropdownContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@data-testid='geo_place_id_dropdown_panel']")
        ));

        // Get all dropdown options (buttons inside the dropdown)
        List<WebElement> dropdownOptions = dropdownContainer.findElements(By.xpath(".//button"));

        // Store dropdown values in a list
        List<String> dropdownValues = new ArrayList<>();
        for (WebElement option : dropdownOptions) {
            dropdownValues.add(option.getText());
        }

        // Print extracted dropdown values
        System.out.println("Extracted Dropdown Options:");
        dropdownValues.forEach(System.out::println);

        // Assert that the dropdown contains options
        Assert.assertFalse(dropdownValues.isEmpty(), "❌ Dropdown is empty!");

        // ✅ Iterate through each dropdown value and select the first available choice
        for (String location : dropdownValues) {
            System.out.println("🔍 Searching for: " + location);

            // Clear and enter new location value
            WebElement searchBox = driver.findElement(By.name("geo_place_id"));
            searchBox.clear();  // Clear previous input
            sendKeysToElement(By.name("geo_place_id"), location);

            // Wait for the dropdown to appear again
            dropdownContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[@data-testid='geo_place_id_dropdown_panel']")
            ));

            // Click the first option dynamically
            WebElement firstChoice = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@data-testid='geo_place_id_dropdown_panel']//button[1]")
            ));
            firstChoice.click();

            System.out.println("✅ Selected: " + location);

            // Small wait before next iteration (optional)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        clickElement(By.xpath("//button[contains(@class,'area-tag-button')]")); // Confirm selection
        clickElement(By.xpath("//input[@value='Αναζήτηση']")); // Search button

        clickElement(By.xpath("//body/main[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/button[1]")); // Open filter menu

        sendKeysToElement(By.name("minimum_price"), "200");
        sendKeysToElement(By.name("maximum_price"), "700");

        clickElement(By.xpath("//button[contains(text(),'Τετραγωνικά')]")); // Square meters filter
        sendKeysToElement(By.name("minimum_size"), "75");
        sendKeysToElement(By.name("maximum_size"), "150");



        clickElement(By.xpath("//body[1]/main[1]/div[1]/div[1]/div[1]/div[2]"));



        // ✅ Validate Property Prices & Sizes
        validatePropertyPrices(200, 700);
        validatePropertySizes(75, 150);
        validateDescendingPriceSorting();
        validateImagesAndPhoneNumbers();

    }



    /**
     * ✅ Validates property prices fall within the defined range.
     */
    private void validatePropertyPrices(int minPrice, int maxPrice) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Scroll to load all results
        long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

        while (true) {
            js.executeScript("window.scrollBy(0, 500);"); // Scroll down step-by-step
            try {
                Thread.sleep(9000); // Wait for new results to load
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (newHeight == lastHeight) {
                break; // Stop if no new content is loaded
            }
            lastHeight = newHeight;
        }

        // Wait until all price elements are visible
        List<WebElement> priceElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("div.common-property-ad-price span.property-ad-price[data-testid='property-ad-price']")
        ));

        System.out.println("Number of price elements found: " + priceElements.size());

        // Validate extracted prices
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//h3[contains(@data-testid, 'property-ad-title')]")));

        List<WebElement> sizeElements = driver.findElements(By.xpath("//h3[contains(@data-testid, 'property-ad-title')]"));

        System.out.println("Number of Titles elements found: " + sizeElements.size());
        //System.out.println("Property Titles:");
        //for (WebElement title : sizeElements) {
        //System.out.println(title.getText());
        //}

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


    private void validateImagesAndPhoneNumbers() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        System.out.println("🔄 Extracting all ad properties...");

        // Find all listings
        List<WebElement> listings = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div.lazyload-wrapper.cell.huge-3.xxxlarge-4.large-6.medium-4.small-6.tiny-12.scroll")
        ));

        System.out.println("Total listings found: " + listings.size());

        for (WebElement listing : listings) {
            try {
                // ✅ Find the first image inside the listing
                WebElement image = listing.findElement(By.tagName("img"));

                // ✅ Click the image
                image.click();
                System.out.println("✅ Clicked an image.");

                // ✅ Wait for the span element that shows image count
                WebElement spanElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("/html/body/div[3]/div/div/div[2]/div/div[1]/section/div[1]/div[1]/div/button/span")
                ));

                // ✅ Extract and validate image count
                String extractedText = spanElement.getText();
                System.out.println("📝 Extracted Image Count: " + extractedText);
                int imageCount = Integer.parseInt(extractedText);

                if (imageCount > 30) {
                    System.out.println("❌ Validation Failed: Image count exceeds 30!");
                } else {
                    System.out.println("✅ Validation Passed: Image count is within limits.");
                }

                // ✅ Click the "Προβολή τηλεφώνου" button
                WebElement phoneButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]")
                ));

                System.out.println("📞 Found 'Προβολή τηλεφώνου' button.");
                phoneButton.click();

                // ✅ Wait for phone number to appear
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'Τηλέφωνα επικοινωνίας')]")));
                WebElement phoneText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//span[contains(text(), '+30')]")
                ));

                Assert.assertTrue(phoneText.isDisplayed(), "❌ Phone number did not appear!");
                System.out.println("📱 Phone number is correctly revealed.");

            } catch (Exception e) {
                System.out.println("❌ Error processing listing: " + e.getMessage());
            } finally {
                // ✅ Always close the modal before moving to the next listing
                closeModalIfExists(wait);
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

    private void closeModalIfExists(WebDriverWait wait) {
        try {
            WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.close-button-selector")  // Adjust selector as needed
            ));
            closeButton.click();
            System.out.println("🔙 Closed modal.");
        } catch (Exception e) {
            System.out.println("⚠ No close button found, navigating back instead.");
            driver.navigate().back();
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
