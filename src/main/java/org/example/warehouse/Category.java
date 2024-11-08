package org.example.warehouse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Category {
    private static final Map<String, Category> CATEGORIES = new ConcurrentHashMap<>();
    private final String name;

    private Category(String name) {
        this.name = name;
    }

    public static Category of(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Category name can't be null");
        }
        String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);
        return CATEGORIES.computeIfAbsent(formattedName, Category::new);
    }

    public String getName() {
        return name;
    }
}
