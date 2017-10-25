package com.fitbit.api.common.model.body;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class BodyWithGoals {

    private Body body;
    private BodyGoals bodyGoals;

    public BodyWithGoals(Body body, BodyGoals bodyGoals) {
        this.body = body;
        this.bodyGoals = bodyGoals;
    }

    public static BodyWithGoals constructBodyWithGoals(Response res) throws FitbitAPIException, JSONException {
        Body body = new Body(res.asJSONObject().getJSONObject("body"));
        JSONObject goalsJson = res.asJSONObject().optJSONObject("goals");
        BodyGoals bodyGoals = goalsJson != null ? new BodyGoals(goalsJson) : null;
        return new BodyWithGoals(body, bodyGoals);
    }

    public Body getBody() {
        return body;
    }

    public BodyGoals getBodyGoals() {
        return bodyGoals;
    }
}
