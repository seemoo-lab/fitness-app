package com.fitbit.api.common.model.foods;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import com.fitbit.api.common.service.FitbitApiService;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: gkutlu
 * Date: Jul 24, 2010
 * Time: 1:55:43 AM
 */
public class FoodLog {
    protected final long logId;
    protected final LoggedFood loggedFood;
    protected final NutritionalValues nutritionalValues;
    protected final boolean isFavorite;
    private LocalDate logDate;

    public FoodLog(long logId, LoggedFood loggedFood, NutritionalValues nutritionalValues, boolean favorite) {
        this.logId = logId;
        this.loggedFood = loggedFood;
        this.nutritionalValues = nutritionalValues;
        isFavorite = favorite;
    }

    public FoodLog(long logId, LoggedFood loggedFood, NutritionalValues nutritionalValues, boolean favorite, LocalDate logDate) {
        this(logId, loggedFood, nutritionalValues, favorite);
        this.logDate = logDate;
    }

    public FoodLog(JSONObject json) throws JSONException {
        logId = json.getLong("logId");
        loggedFood = new LoggedFood(json.getJSONObject("loggedFood"));
        nutritionalValues = new NutritionalValues(json.getJSONObject("nutritionalValues"));
        isFavorite = json.getBoolean("isFavorite");
        logDate = FitbitApiService.getValidLocalDateOrNull(json.getString("logDate"));
    }

    public static List<FoodLog> constructFoodlogList(Response res) throws FitbitAPIException {
        return constructFoodLogList(res, "foods");
    }

    public static List<FoodLog> constructFoodLogList(Response res, String arrayName) throws FitbitAPIException {
        try {
            JSONObject json = res.asJSONObject();
            return jsonArrayToFoodLogList(json.getJSONArray(arrayName));
         } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    static List<FoodLog> jsonArrayToFoodLogList(JSONArray array) throws JSONException {
        List<FoodLog> foodList = new ArrayList<FoodLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject food = array.getJSONObject(i);
            foodList.add(new FoodLog(food));
        }
        return foodList;
    }

    public final long getLogId() {
        return logId;
    }

    public final LoggedFood getLoggedFood() {
        return loggedFood;
    }

    public final NutritionalValues getNutritionalValues() {
        return nutritionalValues;
    }

    public final boolean isFavorite() {
        return isFavorite;
    }

    /**
     * Returns the log date if it is set. Throws an exception if it is not set.
     * <p>
     * Typically, the API will not return the log date when the request narrows the log date to a single value.
     * This is the case, e.g., when the list of logged resources on a given date is requested. 
     * </p>
     * @return the log date if it is set.
     * @throws UnsupportedOperationException if parsing is not supported
     */
    public LocalDate logDate() {
        if (null == logDate) {
            throw new UnsupportedOperationException("Log date is not available. This is an error only if log date was expected.");
        }
        return logDate;
    }

    public String getLogDate() {
        return null == logDate ? "" : FitbitApiService.LOCAL_DATE_FORMATTER.print(logDate);
    }
}