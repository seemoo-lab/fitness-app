package com.fitbit.api.model;

public class FitbitGroup extends FitbitResourceOwner {

	public FitbitGroup(String groupId) {
		super(groupId, ResourceOwnerType.group);
	}
	
}
