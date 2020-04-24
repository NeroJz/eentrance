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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hk.com.uatech.eticket.eticket.EntraceStep3Activity;
import hk.com.uatech.eticket.eticket.Item;
import hk.com.uatech.eticket.eticket.pojo.SeatInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketTrans;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;


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

    /**
     * Get total records in a table
     */
    public void totalRows() {
        Cursor cursor = db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor != null && cursor.getCount() > 0) {
            Log.d(Entrance.class.toString(), String.valueOf(cursor.getCount()));

            int i=1;
            while (cursor.moveToNext()) {
                String trans_id = cursor.getString(cursor.getColumnIndex(TRANS_ID));
                String inout = cursor.getString(cursor.getColumnIndex(INOUT_DATETIME));
                String type = cursor.getString(cursor.getColumnIndex(TYPE));

                Log.d(Entrance.class.toString(), i + "---" +trans_id + "---" + inout + "---" + type);
                i++;
            }
        }
    }


    public JSONArray getEntranceList() throws JSONException {
        JSONArray results = new JSONArray();

        Cursor cursor = db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);


        if(cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String trans_id = cursor.getString(cursor.getColumnIndex(TRANS_ID));
                int is_concession = cursor.getString(cursor.getColumnIndex(IS_CONCESSION)).equals("1") ? 1 : 0;
                String inout_dt = cursor.getString(cursor.getColumnIndex(INOUT_DATETIME));
                String type = cursor.getString(cursor.getColumnIndex(TYPE));

                String cinemaID = PreferencesController.getInstance().getCinemaId();

                JSONObject entrance_json_obj = new hk.com.uatech.eticket.eticket.pojo.Entrance(trans_id, is_concession, inout_dt, type).toJSON();
                entrance_json_obj.put("cinema_id", cinemaID);

                results.put(entrance_json_obj);
            }
        }


        return results;
    }


    public void removeAllRecords() {
        int result = 0;
        String sql = "delete FROM " + TABLE_NAME;

        db.execSQL(sql);

    }


    /**
     * Add records based on individual seat status
     * status - [Valid (Not Admitted (out)), Invalid (Admitted (in))]
     * @param trans_id
     * @param seats
     */
    public void add(String trans_id, List<SeatInfo> seats) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            for(SeatInfo seat : seats) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String inoutDT = sdf.format(new Date());

                values.put(TRANS_ID, trans_id);
                values.put(IS_CONCESSION, seat.isConcession());
                values.put(INOUT_DATETIME, inoutDT);

                if(seat.getSeatStatus().equalsIgnoreCase("invalid")) {
                    values.put(TYPE, "out");
                } else if(seat.getSeatStatus().equalsIgnoreCase("valid")) {
                    values.put(TYPE, "in");
                }

                values.put(CREATED_DATE, inoutDT);

                db.insert(TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
//            totalRows();
        }
    }


    /**
     * Add records with TYPE
     * @param trans_id
     * @param seats
     * @param type
     */
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
//            totalRows();
        }
    }


}
