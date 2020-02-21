package hk.com.uatech.eticket.eticket;

import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import hk.com.uatech.eticket.eticket.utils.Utils;


public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_CAMERA = 10099;
    private final static int SPLASH_DISPLAY_LENGTH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean showEditPage = false;

        if (TextUtils.isEmpty(PreferencesController.getInstance().getServerIpAddress())) {
            showEditPage = true;
            PreferencesController.getInstance().setServerIpAddress(
                    Utils.getConfigValue(MainActivity.this, "api_url"));
        }

        if (TextUtils.isEmpty(PreferencesController.getInstance().getEntrance())) {
            showEditPage = true;
            PreferencesController.getInstance().setEntrance(
                    Utils.getConfigValue(MainActivity.this, "api_path"));
        }

        if (TextUtils.isEmpty(PreferencesController.getInstance().getFb())) {
            showEditPage = true;
            PreferencesController.getInstance().setFb(
                    Utils.getConfigValue(MainActivity.this, "fb_api_path"));
        }
        final boolean finalShowEditPage = showEditPage;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
                mainIntent.putExtra("showEditPage", finalShowEditPage ? "Y" : "N");
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(mainIntent);
                    MainActivity.this.finish();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast toast = Toast.makeText(MainActivity.this, "Sorry, the application need the Camera access right", Toast.LENGTH_LONG);
                    toast.show();

                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e("ETicket", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("ETicket", "Failed to open config file.");
        }

        return null;
    }


    public static void appendLog(String text)
    {

        String folderPath = "";

        File logFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }





}
