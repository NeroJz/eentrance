package hk.com.uatech.eticket.eticket;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Model {

    public static SQLiteDatabase db;

    public Model(Context context) {
        db = DBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

}
