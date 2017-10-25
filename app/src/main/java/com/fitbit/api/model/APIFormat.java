package com.fitbit.api.model;

public enum APIFormat {
	XML("xml", "text/xml"),
	JSON("json", "text/javascript");
	
	private String label;
	private String defaultContentType;

	private APIFormat(String label, String defaultContentType) {
		this.label = label;
		this.defaultContentType = defaultContentType;
	}

    public String getLabel() {
        return label;
    }

    public String getDefaultContentType() {
		return defaultContentType;
	}
}
