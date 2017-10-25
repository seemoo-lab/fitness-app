package com.fitbit.api.model;

public enum APIVersion {

	BETA_1;
	
	private String version;

	private APIVersion() {
		version = name().replaceAll("^.*_([^_]+)$", "$1");
	}

	public String getVersion() {
		return version;
	}
	
	public static APIVersion fromVersion(String v) {
		return APIVersion.valueOf("BETA_" + v);
	}
}
