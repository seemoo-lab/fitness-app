package com.fitbit.api.model;

public enum ApiQuotaType {
    CLIENT,
    CLIENT_AND_VIEWER;

    private static int length = values().length;

    public static int numTypes() {
        return length;
    }

    public String getDescription() {
        return name().toLowerCase().replaceAll("_", " ");
    }
}
