package seemoo.fitbit.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;

import seemoo.fitbit.R;

public class StepsGraphDialog extends Dialog {
    public StepsGraphDialog(@NonNull Context context, DataPoint[] datapoints) {
        super(context);
        setContentView(R.layout.dialog_dump_info_graph);
        setTitle("Your step graph");

        GraphView graph = (GraphView) findViewById(R.id.graph_steps);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(datapoints);

        series.setDrawDataPoints(true);

        graph.addSeries(series);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(datapoints[0].getX());
        graph.getViewport().setMaxX(datapoints[datapoints.length-1].getX());
        graph.getViewport().setXAxisBoundsManual(true);

        // set manual y bounds to have nice steps
        graph.getViewport().setMinY(0);
        graph.getViewport().setYAxisBoundsManual(true);



        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graph.getGridLabelRenderer().setHumanRounding(false);

    }
}
