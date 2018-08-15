package seemoo.fitbit.information;

import seemoo.fitbit.miscellaneous.DumpListItem;

/**
 * A piece of information.
 */
public class Information extends DumpListItem {

    private final String TAG = this.getClass().getSimpleName();

    private String data;

    /**
     * Creates a piece of infromation.
     *
     * @param data The data of the information.
     */
    public Information(String data) {
        this.data = data;
    }

    /**
     * Returns the data of the information.
     *
     * @return The data.
     */
    protected String getData() {
        return data;
    }

    /**
     * Returns the data of the information as a string.
     *
     * @return The data.
     */
    public String toString() {
        return data;
    }

    /**
     * {@inheritDoc}
     * Returns true, if the data of the object is equal to the data of this information.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Information)) {
            return false;
        }
        return data.equals(((Information) obj).getData());
    }

}
