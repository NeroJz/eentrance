package hk.com.uatech.eticket.eticket.qrCode;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import hk.com.uatech.eticket.eticket.R;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;


import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;

public abstract class QRActivity extends AppCompatActivity implements NetworkRepository.QueryCallback {

    final private static int ACTION_INDEX = 0;
    final private static int SHOW_ID_INDEX = 1;
    final private static int TRANS_ID_INDEX = 2;
    final private static int CINEMA_ID_INDEX = 3;
    final private static int HOUSE_ID_INDEX = 4;
    final private static int SHOW_DATE_INDEX = 5;
    final private static int MOVIE_ID_INDEX = 6;
    final private static int MOVIE_CTG_INDEX = 7;
    final private static int SEAT_NO_INDEX = 8;
    final private static int TICKET_TYPE_INDEX = 9;

    private ProgressDialog loading = null;

    protected abstract IBinder getToken();
    protected abstract void goNext(String json,
                                   String encryptRefNo,
                                   String refType,
                                   String foodRefNo);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void playSuccess() {
        this.invokeBGMusic(R.raw.valid);
    }


    protected void playBuzzer() {
        this.invokeBGMusic(R.raw.invalid);
        // Invoke vibration
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(v != null) {
            v.vibrate(500);
        }
    }


    /**
     * Play music in background
     * @param resID
     */
    protected void invokeBGMusic(int resID) {
        final MediaPlayer player = MediaPlayer.create(this, resID);

        if(player == null) return;

        player.start();
    }


    /**
     * Handle the QR operation
     * @param scanned
     */
    protected void handleQrCode(String scanned) {
        Log.d("QRActivity", scanned);
    }


    /**
     * Handle the QR Operation with encrypted data
     * @param scanned
     * @param isEncrypted
     */
    protected void handleQrCode(String scanned, Boolean isEncrypted) {
        Log.d("QRActivity", scanned);

        String output = DecryptQR.decode(scanned);

        Log.d("QRActivity", output);

        String[] parts = output.split("#%");

        if(parts.length == 0) {
            return;
        }

        int action_id = Integer.parseInt(parts[ACTION_INDEX]);
        int show_id = Integer.parseInt(parts[SHOW_ID_INDEX]);
        int trans_id = Integer.parseInt(parts[TRANS_ID_INDEX]);
        int cinema_id = Integer.parseInt(parts[CINEMA_ID_INDEX]);
        int house_id = Integer.parseInt(parts[HOUSE_ID_INDEX]);

        String show_date = parts[SHOW_DATE_INDEX];
        int movie_id = Integer.parseInt(parts[MOVIE_ID_INDEX]);
        String movie_ctg = parts[MOVIE_CTG_INDEX];
        String seat = parts[SEAT_NO_INDEX];
        String ticket_type = parts[TICKET_TYPE_INDEX];




        if(action_id != 1) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Invalid QR code", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
    }


    /**
     * Handle ResfulApi response
     *
     * @param responseType
     * @param result
     */
    @Override
    public void onResponse(ResponseType responseType, String result) {
        if (loading != null) {
            loading.dismiss(); // dismiss loading
        }
    }
}
