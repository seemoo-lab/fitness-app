package com.fitbit.api.client;

public class Version {
    private final static String VERSION = "1";
    private final static String TITLE = "Fitbit4J";

    public static String getVersion() {
        return VERSION;
    }

    public static void main(String[] args) {
        System.out.println(TITLE + ' ' + VERSION);
    }
}