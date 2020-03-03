/**
 * Entrance model
 * Author: Jz
 * Date: 02-03-2020
 * Version: 0.0.1
 */

package hk.com.uatech.eticket.eticket.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hk.com.uatech.eticket.eticket.Item;
import hk.com.uatech.eticket.eticket.pojo.SeatInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketTrans;


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


    public void totalRows() {
        Cursor cursor = db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor != null) {
            Log.d(Entrance.class.toString(), String.valueOf(cursor.getCount()));
        }
    }


    public void add(String trans_id, List<SeatInfo> seats, String type) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            for(SeatInfo seat : seats) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String inoutDT = sdf.format(new Date());

                values.put(TRANS_ID, trans_id);
                values.put(IS_CONCESSION, seat.isConcession());
                values.put(INOUT_DATETIME, inoutDT);
                values.put(TYPE, type);
                values.put(CREATED_DATE, inoutDT);

                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            totalRows();
        }
    }


}
