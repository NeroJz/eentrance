/**
 * ShowNotifier to notify changes of task
 * Author: JZ
 * Date: 24-02-2020
 * Version: 0.0.1
 */

package hk.com.uatech.eticket.eticket.delegate.showtime;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;

import hk.com.uatech.eticket.eticket.database.Show;
import hk.com.uatech.eticket.eticket.pojo.ShowPojo;

public class ShowtimeNotifier {

    private ShowtimeEvent se;
    private static String FILE_NAME = "show_test.json";
    private final String DIRECTORY = "/ETicket";

    public ShowtimeNotifier(ShowtimeEvent event) {
        se = event;
    }

    /**
     * Read JSON file from external storage
     * and the data into SQLite
     * @param context
     */
    public void doWork(Context context) {
        File directory = new File(Environment.getExternalStorageDirectory(), DIRECTORY);

        File file = new File(directory, FILE_NAME);

        if(!file.exists()) {
            Log.d(ShowtimeNotifier.class.toString(), "File not existed!");
            delegate();
            return;
        }

        try {
            FileInputStream is = new FileInputStream(file);
            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);
            is.close();

            String json_text = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            ShowPojo shows = gson.fromJson(json_text, ShowPojo.class);

            if(shows != null && shows.getShow().length > 0) {
                Show model = new Show(context);

                // Clear existing data
                model.clear();

                // Insert new data
                model.add_shows(shows.getShow());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            delegate();
        }

    }


    private void delegate() {
        if(se != null) {
            se.completeHandler();
        }
    }

}
