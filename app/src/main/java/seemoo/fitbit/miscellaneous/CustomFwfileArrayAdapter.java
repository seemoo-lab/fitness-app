package seemoo.fitbit.miscellaneous;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import seemoo.fitbit.R;

public class CustomFwfileArrayAdapter extends ArrayAdapter<FirmwareFileDescriptor> {

    private Context context;
    private ArrayList<FirmwareFileDescriptor> fileArrayList;

    public CustomFwfileArrayAdapter(Context context, ArrayList<FirmwareFileDescriptor> fileArrayList) {
        super(context, R.layout.listitem_fwdownloadfile, fileArrayList);

        this.context = context;
        this.fileArrayList = fileArrayList;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.listitem_fwdownloadfile, parent, false);

        // 3. Get the two text view from the rowView
        TextView fwdevicename = (TextView) rowView.findViewById(R.id.tv_fwdevicename);
        TextView fwversion = (TextView) rowView.findViewById(R.id.tv_fwversion);
        TextView fwshortname = (TextView) rowView.findViewById(R.id.tv_fwshortname);
        TextView fwdescription = (TextView) rowView.findViewById(R.id.tv_fwdescription);

        Resources res = context.getResources();
        // 4. Set the text for textView
        fwshortname.setText(fileArrayList.get(position).getFwshortname());
        fwversion.setText(String.format(res.getString(R.string.fwversion_text), fileArrayList.get(position).getVersion()));
        fwdevicename.setText(String.format(res.getString(R.string.device_text), fileArrayList.get(position).getDeviceName()));
        fwdescription.setText(fileArrayList.get(position).getDescription());

        // 5. retrn rowView
        return rowView;
    }
}
