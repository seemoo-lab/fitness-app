package seemoo.fitbit.miscellaneous;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import seemoo.fitbit.R;
import seemoo.fitbit.information.Information;

public class InfoArrayAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<DumpListItem> itemList;

    public InfoArrayAdapter(Context context, ArrayList<DumpListItem> objects) {
        ctx = context;
        itemList = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        int type = getItemViewType(position);
        if (v == null) {
            // Inflate the layout according to the view type
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (type == DumpListItem.TEXT_VIEW) {
                // Inflate the layout with image
                v = inflater.inflate(R.layout.listitem_textline, parent, false);
                TextView text = (TextView) v.findViewById(R.id.dump_text);
                DumpListItem dumpListItem = itemList.get(position).getItem();
                Information info = (Information) dumpListItem.getItem();
                text.setText(info.toString());
            }
            else {
                v = inflater.inflate(R.layout.listitem_dumpgraph, parent, false);

                DumpListItem dumpListItem = itemList.get(position);

                DumpGraphDataPoints dgDataPoints = (DumpGraphDataPoints) dumpListItem.getItem();
                DataPoint[] dataPoints = dgDataPoints.getDatapoints();

                GraphView graph = (GraphView) v.findViewById(R.id.graph);

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

                series.setDrawDataPoints(true);

                graph.addSeries(series);

                // set date label formatter
                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(ctx));
                graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

                // set manual x bounds to have nice steps
                graph.getViewport().setMinX(dataPoints[0].getX());
                graph.getViewport().setMaxX(dataPoints[dataPoints.length-1].getX());
                graph.getViewport().setXAxisBoundsManual(true);

                // set manual y bounds to have nice steps
                graph.getViewport().setMinY(0);
                graph.getViewport().setYAxisBoundsManual(true);

                // as we use dates as labels, the human rounding to nice readable numbers
                // is not necessary
                graph.getGridLabelRenderer().setHumanRounding(false);
            }
        }
        return v;
    }


    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getItemType();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        // just returns ListView position
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

}
