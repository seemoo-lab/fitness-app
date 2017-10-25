package com.fitbit.api.common.model.achievement;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 10/18/11
 * Time: 12:55 AM
 */
public class LifetimeAchievements {

    private Achievement tracker;
    private Achievement total;

    public LifetimeAchievements(Achievement tracker, Achievement total) {
        this.tracker = tracker;
        this.total = total;
    }

    public LifetimeAchievements(JSONObject jsonObject) throws JSONException {
        tracker = new Achievement(jsonObject.getJSONObject("tracker"));
        total = new Achievement(jsonObject.getJSONObject("total"));
    }

    public Achievement getTracker() {
        return tracker;
    }

    public Achievement getTotal() {
        return total;
    }
}