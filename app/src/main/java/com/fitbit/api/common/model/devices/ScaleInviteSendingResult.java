package com.fitbit.api.common.model.devices;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScaleInviteSendingResult {

    private boolean success;
    private String email;
    private String message;
    private ScaleInvite scaleInvite;

    public ScaleInviteSendingResult(JSONObject json) throws JSONException {
        success = json.getBoolean("success");
        email = json.getString("email");
        if (json.has("message")) {
            message = json.getString("message");
        }
        if (json.has("scaleInvite")) {
            scaleInvite = new ScaleInvite(json.getJSONObject("scaleInvite"));
        }
    }

    public static List<ScaleInviteSendingResult> jsonArrayToScaleInviteSendingResultsList(JSONArray array) throws JSONException {
        List<ScaleInviteSendingResult> scaleInviteSendingResultsList = new ArrayList<ScaleInviteSendingResult>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject scaleInviteSendingResult = array.getJSONObject(i);
            scaleInviteSendingResultsList.add(new ScaleInviteSendingResult(scaleInviteSendingResult));
        }
        return scaleInviteSendingResultsList;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public ScaleInvite getScaleInvite() {
        return scaleInvite;
    }
}
