package com.fitbit.api.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fitbit.api.model.APIResourceCredentials;


public class FitbitApiCredentialsCacheMapImpl implements FitbitApiCredentialsCache {

    private final Map<LocalUserDetail, APIResourceCredentials> mapUserIdResourceCredentials = 
    					Collections.synchronizedMap(new HashMap<LocalUserDetail, APIResourceCredentials>());
    private final Map<String, APIResourceCredentials> mapTempTokenResourceCredentials = 
    					Collections.synchronizedMap(new HashMap<String, APIResourceCredentials>());

    @Override
    public APIResourceCredentials getResourceCredentials(LocalUserDetail user) {
        return mapUserIdResourceCredentials.get(user);
    }

    @Override
    public APIResourceCredentials getResourceCredentialsByTempToken(String tempToken) {
        return mapTempTokenResourceCredentials.get(tempToken);
    }

    @Override
    public APIResourceCredentials saveResourceCredentials(LocalUserDetail user, APIResourceCredentials resourceCredentials) {
        mapTempTokenResourceCredentials.put(resourceCredentials.getTempToken(), resourceCredentials);
        return mapUserIdResourceCredentials.put(user, resourceCredentials);
    }

    @Override
    public APIResourceCredentials expireResourceCredentials(LocalUserDetail user) {
        APIResourceCredentials resourceCredentials = mapUserIdResourceCredentials.get(user);
        if (resourceCredentials != null) {
            mapTempTokenResourceCredentials.remove(resourceCredentials.getTempToken());
        }
        return mapUserIdResourceCredentials.remove(user);
    }

}
