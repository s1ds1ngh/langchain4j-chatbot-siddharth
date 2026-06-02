package com.enterprise.chatbot.service.tools;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * LangChain4j Tool: Product Information Lookup
 *
 * The AI assistant will automatically invoke these methods when the user asks
 * about products or inventory. LangChain4j handles the function-calling
 * protocol transparently — no explicit routing code is needed.
 *
 * Demonstrates the @Tool annotation for function/tool calling.
 */
@Component
public class ProductTool {

    private static final Logger log = LoggerFactory.getLogger(ProductTool.class);

    // ── Simulated product catalogue (in-memory) ───────────────────────────────

    private static final Map<String, Map<String, Object>> PRODUCT_CATALOGUE = new HashMap<>();

    static {
        PRODUCT_CATALOGUE.put("laptop", Map.of(
                "name", "ProBook X15 Laptop",
                "price", "$1,299.99",
                "brand", "TechCorp",
                "specs", "Intel Core i7, 16GB RAM, 512GB SSD, 15.6\" FHD Display",
                "category", "Electronics",
                "rating", "4.7/5"
        ));
        PRODUCT_CATALOGUE.put("headphones", Map.of(
                "name", "SoundElite Pro Headphones",
                "price", "$249.99",
                "brand", "AudioMax",
                "specs", "Active Noise Cancellation, 40hr battery, Bluetooth 5.3",
                "category", "Electronics",
                "rating", "4.8/5"
        ));
        PRODUCT_CATALOGUE.put("keyboard", Map.of(
                "name", "MechType K90 Keyboard",
                "price", "$149.99",
                "brand", "KeyForge",
                "specs", "Mechanical switches, RGB backlit, TKL layout, USB-C",
                "category", "Peripherals",
                "rating", "4.6/5"
        ));
        PRODUCT_CATALOGUE.put("monitor", Map.of(
                "name", "ViewPro 27\" 4K Monitor",
                "price", "$549.99",
                "brand", "VisualTech",
                "specs", "27\" 4K UHD, 144Hz, IPS panel, HDR400, USB-C hub",
                "category", "Electronics",
                "rating", "4.9/5"
        ));
        PRODUCT_CATALOGUE.put("mouse", Map.of(
                "name", "SwiftClick G500 Mouse",
                "price", "$79.99",
                "brand", "PrecisionGear",
                "specs", "25,600 DPI, 11 programmable buttons, wireless, ergonomic",
                "category", "Peripherals",
                "rating", "4.5/5"
        ));
    }

    // ── Inventory (simulated stock levels) ───────────────────────────────────

    private static final Map<String, Integer> INVENTORY = Map.of(
            "laptop", 12,
            "headphones", 45,
            "keyboard", 0,
            "monitor", 7,
            "mouse", 89
    );

    // ── Tool Methods ─────────────────────────────────────────────────────────

    /**
     * Fetches detailed product information from the catalogue.
     *
     * @param productName The name/type of the product to look up (e.g., "laptop", "headphones")
     * @return A formatted string with product details, or a not-found message.
     */
    @Tool("Fetches detailed information about a product from the enterprise catalogue. " +
          "Use this when the user asks about product specifications, price, brand, or features. " +
          "Supported products: laptop, headphones, keyboard, monitor, mouse.")
    public String getProductDetails(String productName) {
        log.info("[TOOL CALLED] getProductDetails(\"{}\")", productName);

        String key = productName.toLowerCase().trim();
        Map<String, Object> product = PRODUCT_CATALOGUE.get(key);

        if (product == null) {
            // Try partial match
            for (String catalogueKey : PRODUCT_CATALOGUE.keySet()) {
                if (catalogueKey.contains(key) || key.contains(catalogueKey)) {
                    product = PRODUCT_CATALOGUE.get(catalogueKey);
                    break;
                }
            }
        }

        if (product == null) {
            return "Product '" + productName + "' was not found in the catalogue. " +
                   "Available products are: laptop, headphones, keyboard, monitor, mouse.";
        }

        return String.format(
                "Product: %s | Brand: %s | Price: %s | Specs: %s | Category: %s | Rating: %s",
                product.get("name"), product.get("brand"), product.get("price"),
                product.get("specs"), product.get("category"), product.get("rating")
        );
    }

    /**
     * Checks the current stock level for a product.
     *
     * @param productName The name/type of the product to check inventory for.
     * @return A human-readable inventory status string.
     */
    @Tool("Checks the current inventory / stock level for a product. " +
          "Use this when the user asks if a product is available, in stock, or how many units remain.")
    public String checkInventory(String productName) {
        log.info("[TOOL CALLED] checkInventory(\"{}\")", productName);

        String key = productName.toLowerCase().trim();
        Integer stock = INVENTORY.get(key);

        if (stock == null) {
            for (String invKey : INVENTORY.keySet()) {
                if (invKey.contains(key) || key.contains(invKey)) {
                    stock = INVENTORY.get(invKey);
                    break;
                }
            }
        }

        if (stock == null) {
            return "Inventory information for '" + productName + "' is not available.";
        }

        if (stock == 0) {
            return productName + " is currently OUT OF STOCK. We expect restocking within 5-7 business days.";
        } else if (stock <= 10) {
            return productName + " has limited stock: only " + stock + " units remaining. Order soon!";
        } else {
            return productName + " is IN STOCK with " + stock + " units available.";
        }
    }
}
