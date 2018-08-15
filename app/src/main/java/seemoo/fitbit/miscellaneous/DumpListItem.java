package seemoo.fitbit.miscellaneous;

public abstract class DumpListItem {

    final public static int TEXT_VIEW = 0;
    final public static int GRAPH_VIEW = 1;

    private int type = TEXT_VIEW;

    public DumpListItem() {
    }

    public int getItemType() {
        return type;
    }

    public DumpListItem getItem() {
        return this;
    }

    public void setType(int type) {
        this.type = type;
    }
}
