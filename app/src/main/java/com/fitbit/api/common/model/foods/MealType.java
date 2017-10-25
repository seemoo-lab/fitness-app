package com.fitbit.api.common.model.foods;

public enum MealType {

    BEFORE_BREAKFAST((byte) 0, "Before Breakfast"),
    BREAKFAST((byte) 1, "Breakfast"),
    BEFORE_LUNCH((byte) 2, "Morning Snack"),
    LUNCH((byte) 3, "Lunch"),
    BEFORE_DINNER((byte) 4, "Afternoon Snack"),
    DINNER((byte) 5, "Dinner"),
    AFTER_DINNER((byte) 6, "After Dinner"),
    NA((byte) 7, "Anytime");

    private byte id;
    private String title;

    private MealType(byte id, String title) {
        this.id = id;
        this.title = title;
    }

    public byte getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static MealType valueOf(byte id) {
        for (MealType type : MealType.values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}