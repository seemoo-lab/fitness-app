package com.fitbit.api.client;

import com.fitbit.api.model.APIResourceCredentials;

public interface FitbitApiCredentialsCache {

    APIResourceCredentials getResourceCredentials(LocalUserDetail user);

    APIResourceCredentials getResourceCredentialsByTempToken(String tempToken);

    APIResourceCredentials saveResourceCredentials(LocalUserDetail user, APIResourceCredentials resourceCredentials);

    APIResourceCredentials expireResourceCredentials(LocalUserDetail user);

}
