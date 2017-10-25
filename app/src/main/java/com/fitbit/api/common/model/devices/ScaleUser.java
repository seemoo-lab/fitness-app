package com.fitbit.api.common.model.devices;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import com.fitbit.api.common.model.user.UserInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScaleUser {

    private String userId;
    private String scaleUserName;
    private BodyType bodyType;
    private UserInfo userInfo;

    public ScaleUser(JSONObject jsonObject) throws JSONException {
        userId = jsonObject.getString("userId");
        scaleUserName = jsonObject.getString("scaleUserName");
        bodyType = BodyType.valueOf(jsonObject.getString("bodyType"));
        userInfo = new UserInfo(jsonObject.getJSONObject("userInfo"), false);
    }

    public static List<ScaleUser> jsonArrayToScaleUsersList(JSONArray array) throws JSONException {
        List<ScaleUser> scaleUsersList = new ArrayList<ScaleUser>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject scaleUserReference = array.getJSONObject(i);
            scaleUsersList.add(new ScaleUser(scaleUserReference));
        }
        return scaleUsersList;
    }

    public String getUserId() {
        return userId;
    }

    public String getScaleUserName() {
        return scaleUserName;
    }

    public String getBodyType() {
        return bodyType.name();
    }

    public BodyType bodyType() {
        return bodyType;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
