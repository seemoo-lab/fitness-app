package com.fitbit.api.model;

public enum APIApplicationType {
	CLIENT("Client"),
	BROWSER("Browser");

    String label;

    APIApplicationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
