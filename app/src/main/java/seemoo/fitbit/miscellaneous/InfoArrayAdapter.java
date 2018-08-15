package seemoo.fitbit.miscellaneous;

import android.content.Context;
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
import java.util.Calendar;
import java.util.Date;

import seemoo.fitbit.R;
import seemoo.fitbit.information.Information;

public class InfoArrayAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<InfoListItem> itemList;

    public InfoArrayAdapter(Context context, ArrayList<InfoListItem> objects) {
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
            if (type == InfoListItem.TEXT_VIEW) {
                // Inflate the layout with image
                v = inflater.inflate(R.layout.listitem_textline, parent, false);
                TextView text = (TextView) v.findViewById(R.id.dump_text);
                InfoListItem infoListItem = itemList.get(position).getItem();
                Information info = (Information) infoListItem.getItem();
                text.setText(info.toString());
            }
            else {
                v = inflater.inflate(R.layout.listitem_dumpgraph, parent, false);

                InfoListItem infoListItem = itemList.get(position);

                InfoGraphDataPoints dgDataPoints = (InfoGraphDataPoints) infoListItem.getItem();
                DataPoint[] dataPoints = dgDataPoints.getDatapoints();
/*
                // TODO: remove, only used for screenshot:
                dataPoints = new DataPoint[5];
                Calendar calendar = Calendar.getInstance();
                Date d1 = calendar.getTime();
                calendar.add(Calendar.DATE, 1);
                Date d2 = calendar.getTime();
                calendar.add(Calendar.DATE, 1);
                Date d3 = calendar.getTime();
                calendar.add(Calendar.DATE, 1);
                Date d4 = calendar.getTime();
                calendar.add(Calendar.DATE, 1);
                Date d5 = calendar.getTime();
                dataPoints[0] = new DataPoint(d1, 1977);
                dataPoints[1] = new DataPoint(d2, 7471);
                dataPoints[2] = new DataPoint(d3, 4801);
                dataPoints[3] = new DataPoint(d4, 6051);
                dataPoints[4] = new DataPoint(d5, 6801);
*/
                GraphView graph = (GraphView) v.findViewById(R.id.graph);

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

                series.setDrawDataPoints(true);

                graph.addSeries(series);

                // set date label formatter
                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(ctx));
                if(dataPoints.length>3) {
                    graph.getGridLabelRenderer().setNumHorizontalLabels(4); // max 4 because of the space
                }else{
                    graph.getGridLabelRenderer().setNumHorizontalLabels(dataPoints.length);
                }

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
