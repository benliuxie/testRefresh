package benliu.com.test.currencyconverter;

/**
 * Created by beliu on 2017-06-15.
 */

public class TableItems {
    public static final String TABLENAME = TableItems.class.getSimpleName().toLowerCase();
    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String RATE = "rate";


    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLENAME +
                    " ( " +
                    _ID + " integer primary key autoincrement, " +
                    NAME + " text, " +
                    RATE + " real " +
                    " ); ";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLENAME;
    public static String[] Columns = new String[]{_ID, NAME, RATE};
}

