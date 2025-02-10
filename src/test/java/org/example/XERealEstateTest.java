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
        validateContactPhoneVisibility();
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


        System.out.println("Property Titles:");
        for (WebElement title : sizeElements) {
            System.out.println(title.getText());
        }




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
     * ✅ Validates that no property ad contains more than the specified max number of images. Test
     */
    private void validateMaxImagesInAd(int maxImages) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//a[contains(@href, '/property/d/enoikiaseis-katoikion/')]//div[contains(@class, 'common-property-ad-image')]//img\n")));

        List<WebElement> propertyAds = driver.findElements(By.xpath("//a[contains(@href, '/property/d/enoikiaseis-katoikion/')]//div[contains(@class, 'common-property-ad-image')]//img\n"));

        for (WebElement ad : propertyAds) {
            //List<WebElement> images = ad.findElements(By.xpath("//img[contains(@data-testid, 'ad-gallery-image')]")); // Get all images inside carousel
            //List<WebElement> images = ad.findElements(By.xpath(".//img"));
            List<WebElement> images = ad.findElements(By.xpath("//a[contains(@href, '/property/d/enoikiaseis-katoikion/')]//div[contains(@class, 'common-property-ad-image')]//img\n"));


            int imageCount = images.size();

            // Print property ad ID and image count
            String adId = ad.getAttribute("id"); // Extracting the ID of the property ad
            System.out.println("Property Ad ID: " + adId + " | Number of Images: " + imageCount);

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







    /**private void validateContactPhoneVisibility() {


        clickElement(By.xpath("//body/main[1]/div[1]/div[1]/div[1]/div[3]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]/a[1]/div[1]/h3[1]"));

        // Wait for the ads to be loaded

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]")));

        clickElement(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]"));

        // Locate all property ads
        List<WebElement> phoneButtons = driver.findElements(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]"));

        //clickElement(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]"));



        for (WebElement ad : phoneButtons) {
            try {
                // ✅ Ensure the phone number button container is present
                Assert.assertFalse(phoneButtons.isEmpty(), "❌ 'Προβολή τηλεφώνου' button is NOT displayed!");
                System.out.println("✅ 'Προβολή τηλεφώνου' button is correctly displayed.");

                // ✅ Find all matching buttons inside the ad to avoid exceptions
                List<WebElement> revealButtons = ad.findElements(By.xpath(".//span[contains(text(),'Προβολή τηλεφώνου')]"));

                // ✅ Ensure the button exists before interacting with it
                if (!revealButtons.isEmpty()) {
                    WebElement revealButton = revealButtons.get(0); // Get the first matching button
                    Assert.assertTrue(revealButton.isDisplayed(), "❌ Phone reveal button is missing!");

                    // ✅ Click the button to reveal the phone number
                    revealButton.click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'Τηλέφωνα επικοινωνίας')]")));

                    // ✅ Verify that the phone number appears inside the pop-up
                    WebElement phonePopup = driver.findElement(By.xpath("//div[contains(text(),'Τηλέφωνα επικοινωνίας')]"));
                    //WebElement phoneText = phonePopup.findElement(By.xpath("//div[@data-testid='phones']"));
                    WebElement phoneText = driver.findElement(By.xpath("//span[contains(text(), '+30')]"));
                    //String extractedPhoneNumber = phoneText.getText().trim();
                    //Assert.assertFalse(extractedPhoneNumber.isEmpty(), "❌ Phone number is not displayed!");
                    Assert.assertTrue(phoneText.isDisplayed(), "❌ Phone number did not appear after clicking the button!");

                    System.out.println("✅ Phone number is correctly hidden and revealed upon clicking.");
                } else {
                    System.out.println("⚠️ No 'Προβολή τηλεφώνου' button found in this ad.");
                }

            } catch (Exception e) {
                System.out.println("❌ Error processing ad: " + e.getMessage());
            }
        }*/


    private void validateContactPhoneVisibility() {
        clickElement(By.xpath("//body/main[1]/div[1]/div[1]/div[1]/div[3]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]/a[1]/div[1]/h3[1]"));

        // ✅ Wait until all "Προβολή τηλεφώνου" buttons are visible before proceeding
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]")));

        // ✅ Re-fetch elements after waiting
        List<WebElement> phoneButtons = driver.findElements(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]"));

        // ✅ Ensure buttons exist before continuing
        if (phoneButtons.isEmpty()) {
            throw new AssertionError("❌ No 'Προβολή τηλεφώνου' buttons found on the page.");
        }

        for (WebElement ad : phoneButtons) {
            try {
                System.out.println("✅ 'Προβολή τηλεφώνου' button is correctly displayed.");

                // ✅ Find buttons inside the current `ad` element
                List<WebElement> revealButtons = driver.findElements(By.xpath("//span[contains(text(),'Προβολή τηλεφώνου')]"));

                if (!revealButtons.isEmpty()) {
                    WebElement revealButton = revealButtons.get(0);
                    Assert.assertTrue(revealButton.isDisplayed(), "❌ Phone reveal button is missing!");

                    // ✅ Click the button
                    revealButton.click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'Τηλέφωνα επικοινωνίας')]")));

                    // ✅ Locate the phone number and assert its existence
                    WebElement phonePopup = driver.findElement(By.xpath("//div[contains(text(),'Τηλέφωνα επικοινωνίας')]"));
                    wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//span[contains(text(), '+30')]")));
                    WebElement phoneText = driver.findElement(By.xpath("//span[contains(text(), '+30')]"));
                    Assert.assertTrue(phoneText.isDisplayed(), "❌ Phone number did not appear after clicking the button!");

                    System.out.println("✅ Phone number is correctly revealed upon clicking.");
                } else {
                    System.out.println("⚠️ No 'Προβολή τηλεφώνου' button found in this ad.");
                }

            } catch (Exception e) {
                System.out.println("❌ Error processing ad: " + e.getMessage());
            }
        }

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
