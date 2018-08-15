package seemoo.fitbit.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import seemoo.fitbit.R;

public class StepsGraphDialog extends Dialog {
    public StepsGraphDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_dump_info_graph);
        setTitle("Your step graph");

        GraphView graph = (GraphView) findViewById(R.id.graph_steps);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });
        graph.addSeries(series);
    }
}
