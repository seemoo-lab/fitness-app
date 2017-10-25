package com.fitbit.api.model;

public enum APICollectionType {
    activities("activities"),
    foods("foods/log", "foods"),
    meals("meals"),
    sleep("sleep"),
    body("body"),
    user("user"),
    weight("body/weight");
    
    String urlPath;
    String subscriptionPath;

    APICollectionType(String urlPath) {
        this.urlPath = urlPath;
        this.subscriptionPath = urlPath;
    }

    APICollectionType(String urlPath, String subscriptionPath) {
        this.urlPath = urlPath;
        this.subscriptionPath = subscriptionPath;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getSubscriptionPath() {
        return subscriptionPath;
    }
}
