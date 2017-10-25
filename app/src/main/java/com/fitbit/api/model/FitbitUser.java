package com.fitbit.api.model;

public class FitbitUser extends FitbitResourceOwner {

	public static final FitbitUser CURRENT_AUTHORIZED_USER = new FitbitUser("-");
	
	public FitbitUser(String userId) {
		super(userId, ResourceOwnerType.user);
	}
	
}
