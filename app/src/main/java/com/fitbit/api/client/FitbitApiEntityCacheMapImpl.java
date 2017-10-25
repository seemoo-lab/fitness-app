package com.fitbit.api.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fitbit.api.model.APIResourceCredentials;

public class FitbitApiEntityCacheMapImpl implements FitbitAPIEntityCache {

	private static final Log log = LogFactory.getLog(FitbitApiEntityCacheMapImpl.class);
	
	private Map<APIResourceCredentials, Map<Object,Object>> cache =
		Collections.synchronizedMap(new HashMap<APIResourceCredentials, Map<Object,Object>>());

	protected Map<Object,Object> getCredentialsMap(APIResourceCredentials credentials) {
		Map<Object,Object> result = cache.get(credentials);
		if (null==result) {
			result = Collections.synchronizedMap(new HashMap<Object, Object>());
			cache.put(credentials, result);
		}
		return result;
	}
	
	@Override
	public Object get(APIResourceCredentials credentials, Object key) {
		
		Object result = getCredentialsMap(credentials).get(key);
		log.info("Cache get(" + credentials + ": " + key + ") => " + result);
		return result;
	}

	@Override
	public Object put(APIResourceCredentials credentials, Object key, Object value) {
		Object result = getCredentialsMap(credentials).put(key, value);
		log.info("Cache put(" + credentials + ": " + key + ", " + value + ") => " + result);
		return result;
	}

	@Override
	public Object remove(APIResourceCredentials credentials, Object key) {
		Object result = getCredentialsMap(credentials).remove(key);
		log.info("Cache remove(" + credentials + ": " + key + ") => " + result);
		return result;
	}

}
