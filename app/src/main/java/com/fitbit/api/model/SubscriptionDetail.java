package com.fitbit.api.model;

import org.json.JSONException;
import org.json.JSONObject;


public class SubscriptionDetail {

	private String subscriberId;
	private String subscriptionId;
	private FitbitResourceOwner owner;
	private APICollectionType collectionType;
	
	public SubscriptionDetail(String subscriberId, String subscriptionId, FitbitResourceOwner owner, APICollectionType collectionType) {
		this.subscriberId = subscriberId;
		this.subscriptionId = subscriptionId;
		this.owner = owner;
		this.collectionType = collectionType;
	}
	
	public SubscriptionDetail(JSONObject json) {
		try {
			this.subscriberId = json.getString("subscriberId");
			this.subscriptionId = json.getString("subscriptionId");
			this.owner = FitbitResourceOwner.fromJson(json);
			if (json.has("collectionType")) {
				this.collectionType = APICollectionType.valueOf(json.getString("collectionType"));
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException("Given JSON object '" + json + "' does not contain all required elements: " + e, e);
		}
	}
	
	public String getSubscriberId() {
		return subscriberId;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public FitbitResourceOwner getOwner() {
		return owner;
	}

	public APICollectionType getCollectionType() {
		return collectionType;
	}

	@Override
	public int hashCode() {
		// Auto generated
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((collectionType == null) ? 0 : collectionType.hashCode());
		result = prime * result
				+ ((subscriberId == null) ? 0 : subscriberId.hashCode());
		result = prime * result
				+ ((subscriptionId == null) ? 0 : subscriptionId.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// Auto generated
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		SubscriptionDetail other = (SubscriptionDetail) obj;
		if (collectionType == null) {
			if (other.collectionType != null) return false;
		} else if (!collectionType.equals(other.collectionType)) return false;
		
		if (subscriberId == null) {
			if (other.subscriberId != null)	return false;
		} else if (!subscriberId.equals(other.subscriberId)) return false;

		if (subscriptionId == null) {
			if (other.subscriptionId != null) return false;
		} else if (!subscriptionId.equals(other.subscriptionId)) return false;
		
		if (owner == null) {
			if (other.owner != null) return false;
		} else if (!owner.equals(other.owner)) return false;
		
		return true;
	}
	
}
