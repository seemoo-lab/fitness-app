package com.fitbit.api.common.model.user;

import com.fitbit.api.common.service.FitbitApiService;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {

    private final String encodedId;
    private final String displayName;
    private final Gender gender;
    private final LocalDate dateOfBirth;
    private final double height;
    private final double weight;
    private final double strideLengthWalking;
    private final double strideLengthRunning;
    private final String fullName;
    private final String nickname;
    private final String country;
    private final String state;
    private final String city;
    private final String aboutMe;
    private final LocalDate memberSince;
    private final DateTimeZone timezone;
    /**
     * Millisecond offset to add to UTC to get timezone
     */
    private final int offsetFromUTCMillis;
    private String locale;
    private final String avatar;

    private String weightUnit;
    private String distanceUnit;
    private String heightUnit;
    private String waterUnit;
    private String glucoseUnit;

    public UserInfo(JSONObject json) throws JSONException {
        this(json, true);
    }

    public UserInfo(JSONObject jsonObject, boolean wrapped) throws JSONException {
        JSONObject userJson = wrapped ? jsonObject.getJSONObject("user") : jsonObject;
        encodedId = userJson.getString("encodedId");
        displayName = userJson.getString("displayName");
        gender = Gender.valueOf(userJson.getString("gender"));
        dateOfBirth = FitbitApiService.getValidLocalDateOrNull(userJson.optString("dateOfBirth"));
        height = userJson.optDouble("height");
        weight = userJson.optDouble("weight");
        strideLengthWalking = userJson.optDouble("strideLengthWalking");
        strideLengthRunning = userJson.optDouble("strideLengthRunning");
        fullName = userJson.optString("fullName");
        nickname = userJson.optString("nickname");
        country = userJson.optString("country");
        state = userJson.optString("state");
        city = userJson.optString("city");
        aboutMe = userJson.optString("aboutMe");
        memberSince = FitbitApiService.getValidLocalDateOrNull(userJson.optString("memberSince"));
        timezone = DateTimeZone.forID(userJson.getString("timezone"));
        offsetFromUTCMillis = userJson.optInt("offsetFromUTCMillis");
        locale = userJson.optString("locale");
        avatar = userJson.optString("avatar");

        weightUnit = userJson.optString("weightUnit");
        distanceUnit = userJson.optString("distanceUnit");
        heightUnit = userJson.optString("weightUnit");
        waterUnit = userJson.optString("weightUnit");
        glucoseUnit = userJson.optString("weightUnit");
    }

    public static List<UserInfo> friendJsonArrayToUserInfoList(JSONArray array) throws JSONException {
        List<UserInfo> userInfoList = new ArrayList<UserInfo>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonFriendObject = array.getJSONObject(i);
            userInfoList.add(new UserInfo(jsonFriendObject));
        }
        return userInfoList;
    }


    public String getEncodedId() {
        return encodedId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Gender getGender() {
        return gender;
    }

    public String getDateOfBirth() {
        return null == dateOfBirth ? "" : FitbitApiService.LOCAL_DATE_FORMATTER.print(dateOfBirth);
    }

    public LocalDate dateOfBirth() {
        return dateOfBirth;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public double getStrideLengthWalking() {
        return strideLengthWalking;
    }

    public double getStrideLengthRunning() {
        return strideLengthRunning;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public LocalDate getMemberSince() {
        return memberSince;
    }

    public String getTimezone() {
        return timezone.toString();
    }

    public DateTimeZone timezone() {
        return timezone;
    }

    public int getOffsetFromUTCMillis() {
        return offsetFromUTCMillis;
    }

    public String getLocale() {
        return locale;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getGlucoseUnit() {
        return glucoseUnit;
    }

    public String getWaterUnit() {
        return waterUnit;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public String getWeightUnit() {
        return weightUnit;
    }
}
