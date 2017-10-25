package com.fitbit.api.client;

import java.util.List;

public interface FitbitApiSubscriptionStorage {

	public void save(LocalSubscriptionDetail subscription);
	public LocalSubscriptionDetail getBySubscriptionId(String subscriptionId);

	public void delete(LocalSubscriptionDetail subscription);
	
	public List<LocalSubscriptionDetail> getAllSubscriptions();
	
}
