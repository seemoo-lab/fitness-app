package seemoo.fitbit.miscellaneous;

import com.jjoe64.graphview.series.DataPoint;

public class DumpGraphDataPoints extends DumpListItem {

    private DataPoint[] datapoints;

    public DumpGraphDataPoints(int size){
        datapoints = new DataPoint[size];
    }
    public DumpGraphDataPoints(DataPoint[] dataPoints){
        this.datapoints = dataPoints;
    }

    public DataPoint[] getDatapoints() {
        return datapoints;
    }

    public void addDataPoint(int pos, DataPoint point){
        datapoints[pos] = point;
    }
}
