/**
 * Entrance model
 * Author: Jz
 * Date: 02-03-2020
 * Version: 0.0.1
 */

package hk.com.uatech.eticket.eticket.database;


import android.content.Context;

public class Entrance  extends Model{

    // TABLE NAME
    public static final String TABLE_NAME = "entrance";

    // COLUMNS
    public static final String TRANS_ID = "trans_id";
    public static final String IS_CONCESSION = "is_concession";
    public static final String INOUT_DATETIME = "inout_datetime";
    public static final String TYPE = "type";
    public static final String CREATED_DATE = "created_date";


    public static final String CREATE_TABLE =
            "CREATE TABLE " +
                    TABLE_NAME + " (" +
                    TRANS_ID + " TEXT NOT NULL, " +
                    IS_CONCESSION + " INTEGER DEFAULT 0, " +
                    INOUT_DATETIME + " TEXT NOT NULL," +
                    TYPE + " TEXT NOT NULL," +
                    CREATED_DATE + " TEXT NOT NULL)";


    public static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public Entrance(Context context) { super(context); }


}
