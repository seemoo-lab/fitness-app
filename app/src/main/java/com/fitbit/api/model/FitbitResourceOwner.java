package com.fitbit.api.model;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class FitbitResourceOwner {

	private final String id;
	private final ResourceOwnerType resourceOwnerType;

	
	public static FitbitResourceOwner fromJson(JSONObject json) {
		try {
			ResourceOwnerType type = ResourceOwnerType.valueOf(json.getString("ownerType"));
			String id = json.getString("ownerId");
			return FitbitResourceOwner.fromIdentifier(type, id);
		} catch (JSONException e) {
			throw new IllegalArgumentException("Unable to parse JSON " + json);		
		}

	}
	
	public static FitbitResourceOwner fromIdentifier(ResourceOwnerType resourceOwnerType, String id) {
		switch (resourceOwnerType) {
			case group:
				return new FitbitGroup(id);
				
			case user:
				return new FitbitUser(id);
		}
		throw new IllegalArgumentException("Unknown type " + resourceOwnerType);
	}

	
	FitbitResourceOwner(String id, ResourceOwnerType resourceOwnerType) {
		this.id = id;
		this.resourceOwnerType = resourceOwnerType;
	}
	
	public ResourceOwnerType getResourceOwnerType() {
		return resourceOwnerType;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (resourceOwnerType == null ? 0 : resourceOwnerType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FitbitResourceOwner other = (FitbitResourceOwner) obj;
		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;

		if (resourceOwnerType == null) {
			if (other.resourceOwnerType != null) return false;
		} else if (!resourceOwnerType.equals(other.resourceOwnerType)) return false;
        
		return true;
	}
	
}
