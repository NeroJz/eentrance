package hk.com.uatech.eticket.eticket.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import hk.com.uatech.eticket.eticket.DBHelper;
import hk.com.uatech.eticket.eticket.Model;

public class Show extends Model {
    // TABLE NAME
    public static final String TABLE_NAME = "eent_show";

    // SHOW DETAILS
    public static final String SHOW_ID = "show_id";
    public static final String SHOW_DATE = "show_date";
    public static final String SHOW_NO = "show_no";

    // HOUSE DETAILS
    public static final String HOUSE_ID = "house_id";
    public static final String HOUSE_ENAME = "house_ename";
    public static final String HOUSE_CNAME = "house_cname";

    // CINEMA DETAILS
    public static final String CINEMA_ID = "cinema_id";
    public static final String CINEMA_ENAME = "cinema_ename";
    public static final String CINEMA_CNAME = "cinema_cname";

    // MOVIE DETAILS
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_ENAME = "movie_ename";
    public static final String MOVIE_CNAME = "movie_cname";
    public static final String DURATION = "duration";
    public static final String MV_ATTRIBUTES = "mv_attributes";

    // TICKET GROUP DETAILS
    public static final String TG_CODE = "tg_code";
    public static final String TG_ENAME = "tg_ename";
    public static final String TG_CNAME = "tg_cname";

    public static final String CREATED_DATE = "created_date";

    public static final String CREATE_TABLE =
            "CREATE TABLE " +
                    TABLE_NAME + " (" +
                    SHOW_ID + " INTEGER PRIMARY KEY, " +
                    SHOW_DATE + " TEXT, " +
                    SHOW_NO + " INTEGER, " +

                    // House
                    HOUSE_ID + " INTEGER, " +
                    HOUSE_ENAME + " TEXT, " +
                    HOUSE_CNAME + " TEXT, " +

                    // Cinema
                    CINEMA_ID + " INTEGER, " +
                    CINEMA_ENAME + " TEXT, " +
                    CINEMA_CNAME + " TEXT, " +

                    // Movie
                    MOVIE_ID + " INTEGER, " +
                    MOVIE_ENAME + " TEXT, " +
                    MOVIE_CNAME + " TEXT, " +
                    DURATION + " INTEGER, " +
                    MV_ATTRIBUTES + " TEXT, " +

                    // Ticket group
                    TG_CODE + " TEXT, " +
                    TG_ENAME + " TEXT, " +
                    TG_CNAME + " TEXT, " +

                    CREATED_DATE + " TEXT)";


    public static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public Show(Context context) {
        super(context);
    }


    public long getNoRows() {
        Cursor cursor = db.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
                );

        return cursor.getCount();
    }


    public void add_shows(hk.com.uatech.eticket.eticket.pojo.Show[] shows) {
        Log.d(Show.class.toString(), "Start Add Shows");
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();

            for(hk.com.uatech.eticket.eticket.pojo.Show show : shows) {
                cv.put(SHOW_ID, show.getId());
                cv.put(SHOW_DATE, show.getDate());
                cv.put(SHOW_NO, show.getNo());

                cv.put(HOUSE_ID, show.getHouse().getId());
                cv.put(HOUSE_ENAME, show.getHouse().getName().getEn());
                cv.put(HOUSE_CNAME, show.getHouse().getName().getZh());

                cv.put(CINEMA_ID, show.getCinema().getId());
                cv.put(CINEMA_ENAME, show.getCinema().getName().getEn());
                cv.put(CINEMA_CNAME, show.getCinema().getName().getZh());

                cv.put(MOVIE_ID, show.getMovie().getId());
                cv.put(MOVIE_ENAME, show.getMovie().getName().getEn());
                cv.put(MOVIE_CNAME, show.getMovie().getName().getZh());
                cv.put(DURATION, show.getMovie().getDuration());
                cv.put(MV_ATTRIBUTES, show.getMovie().getAttribute());

                cv.put(TG_CODE, show.getTicket_group().getCode());
                cv.put(TG_ENAME, show.getTicket_group().getName().getEn());
                cv.put(TG_CNAME, show.getTicket_group().getName().getZh());

                db.insert(TABLE_NAME, null, cv);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.d(Show.class.toString(), "Completed bulk insertion");
            Log.d(Show.class.toString(), String.valueOf(getNoRows()));
        }
    }



    /**
     * Clear the table
     */
    public void clear() {
        int deleteRows = db.delete(TABLE_NAME, null, null);

        Log.d(Show.class.toString() + " Delete", String.valueOf(deleteRows));
    }

}
