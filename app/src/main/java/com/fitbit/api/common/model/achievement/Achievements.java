package com.fitbit.api.common.model.achievement;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 1/6/12
 * Time: 2:13 PM
 */
public class Achievements {

    private LifetimeAchievements lifetimeAchievements;
    private BestAchievements bestAchievements;

    public Achievements(LifetimeAchievements lifetimeAchievements, BestAchievements bestAchievements) {
        this.lifetimeAchievements = lifetimeAchievements;
        this.bestAchievements = bestAchievements;
    }

    public static Achievements constructAchievements(Response res) throws FitbitAPIException, JSONException {
        JSONObject jsonObject = res.asJSONObject();
        LifetimeAchievements lifetimeAchievements = new LifetimeAchievements(jsonObject.getJSONObject("lifetime"));
        BestAchievements bestAchievements = null;
        if(jsonObject.has("best")) {
            bestAchievements = new BestAchievements(jsonObject.getJSONObject("best"));
        }
        return new Achievements(lifetimeAchievements, bestAchievements);
    }

    public LifetimeAchievements getLifetimeAchievements() {
        return lifetimeAchievements;
    }

    public BestAchievements getBestAchievements() {
        return bestAchievements;
    }
}
