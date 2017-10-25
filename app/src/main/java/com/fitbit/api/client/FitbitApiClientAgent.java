package com.fitbit.api.client;

import com.fitbit.api.APIUtil;
import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.*;
import com.fitbit.api.common.model.achievement.Achievements;
import com.fitbit.api.common.model.achievement.LifetimeAchievements;
import com.fitbit.api.common.model.activities.*;
import com.fitbit.api.common.model.body.*;
import com.fitbit.api.common.model.bp.Bp;
import com.fitbit.api.common.model.bp.BpLog;
import com.fitbit.api.common.model.devices.*;
import com.fitbit.api.common.model.foods.*;
import com.fitbit.api.common.model.glucose.Glucose;
import com.fitbit.api.common.model.heart.Heart;
import com.fitbit.api.common.model.heart.HeartLog;
import com.fitbit.api.common.model.sleep.Sleep;
import com.fitbit.api.common.model.sleep.SleepLog;
import com.fitbit.api.common.model.timeseries.*;
import com.fitbit.api.common.model.units.UnitSystem;
import com.fitbit.api.common.model.units.VolumeUnits;
import com.fitbit.api.common.model.user.FriendStats;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.common.service.FitbitApiService;
import com.fitbit.api.model.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;


@SuppressWarnings({"NonPrivateFieldAccessedInSynchronizedContext"})
public class FitbitApiClientAgent extends FitbitAPIClientSupport implements Serializable {
    private static final FitbitApiCredentialsCache DEFAULT_CREDENTIALS_CACHE = new FitbitApiCredentialsCacheMapImpl();

    private static final String DEFAULT_API_BASE_URL = "api.fitbit.com";
    private static final String DEFAULT_WEB_BASE_URL = "https://www.fitbit.com";
    private static final long serialVersionUID = -1486360080128882436L;
    protected static final String SUBSCRIBER_ID_HEADER_NAME = "X-Fitbit-Subscriber-Id";

    private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private String apiBaseUrl = DEFAULT_API_BASE_URL;
    private APIVersion apiVersion = APIVersion.BETA_1;

    private FitbitApiCredentialsCache credentialsCache;


    /**
     * Default constructor. Creates FitbitApiClientAgent with default API hosts and credentials cache.
     */
    public FitbitApiClientAgent() {
        this(DEFAULT_API_BASE_URL, DEFAULT_WEB_BASE_URL, (FitbitApiCredentialsCache) null);
    }

    /**
     * Creates FitbitApiClientAgent with custom API hosts and credentials cache.
     *
     * @param apiBaseUrl e.g. api.fitbit.com
     * @param webBaseUrl e.g. https://www.fitbit.com
     * @param credentialsCache Credentials cache
     *
     * @see <a href="http://wiki.fitbit.com/display/API/API-Client-Reference-App">Fitbit API: API-Client-Reference-App</a>
     */
    public FitbitApiClientAgent(String apiBaseUrl, String webBaseUrl, FitbitApiCredentialsCache credentialsCache) {
        this("https://" + apiBaseUrl + "/oauth/request_token", webBaseUrl + "/oauth/authorize", "https://" + apiBaseUrl + "/oauth/access_token");
        this.apiBaseUrl = apiBaseUrl;
        if (null == credentialsCache) {
            this.credentialsCache = DEFAULT_CREDENTIALS_CACHE;
        } else {
            this.credentialsCache = credentialsCache;
        }
    }

    /**
     * @param requestTokenURL e.g. https://api.fitbit.com/oauth/request_token
     * @param authorizationURL e.g. https://www.fitbit.com/oauth/authorize
     * @param accessTokenURL https://api.fitbit.com/oauth/access_token
     *
     * @see <a href="http://wiki.fitbit.com/display/API/API-Client-Reference-App">Fitbit API: API-Client-Reference-App</a>
     */
    public FitbitApiClientAgent(String requestTokenURL, String authorizationURL, String accessTokenURL) {
        super();
        init(requestTokenURL, authorizationURL, accessTokenURL);
    }

    private void init(String requestTokenURL, String authorizationURL, String accessTokenURL) {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        http.setRequestTokenURL(requestTokenURL);
        http.setAuthorizationURL(authorizationURL);
        http.setAccessTokenURL(accessTokenURL);
    }

    /**
     * Returns the base API URL
     *
     * @return the base API URL
     */
    public String getApiBaseUrl() {
        return "https://" + apiBaseUrl;
    }

    /**
     * Returns the base API SSL URL
     *
     * @return the secured base API URL
     */
    public String getApiBaseSecuredUrl() {
        return "https://" + apiBaseUrl;
    }

    /**
     * Returns currently used API version
     *
     * @return API version
     */
    public APIVersion getApiVersion() {
        return apiVersion;
    }

    /**
     * Retrieves a request token
     *
     * @return retrieved request token@since Fitbit 1.0
     *
     * @throws FitbitAPIException when Fitbit API service or network is unavailable
     * @see <a href="http://wiki.fitbit.com/display/API/OAuth-Authentication-API">Fitbit API: OAuth-Authentication-API<</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 - 6.1.  Obtaining an Unauthorized Request Token</a>
     */
    public TempCredentials getOAuthTempToken() throws FitbitAPIException {
        return http.getOAuthRequestToken();
    }

    /**
     * Retrieves a request token, providing custom callback url
     *
     * @return retrieved request token@since Fitbit 1.0
     *
     * @throws FitbitAPIException when Fitbit API service or network is unavailable
     * @see <a href="http://wiki.fitbit.com/display/API/OAuth-Authentication-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 - 6.1.  Obtaining an Unauthorized Request Token</a>
     */
    public TempCredentials getOAuthTempToken(String callback_url) throws FitbitAPIException {
        return http.getOauthRequestToken(callback_url);
    }

    /**
     * Retrieves an access token associated with the supplied request token.
     *
     * @param tempToken the request token
     *
     * @return access token associated with the supplied request token.
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable, or the user has not authorized
     * @see <a href="http://wiki.fitbit.com/OAuth-Authentication-API">Fitbit API: OAuth-Authentication-API<</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public synchronized AccessToken getOAuthAccessToken(TempCredentials tempToken) throws FitbitAPIException {
        return http.getOAuthAccessToken(tempToken);
    }

    /**
     * Retrieves an access token associated with the supplied request token and retrieved pin, sets userId.
     *
     * @param tempToken the request token
     * @param pin pin
     *
     * @return access token associsted with the supplied request token.
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable, or the user has not authorized
     * @see <a href="http://wiki.fitbit.com/OAuth-Authenticaion-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public synchronized AccessToken getOAuthAccessToken(TempCredentials tempToken, String pin) throws FitbitAPIException {
        AccessToken accessToken = http.getOAuthAccessToken(tempToken, pin);
        setUserId(accessToken.getEncodedUserId());
        return accessToken;
    }

    /**
     * Retrieves an access token associated with the supplied request token, retrieved tokenSecret and oauth_verifier or pin
     *
     * @param token request token
     * @param tokenSecret request token secret
     * @param oauth_verifier oauth_verifier or pin
     *
     * @return access token associsted with the supplied request token.
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable, or the user has not authorized
     * @see <a href="http://wiki.fitbit.com/OAuth-Authenticaion-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public synchronized AccessToken getOAuthAccessToken(String token, String tokenSecret, String oauth_verifier) throws FitbitAPIException {
        return http.getOAuthAccessToken(token, tokenSecret, oauth_verifier);
    }

    /**
     * Sets the access token
     *
     * @param accessToken access token
     *
     * @see <a href="http://wiki.fitbit.com/OAuth-Authenticaion-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public void setOAuthAccessToken(AccessToken accessToken) {
        http.setOAuthAccessToken(accessToken);
    }

    /**
     * Sets the access token and secret
     *
     * @param token access token
     * @param tokenSecret access token secret
     */
    public void setOAuthAccessToken(String token, String tokenSecret) {
        setOAuthAccessToken(new AccessToken(token, tokenSecret));
    }

    /**
     * Sets the access token and secret
     *
     * @param token access token
     * @param tokenSecret access token secret
     * @param encodedUserId userId
     */
    public void setOAuthAccessToken(String token, String tokenSecret, String encodedUserId) {
        setOAuthAccessToken(new AccessToken(token, tokenSecret));
    }

    /**
     * Sets the OAuth consumer credentials
     *
     * @param consumerKey consumer key
     * @param consumerKey consumer secret
     */
    public synchronized void setOAuthConsumer(String consumerKey, String consumerSecret) {
        http.setOAuthConsumer(consumerKey, consumerSecret);
    }

    /**
     * Sets id of a default subscriber for subscription requests
     *
     * @param subscriberId default subscriber id
     *
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Configureyouraccountwithasubscriberendpoint">Fitbit API: Subscriptions-API</a>
     */
    protected void setSubscriberId(String subscriberId) {
        if (null != subscriberId) {
            http.setRequestHeader(SUBSCRIBER_ID_HEADER_NAME, subscriberId);
        }
    }

