# 🏡 XE Real Estate Test Automation

This project is a **Selenium WebDriver** test suite written in **Java** for automating the search and validation of real estate rental listings on **[xe.gr](https://xe.gr/)**.

---

## 🚀 Features
- ✅ **Automates the property search process** using filters (location, price, size).
- ✅ **Handles dropdown selection dynamically** for multiple location suggestions.
- ✅ **Validates property prices and sizes** to ensure they are within defined criteria.
- ✅ **Checks if properties are sorted in descending order by price**.
- ✅ **Validates the number of images per ad** (ensuring no listing exceeds 30 images).
- ✅ **Checks if phone numbers are properly displayed** when clicking on the "View Phone" button.
- ✅ **Handles scrolling dynamically** to load all search results.
- ✅ **Uses JavaScript Executor for better interaction** with elements.

---

## 🛠 Tech Stack
- **Java** (Selenium WebDriver)
- **TestNG** (Test framework)
- **WebDriverManager** (Manages ChromeDriver)
- **Maven** (Dependency Management)
- **XPath & CSS Selectors** (Locating Elements)
- **JavaScript Executor** (For dynamic interactions)

---

## 📦 Installation & Setup
### 1️⃣ **Clone the Repository**
```sh
git clone https://github.com/tveryk/XE.git

## Ensure you have Maven installed. Run:
mvn clean install

## Test Flow
1️⃣ Navigates to xe.gr
2️⃣ Accepts Cookies
3️⃣ Searches for properties in "Παγκράτι"
4️⃣ Dynamically selects from dropdown suggestions
5️⃣ Applies filters (price, size, etc.)
6️⃣ Validates:
Property prices & sizes
Sorting order (descending)
Image count (≤ 30 per ad)
Phone number visibility

## Key Methods
🔹 searchRentalProperties()
Handles search input, dropdown selection, and filter application.
🔹 validatePropertyPrices(int min, int max)
Ensures listed prices fall within the defined range.
🔹 validatePropertySizes(int min, int max)
Checks that property sizes are correctly listed.
🔹 validateDescendingPriceSorting()
Ensures properties are sorted in descending price order.
🔹 validateImagesAndPhoneNumbers()
Checks each listing for:
Image count (≤ 30)
Phone number visibility
🔹 Utility Methods:
clickElement(By locator) → Clicks an element with JavaScript fallback.
sendKeysToElement(By locator, String value) → Enters text with JavaScript clearing.
scrollToLoadAllResults() → Dynamically loads more results.

