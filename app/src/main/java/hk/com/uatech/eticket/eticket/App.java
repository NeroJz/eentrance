package hk.com.uatech.eticket.eticket;

import android.app.Application;

import hk.com.uatech.eticket.eticket.preferences.PreferencesController;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferencesController.getInstance().init(this);

    }
}
