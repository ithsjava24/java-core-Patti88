package org.example.warehouse;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Warehouse {
    private static final Map<String, Warehouse> WAREHOUSES = new ConcurrentHashMap<>();
    private static final List<ProductRecord> PRODUCTS = Collections.synchronizedList(new ArrayList<>());
    private static final Map<UUID, ProductRecord> PRODUCT_MAP = new ConcurrentHashMap<>();
    private static final Set<ProductRecord> CHANGED_PRODUCTS = Collections.synchronizedSet(new HashSet<>());

    private final String name;


    private Warehouse(String name) {
        this.name = name;
    }

    public static Warehouse getInstance() {
        Warehouse.clearAllProducts();
        return new Warehouse("");
    }

    public static Warehouse getInstance(String name) {
        // Check if a Warehouse instance already exists for the provided name
        if (WAREHOUSES.containsKey(name)) {
            return WAREHOUSES.get(name);
        }{
            // Create a new instance if it does not exist
            Warehouse newWarehouse = new Warehouse(name);
            WAREHOUSES.put(name, newWarehouse);
            return newWarehouse;
        }
    }

    public boolean isEmpty() {
        return PRODUCTS.isEmpty();
    }

    public List<ProductRecord> getProducts() {
        return Collections.unmodifiableList(PRODUCTS);
    }

    public Optional<ProductRecord> getProductById(UUID uuid) {
        return Optional.ofNullable(PRODUCT_MAP.get(uuid));
    }

    public ProductRecord addProduct(UUID uuid, String name, Category category, BigDecimal price) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name can't be null or empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category can't be null.");
        }
        if (uuid == null) {
            uuid = UUID.randomUUID();
        } else if (PRODUCT_MAP.containsKey(uuid)) {
            throw new IllegalArgumentException("Product with that id already exists, use updateProduct for updates.");
        }
        if (price == null) {
            price = BigDecimal.ZERO;
        }

        ProductRecord product = new ProductRecord(uuid, name, category, price);
        PRODUCTS.add(product);
        PRODUCT_MAP.put(uuid, product);
        return product;
    }

    public void updateProductPrice(UUID uuid, BigDecimal newPrice) {
        if (!PRODUCT_MAP.containsKey(uuid)) {
            throw new IllegalArgumentException("Product with that id doesn't exist.");
        }
        ProductRecord existingProduct = PRODUCT_MAP.get(uuid);
        ProductRecord updatedProduct = new ProductRecord(uuid, existingProduct.name(), existingProduct.category(), newPrice);

        int index = PRODUCTS.indexOf(existingProduct);
        if (index != -1) {
            PRODUCTS.set(index, updatedProduct);
        } else {
            PRODUCTS.add(updatedProduct);
        }
        PRODUCT_MAP.put(uuid, updatedProduct);

        CHANGED_PRODUCTS.add(existingProduct);
    }

    public static void clearAllProducts() {
        PRODUCTS.clear();
        PRODUCT_MAP.clear();
        CHANGED_PRODUCTS.clear();
    }

    public Set<ProductRecord> getChangedProducts() {
        return Collections.unmodifiableSet(CHANGED_PRODUCTS);
    }

    public Map<Category, List<ProductRecord>> getProductsGroupedByCategories() {
        return PRODUCTS.stream().collect(Collectors.groupingBy(ProductRecord::category));
    }

    public List<ProductRecord> getProductsBy(Category category) {
        return PRODUCTS.stream()
                .filter(p -> p.category().equals(category))
                .collect(Collectors.toList());
    }
}
