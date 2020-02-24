package hk.com.uatech.eticket.eticket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex_ on 08/09/2017.
 */

public class OfflineDatabase extends Model {

    // 表格名稱
    public static final String TABLE_NAME = "test_data";
    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";
    // 其它表格欄位名稱
    public static final String REF_NO_COLUMN = "ref_no";
    public static final String SEAT_ID_COLUMN = "seat_id";
    public static final String TICKET_TYPE_COLUMN = "ticket_type";
    public static final String SEAT_STATUS_COLUMN = "status";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    REF_NO_COLUMN + " TEXT NOT NULL, " +
                    SEAT_ID_COLUMN + " TEXT NOT NULL, " +
                    TICKET_TYPE_COLUMN + " TEXT NOT NULL, " +
                    SEAT_STATUS_COLUMN + " TEXT NOT NULL)";

    public static final String INDEX_1 = "CREATE UNIQUE INDEX data_idx ON  " + TABLE_NAME  + " (" + REF_NO_COLUMN + ", " + SEAT_ID_COLUMN + ");";


    // 資料庫物件
//    private SQLiteDatabase db;
    // 建構子，一般的應用都不需要修改
    public OfflineDatabase(Context context){
        super(context);
//        db = DBHelper.getDatabase(context);
    }
    // 關閉資料庫，一般的應用都不需要修改
//    public void close() {
//        db.close();
//    }



    // 新增參數指定的物件
    public Item insert(Item item) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();
        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(REF_NO_COLUMN, item.getRefNo());
        cv.put(SEAT_ID_COLUMN, item.getSeatId());
        cv.put(TICKET_TYPE_COLUMN, item.getTicketType());
        cv.put(SEAT_STATUS_COLUMN, item.getSeatStatus());
        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);
        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }
    // 修改參數指定的物件
    public boolean update(Item item) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();
        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(REF_NO_COLUMN, item.getRefNo());
        cv.put(SEAT_ID_COLUMN, item.getSeatId());
        cv.put(TICKET_TYPE_COLUMN, item.getTicketType());
        cv.put(SEAT_STATUS_COLUMN, item.getSeatStatus());
        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }
    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }



    public void removeRecordsByRefNo(String refNo) {
        int result = 0;
        String sql = "delete FROM " + TABLE_NAME +  " where " + REF_NO_COLUMN + " = '" + refNo + "' ";

        db.execSQL(sql);

    }

    public void removeAllRecords() {
        int result = 0;
        String sql = "delete FROM " + TABLE_NAME;

        db.execSQL(sql);

    }

    // 讀取所有記事資料
    public List<Item> getAll() {
        List<Item> result = new ArrayList<Item>();
        //游標指向該資料表
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        //將所有資料轉成Item並添加進List
        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }
        //關閉游標
        cursor.close();
        return result;
    }
    // 取得指定編號的資料物件
    public Item get(long id) {
        // 準備回傳結果用的物件
        Item item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }
    // 把游標Cursor取得的資料轉換成目前的資料包裝為物件
    public Item getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        Item result = new Item();
        result.setId(cursor.getLong(0));
        result.setRefNo(cursor.getString(1));
        result.setSeatId(cursor.getString(2));
        result.setTicketType(cursor.getString(3));
        result.setSeatStatus(cursor.getString(4));

        return result;
    }

    public List<String> getDistinctRefNo() {

        // 準備回傳結果用的物件
        ArrayList items = new ArrayList();
        String name = "";
        // 使用編號為查詢條件

        // 執行查詢
        //Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);
        Cursor result = db.rawQuery("SELECT distinct " + REF_NO_COLUMN + " FROM " + TABLE_NAME + " ", null);
        // 如果有查詢結果
       // if (result.moveToFirst()) {
        while (result.moveToNext()) {
            // 讀取包裝一筆資料的物件
            name = result.getString(0); //getRecord(result);

            items.add(name);
        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return items;

    }

    public List<Item> getRecordByRefNo(String refNo) {
        // 準備回傳結果用的物件
        ArrayList items = new ArrayList();
        Item item = null;
        // 使用編號為查詢條件

        // 執行查詢
        //Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME + " where " + REF_NO_COLUMN + " = '" + refNo + "'", null);
        // 如果有查詢結果

        while (result.moveToNext()) {
            item = getRecord(result);

            items.add(item);
        }


        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return items;
    }

    public Item getRecordBySeatId(String refNo, String seatId) {
        // 準備回傳結果用的物件

        Item item = null;
        // 使用編號為查詢條件

        // 執行查詢
        //Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME + " where " + REF_NO_COLUMN + " = '" + refNo + "' and " + SEAT_ID_COLUMN + " = '" + seatId + "'", null);
        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);


        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    public int getCountByRefNo(String refNo) {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " where " + REF_NO_COLUMN + " = '" + refNo + "'", null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    // Batch Update / Insert
    public void accept(List<Item> items) throws Exception {

        if (items != null) {


            db.beginTransaction();
            try {
                for (int j = 0; j < items.size(); j++) {
                    Item item = items.get(j);

                    String sql = "INSERT OR REPLACE INTO " + TABLE_NAME +
                                " ( " +
                                 KEY_ID + "," +
                                 REF_NO_COLUMN + "," +
                                 SEAT_ID_COLUMN + "," +
                                 TICKET_TYPE_COLUMN + "," +
                                SEAT_STATUS_COLUMN +
                            " ) " +
                            "   VALUES ( " +
                                " NULL, " +
                                "'" + item.getRefNo() + "' ," +
                                "'" + item.getSeatId() + "' ," +
                                "'" + item.getTicketType() + "' ," +
                                "'" + item.getSeatStatus() + "' " +
                            ");";

                    db.execSQL(sql);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    // 建立範例資料
    public void sample() {

    }
}
