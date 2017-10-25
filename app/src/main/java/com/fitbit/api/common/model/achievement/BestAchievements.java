package com.fitbit.api.common.model.achievement;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 10/18/11
 * Time: 12:55 AM
 */
public class BestAchievements {

    private BestAchievement tracker;
    private BestAchievement total;

    public BestAchievements(BestAchievement tracker, BestAchievement total) {
        this.tracker = tracker;
        this.total = total;
    }

    public BestAchievements(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("tracker")) {
            tracker = new BestAchievement(jsonObject.getJSONObject("tracker"));
        } else {
            tracker = null;
        }
        if (jsonObject.has("tracker")) {
            total = new BestAchievement(jsonObject.getJSONObject("total"));
        } else {
            total = null;
        }
    }

    public BestAchievement getTracker() {
        return tracker;
    }

    public BestAchievement getTotal() {
        return total;
    }
}