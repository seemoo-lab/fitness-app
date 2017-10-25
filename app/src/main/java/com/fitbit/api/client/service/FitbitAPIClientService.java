package com.fitbit.api.client.service;

import com.fitbit.api.APIUtil;
import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.FitbitAPISecurityException;
import com.fitbit.api.client.FitbitAPIEntityCache;
import com.fitbit.api.client.FitbitApiClientAgent;
import com.fitbit.api.client.FitbitApiCredentialsCache;
import com.fitbit.api.client.FitbitApiSubscriptionStorage;
import com.fitbit.api.client.LocalSubscriptionDetail;
import com.fitbit.api.client.LocalUserDetail;
import com.fitbit.api.client.http.AccessToken;
import com.fitbit.api.client.http.TempCredentials;
import com.fitbit.api.common.model.activities.Activities;
import com.fitbit.api.common.model.foods.Foods;
import com.fitbit.api.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class FitbitAPIClientService<C extends FitbitApiClientAgent> {
    protected static final Log log = LogFactory.getLog(FitbitAPIClientService.class);

    protected C client;

    protected FitbitAPIEntityCache entityCache;
    protected FitbitApiCredentialsCache credentialsCache;
    protected FitbitApiSubscriptionStorage subscriptionStore;
    protected String subscriberSecret;

    public FitbitAPIClientService(C client, String consumerKey, String consumerSecret,
                                  FitbitApiCredentialsCache credentialsCache, FitbitAPIEntityCache entityCache,
                                  FitbitApiSubscriptionStorage subscriptionStore) {
        this.client = client;
        client.setOAuthConsumer(consumerKey, consumerSecret);
        subscriberSecret = consumerSecret;
        this.credentialsCache = credentialsCache;
        this.entityCache = entityCache;
        this.subscriptionStore = subscriptionStore;
    }

    public C getClient() {
        return client;
    }
    
    public APIResourceCredentials getResourceCredentialsByUser(LocalUserDetail user) {
        if (null == credentialsCache) {
            return null;
        }
        return credentialsCache.getResourceCredentials(user);
    }

    public APIResourceCredentials getResourceCredentialsByTempToken(String tempToken) {
        if (null == credentialsCache) {
            return null;
        }
        return credentialsCache.getResourceCredentialsByTempToken(tempToken);
    }

    public APIResourceCredentials saveResourceCredentials(LocalUserDetail user, APIResourceCredentials resourceCredentials) {
        if (null == credentialsCache) {
            return null;
        }
        return credentialsCache.saveResourceCredentials(user, resourceCredentials);
    }

    public APIResourceCredentials expireResourceCredentials(LocalUserDetail user) {
        if (null == credentialsCache) {
            return null;
        }
        return credentialsCache.expireResourceCredentials(user);
    }

    public String getResourceOwnerAuthorizationURL(LocalUserDetail user, String callbackURL) throws FitbitAPIException {
        // Get temporary credentials. Include callback URL which the Fitbit API service will save and redirect to when
        // the user authorizes.
        TempCredentials tempCredentials = client.getOAuthTempToken(callbackURL);
        // Create and save temporary resource credentials:
        APIResourceCredentials resourceCredentials = new APIResourceCredentials(user.getUserId(), tempCredentials.getToken(), tempCredentials.getTokenSecret());
        saveResourceCredentials(user, resourceCredentials);
        // Return Fitbit URL to redirect to where the user can authorize:
        return tempCredentials.getAuthorizationURL();
    }

    public void getTokenCredentials(LocalUserDetail user) throws FitbitAPIException {
        // Get cached resource credentials:
        APIResourceCredentials resourceCredentials = getResourceCredentialsByUser(user);
        if (resourceCredentials == null) {
            throw new FitbitAPIException("User " + user.getUserId() + " does not have resource credentials. Need to grant authorization first.");
        }

        String tempToken = resourceCredentials.getTempToken();
        String tempTokenSecret = resourceCredentials.getTempTokenSecret();
        if (tempToken == null || tempTokenSecret == null) {
            throw new FitbitAPIException("Resource credentials for resource " + user.getUserId() + " are in an invalid state: temporary token or secret is null.");
        }

        // Get and save token credentials:
        AccessToken accessToken = client.getOAuthAccessToken(tempToken, tempTokenSecret, resourceCredentials.getTempTokenVerifier());
        resourceCredentials.setAccessToken(accessToken.getToken());
        resourceCredentials.setAccessTokenSecret(accessToken.getTokenSecret());
        resourceCredentials.setResourceId(accessToken.getEncodedUserId());
    }

    public Activities getActivities(LocalUserDetail user, LocalDate date) throws FitbitAPIException {
        return (Activities) getCollectionForDate(user, date, APICollectionType.activities);
    }

    public Foods getFoods(LocalUserDetail user, LocalDate date) throws FitbitAPIException {
        return (Foods) getCollectionForDate(user, date, APICollectionType.foods);
    }

    public Object getCollectionForDate(LocalUserDetail user, LocalDate date, APICollectionType type) throws FitbitAPIException {
        // Get cache key for collection:
        String cacheKey = getCacheKey(date, type);
        // Get the resource credentials:
        APIResourceCredentials credentials = credentialsCache.getResourceCredentials(user);
        // First consult the cache:
        Object result = getFromCache(user, credentials, cacheKey);
        // If not in cache, retrieve from the API service and place in cache:
        if (null == result) {
            result = client.getCollectionForDate(user, FitbitUser.CURRENT_AUTHORIZED_USER, type, date);
            putInCache(result, credentials, cacheKey);
        }
        return result;
    }

    public String getCacheKey(LocalDate date, APICollectionType type) {
        return APIUtil.constructFullUrl(
                client.getApiBaseUrl(),
                client.getApiVersion(),
                FitbitUser.CURRENT_AUTHORIZED_USER,
                type,
                date,
                APIFormat.JSON
        );
    }

    public Object getFromCache(LocalUserDetail user, APIResourceCredentials credentials, String cacheKey) {
        if (null != subscriptionStore && null != subscriptionStore.getBySubscriptionId(user.getUserId()) && null != entityCache) {
            return entityCache.get(credentials, cacheKey);
        } else {
            return null;
        }
    }

    public void putInCache(Object result, APIResourceCredentials credentials, String cacheKey) {
        if (null != entityCache) {
            entityCache.put(credentials, cacheKey, result);
        }
    }

    public ApiRateLimitStatus getClientRateLimitStatus() throws FitbitAPIException {
        return client.getClientRateLimitStatus();
    }

    public ApiRateLimitStatus getClientAndViewerRateLimitStatus(LocalUserDetail user) throws FitbitAPIException {
        return client.getClientAndViewerRateLimitStatus(user);
    }

    /**
     * Creates new subscription
     *
     * @param collectionType Collection to receive notifications from
     * @param subscriptionId ID associated with subscription
     * @return SubscriptionDetail
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail user, APICollectionType collectionType, final String subscriptionId) throws FitbitAPIException {
        if (null == subscriptionStore) {
            throw new FitbitAPIException("Can not deal with subscriptions without a place to store information about them.");
        }

        // This example application only allows a single subscription per
        // (local) user. We use the user's ID as the subscription ID to avoid
        // having to maintain a mapping.
        SubscriptionDetail result = client.subscribe(subscriberId, user, FitbitUser.CURRENT_AUTHORIZED_USER, collectionType, subscriptionId);
        if (null != result) {
            LocalSubscriptionDetail detail = new LocalSubscriptionDetail(result, true);
            subscriptionStore.save(detail);
        }
        return result;
    }

    /**
     * Removes subscription
     *
     * @param collectionType
     * @param subscriptionId
     */
    public void unsubscribe(String subscriberId, LocalUserDetail user, APICollectionType collectionType, final String subscriptionId) throws FitbitAPIException {
        if (null == subscriptionStore) {
            throw new FitbitAPIException("Can not deal with subscriptions without a place to store information about them.");
        }

        // This example application only allows a single subscription per
        // (local) user. We use the user's ID as the subscription ID to avoid
        // having to maintain a mapping.
        client.unsubscribe(subscriberId, user, FitbitUser.CURRENT_AUTHORIZED_USER, collectionType, subscriptionId);
        LocalSubscriptionDetail subscription = subscriptionStore.getBySubscriptionId(user.getUserId());
        if (null != subscription) {
            subscriptionStore.delete(subscription);
        }
    }

    public void evictUpdatedResourcesFromCache(String subscriberId, InputStream updateMessageStream, String serverSignature) throws FitbitAPIException {
        try {
            if (null == serverSignature) {
                throw new FitbitAPISecurityException("Missing signature.");
            }

            String updateMessage = APIUtil.inputStreamToString(updateMessageStream);

            String ourSignature = APIUtil.generateSignature(updateMessage, subscriberSecret);
            if (null == ourSignature || !ourSignature.equals(serverSignature)) {
                throw new FitbitAPISecurityException("Signatures do not match, given " + serverSignature);
            }

            UpdateNotification notification = new UpdateNotification(new JSONArray(updateMessage));

            int i = 0;
            for (UpdatedResource resource : notification.getUpdatedResources()) {
                //noinspection UnnecessaryParentheses,ValueOfIncrementOrDecrementUsed
                log.info("Processing update notification " + (++i) + " for subscription " + resource.getSubscriptionId());

                LocalSubscriptionDetail sub = subscriptionStore.getBySubscriptionId(resource.getSubscriptionId());
                if (null == sub) {
                    log.info("Nothing known about subscription " + resource.getSubscriptionId() + ", creating placeholder.");

                    sub = new LocalSubscriptionDetail(
                            new SubscriptionDetail(
                                    subscriberId,
                                    resource.getSubscriptionId(),
                                    resource.getOwner(),
                                    resource.getCollectionType()
                            ),
                            false
                    );
                    subscriptionStore.save(sub);
                }

                sub.setLastUpdateNotificationDate(new Date());

                APIResourceCredentials credentials = credentialsCache.getResourceCredentials(new LocalUserDetail(resource.getSubscriptionId()));

                String cacheKeyWithUserId =
                        APIUtil.constructFullUrl(
                                client.getApiBaseUrl(),
                                client.getApiVersion(),
                                resource.getOwner(),
                                resource.getCollectionType(),
                                resource.getDate(),
                                APIFormat.JSON
                        );

                Activities entity = (Activities) entityCache.get(credentials, cacheKeyWithUserId);
                if (null != entity) {
                    log.info("Evicting entity " + cacheKeyWithUserId);
                    entityCache.remove(credentials, cacheKeyWithUserId);
                } else {
                    log.info("There is no cached version of entity " + cacheKeyWithUserId);
                }

                String cacheKeyWithPlaceholder =
                        APIUtil.constructFullUrl(
                                client.getApiBaseUrl(),
                                client.getApiVersion(),
                                FitbitUser.CURRENT_AUTHORIZED_USER,
                                resource.getCollectionType(),
                                resource.getDate(),
                                APIFormat.JSON
                        );

                entity = (Activities) entityCache.get(credentials, cacheKeyWithPlaceholder);
                if (null != entity) {
                    log.info("Evicting entity " + cacheKeyWithPlaceholder);
                    entityCache.remove(credentials, cacheKeyWithPlaceholder);
                } else {
                    log.info("There is no cached version of entity " + cacheKeyWithPlaceholder);
                }
            }
        } catch (IOException e) {
            throw new FitbitAPIException("Notification stream is malformed: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Unable to parse update message: " + e, e);
        }
    }

}
