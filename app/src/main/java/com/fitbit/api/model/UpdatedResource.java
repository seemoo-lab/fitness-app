package com.fitbit.api.model;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import com.fitbit.api.APIUtil;

public class UpdatedResource {

	private String subscriptionId;
	
	private FitbitResourceOwner owner;
	private APICollectionType collectionType;
	private LocalDate date;
	
	public UpdatedResource(String subscriptionId, FitbitResourceOwner owner, APICollectionType collectionType, LocalDate date) {
		this.subscriptionId = subscriptionId;
		this.owner = owner;
		this.collectionType = collectionType;
		this.date = date;
		verifyState();
	}
	
	public UpdatedResource(JSONObject json) {
		try {
			this.subscriptionId = json.getString("subscriptionId");
			this.owner = FitbitResourceOwner.fromJson(json);
			if (json.has("collectionType")) {
				this.collectionType = APICollectionType.valueOf(json.getString("collectionType"));
			}
			if (json.has("date")) {
				this.date = APIUtil.parseDate(json.getString("date"));
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException("Given JSON object does not contain all required elements.", e);
		}
		verifyState();
	}

	private void verifyState() {
		if (null==subscriptionId || subscriptionId.length() < 1) {
			throw new IllegalArgumentException("subscriptionId is required");
		}
		if (null==owner || owner.getId().length() < 1) {
			throw new IllegalArgumentException("owner is required");
		}
	}
	
	public String getSubscriptionId() {
		return subscriptionId;
	}

    @ApiTransient
	public FitbitResourceOwner getOwner() {
		return owner;
	}
	
	public String getOwnerId() {
		return getOwner().getId();
	}
	
	public ResourceOwnerType getOwnerType() {
		return getOwner().getResourceOwnerType();
	}

	public APICollectionType getCollectionType() {
		return collectionType;
	}

	public LocalDate getDate() {
		return date;
	}
	
}
