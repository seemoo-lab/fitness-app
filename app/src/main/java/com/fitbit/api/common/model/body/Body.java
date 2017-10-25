package com.fitbit.api.common.model.body;

import org.json.JSONException;
import org.json.JSONObject;

public class Body {
    private double weight;
    private double bmi;
    private double fat;
    private double neck;
    private double bicep;
    private double forearm;
    private double chest;
    private double waist;
    private double hips;
    private double thigh;
    private double calf;

    public Body(double weight, double fat, double neck, double bicep, double forearm, double chest, double waist, double hips, double thigh, double calf) {
        this.weight = weight;
        this.fat = fat;
        this.neck = neck;
        this.bicep = bicep;
        this.forearm = forearm;
        this.chest = chest;
        this.waist = waist;
        this.hips = hips;
        this.thigh = thigh;
        this.calf = calf;
    }

    public Body(JSONObject bodyJson) throws JSONException {
        weight = bodyJson.getDouble("weight");
        bmi = bodyJson.getDouble("bmi");
        fat = bodyJson.getDouble("fat");
        neck = bodyJson.getDouble("neck");
        bicep = bodyJson.getDouble("bicep");
        forearm = bodyJson.getDouble("forearm");
        chest = bodyJson.getDouble("chest");
        waist = bodyJson.getDouble("waist");
        hips = bodyJson.getDouble("hips");
        thigh = bodyJson.getDouble("thigh");
        calf = bodyJson.getDouble("calf");
    }

    public double getWeight() {
        return weight;
    }

    public double getBmi() {
        return bmi;
    }

    public double getFat() {
        return fat;
    }

    public double getNeck() {
        return neck;
    }

    public double getBicep() {
        return bicep;
    }

    public double getForearm() {
        return forearm;
    }

    public double getChest() {
        return chest;
    }

    public double getWaist() {
        return waist;
    }

    public double getHips() {
        return hips;
    }

    public double getThigh() {
        return thigh;
    }

    public double getCalf() {
        return calf;
    }
}