    /**
     * Retrieves credentials cache
     *
     * @return credentials cache
     */
    public FitbitApiCredentialsCache getCredentialsCache() {
        return credentialsCache;
    }

    /**
     * Get user's activity statistics (lifetime and bests)
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return user's activity statistics
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activity-Stats">Fitbit API: API-Get-Activity-Stats</a>
     */
    public Achievements getAchievements(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/activities.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/activities", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Achievements.constructAchievements(res);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing achievements: " + e, e);
        }
    }

    /**
     * Get user's lifetime activity statistics
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return user's activity statistics
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activity-Stats">Fitbit API: API-Get-Activity-Stats</a>
     */
    @Deprecated
    public LifetimeAchievements getActivitiesAchievements(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/activities.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/activities", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new LifetimeAchievements(res.asJSONObject().getJSONObject("lifetime"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing lifetime achievements: " + e, e);
        }
    }

    /**
     * Get a summary and list of a user's activities and activity log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data dor
     *
     * @return activities for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activities">Fitbit API: API-Get-Activities</a>
     */
    public Activities getActivities(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/date/2010-02-25.json
        Response res = getCollectionResponseForDate(localUser, fitbitUser, APICollectionType.activities, date);
        throwExceptionIfError(res);
        return Activities.constructActivities(res);
    }

    /**
     * Get a list of a user's favorite activities. The activity id contained in the record retrieved can be used to log the activity
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's favorite activities
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Favorite-Activities">Fitbit API: API-Get-Favorite-Activities</a>
     */
    public List<ActivityReference> getFavoriteActivities(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/favorite.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.activities, ApiCollectionProperty.favorite);
        throwExceptionIfError(res);
        return ActivityReference.constructActivityReferenceList(res);
    }

    /**
     * Get a list of a user's recent activities. The activity id contained in the record retrieved can be used to log the activity
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's recent activities
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Recent-Activities">Fitbit API: API-Get-Recent-Activities</a>
     */
    public List<LoggedActivityReference> getRecentActivities(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/recent.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.activities, ApiCollectionProperty.recent);
        throwExceptionIfError(res);
        return LoggedActivityReference.constructLoggedActivityReferenceList(res);
    }

    /**
     * Get a list of a user's frequent activities. The activity id contained in the record retrieved can be used to log the activity
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's frequent activities
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Frequent-Activities">Fitbit API: API-Get-Frequent-Activities</a>
     */
    public List<LoggedActivityReference> getFrequentActivities(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/recent.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.activities, ApiCollectionProperty.frequent);
        throwExceptionIfError(res);
        return LoggedActivityReference.constructLoggedActivityReferenceList(res);
    }

    /**
     * Create log entry for an activity
     *
     * @param localUser authorized user
     * @param activityId Activity id
     * @param steps Start time
     * @param durationMillis Duration
     * @param distance Distance
     * @param date Log entry date
     * @param startTime Start time
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser,
                            long activityId,
                            int steps,
                            int durationMillis,
                            float distance,
                            LocalDate date,
                            LocalTime startTime) throws FitbitAPIException {

        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("activityId", activityId));
        params.add(new PostParameter("steps", steps));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("distance", distance));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));

        return logActivity(localUser, params);
    }

    /**
     * Create log entry for an activity
     *
     * @param localUser authorized user
     * @param activityId Activity id
     * @param steps Start time
     * @param durationMillis Duration
     * @param distance Distance
     * @param distanceUnit distance measurement unit;
     * @param date Log entry date
     * @param startTime Start time
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser,
                            long activityId,
                            int steps,
                            int durationMillis,
                            float distance,
                            String distanceUnit,
                            LocalDate date,
                            LocalTime startTime) throws FitbitAPIException {

        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("activityId", activityId));
        params.add(new PostParameter("steps", steps));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("distance", distance));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("distanceUnit", distanceUnit));

        return logActivity(localUser, params);
    }

    /**
     * Create log entry for an activity
     *
     * @param localUser authorized user
     * @param activityId Activity id
     * @param steps Start time
     * @param durationMillis Duration
     * @param distance Distance
     * @param distanceUnit distance measurement unit;
     * @param date Log entry date
     * @param startTime Start time
     * @param manualCalories manual calories
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser,
                            long activityId,
                            int steps,
                            int durationMillis,
                            float distance,
                            String distanceUnit,
                            LocalDate date,
                            LocalTime startTime,
                            int manualCalories) throws FitbitAPIException {

        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("activityId", activityId));
        params.add(new PostParameter("steps", steps));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("distance", distance));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("manualCalories", manualCalories));
        params.add(new PostParameter("distanceUnit", distanceUnit));

        return logActivity(localUser, params);
    }

    /**
     * Create log entry for a custom activity
     *
     * @param localUser authorized user
     * @param activityName Activity name
     * @param durationMillis Duration
     * @param date Log entry date
     * @param startTime Start time
     * @param manualCalories manual calories
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser,
                            String activityName,
                            int durationMillis,
                            LocalDate date,
                            LocalTime startTime,
                            int manualCalories) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("activityName", activityName));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("manualCalories", manualCalories));

        return logActivity(localUser, params);
    }

    /**
     * Create log entry for a custom activity
     *
     * @param localUser authorized user
     * @param activityName Activity name
     * @param durationMillis Duration
     * @param distance Distance
     * @param date Log entry date
     * @param startTime Start time
     * @param manualCalories manual calories
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser,
                            String activityName,
                            int durationMillis,
                            float distance,
                            LocalDate date,
                            LocalTime startTime,
                            int manualCalories) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(6);
        params.add(new PostParameter("activityName", activityName));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("distance", distance));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("manualCalories", manualCalories));

        return logActivity(localUser, params);
    }

    /**
     * Create log entry for a custom activity
     *
     * @param localUser authorized user
     * @param activityName Activity name
     * @param durationMillis Duration
     * @param distance Distance
     * @param distanceUnit distance measurement unit;
     * @param date Log entry date
     * @param startTime Start time
     * @param manualCalories manual calories
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser,
                            String activityName,
                            int durationMillis,
                            float distance,
                            String distanceUnit,
                            LocalDate date,
                            LocalTime startTime,
                            int manualCalories) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(7);
        params.add(new PostParameter("activityName", activityName));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("distance", distance));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("manualCalories", manualCalories));
        params.add(new PostParameter("distanceUnit", distanceUnit));

        return logActivity(localUser, params);
    }


    /**
     * Create log entry for an activity
     *
     * @param localUser authorized user
     * @param params POST request parameters
     *
     * @return new activity log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public ActivityLog logActivity(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/activities.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities", APIFormat.JSON);

        Response res;
        try {
            res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error creating activity: " + e, e);
        }

        if (res.getStatusCode() != HttpServletResponse.SC_CREATED) {
            throw new FitbitAPIException("Error creating activity: " + res.getStatusCode());
        }
        try {
            return new ActivityLog(res.asJSONObject().getJSONObject("activityLog"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error creating activity: " + e, e);
        }
    }

    /**
     * Delete user's activity log entry with the given id
     *
     * @param localUser authorized user
     * @param activityLogId Activity log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Activity-Log">Fitbit API: API-Delete-Activity-Log</a>
     */
    public void deleteActivityLog(LocalUserDetail localUser, String activityLogId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/activities/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities/" + activityLogId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting activity log entry: " + e, e);
        }
    }

    /**
     * Get a tree of all valid Fitbit public activities from the activities catalog as well as private custom activities the user created
     *
     * @param localUser authorized user
     *
     * @return activities catalog
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Browse-Activities">Fitbit API: API-Browse-Activities</a>
     */
    public List<ActivityCategory> getActivityCategories(LocalUserDetail localUser) throws FitbitAPIException {
        if (localUser != null) {
            setAccessToken(localUser);
        }
        // Example: GET /1/activities.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/activities", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return ActivityCategory.jsonArrayToActivityCategoryList(res.asJSONObject().getJSONArray("categories"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving activity: " + e, e);
        }
    }

    /**
     * Get the details of a specific activity in Fitbit activities database. If activity has levels, also get list of activity level details.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @return activity description
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activity">Fitbit API: API-Get-Activity</a>
     */
    public Activity getActivity(LocalUserDetail localUser, long activityId) throws FitbitAPIException {
        return getActivity(localUser, String.valueOf(activityId));
    }

    /**
     * Get the details of a specific activity in Fitbit activities database. If activity has levels, also get list of activity level details.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @return activity description
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activity">Fitbit API: API-Get-Activity</a>
     */
    public Activity getActivity(LocalUserDetail localUser, String activityId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/activities/90009.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/activities/" + activityId, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Activity.constructActivity(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving activity: " + e, e);
        }
    }

    /**
     * Adds the activity with the given id to user's list of favorite activities.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Add-Favorite-Activity">Fitbit API: API-Add-Favorite-Activity</a>
     */
    public void addFavoriteActivity(LocalUserDetail localUser, String activityId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/activities/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities/favorite/" + activityId, APIFormat.JSON);
        try {
            httpPost(url, null, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error adding favorite activity: " + e, e);
        }
    }

    /**
     * Delete the activity with the given id from user's list of favorite activities.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Favorite-Activity">Fitbit API: API-Delete-Favorite-Activity</a>
     */
    public void deleteFavoriteActivity(LocalUserDetail localUser, String activityId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/activities/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities/favorite/" + activityId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting favorite activity: " + e, e);
        }

    }

    /**
     * Create new private food for a user
     *
     * @param localUser authorized user
     * @param name Food name
     * @param description Food description
     * @param defaultFoodMeasurementUnitId Default measurement unit for a food
     * @param defaultServingSize Default size of a serving
     * @param caloriesPerServingSize Calories in default serving
     * @param formType Form type
     *
     * @return new food object
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Food">Fitbit API: API-Create-Food</a>
     */
    public Food createFood(LocalUserDetail localUser, String name, String description, long defaultFoodMeasurementUnitId,
                           float defaultServingSize, int caloriesPerServingSize, FoodFormType formType) throws FitbitAPIException {
        NutritionalValuesEntry nutritionalValuesEntry = new NutritionalValuesEntry();
        nutritionalValuesEntry.setCalories(caloriesPerServingSize);
        return createFood(localUser, name, description, defaultFoodMeasurementUnitId, defaultServingSize, formType, nutritionalValuesEntry);
    }

    /**
     * Create new private food for a user
     *
     * @param localUser authorized user
     * @param name Food name
     * @param description Food description
     * @param defaultFoodMeasurementUnitId Default measurement unit for a food
     * @param defaultServingSize Default size of a serving
     * @param formType Form type
     * @param nutritionalValuesEntry Set of nutritional values for a default serving
     *
     * @return new food object
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Food">Fitbit API: API-Create-Food</a>
     */
    public Food createFood(LocalUserDetail localUser, String name, String description, long defaultFoodMeasurementUnitId,
                           float defaultServingSize, FoodFormType formType,
                           NutritionalValuesEntry nutritionalValuesEntry) throws FitbitAPIException {
        setAccessToken(localUser);
        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("name", name));
        params.add(new PostParameter("description", description));
        params.add(new PostParameter("defaultFoodMeasurementUnitId", defaultFoodMeasurementUnitId));
        params.add(new PostParameter("defaultServingSize", defaultServingSize));
        params.add(new PostParameter("formType", formType.toString()));

        for(Map.Entry<String, Number> entry  : nutritionalValuesEntry.asMap().entrySet()) {
            params.add(new PostParameter(entry.getKey(), entry.getValue().toString()));
        }

        // Example: POST /1/food/create.json
        String url = APIUtil.contextualizeUrl(getApiBaseSecuredUrl(), getApiVersion(), "/foods", APIFormat.JSON);

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new Food(response.asJSONObject().getJSONObject("food"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to Food object: ", e);
        }
    }

    /**
     * Get a summary and list of a user's food log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data dor
     *
     * @return food records for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Foods">Fitbit API: API-Get-Foods</a>
     */
    public Foods getFoods(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/date/2010-02-25.json
        Response res = getCollectionResponseForDate(localUser, fitbitUser, APICollectionType.foods, date);
        return Foods.constructFoods(res);
    }

    public List<LoggedFood> getLoggedFoods(LocalUserDetail localUser, FitbitUser fitbitUser, ApiCollectionProperty property) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/recent.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.foods, property);
        return LoggedFood.constructLoggedFoodReferenceList(res);
    }

    /**
     * Get a list of a user's favorite foods. A favorite food provides a quick way to log the food
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's favorite foods
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Favorite-Foods">Fitbit API: API-Get-Favorite-Foods</a>
     */
    public List<Food> getFavoriteFoods(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/favorite.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.foods, ApiCollectionProperty.favorite);
        return Food.constructFoodListFromArrayResponse(res);
    }

    /**
     * Get a list of a user's recent foods
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's recent foods
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Recent-Foods">Fitbit API: API-Get-Recent-Foods</a>
     */
    public List<LoggedFood> getRecentFoods(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/recent.json
        return getLoggedFoods(localUser, fitbitUser, ApiCollectionProperty.recent);
    }

    /**
     * Get a list of a user's frequent foods
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's frequent foods
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Frequent-Foods">Fitbit API: API-Get-Frequent-Foods</a>
     */
    public List<LoggedFood> getFrequentFoods(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/frequent.json
        return getLoggedFoods(localUser, fitbitUser, ApiCollectionProperty.frequent);
    }

    /**
     * Given a search query, get a list of public foods from Fitbit foods database and private foods the user created
     *
     * @param localUser authorized user
     * @param query search query
     *
     * @return list of food search results
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Search-Foods">Fitbit API: API-Search-Foods</a>
     */
    public List<Food> searchFoods(LocalUserDetail localUser, String query) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/search.json?query=apple
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/foods/search", APIFormat.JSON);
        List<PostParameter> params = new ArrayList<PostParameter>(1);
        params.add(new PostParameter("query", query));
        Response res = httpGet(url, params.toArray(new PostParameter[params.size()]), true);
        return Food.constructFoodList(res);
    }

    /**
     * Get the details of a specific food in Fitbit Food database (or private food for the user)
     *
     * @param localUser authorized user
     * @param foodId Food id
     *
     * @return food description
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Food">Fitbit API: API-Get-Food</a>
     */
    public Food getFood(LocalUserDetail localUser, Long foodId) throws FitbitAPIException {
        if (localUser != null) {
            setAccessToken(localUser);
        }
        // Example: GET /1/foods/1.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/foods/" + foodId, APIFormat.JSON);
        Response res = httpGet(url, true);
        try {
            return new Food(res.asJSONObject().getJSONObject("food"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving food details: " + e, e);
        }
    }

    /**
     * Get list of all valid Fitbit food units
     *
     * @return list of valid food units
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Food-Units">Fitbit API: API-Get-Food-Units</a>
     */
    public List<FoodUnit> getFoodUnits() throws FitbitAPIException {
        clearAccessToken();
        // Example: GET https://api.fitbit.com/1/foods/units.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/foods/units", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return FoodUnit.constructFoodUnitList(res);
    }

    /**
     * Create log entry for a food
     *
     * @param localUser authorized user
     * @param foodId Food id
     * @param mealTypeId Meal type id
     * @param unitId Unit id
     * @param amount Amount consumed
     * @param date Log entry date
     *
     * @return new food log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Food">Fitbit API: API-Log-Food</a>
     */
    public FoodLog logFood(LocalUserDetail localUser, long foodId, int mealTypeId, int unitId, String amount, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("foodId", String.valueOf(foodId)));
        params.add(new PostParameter("mealTypeId", mealTypeId));
        params.add(new PostParameter("unitId", String.valueOf(unitId)));
        params.add(new PostParameter("amount", amount));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        return logFood(localUser, params);
    }

        /**
     * Create log entry for a food
     *
     * @param localUser authorized user
     * @param foodName Food name
     * @param brandName Brand name
     * @param nutritionalValuesEntry Nutritional Values
     * @param mealTypeId Meal type id
     * @param unitId Unit id
     * @param amount Amount consumed
     * @param date Log entry date
     *
     * @return new food log entry
     *
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Food">Fitbit API: API-Log-Food</a>
     */
    public FoodLog logFood(LocalUserDetail localUser, String foodName, String brandName, NutritionalValuesEntry nutritionalValuesEntry, int mealTypeId, int unitId, String amount, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("foodName", foodName));
        if(StringUtils.isNotBlank(brandName)) {
            params.add(new PostParameter("brandName", brandName));
        }
        for(Map.Entry<String, Number> entry  : nutritionalValuesEntry.asMap().entrySet()) {
            params.add(new PostParameter(entry.getKey(), entry.getValue().toString()));
        }
        params.add(new PostParameter("mealTypeId", mealTypeId));
        params.add(new PostParameter("unitId", String.valueOf(unitId)));
        params.add(new PostParameter("amount", amount));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        return logFood(localUser, params);
    }

    /**
     * Create log entry for a food
     *
     * @param localUser authorized user
     * @param params POST request parameters
     *
     * @return new food log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Food">Fitbit API: API-Log-Food</a>
     */
    public FoodLog logFood(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/food/log.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log", APIFormat.JSON);

        Response res;
        try {
            res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error creating food log entry: " + e, e);
        }
        if (res.getStatusCode() != HttpServletResponse.SC_CREATED) {
            throw new FitbitAPIException("Error creating activity: " + res.getStatusCode());
        }
        try {
            return new FoodLog(res.asJSONObject().getJSONObject("foodLog"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error creating food log entry: " + e, e);
        }
    }

    /**
     * Delete the user's food log entry with the given id
     *
     * @param localUser authorized user
     * @param foodLogId Food log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Food-Log">Fitbit API: API-Delete-Food-Log</a>
     */
    public void deleteFoodLog(LocalUserDetail localUser, String foodLogId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/food/log/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/" + foodLogId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting food log entry: " + e, e);
        }
    }

    /**
     * Add the food with the given id to user's list of favorite foods
     *
     * @param localUser authorized user
     * @param foodId Food id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Add-Favorite-Food">Fitbit API: API-Add-Favorite-Food</a>
     */
    public void addFavoriteFood(LocalUserDetail localUser, String foodId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/food/log/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/favorite/" + foodId, APIFormat.JSON);
        try {
            httpPost(url, null, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error adding favorite food: " + e, e);
        }
    }

    /**
     * Delete the food with the given id from user's list of favorite foods
     *
     * @param localUser authorized user
     * @param foodId Food id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Favorite-Food">Fitbit API: API-Delete-Favorite-Food</a>
     */
    public void deleteFavoriteFood(LocalUserDetail localUser, String foodId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/food/log/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/favorite/" + foodId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting favorite food: " + e, e);
        }

    }

    /**
     * Get a list of meals created by user
     *
     * @param localUser authorized user
     *
     * @return list of meals
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Meals">Fitbit API: API-Get-Meals</a>
     */
    public List<Meal> getMeals(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/meals.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/meals", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Meal.constructMeals(res.asJSONObject().getJSONArray("meals"));
        } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ": " + res.asString(), e);
        }
    }

    /**
     * Retrieves the list of Fitbit devices for a user
     *
     * @param localUser authorized user
     *
     * @return list of devices
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Devices">Fitbit API: API-Get-Devices</a>
     */
    public List<Device> getDevices(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return Device.constructDeviceList(res);
    }

    /**
     * Retrieve the attributes of user's Fitbit device
     *
     * @param localUser authorized user
     * @param deviceId device id
     *
     * @return device
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Device">Fitbit API: API-Get-Device</a>
     */
    public Device getDevice(LocalUserDetail localUser, String deviceId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/1234.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/" + deviceId, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Device(res.asJSONObject().getJSONObject("device"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving device: " + e, e);
        }
    }


    /**
     * Retrieves the list of user's scales
     *
     * @param localUser authorized user
     *
     * @return list of user's scales
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<Scale> getScales(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/scale.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale", APIFormat.JSON);
        Response response = httpGet(url, true);
        throwExceptionIfError(response);
        try {
            return Scale.jsonArrayToScalesList(response.asJSONObject().getJSONArray("scales"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of Scale : ", e);
        }
    }

    /**
     * Retrieve the attributes of user's scale
     *
     * @param localUser authorized user
     * @param deviceId scale serial number
     *
     * @return scale info
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Scale getScale(LocalUserDetail localUser, String deviceId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/scale/A123D4.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Scale(res.asJSONObject().getJSONObject("scale"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to scale : " + e, e);
        }
    }

    /**
     * Update scale settings
     *
     * @param localUser authorized user
     * @param deviceId scale device id
     * @param name scale name
     * @param unitSystem unit system
     * @param brightness brightness
     *
     * @return scale info
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Scale updateScaleSettings(LocalUserDetail localUser, String deviceId, String name, UnitSystem unitSystem, Integer brightness) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/devices/scale/A123D4.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId, APIFormat.JSON);

        List<PostParameter> params = new ArrayList<PostParameter>();
        if (name != null) {
            params.add(new PostParameter("name", name));
        }
        if (unitSystem != null) {
            params.add(new PostParameter("defaultUnit", unitSystem.getDisplayLocale()));
        }
        if (brightness != null) {
            params.add(new PostParameter("brightness", brightness));
        }

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        throwExceptionIfError(response);
        try {
            return new Scale(response.asJSONObject().getJSONObject("scale"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to Scale : ", e);
        }
    }

    public List<ScaleUser> getScaleUsers(LocalUserDetail localUser, String deviceId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/scale/AB1D234/users.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/users", APIFormat.JSON);
        Response response = httpGet(url, true);
        throwExceptionIfError(response);
        try {
            return ScaleUser.jsonArrayToScaleUsersList(response.asJSONObject().getJSONArray("scaleUsers"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of ScaleUser : ", e);
        }
    }

    public ScaleUser updateScaleUser(LocalUserDetail localUser, String deviceId, String scaleUserName, BodyType bodyType) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/devices/scale/AB1D234/users.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/users", APIFormat.JSON);
        List<PostParameter> params = new ArrayList<PostParameter>();
        if (scaleUserName != null) {
            params.add(new PostParameter("scaleUserName", scaleUserName));
        }
        if (bodyType != null) {
            params.add(new PostParameter("bodyType", bodyType.name()));
        }

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        throwExceptionIfError(response);

        try {
            return new ScaleUser(response.asJSONObject().getJSONObject("scaleUser"));
        } catch (Exception e) {
            throw new FitbitAPIException("Error parsing json response to ScaleUser : " + e, e);
        }
    }

    public void deleteScaleUser(LocalUserDetail localUser, String deviceId, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/devices/scale/AB1D234/users/22PY5R.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/users/" + fitbitUser.getId(), APIFormat.JSON);
        Response response = httpDelete(url, true);
        throwExceptionIfError(response, HttpServletResponse.SC_NO_CONTENT);
    }

    public List<ScaleInviteSendingResult> inviteUsersToScale(LocalUserDetail localUser, String deviceId, String invitedUserEmails, String message) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/devices/scale/AB1D234/users/invitations.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/users/invitations", APIFormat.JSON);
        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("invitedUserEmails", invitedUserEmails));
        if (message != null) {
            params.add(new PostParameter("message", message));
        }

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        throwExceptionIfError(response, HttpServletResponse.SC_CREATED);

        try {
            return ScaleInviteSendingResult.jsonArrayToScaleInviteSendingResultsList(response.asJSONObject().getJSONArray("scaleInviteSendingResults"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of ScaleInviteSendingResult : ", e);
        }
    }

    public List<ScaleInvite> getScaleInvites(LocalUserDetail localUser, String deviceId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/scale/AB1D234/users/invitations.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/users/invitations", APIFormat.JSON);
        Response response = httpGet(url, true);
        throwExceptionIfError(response);
        try {
            return ScaleInvite.jsonArrayToScaleInvitesList(response.asJSONObject().getJSONArray("scaleInvites"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of ScaleInvite : ", e);
        }
    }

    public void deleteScaleInvite(LocalUserDetail localUser, String deviceId, Long invite) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/devices/scale/AB1D234/users/invitations/21145.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/users/invitations/" + invite, APIFormat.JSON);

        Response response = httpDelete(url, true);
        throwExceptionIfError(response, HttpServletResponse.SC_NO_CONTENT);
    }

    public List<ScaleMeasurementLog> getScaleMeasurementLogs(LocalUserDetail localUser, String deviceId, LocalDate startDate, TimePeriod timePeriod) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/1234.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/measurements/date/" + startDate.toString() + "/" + timePeriod.getShortForm(), APIFormat.JSON);
        Response response = httpGet(url, true);
        throwExceptionIfError(response);
        try {
            return ScaleMeasurementLog.jsonArrayToMeasurementLogList(response.asJSONObject().getJSONArray("scaleMeasurements"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of ScaleMeasurementLog : ", e);
        }
    }

    public List<ScaleMeasurementLog> getScaleMeasurementLogs(LocalUserDetail localUser, String deviceId, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/1234.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/measurements/date/" + startDate.toString() + "/" + endDate.toString(), APIFormat.JSON);
        Response response = httpGet(url, true);
        throwExceptionIfError(response);
        try {
            return ScaleMeasurementLog.jsonArrayToMeasurementLogList(response.asJSONObject().getJSONArray("scaleMeasurements"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of ScaleMeasurementLog : ", e);
        }
    }

    public ScaleMeasurementLog reassignScaleMeasurementLogToUser(LocalUserDetail localUser, String deviceId, Long scaleMeasurementLodId, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/devices/scale/A12D34/measurements/23436.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/measurements/" + scaleMeasurementLodId, APIFormat.JSON);
        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("assignUserId", fitbitUser.getId()));

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new ScaleMeasurementLog(response.asJSONObject().getJSONObject("scaleMeasurementLog"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to scale measurement log: " + e, e);
        }
    }

    public ScaleMeasurementLog assignScaleMeasurementLogToGuest(LocalUserDetail localUser, String deviceId, Long scaleMeasurementLodId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/devices/scale/A12D34//measurements/23436.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/measurements/" + scaleMeasurementLodId, APIFormat.JSON);
        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("assignUserId", "GUEST"));

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new ScaleMeasurementLog(response.asJSONObject().getJSONObject("scaleMeasurementLog"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to scale measurement log: " + e, e);
        }
    }

    public void deleteScaleMeasurementLog(LocalUserDetail localUser, String deviceId, Long scaleMeasurementLodId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/devices/scale/A12D34//measurements/23436.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/scale/" + deviceId + "/measurements/" + scaleMeasurementLodId, APIFormat.JSON);
        Response response = httpDelete(url, true);
        throwExceptionIfError(response, HttpServletResponse.SC_NO_CONTENT);
    }

    public Response getCollectionResponseForDate(LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType type, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/log/date/2010-02-25.json
        String url = APIUtil.constructFullUrl(getApiBaseUrl(), getApiVersion(), fitbitUser, type, date, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return res;
    }

    public Response getCollectionResponseForProperty(LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType type, ApiCollectionProperty property) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/log/recent.json
        String url = APIUtil.constructFullUrl(getApiBaseUrl(), getApiVersion(), fitbitUser, type, property, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return res;
    }

    public Object getCollectionForDate(LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType type, LocalDate date) throws FitbitAPIException {
        switch (type) {
            case activities:
                return getActivities(localUser, fitbitUser, date);
            case foods:
                return getFoods(localUser, fitbitUser, date);
            case meals:
                return getMeals(localUser);
            default:
                return null;
        }
    }

    /**
     * Retrieve a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public double getWeight(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        return getBody(localUser, fitbitUser, date).getWeight();
    }

    /**
     * Retrieve a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    @Deprecated
    public double getWeight(LocalUserDetail localUser, String date) throws FitbitAPIException {
        return getBody(localUser, date).getWeight();
    }

    /**
     * Get a summary of a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public Body getBody(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        return getBodyWithGoals(localUser, fitbitUser, date).getBody();
    }

    /**
     * Get a summary of a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public Body getBody(LocalUserDetail localUser, String date) throws FitbitAPIException {
        return getBodyWithGoals(localUser, date).getBody();
    }

    public BodyWithGoals getBodyWithGoals(LocalUserDetail localUser, String date) throws FitbitAPIException {
        if(localUser != null) {
            setAccessToken(localUser);
        }
        // Example: GET /1/user/-/body/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/body/date/" + date, APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return BodyWithGoals.constructBodyWithGoals(res);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving body with goals: " + e, e);
        }
    }

    public BodyWithGoals getBodyWithGoals(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        if(localUser != null) {
            setAccessToken(localUser);
        }
        // Example: GET /1/user/228TQ4/body/date/2010-02-25.json
        String url = APIUtil.constructFullUrl(getApiBaseUrl(), getApiVersion(), fitbitUser, APICollectionType.body, date, APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return BodyWithGoals.constructBodyWithGoals(res);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving body with goals with goals: " + e, e);
        }
    }

    /**
     * Get a summary of a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param body updated body measurements
     * @param date day to retrieve data for
     *
     * @return updated body measurements for selected date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Body-Measurements">Fitbit API: API-Log-Body-Measurements</a>
     */
    public Body logBody(LocalUserDetail localUser, Body body, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>();
        if(body.getWeight() > 0) {
            params.add(new PostParameter("weight", body.getWeight()));
        }
        if(body.getFat() > 0) {
            params.add(new PostParameter("fat", body.getFat()));
        }
        if(body.getNeck() > 0) {
            params.add(new PostParameter("neck", body.getNeck()));
        }
        if(body.getBicep() > 0) {
            params.add(new PostParameter("bicep", body.getBicep()));
        }
        if(body.getForearm() > 0) {
            params.add(new PostParameter("forearm", body.getForearm()));
        }
        if(body.getChest() > 0) {
            params.add(new PostParameter("chest", body.getChest()));
        }
        if(body.getWaist() > 0) {
            params.add(new PostParameter("waist", body.getWaist()));
        }
        if(body.getHips() > 0) {
            params.add(new PostParameter("hips", body.getHips()));
        }
        if(body.getThigh() > 0) {
            params.add(new PostParameter("thigh", body.getThigh()));
        }
        if(body.getCalf() > 0) {
            params.add(new PostParameter("calf", body.getCalf()));
        }
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        return logBody(localUser, params);
    }

    public Body logBody(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/body.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/body", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new Body(res.asJSONObject().getJSONObject("body"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging weight: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging weight: " + e, e);
        }
    }

    /**
     * Log weight
     *
     * @param localUser authorized user
     * @param weight Weight
     * @param date Log entry date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Body-Measurements">Fitbit API: API-Log-Body-Measurements</a>
     */
    @Deprecated
    public void logWeight(LocalUserDetail localUser, float weight, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(2);
        params.add(new PostParameter("weight", weight));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        logBody(localUser, params);
    }

    /**
     * Create log entry for a water in custom volume units
     *
     * @param localUser authorized user
     * @param amount Amount consumed
     * @param date Log entry date
     * @param volumeUnit Custom volume unit
     *
     * @return new water log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Water">Fitbit API: API-Log-Water</a>
     */
    public WaterLog logWater(LocalUserDetail localUser, float amount, VolumeUnits volumeUnit, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(2);
        params.add(new PostParameter("amount", amount));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        if (volumeUnit != null) {
            params.add(new PostParameter("unit", volumeUnit.getText()));
        }

        return logWater(localUser, params);
    }

    /**
     * Create log entry for a water
     *
     * @param localUser authorized user
     * @param amount Amount consumed
     * @param date Log entry date
     *
     * @return new water log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Water">Fitbit API: API-Log-Water</a>
     */
    public WaterLog logWater(LocalUserDetail localUser, float amount, LocalDate date) throws FitbitAPIException {
        return logWater(localUser, amount, null, date);
    }

    public WaterLog logWater(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/foods/log/water.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/water", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new WaterLog(res.asJSONObject().getJSONObject("waterLog"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging water: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging water: " + e, e);
        }
    }

    /**
     * Get a summary and list of a user's water log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return water for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Water">Fitbit API: API-Get-Water</a>
     */
    public Water getLoggedWater(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/log/water/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/foods/log/water/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Water(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving water: " + e, e);
        }
    }

    /**
     * Delete user's water log entry with the given id
     *
     * @param localUser authorized user
     * @param logWaterId Water log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Water-Log">Fitbit API: API-Delete-Water-Log</a>
     */
    public void deleteWater(LocalUserDetail localUser, String logWaterId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/foods/log/water/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/water/" + logWaterId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting water: " + e, e);
        }

    }


    /**
     * Create log entry for a blood pressure measurement
     *
     * @param localUser authorized user
     * @param systolic Systolic blood pressure
     * @param diastolic Diastolic blood pressure
     * @param date Log entry date
     * @param time Log entry time string
     *
     * @return new blood pressure log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Blood-Pressure">Fitbit API: API-Log-Blood-Pressure</a>
     */
    public BpLog logBp(LocalUserDetail localUser, int systolic, int diastolic, LocalDate date, String time) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(4);
        params.add(new PostParameter("systolic", systolic));
        params.add(new PostParameter("diastolic", diastolic));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        if (time != null) {
            params.add(new PostParameter("time", time));
        }

        return logBp(localUser, params);
    }

    /**
     * Create log entry for a blood pressure measurement
     *
     * @param localUser authorized user
     * @param systolic Systolic blood pressure
     * @param diastolic Diastolic blood pressure
     * @param date Log entry date
     *
     * @return new blood pressure log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Blood-Pressure">Fitbit API: API-Log-Blood-Pressure</a>
     */
    public BpLog logBp(LocalUserDetail localUser, int systolic, int diastolic, LocalDate date) throws FitbitAPIException {
        return logBp(localUser, systolic, diastolic, date, null);
    }

    public BpLog logBp(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/bp", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new BpLog(res.asJSONObject().getJSONObject("bpLog"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging blood pressure: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging blood pressure: " + e, e);
        }
    }

    /**
     * Get an average and list of a user's blood pressure log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return blood pressure entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Blood-Pressure">Fitbit API: API-Get-Blood-Pressure</a>
     */
    public Bp getLoggedBp(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/bp/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/bp/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Bp(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving blood pressure: " + e, e);
        }
    }

    /**
     * Delete user's blood pressure log entry with the given id
     *
     * @param localUser authorized user
     * @param logId Blood pressure log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Blood-Pressure-Log">Fitbit API: API-Delete-Blood-Pressure-Log</a>
     */
    public void deleteBp(LocalUserDetail localUser, String logId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/bp/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/bp/" + logId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting water: " + e, e);
        }
    }

    /**
     * Create log entry for a glucose
     *
     * @param localUser authorized user
     * @param tracker tracker name
     * @param glucose glucose value
     * @param hba1c hba1c value
     * @param date Log entry date
     * @param time Log entry time string
     *
     * @return new glucose log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Glucose logGlucose(LocalUserDetail localUser, String tracker, Float glucose, Float hba1c, LocalDate date, String time) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        if(tracker != null) {
            params.add(new PostParameter("tracker", tracker));
        }
        if(glucose != null) {
            params.add(new PostParameter("glucose", glucose));
        }
        if(hba1c != null) {
            params.add(new PostParameter("hba1c", hba1c));
        }
        if (time != null) {
            params.add(new PostParameter("time", time));
        }

        return logGlucose(localUser, params);
    }

    /**
     * Create log entry for a glucose
     *
     * @param localUser authorized user
     * @param tracker tracker name
     * @param glucose glucose value
     * @param hba1c hba1c value
     * @param date Log entry date
     *
     * @return new blood pressure log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Glucose logGlucose(LocalUserDetail localUser, String tracker, Float glucose, Float hba1c, LocalDate date) throws FitbitAPIException {
        return logGlucose(localUser, tracker, glucose, hba1c, date, null);
    }

    public Glucose logGlucose(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/glucose", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new Glucose(res.asJSONObject());
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging glucose: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging glucose: " + e, e);
        }
    }

    /**
     * Get list of a user's glucose log entries and hba1c for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return glucose entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Glucose getLoggedGlucose(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/glucose/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/glucose/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Glucose(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving blood pressure: " + e, e);
        }
    }
    
    /**
     * Create log entry for a heart rate
     *
     * @param localUser authorized user
     * @param tracker Tracker name
     * @param heartRate Heart rate
     * @param date Log entry date
     * @param time Log entry time string
     *
     * @return new heart rate log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public HeartLog logHeartRate(LocalUserDetail localUser, String tracker, int heartRate, LocalDate date, String time) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(4);
        params.add(new PostParameter("tracker", tracker));
        params.add(new PostParameter("heartRate", heartRate));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        if (time != null) {
            params.add(new PostParameter("time", time));
        }

        return logHeartRate(localUser, params);
    }

    /**
     * Create log entry for a heart rate
     *
     * @param localUser authorized user
     * @param tracker Tracker name
     * @param heartRate Heart rate
     * @param date Log entry date
     *
     * @return new heart rate log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public HeartLog logHeartRate(LocalUserDetail localUser, String tracker, int heartRate, LocalDate date) throws FitbitAPIException {
        return logHeartRate(localUser, tracker, heartRate, date, null);
    }

    public HeartLog logHeartRate(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/heart", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new HeartLog(res.asJSONObject().getJSONObject("heartLog"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging heart rate: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging heart rate: " + e, e);
        }
    }

    /**
     * Get an average and list of a user's heart rate log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return heart rate entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Heart getLoggedHeartRate(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/heart/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/heart/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Heart(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving heart rate: " + e, e);
        }
    }

    /**
     * Delete user's heart rate log entry with the given id
     *
     * @param localUser authorized user
     * @param logId Heart rate log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public void deleteHeartRate(LocalUserDetail localUser, String logId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/heart/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/heart/" + logId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting heart rate: " + e, e);
        }
    }

    /**
     * Get a user's profile
     *
     * @param localUser authorized user
     *
     * @return profile of a user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-User-Info">Fitbit API: API-Get-User-Info</a>
     */
    public UserInfo getUserInfo(LocalUserDetail localUser) throws FitbitAPIException {
        return getUserInfo(localUser, FitbitUser.CURRENT_AUTHORIZED_USER);
    }

    /**
     * Get a user's profile
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return profile of a user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-User-Info">Fitbit API: API-Get-User-Info</a>
     */
    public UserInfo getUserInfo(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/profile.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/profile", APIFormat.JSON);

        try {
            Response response = httpGet(url, true);
            throwExceptionIfError(response);
            return new UserInfo(response.asJSONObject());
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error getting user info: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error getting user info: " + e, e);
        }
    }

    /**
     * Update user's profile
     *
     * @param localUser authorized user
     * @param params list of values to update
     *
     * @return updated profile of a user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Update-User-Info">Fitbit API: API-Update-User-Info</a>
     */
    public UserInfo updateUserInfo(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/profile.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/profile", APIFormat.JSON);

        try {
            Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            throwExceptionIfError(response);
            return new UserInfo(response.asJSONObject());
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error updating profile: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error updating profile: " + e, e);
        }
    }

    /**
     * Invite another user to be a friend given his userId
     *
     * @param localUser authorized user
     * @param invitedUserId invited user id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Invite">Fitbit API: API-Create-Invite</a>
     */
    public void inviteByUserId(LocalUserDetail localUser, String invitedUserId) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("invitedUserId", invitedUserId));

        // POST /1/user/-/friends/invitations.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations", APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }

    /**
     * Invite another user to be a friend given his email
     *
     * @param localUser authorized user
     * @param invitedUserEmail invited user's email
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Invite">Fitbit API: API-Create-Invite</a>
     */
    public void inviteByEmail(LocalUserDetail localUser, String invitedUserEmail) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("invitedUserEmail", invitedUserEmail));

        // POST /1/user/-/friends/invitations.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations", APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }

    /**
     * Accept friend invitation from another user
     *
     * @param localUser authorized user
     * @param fitbitUser inviting user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Accept-Invite">Fitbit API: API-Accept-Invite</a>
     */
    public void acceptInvitationFromUser(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("accept", String.valueOf(true)));

        // POST /1/user/-/friends/invitations/228KP9.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations/" + fitbitUser.getId(), APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }

    /**
     * Reject friend invitation from another user
     *
     * @param localUser authorized user
     * @param fitbitUser inviting user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Accept-Invite">Fitbit API: API-Accept-Invite</a>
     */
    public void rejectInvitationFromUser(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("accept", String.valueOf(false)));

        // POST /1/user/-/friends/invitations/228KP9.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations/" + fitbitUser.getId(), APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }


    /**
     * Get a summary and list of a user's sleep log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return sleep for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Sleep">Fitbit API: API-Get-Sleep</a>
     */
    public Sleep getSleep(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/sleep/date/2010-02-25.json
        Response res = getCollectionResponseForDate(localUser, fitbitUser, APICollectionType.sleep, date);
        throwExceptionIfError(res);
        return Sleep.constructSleep(res);
    }

    /**
     * Create log entry for a sleep
     *
     * @param localUser authorized user
     * @param date Log entry date
     * @param startTime Start time
     * @param duration Duration
     *
     * @return new sleep log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Sleep">Fitbit API: API-Log-Sleep</a>
     */
    public SleepLog logSleep(LocalUserDetail localUser, LocalDate date, LocalTime startTime, long duration) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("date", FitbitApiService.LOCAL_DATE_FORMATTER.print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("duration", duration));

        // POST /1/user/-/sleep.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/sleep", APIFormat.JSON);

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new SleepLog(response.asJSONObject().getJSONObject("sleep"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to SleepLog object: ", e);
        }
    }

    /**
     * Delete user's sleep log entry with the given id
     *
     * @param localUser authorized user
     * @param sleepLogId Sleep log entry id
     *
     * @return list of friends
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Sleep-Log">Fitbit API: API-Delete-Sleep-Log</a>
     */
    public void deleteSleepLog(LocalUserDetail localUser, Long sleepLogId) throws FitbitAPIException {
        setAccessToken(localUser);

        // POST /1/user/-/sleep/345275.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/sleep/" + sleepLogId, APIFormat.JSON);

        httpDelete(url, true);
    }

    /**
     * Get a list of user's friends
     *
     * @param localUser authorized user
     *
     * @return list of user's friends
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Friends">Fitbit API: API-Get-Friends</a>
     */
    public List<UserInfo> getFriends(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // GET /1/user/-/friends.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends", APIFormat.JSON);
        return getFriends(url);
    }

    /**
     * Get a list of user's friends
     *
     * @param owner user to retrieve data from
     *
     * @return list of user's friends
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Friends">Fitbit API: API-Get-Friends</a>
     */
    public List<UserInfo> getFriends(FitbitUser owner) throws FitbitAPIException {
        // GET /1/user/XXXX/friends.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + owner.getId() + "/friends", APIFormat.JSON);
        return getFriends(url);
    }

    /**
     * Get a list of user's friends
     *
     * @param localUser authorized user
     * @param owner user to retrieve data from
     *
     * @return list of user's friends
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Friends">Fitbit API: API-Get-Friends</a>
     */
    public List<UserInfo> getFriends(LocalUserDetail localUser, FitbitUser owner) throws FitbitAPIException {
        setAccessToken(localUser);
        // GET /1/user/-/friends.json
        // GET /1/user/XXX/friends.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + owner.getId() + "/friends", APIFormat.JSON);
        return getFriends(url);
    }

    /**
     * Get a list of user's friends
     *
     * @param url full url for the requested resource in the API in json format
     *
     * @return list of user's friends
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Friends">Fitbit API: API-Get-Friends</a>
     */
    private List<UserInfo> getFriends(String url) throws FitbitAPIException {
        Response response = httpGet(url, true);
        throwExceptionIfError(response);

        try {
            return UserInfo.friendJsonArrayToUserInfoList(response.asJSONObject().getJSONArray("friends"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of UserInfo : ", e);
        }
    }

    /**
     * Get a leaderboard of user's friends progress
     *
     * @param localUser authorized user
     * @param timePeriod leaderboard time period (currently support only TimePeriod.SEVEN_DAYS and TimePeriod.THIRTY_DAYS)
     *
     * @return list of user friend's stats
     *
     * @throws FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Friends-Leaderboard">Fitbit API: API-Get-Friends-Leaderboard</a>
     */
    public List<FriendStats> getFriendsLeaderboard(LocalUserDetail localUser, TimePeriod timePeriod) throws FitbitAPIException {
        setAccessToken(localUser);
        // GET /1/user/-/friends/leaders.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/leaders/" + timePeriod.getShortForm(), APIFormat.JSON);
        Response response = httpGet(url, true);
        throwExceptionIfError(response);
        try {
            return FriendStats.jsonArrayToFriendStatsList(response.asJSONObject().getJSONArray("friends"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to list of FriendStats : ", e);
        }
    }


    /**
     * Get Rate Limiting Quota left for the Client
     *
     * @return quota
     *
     * @throws FitbitAPIException Fitbit API Exception
     */
    public ApiRateLimitStatus getClientRateLimitStatus() throws FitbitAPIException {
        clearAccessToken();
        return getRateLimitStatus(ApiQuotaType.CLIENT);
    }

    /**
     * Get Rate Limiting Quota left for the Client+Viewer
     *
     * @param localUser authorized user
     *
     * @return quota
     *
     * @throws FitbitAPIException Fitbit API Exception
     */
    public ApiRateLimitStatus getClientAndViewerRateLimitStatus(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        return getRateLimitStatus(ApiQuotaType.CLIENT_AND_VIEWER);
    }

    public ApiRateLimitStatus getRateLimitStatus(ApiQuotaType quotaType) throws FitbitAPIException {
        // Example: GET /1/account/clientAndViewerRateLimitStatus.json OR /1/account/clientRateLimitStatus.json
        String quoteTypeToken;
        switch (quotaType) {
            case CLIENT_AND_VIEWER:
                quoteTypeToken = "clientAndViewer";
                break;
            case CLIENT:
                quoteTypeToken = "client";
                break;
            default:
                throw new FitbitAPIException(String.format("Illegal quote type '%s'", quotaType));
        }
        String relativePath = "/account/" + quoteTypeToken + "RateLimitStatus";
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), APIVersion.BETA_1, relativePath, APIFormat.JSON);
        return new ApiRateLimitStatus(httpGet(url, true));
    }

    /**
     * Adds a subscription to all user's collections
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, null, null);
    }

    /**
     * Adds a subscription to user's collection
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param collectionType type of a collection to subscribe to
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, collectionType, null);
    }

    /**
     * Adds a subscription with given id to all user's collections
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param subscriptionId The ID of the subscription that makes sense to your application
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, String subscriptionId) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, null, subscriptionId);
    }

    /**
     * Adds a subscription with given id to user's collection
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param collectionType type of a collection to subscribe to
     * @param subscriptionId The ID of the subscription that makes sense to your application
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, collectionType, subscriptionId);
    }

    /**
     * Removes a subscription with given id from all user's collections
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param subscriptionId The ID of the subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Removeasubscription">Fitbit API: Subscriptions-API</a>
     */
    public void unsubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, String subscriptionId) throws FitbitAPIException {
        nullSafeUnsubscribe(subscriberId, localUser, fitbitUser, null, subscriptionId);
    }

    /**
     * Removes a subscription with given id from user's collection
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param collectionType type of a collection to unsubscribe from
     * @param subscriptionId The ID of the subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Removeasubscription">Fitbit API: Subscriptions-API</a>
     */
    public void unsubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        nullSafeUnsubscribe(subscriberId, localUser, fitbitUser, collectionType, subscriptionId);
    }

    /* ********************************************************************* */

    protected SubscriptionDetail nullSafeSubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        setAccessToken(localUser);

        String url =
                APIUtil.constructFullSubscriptionUrl(
                        getApiBaseUrl(),
                        getApiVersion(),
                        fitbitUser,
                        collectionType,
                        null == subscriptionId ? APIUtil.UNSPECIFIED_SUBSCRIPTION_ID : subscriptionId,
                        APIFormat.JSON
                );
        setSubscriberId(subscriberId);

        try {
            return new SubscriptionDetail(httpPost(url, null, true).asJSONObject());
        } catch (FitbitAPIException e) {
            throw e;
        } catch (Exception e) {
            throw new FitbitAPIException("Could not create subscription: " + e, e);
        }
    }

    protected void nullSafeUnsubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        setAccessToken(localUser);

        String url =
                APIUtil.constructFullSubscriptionUrl(
                        getApiBaseUrl(),
                        getApiVersion(),
                        fitbitUser,
                        collectionType,
                        subscriptionId,
                        APIFormat.JSON
                );
        setSubscriberId(subscriberId);

        httpDelete(url, true);
    }

    public List<ApiSubscription> getSubscriptions(LocalUserDetail localUser) throws FitbitAPIException {
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/apiSubscriptions", APIFormat.JSON);
        return getSubscriptions(localUser, url);
    }

    public List<ApiSubscription> getSubscriptions(LocalUserDetail localUser, APICollectionType collectionType) throws FitbitAPIException {
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/" + collectionType + "/apiSubscriptions", APIFormat.JSON);
        return getSubscriptions(localUser, url);
    }

    private List<ApiSubscription> getSubscriptions(LocalUserDetail localUser, String url) throws FitbitAPIException {
        setAccessToken(localUser);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            JSONObject jsonObject = res.asJSONObject();
            JSONArray jsonArray = jsonObject.getJSONArray("apiSubscriptions");
            List<ApiSubscription> result = new ArrayList<ApiSubscription>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                ApiSubscription apiSubscription = new ApiSubscription(jsonArray.getJSONObject(i));
                result.add(apiSubscription);
            }
            return result;
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving water: " + e, e);
        }
    }

    /**
     * Get time series in the specified range for a given resource of a user (as an unauthorized)
     *
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(null, user, resourceType, startDate.toString(), period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user (as an unauthorized)
     *
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(FitbitUser user, TimeSeriesResourceType resourceType, String startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(null, user, resourceType, startDate, period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user (as an unauthorized)
     *
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate Start date of a time range
     * @param endDate End date of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        return getTimeSeries(null, user, resourceType, startDate.toString(), endDate.toString());
    }

    /**
     * Get time series in the specified range for a given resource of a user
     *
     * @param localUser authorized user
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(localUser, user, resourceType, startDate.toString(), period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user
     *
     * @param localUser authorized user
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(localUser, user, resourceType, startDate, period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user
     *
     * @param localUser authorized user
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate Start date of a time range
     * @param endDate End date of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        return getTimeSeries(localUser, user, resourceType, startDate.toString(), endDate.toString());
    }

    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String startDate, String periodOrEndDate) throws FitbitAPIException {
        if (localUser != null) {
            setAccessToken(localUser);
        } else {
            clearAccessToken();
        }

        String url = APIUtil.constructTimeSeriesUrl(getApiBaseUrl(), getApiVersion(), user, resourceType, startDate, periodOrEndDate, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Data.jsonArrayToDataList(res.asJSONObject().getJSONArray(resourceType.getResourcePath().substring(1).replace('/', '-')));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to data list : ", e);
        }
    }

    public IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate date, LocalTime startTime, LocalTime endTime) throws FitbitAPIException {
        return getIntraDayTimeSeries(localUser, user, resourceType, date.toString(), FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime), FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(endTime));
    }

    public IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String date, String startTime, String endTime) throws FitbitAPIException {
        String url = APIUtil.constructTimeSeriesUrl(getApiBaseUrl(), getApiVersion(), user, resourceType, date, TimePeriod.INTRADAY.getShortForm(), startTime, endTime, APIFormat.JSON);
        return getIntraDayTimeSeries(localUser, resourceType, url);
    }

    public IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate date) throws FitbitAPIException {
        return getIntraDayTimeSeries(localUser, user, resourceType, date.toString());
    }

    public IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String date) throws FitbitAPIException {
        String url = APIUtil.constructTimeSeriesUrl(getApiBaseUrl(), getApiVersion(), user, resourceType, date, TimePeriod.INTRADAY.getShortForm(), APIFormat.JSON);
        return getIntraDayTimeSeries(localUser, resourceType, url);
    }

    private IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, TimeSeriesResourceType resourceType, String url) throws FitbitAPIException {
        if (localUser != null) {
            setAccessToken(localUser);
        } else {
            clearAccessToken();
        }

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new IntradaySummary(res.asJSONObject(), resourceType);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to IntradaySummary : ", e);
        }
    }

    /* ********************************************************************* */

    protected void setAccessToken(LocalUserDetail localUser) {
        // Get the access token for the user:
        APIResourceCredentials resourceCredentials = credentialsCache.getResourceCredentials(localUser);
        // Set the access token in the client:
        setOAuthAccessToken(resourceCredentials.getAccessToken(), resourceCredentials.getAccessTokenSecret(), resourceCredentials.getLocalUserId());
    }

    protected void clearAccessToken() {
        // Set the access token in the client to null:
        setOAuthAccessToken(null);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */

    protected Response httpGet(String url, boolean authenticate) throws FitbitAPIException {
        return httpGet(url, null, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @param name1 the name of the first parameter
     * @param value1 the value of the first parameter
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */

    protected Response httpGet(String url, String name1, String value1, boolean authenticate) throws FitbitAPIException {
        return httpGet(url, new PostParameter[]{new PostParameter(name1, value1)}, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param name1 the name of the first parameter
     * @param value1 the value of the first parameter
     * @param name2 the name of the second parameter
     * @param value2 the value of the second parameter
     * @param authenticate if true, the request will be sent with BASIC authentication header
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */
    protected Response httpGet(String url, String name1, String value1, String name2, String value2, boolean authenticate) throws FitbitAPIException {
        return httpGet(url, new PostParameter[]{new PostParameter(name1, value1), new PostParameter(name2, value2)}, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param params the request parameters
     * @param authenticate if true, the request will be sent with BASIC authentication header
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */
    protected Response httpGet(String url, PostParameter[] params, boolean authenticate) throws FitbitAPIException {
        return http.get(appendParamsToUrl(url, params), authenticate);
    }

    protected Response httpPost(String url, PostParameter[] params, boolean authenticate) throws FitbitAPIException {
        return http.post(url, params, authenticate);
    }

    protected Response httpDelete(String url, boolean authenticate) throws FitbitAPIException {
        return httpDelete(url, null, authenticate);
    }

    protected Response httpDelete(String url, PostParameter[] params, boolean authenticate) throws FitbitAPIException {
        // We use Sun's HttpURLConnection, which does not like request entities
        // submitted on HTTP DELETE
        return http.delete(appendParamsToUrl(url, params), authenticate);
    }

    protected static String appendParamsToUrl(String url, PostParameter[] params) {
        if (null != params && params.length > 0) {
            return url + '?' + HttpClient.encodeParameters(params);
        }
        return url;
    }

    public static void throwExceptionIfError(Response res) throws FitbitAPIException {
        if (res.isError()) {
            throw new FitbitAPIException(getErrorMessage(res));
        }
    }

    public static void throwExceptionIfError(Response res, int expectedStatusCode) throws FitbitAPIException {
        if (res.getStatusCode() != expectedStatusCode) {
            throw new FitbitAPIException(getErrorMessage(res));
        }
    }

    public static String getErrorMessage(Response res) throws FitbitAPIException {
        return res.isError() ? res.asString() : "";
    }

    /**
     * Set unit system for future API calls
     *
     * @param locale requested unit system
     *
     * @see <a href="http://wiki.fitbit.com/display/API/API-Unit-System">Fitbit API: API-Unit-System</a>
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            http.removeRequestHeader("Accept-Language");
        } else {
            http.setRequestHeader("Accept-Language", locale.toString());
        }
    }

    /**
     * Create log entry for a weight
     *
     * @param localUser authorized user
     * @param weight weight
     * @param date Log entry date
     *
     * @return new weight log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public WeightLog logWeight(LocalUserDetail localUser, double weight, LocalDate date) throws FitbitAPIException {
        return logWeight(localUser, weight, date, null);
    }

    /**
     * Create log entry for a weight
     *
     * @param localUser authorized user
     * @param weight weight
     * @param date Log entry date
     * @param time Log entry time
     *
     * @return new weight log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public WeightLog logWeight(LocalUserDetail localUser, double weight, LocalDate date, String time) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(4);
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("weight", weight));

        if (time != null) {
            params.add(new PostParameter("time", time));
        }

        return logWeight(localUser, params);
    }

    public WeightLog logWeight(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/body/log/weight", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new WeightLog(res.asJSONObject().getJSONObject("weightLog"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging weight: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging weight: " + e, e);
        }
    }

    /**
     * Get list of a user's weight log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return weight entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<WeightLog> getLoggedWeight(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/log/weight/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/weight/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return WeightLog.constructWeightLogList(res.asJSONObject().getJSONArray("weight"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving weight: " + e, e);
        }
    }

    /**
     * Get list of a user's weight log entries for a given days' range
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param startDate date to retrieve data from
     * @param endDate date to retrieve data to
     *
     * @return weight entries days' range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<WeightLog> getLoggedWeight(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/log/weight/date/2010-02-25/2010-02-28.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/weight/date/" +
                DateTimeFormat.forPattern("yyyy-MM-dd").print(startDate) + "/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(endDate), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return WeightLog.constructWeightLogList(res.asJSONObject().getJSONArray("weight"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving weight: " + e, e);
        }
    }

    /**
     * Get list of a user's weight log entries for a given days' period
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param endDate date to retrieve data to
     * @param period data period
     *
     * @return weight entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<WeightLog> getLoggedWeight(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate endDate, DataPeriod period) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/log/weight/date/2010-02-25/30d.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/weight/date/" +
                DateTimeFormat.forPattern("yyyy-MM-dd").print(endDate) + "/" + period.getShortForm(), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return WeightLog.constructWeightLogList(res.asJSONObject().getJSONArray("weight"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving weight: " + e, e);
        }
    }

    /**
     * Get list of a user's weight log entries
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param dateUrl date url part
     *
     * @return weight entries
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<WeightLog> getLoggedWeight(LocalUserDetail localUser, FitbitUser fitbitUser, String dateUrl) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/weight/date/" + dateUrl, APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return WeightLog.constructWeightLogList(res.asJSONObject().getJSONArray("weight"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving weight: " + e, e);
        }
    }

    /**
     * Delete user's weight log entry with the given id
     *
     * @param localUser authorized user
     * @param logId Weight log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public void deleteWeight(LocalUserDetail localUser, String logId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/body/log/weight/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(),
                "/user/-/body/log/weight/" + logId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting weight: " + e, e);
        }
    }

    /**
     * Create log entry for a fat
     *
     * @param localUser authorized user
     * @param fat fat
     * @param date Log entry date
     *
     * @return new fat log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public FatLog logFat(LocalUserDetail localUser, double fat, LocalDate date) throws FitbitAPIException {
        return logFat(localUser, fat, date, null);
    }

    /**
     * Create log entry for a fat
     *
     * @param localUser authorized user
     * @param fat fat
     * @param date Log entry date
     * @param time Log entry time
     *
     * @return new fat log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public FatLog logFat(LocalUserDetail localUser, double fat, LocalDate date, String time) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(4);
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("fat", fat));

        if (time != null) {
            params.add(new PostParameter("time", time));
        }

        return logFat(localUser, params);
    }

    public FatLog logFat(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/body/log/fat", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new FatLog(res.asJSONObject().getJSONObject("fatLog"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging fat: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging fat: " + e, e);
        }
    }

    /**
     * Get list of a user's fat log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return fat entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<FatLog> getLoggedFat(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/log/fat/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/fat/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return FatLog.constructFatLogList(res.asJSONObject().getJSONArray("fat"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving fat: " + e, e);
        }
    }

    /**
     * Get list of a user's fat log entries for a given days' range
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param startDate date to retrieve data from
     * @param endDate date to retrieve data to
     *
     * @return fat entries days' range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<FatLog> getLoggedFat(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/log/fat/date/2010-02-25/2010-02-28.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/fat/date/" +
                DateTimeFormat.forPattern("yyyy-MM-dd").print(startDate) + "/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(endDate), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return FatLog.constructFatLogList(res.asJSONObject().getJSONArray("fat"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving fat: " + e, e);
        }
    }

    /**
     * Get list of a user's fat log entries for a given days' period
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param endDate date to retrieve data to
     * @param period data period
     *
     * @return fat entries for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<FatLog> getLoggedFat(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate endDate, DataPeriod period) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/log/fat/date/2010-02-25/30d.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/fat/date/" +
                DateTimeFormat.forPattern("yyyy-MM-dd").print(endDate) + "/" + period.getShortForm(), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return FatLog.constructFatLogList(res.asJSONObject().getJSONArray("fat"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving fat: " + e, e);
        }
    }

    /**
     * Get list of a user's fat log entries
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param dateUrl date url part
     *
     * @return fat entries
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public List<FatLog> getLoggedFat(LocalUserDetail localUser, FitbitUser fitbitUser, String dateUrl) throws FitbitAPIException {
        setAccessToken(localUser);
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/body/log/fat/date/" + dateUrl, APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return FatLog.constructFatLogList(res.asJSONObject().getJSONArray("fat"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving fat: " + e, e);
        }
    }

    /**
     * Delete user's fat log entry with the given id
     *
     * @param localUser authorized user
     * @param logId Fat log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public void deleteFat(LocalUserDetail localUser, String logId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/body/log/fat/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(),
                "/user/-/body/log/fat/" + logId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting fat: " + e, e);
        }
    }

    public void setLocalization(Locale locale) {
        if (locale == null) {
            http.removeRequestHeader("Accept-Locale");
        } else {
            http.setRequestHeader("Accept-Locale", locale.toString());
        }
    }
}
