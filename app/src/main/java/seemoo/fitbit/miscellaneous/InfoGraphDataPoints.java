package seemoo.fitbit.miscellaneous;

import com.jjoe64.graphview.series.DataPoint;

public class InfoGraphDataPoints extends InfoListItem {

    private DataPoint[] datapoints;

    public InfoGraphDataPoints(int size){
        datapoints = new DataPoint[size];
    }
    public InfoGraphDataPoints(DataPoint[] dataPoints){
        this.datapoints = dataPoints;
    }

    public DataPoint[] getDatapoints() {
        return datapoints;
    }

    public void addDataPoint(int pos, DataPoint point){
        datapoints[pos] = point;
    }
}
