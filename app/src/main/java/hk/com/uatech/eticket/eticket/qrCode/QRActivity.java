package hk.com.uatech.eticket.eticket.qrCode;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hk.com.uatech.eticket.eticket.Item;
import hk.com.uatech.eticket.eticket.OfflineDatabase;
import hk.com.uatech.eticket.eticket.R;
import hk.com.uatech.eticket.eticket.database.Show;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;


import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.pojo.SeatInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketTrans;
import hk.com.uatech.eticket.eticket.pojo.TransInfo;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;

public abstract class QRActivity extends AppCompatActivity implements NetworkRepository.QueryCallback {

    final private static int ACTION_INDEX = 0;
    final private static int TRANS_ID_INDEX = 1;
    final private static int CINEMA_ID_INDEX = 2;
    final private static int HOUSE_ID_INDEX = 3;
    final private static int SHOW_ID_INDEX = 4;
    final private static int SHOW_DATE_INDEX = 5;
    final private static int MOVIE_ID_INDEX = 6;
    final private static int MOVIE_CTG_INDEX = 7;
    final private static int SEAT_NO_INDEX = 8;
    final private static int TICKET_TYPE_INDEX = 9;
    final private static int CONCESSION_TYPE_INDEX = 10;


    final private static int REMAIN_TICKET = 1;
    final private static int TOTAL_TICKET = 4;

    public ProgressDialog loading = null;

    protected String refType = "";

    protected String foodRefNo = "";

    private List<String> houses = new ArrayList<String>();
    private Gson gson = new Gson();

    protected abstract IBinder getToken();
    protected abstract void goNext(String json,
                                   String encryptRefNo,
                                   String refType,
                                   String foodRefNo);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String str_permitted_house = PreferencesController.getInstance().getHousing();

        if(!"".equals(str_permitted_house)) {
            Log.d(QRActivity.class.toString(), str_permitted_house);
            addToHouses(str_permitted_house);
        }

    }


    protected void addToHouses(String str_houses) {
        String[] part = str_houses.split(",");
        houses = Arrays.asList(part);
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

        refType = "A";

        if(scanned == null || "".compareTo(scanned) == 0) {
            return;
        }

        String output = DecryptQR.decode(scanned);

        Log.d("QRActivity", output);

        String[] parts = output.split("#%");

        if(parts.length <= 1) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Error on scanning the QR code",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        int action_id = Integer.parseInt(parts[ACTION_INDEX]);

        int trans_id = Integer.parseInt(parts[TRANS_ID_INDEX]);

        if(action_id != 1) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Invalid Action", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getToken(), 0);


        String qr_house_id = parts[HOUSE_ID_INDEX];

        if(houses.size() > 0) {
            if(!houses.contains(qr_house_id)) {
                Toast.makeText(
                        getApplicationContext(),
                        "You are not allowed to update the data in this house!",
                        Toast.LENGTH_SHORT).show();

                return;
            }
        }



        // Validate the Show time in local db
        // Get the ticket trans info
        TicketTrans ticket = getTicketTrans(parts, PreferencesController.getInstance().getAccessMode());

        if(ticket == null) {
            Toast.makeText(this, "Show not found!", Toast.LENGTH_SHORT).show();
            return;
        }


        if("offline".compareTo(PreferencesController.getInstance().getAccessMode()) == 0) {

            if(parts.length <= 1) {
                Context context = getApplicationContext();
                CharSequence text = "Offline doesn't support manual input! Please scan the QR code";

                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }


            String json = gson.toJson(ticket);

            Log.d(QRActivity.class.toString(), json);

            goNext(json, Integer.toString(trans_id), refType, "");

        } else { // Online mode

            try {
                if(loading == null) {
                    loading = new ProgressDialog(this);
                    loading.setCancelable(false);
                    loading.setMessage("Loading");
                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }

                loading.show();

                // CALL API
                /**
                 * Test Case
                 */

                /*
                int remaining = TOTAL_TICKET - REMAIN_TICKET;

                for(int i=0; i < remaining; i++) {
                    SeatInfo seatInfo = ticket.getSeatInfoList()[i];
                    seatInfo.setSeatStatus("Invalid");
                }

                loading.hide();

                Gson gson = new Gson();
                String json = gson.toJson(ticket);

                Log.d(QRActivity.class.toString(), json);

                goNext(json, Integer.toString(trans_id), refType, "");
                 */

                /**
                 * End of Test Case
                 */

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Get ticket trans
     * Author: Jz
     * Date: 02-03-2020
     * Version: 0.0.1
     */
    private TicketTrans getTicketTrans(String[] parts, String mode) {

        int show_id = Integer.parseInt(parts[SHOW_ID_INDEX]);

        /**
         * Test case
         */
        show_id = 18143720;

        int trans_id = Integer.parseInt(parts[TRANS_ID_INDEX]);
        int cinema_id = Integer.parseInt(parts[CINEMA_ID_INDEX]);
        int house_id = Integer.parseInt(parts[HOUSE_ID_INDEX]);

        boolean is_concession = Integer.parseInt(parts[CONCESSION_TYPE_INDEX]) > 0 ? true : false;

        String show_date = parts[SHOW_DATE_INDEX];

        String[] showTime = show_date.split(" ");


        int movie_id = Integer.parseInt(parts[MOVIE_ID_INDEX]);
        String movie_ctg = parts[MOVIE_CTG_INDEX];
        String seat = parts[SEAT_NO_INDEX];
        String ticket_type = parts[TICKET_TYPE_INDEX];


        Show model = new Show(this);

        TicketInfo result = model.validate(show_id, cinema_id);

        if(result == null) {
            return null;
        }

        result.setTrans_id(Integer.toString(trans_id));
        result.setMovie_ctg(movie_ctg);
        result.setSeat(seat);
        result.setTicket_type(ticket_type);

        TransInfo[] transInfoList = new TransInfo[1];
        transInfoList[0] = new TransInfo(
                result.getTrans_id(),
                result.getMovie_ename(),
                movie_ctg,
                result.getHouse_id(),
                result.getHouse_ename(),
                result.getCinema_ename(),
                showTime[0],
                showTime[1]
        );


        String[] seatArray = seat.split(",");
        String[] ticketTypeArray = ticket_type.split(",");

        SeatInfo[] seatInfoList = new SeatInfo[seatArray.length];
        for(int i=0; i < seatArray.length; i++) {

            OfflineDatabase db = new OfflineDatabase(this);

            SeatInfo seatInfo = new SeatInfo(seatArray[i], ticketTypeArray[i]);
            seatInfo.setConcession(is_concession);

            // Check seat is stored in SQLite when it is OFFLINE
            if("offline".equals(mode)) {
                Item item = db.getRecordBySeatId(result.getTrans_id(), seatArray[i]);
                if(item != null) {
                    seatInfo.setSeatStatus(item.getSeatStatus());
                }
            }

//            SeatInfo seatInfo = new SeatInfo(seatArray[i], ticketTypeArray[i]);
            seatInfoList[i] = seatInfo;
        }

        TicketTrans ticket = new TicketTrans();
        ticket.setResultCode("200");
        ticket.setResultMsg("Success");
        ticket.setTransInfoList(transInfoList);
        ticket.setSeatInfoList(seatInfoList);

        return ticket;
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
