package hk.com.uatech.eticket.eticket;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ETicketExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public ETicketExceptionHandler(Activity a) {
        activity = a;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Intent intent = new Intent(activity, MainActivity.class);

        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

//        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager mgr = (AlarmManager) App.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, pendingIntent);

        activity.startActivity(intent);

//        activity.finish();

        System.exit(2);

    }
}
