package hk.com.uatech.eticket.eticket.delegate.entrance_log;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hk.com.uatech.eticket.eticket.delegate.DelegateType;
import hk.com.uatech.eticket.eticket.pojo.Entrance;
import hk.com.uatech.eticket.eticket.pojo.EntranceLog;
import hk.com.uatech.eticket.eticket.pojo.EntranceLogInput;

public class EntranceLogNotifier {

    private EntranceLogEvent event;
    private final String FILE_NAME = "entrance_log_";
    private final String DIR = "/ETicket";
    private final String FILE_EXT = ".json";

    public EntranceLogNotifier(EntranceLogEvent event) {
        this.event = event;
    }

    public void save(Context context, EntranceLogInput logInput) throws Exception {
        File dir = new File(Environment.getExternalStorageDirectory(), DIR);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());

        File file = new File(dir, FILE_NAME + currentDate + FILE_EXT);

        try {
            if(file.exists()) {
                readFile(file, logInput);
            } else {
                writeFile(file, logInput);
            }

        } catch (Exception e) {
            Log.d(EntranceLogNotifier.class.toString(), e.getMessage());
            throw e;
        } finally {
            if(this.event != null) {
                this.event.completeHandler(DelegateType.ENTRANCE_LOG);
            }
        }

    }


    private void readFile(File file, EntranceLogInput logInput) throws IOException, JSONException {
        FileInputStream is = new FileInputStream(file);

        int fileSize = is.available();
        byte[] buffer = new byte[fileSize];

        is.read(buffer);
        is.close();

        String str = new String(buffer, "UTF-8");

        Gson gson = new Gson();
        EntranceLogInput entrances = gson.fromJson(str, EntranceLogInput.class);

        if(entrances != null && entrances.getEntrances().length > 0) {
            logInput.addEntrance(entrances.getEntrances());
        }

        writeFile(file, logInput);
    }


    private void writeFile(File file, EntranceLogInput logInput)
            throws IOException, JSONException {
        FileOutputStream os = new FileOutputStream(file);

        if(logInput != null) {
            os.write(logInput.toJSON().toString().getBytes());
        }
        os.close();
    }

}
