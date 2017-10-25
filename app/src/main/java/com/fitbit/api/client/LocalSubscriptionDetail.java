package com.fitbit.api.client;

import java.util.Date;

import com.fitbit.api.model.SubscriptionDetail;

public class LocalSubscriptionDetail {

	private SubscriptionDetail subscriptionDetail;
	private boolean knownSubscription;
	private Date lastUpdateNotificationDate;
	
	public LocalSubscriptionDetail(SubscriptionDetail subscriptionDetail, boolean knownSubscription) {
		this.subscriptionDetail = subscriptionDetail;
		this.knownSubscription = knownSubscription;
	}

	public SubscriptionDetail getSubscriptionDetail() {
		return subscriptionDetail;
	}

	public boolean isKnownSubscription() {
		return knownSubscription;
	}

	public Date getLastUpdateNotificationDate() {
		return lastUpdateNotificationDate;
	}

	public void setLastUpdateNotificationDate(Date lastUpdateNotificationDate) {
		this.lastUpdateNotificationDate = lastUpdateNotificationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((subscriptionDetail == null) ? 0 : subscriptionDetail.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		LocalSubscriptionDetail other = (LocalSubscriptionDetail) obj;
		if (subscriptionDetail == null) {
			if (other.subscriptionDetail != null) return false;
		} else if (!subscriptionDetail.equals(other.subscriptionDetail)) return false;

		return true;
	}

}
