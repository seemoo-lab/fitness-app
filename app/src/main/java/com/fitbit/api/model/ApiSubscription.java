package com.fitbit.api.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiSubscription {

    private String subscriberId;
    private String subscriptionId;
    private String ownerId;
    private ResourceOwnerType ownerType;
    private APICollectionType collectionType;

    public ApiSubscription(String subscriberId, String subscriptionId, ResourceOwnerType ownerType, String ownerId, APICollectionType collectionType) {
        this.subscriberId = subscriberId;
        this.subscriptionId = subscriptionId;
        this.ownerId = ownerId;
        this.ownerType = ownerType;
        this.collectionType = collectionType;
    }

    public ApiSubscription(JSONObject json) {
        try {
            this.subscriberId = json.getString("subscriberId");
            this.subscriptionId = json.getString("subscriptionId");
            this.ownerType = ResourceOwnerType.valueOf(json.getString("ownerType"));
			this.ownerId = json.getString("ownerId");
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

    public String getOwnerId() {
        return ownerId;
    }

    public ResourceOwnerType getOwnerType() {
        return ownerType;
    }

    public APICollectionType getCollectionType() {
        return collectionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiSubscription that = (ApiSubscription) o;

        if (collectionType != that.collectionType) return false;
        if (!ownerId.equals(that.ownerId)) return false;
        if (ownerType != that.ownerType) return false;
        if (!subscriberId.equals(that.subscriberId)) return false;
        if (!subscriptionId.equals(that.subscriptionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subscriberId.hashCode();
        result = 31 * result + subscriptionId.hashCode();
        result = 31 * result + ownerId.hashCode();
        result = 31 * result + ownerType.hashCode();
        result = 31 * result + (collectionType != null ? collectionType.hashCode() : 0);
        return result;
    }
}
