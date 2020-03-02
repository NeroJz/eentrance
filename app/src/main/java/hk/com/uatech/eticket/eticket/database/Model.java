package hk.com.uatech.eticket.eticket.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import hk.com.uatech.eticket.eticket.DBHelper;

public class Model {

    public static SQLiteDatabase db;

    public Model(Context context) {
        db = DBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

}
