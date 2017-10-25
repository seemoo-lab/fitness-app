package com.fitbit.api.common.model.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendStats {
    private UserInfo user;
    private StatisticInfo summary;
    private StatisticInfo average;

    public FriendStats(UserInfo user, StatisticInfo summary, StatisticInfo average) {
        this.user = user;
        this.summary = summary;
        this.average = average;
    }

    public FriendStats(JSONObject friendStatsJson) throws JSONException {
        this.user = new UserInfo(friendStatsJson);
        this.summary = new StatisticInfo(friendStatsJson.getJSONObject("summary"));
        this.average = new StatisticInfo(friendStatsJson.getJSONObject("average"));
    }

    public static List<FriendStats> jsonArrayToFriendStatsList(JSONArray array) throws JSONException {
        List<FriendStats> friendStatsList = new ArrayList<FriendStats>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonFriendStatsObject = array.getJSONObject(i);
            friendStatsList.add(new FriendStats(jsonFriendStatsObject));
        }
        return friendStatsList;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public StatisticInfo getSummary() {
        return summary;
    }

    public void setSummary(StatisticInfo summary) {
        this.summary = summary;
    }

    public StatisticInfo getAverage() {
        return average;
    }

    public void setAverage(StatisticInfo average) {
        this.average = average;
    }

    public static class StatisticInfo {
        private int steps;
        private int calories;
        private double distance;

        public StatisticInfo() {

        }

        public StatisticInfo(int steps, int calories, double distance) {
            this.steps = steps;
            this.calories = calories;
            this.distance = distance;
        }

        public StatisticInfo(JSONObject statisticInfoJson) throws JSONException {
            this.steps = statisticInfoJson.getInt("steps");
            this.calories = statisticInfoJson.getInt("calories");
            this.distance = statisticInfoJson.getDouble("distance");
        }

        public int getSteps() {
            return steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public int getCalories() {
            return calories;
        }

        public void setCalories(int calories) {
            this.calories = calories;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

    }
}
