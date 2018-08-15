package seemoo.fitbit.information;

import android.widget.ListView;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

import seemoo.fitbit.miscellaneous.InfoGraphDataPoints;
import seemoo.fitbit.miscellaneous.InfoListItem;


/**
 * A list of information.
 */
public class InformationList {

    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<InfoListItem> list = new ArrayList<>();
    private String name;
    private boolean alreadyUploaded = false;

    /**
     * Creates a list of information.
     *
     * @param name The name of the list.
     */
    public InformationList(String name) {
        this.name = name;
    }

    /**
     * Adds a piece of information to this list.
     *
     * @param infoListItem The information to add.
     */
    public void add(InfoListItem infoListItem) {
        list.add(infoListItem);
    }

    /**
     * Adds all pieces of information of the given information list to this list.
     *
     * @param informationList The information list containing the pieces to add.
     */
    public void addAll(InformationList informationList) {
        for (int i = 0; i < informationList.size(); i++) {
            list.add(informationList.get(i));
        }
    }

    /**
     * The size of this information list.
     *
     * @return The size.
     */
    public int size() {
        return list.size();
    }

    /**
     * The list of this information list.
     *
     * @return The list.
     */
    public ArrayList<InfoListItem> getList() {
        return list;
    }

    //

    /**
     * Overrides the current list with the one given.
     * Use only for information lists which should be displayed on as ListView.
     *
     * @param informationList The information list with the new data.
     * @param listView        The list view object to show the data.
     */
    public void override(InformationList informationList, ListView listView) {
        list.clear();
        list.addAll(informationList.getList());
        listView.invalidateViews();
    }

    /**
     * Sets a piece of infoListItem into the list at a certain position.
     *
     * @param position    The position to set the piece of information.
     * @param infoListItem The infoListItem to set.
     */
    public void set(int position, InfoListItem infoListItem) {
        list.set(position, infoListItem);
    }

    /**
     * Returns a piece of information from the given position in the list.
     *
     * @param position The position of the information.
     * @return The information.
     */
    public InfoListItem get(int position) {
        return list.get(position);
    }

    /**
     * Returns the name of this list.
     *
     * @return The name of this list.
     */
    public String getName() {
        return name;
    }

    /**
     * Removes a piece of information from this list at the given position.
     *
     * @param position The position to remove.
     */
    public void remove(int position) {
        list.remove(position);
    }

    /**
     * Removes several pieces of information from the start to the end position.
     *
     * @param start The start position.
     * @param end   The end position.
     */
    public void remove(int start, int end) {
        for (int i = 0; i < end - start; i++) {
            remove(start);
        }
    }

    /**
     * Concatenates all information of this list and returns it as string.
     *
     * @return The concatenated string.
     */
    public String getData() {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            InfoListItem item = list.get(i);
            if(item.getItemType() == InfoListItem.TEXT_VIEW) {
                Information infoItem = (Information) item;
                result = result + infoItem.toString();
            }
        }
        return result;
    }

    /**
     * Concatenates all information of this list in a beauty way and returns it as string.
     *
     * @return The beautiful concatenated string.
     */
    public String getBeautyData() {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            InfoListItem item = list.get(i);
            if(item.getItemType() == InfoListItem.TEXT_VIEW) {
                Information infoItem = (Information) item;
                result = result + infoItem.toString() + "\n";
            }
        }
        return result;
    }

    /**
     * Returns the position of a piece of information in this list.
     *
     * @param infoListItem The information.
     * @return The position of the information.
     */
    public int getPosition(InfoListItem infoListItem) {
        return list.indexOf(infoListItem);
    }

    /**
     * Returns the value of already uploaded.
     * This variable shows, if this information was already uploaded to the fitbit server in the past.
     *
     * @return The value of already uploaded.
     */
    public boolean getAlreadyUploaded() {
        return alreadyUploaded;
    }

    /**
     * Sets the value of already uploaded.
     * This variable shows, if this information was already uploaded to the fitbit server in the past.
     *
     * @param value The value to set already uploaded to.
     */
    public void setAlreadyUploaded(boolean value) {
        alreadyUploaded = value;
    }
}
