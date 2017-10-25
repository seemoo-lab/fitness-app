package com.fitbit.api.model;

public enum APIAccessType {
    READ((byte) 0, "Read-only", "read"),
    READ_WRITE((byte) 1, "Read & Write", "read and write");

    private byte id;
    String description;
    String label;

    APIAccessType(byte id, String label, String description) {
        this.id = id;
        this.description = description;
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public static APIAccessType valueOf(byte id) {
        for (APIAccessType level : APIAccessType.values()) {
            if (level.id == id) return level;
        }
        return null;
    }

}
