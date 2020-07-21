package hk.com.uatech.eticket.eticket;

import android.app.Application;
import android.content.Context;

import hk.com.uatech.eticket.eticket.preferences.PreferencesController;

public class App extends Application {

    public static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PreferencesController.getInstance().init(this);

    }


    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }


    public static App getInstance() {
        return instance;
    }
}
