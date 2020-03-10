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
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.com.uatech.eticket.eticket.Item;
import hk.com.uatech.eticket.eticket.OfflineDatabase;
import hk.com.uatech.eticket.eticket.R;
import hk.com.uatech.eticket.eticket.database.Show;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;


import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.pojo.GateSeatInfo;
import hk.com.uatech.eticket.eticket.pojo.GateTransactionInfo;
import hk.com.uatech.eticket.eticket.pojo.SeatInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketTrans;
import hk.com.uatech.eticket.eticket.pojo.TransInfo;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;

public abstract class QRActivity extends AppCompatActivity implements NetworkRepository.QueryCallback {

    final private static int OPERATION_BIT_INDEX = 0;
    final private static int TRANS_ID_INDEX = 1;
    final private static int CINEMA_ID_INDEX = 2;
    final private static int CINEMA_NAME_INDEX = 3;
    final private static int HOUSE_ID_INDEX = 4;
    final private static int HOUSE_NAME_INDEX = 5;
    final private static int MOVIE_NAME_ZH_INDEX = 6;
    final private static int MOVIE_NAME_EN_INDEX = 7;
    final private static int MOVIE_CATEGORY_INDEX = 8;
    final private static int SHOW_DATE_INDEX = 9;
    final private static int SEAT_NO_LIST_INDEX = 10;
    final private static int FULL_SEAT_INDEX = 11;
    final private static int TICKET_TYPE_INDEX = 12;
    final private static int CONCESSION_BIT_INDEX = 13;

    public ProgressDialog loading = null;

    protected String refType = "";

    protected String foodRefNo = "";

    private String savedCinemaID = "";
    private List<String> houses = new ArrayList<String>();
    private Gson gson = new Gson();

    // Store ticket transaction
    private TicketTrans transaction;

    protected abstract IBinder getToken();
    protected abstract void goNext(String json,
                                   String encryptRefNo,
                                   String refType,
                                   String foodRefNo);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String str_permitted_house = PreferencesController.getInstance().getHousing();

        savedCinemaID = PreferencesController.getInstance().getCinemaId();

        if(!"".equals(str_permitted_house)) {
//            Log.d(QRActivity.class.toString(), str_permitted_house);
            addToHouses(str_permitted_house);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You are not allowed to update the data. Please check the selected house(s) on Setting page.",
                    Toast.LENGTH_LONG).show();
            return;
        }

    }


    /**
     * Add House ID to houses
     * @param str_houses
     */
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

        int action_id = Integer.parseInt(parts[OPERATION_BIT_INDEX]);
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
        String cinema_id = parts[CINEMA_ID_INDEX];

        if(!isValid(qr_house_id, cinema_id)) {
            return;
        }

        transaction = getTicketTrans(parts, PreferencesController.getInstance().getAccessMode());


        if("offline".compareTo(PreferencesController.getInstance().getAccessMode()) == 0) {

            // Validate the Show time in local db
            // Get the ticket trans info
            try {

                if(transaction == null) {
                    Toast.makeText(this, "Invalid transaction!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(parts.length <= 1) {
                    Context context = getApplicationContext();
                    CharSequence text = "Offline doesn't support manual input! Please scan the QR code";

                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                String json = gson.toJson(transaction);

                goNext(json, transaction.getTrans_id(), refType, "");

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            } finally {
                loading.dismiss();
            }

        } else { // Online mode

            try {
                if(loading == null) {
                    loading = new ProgressDialog(this);
                    loading.setCancelable(false);
                    loading.setMessage("Loading");
                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }

                loading.show();

                // Validate the Show time in local db
                // Get the ticket trans info
                if(transaction == null) {
                    Toast.makeText(this, "Invalid transaction!", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                    return;
                }

                // CALL API
                getGateTransactionInfo(parts[TRANS_ID_INDEX]);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }

    /**
     * Check scanned House and Cinema ID
     * against Preference
     * @param houseID
     * @param cinemaID
     * @return
     */
    private boolean isValid(String houseID, String cinemaID) {

        if(savedCinemaID == "") {
            Toast.makeText(
                    getApplicationContext(),
                    "You are not allowed to update the data. Please check the Cinema ID on Setting page.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(houses.size() == 0) {
            Toast.makeText(
                    getApplicationContext(),
                    "You are not allowed to update the data. Please check the selected house(s) on Setting page.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!savedCinemaID.equals(cinemaID)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Invalid cinema ID from QR code!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!houses.contains(houseID)) {
            Toast.makeText(
                    getApplicationContext(),
                    "You are not allowed to update the data in this house!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    /**
     * Get ticket transaction
     * @param parts
     * @param mode
     * @return
     */
    private TicketTrans getTicketTrans(String[] parts, String mode) {

        String trans_id = parts[TRANS_ID_INDEX];

        String house_id = parts[HOUSE_ID_INDEX];
        String house_name = parts[HOUSE_NAME_INDEX];


        String cinema_id = parts[CINEMA_ID_INDEX];
        String cinema_name = parts[CINEMA_NAME_INDEX];

        String movie_ename = parts[MOVIE_NAME_EN_INDEX];
        String movie_cname = parts[MOVIE_NAME_ZH_INDEX];
        String movie_ctg = parts[MOVIE_CATEGORY_INDEX];

        String show_date = parts[SHOW_DATE_INDEX];

        String[] showTime = show_date.split(" ");


        String seat = parts[SEAT_NO_LIST_INDEX];
        String full_seats = parts[FULL_SEAT_INDEX];
        String ticket_type = parts[TICKET_TYPE_INDEX];

        boolean is_concession = Integer.parseInt(parts[CONCESSION_BIT_INDEX]) > 0 ? true : false;

        TicketInfo ticketInfo = new TicketInfo(
                trans_id,
                show_date,
                house_id,
                house_name,
                cinema_id,
                cinema_name,
                movie_ename,
                movie_cname,
                movie_ctg,
                ticket_type,
                seat,
                full_seats
        );


        TransInfo[] transInfoList = new TransInfo[1];
        transInfoList[0] = new TransInfo(
                ticketInfo.getTrans_id(),
                ticketInfo.getMovie_ename(),
                movie_ctg,
                ticketInfo.getHouse_id(),
                ticketInfo.getHouse_ename(),
                ticketInfo.getCinema_ename(),
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
                Item item = db.getRecordBySeatId(ticketInfo.getTrans_id(), seatArray[i]);
                if(item != null) {
                    seatInfo.setSeatStatus(item.getSeatStatus());
                }
            }

            seatInfoList[i] = seatInfo;
        }

        TicketTrans ticket = new TicketTrans();
        ticket.setResultCode("200");
        ticket.setResultMsg("Success");
        ticket.setTransInfoList(transInfoList);
        ticket.setSeatInfoList(seatInfoList);
        ticket.setTrans_id(trans_id);
        ticket.setConcession(is_concession);

        return ticket;
    }


    /**
     * Get Transaction Info from Gate
     * @param transID
     * @throws Exception
     */
    private void getGateTransactionInfo(String transID) throws Exception {

        if(transaction == null) {
            throw new Exception("Transaction not found!");
        }

        JSONObject jsonVal = new JSONObject();
        jsonVal.put("trans_id", transID);

        NetworkRepository.getInstance().getGateGetTransactionInfo(jsonVal.toString(), this);

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

        switch (responseType) {
            case GATE_GET_TRANSACTION_INFO:

                if(TextUtils.isEmpty(result)) {
                    Toast.makeText(getApplicationContext(),
                            "Please try another ticket!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(result.startsWith("ERROR")) {
                    Toast.makeText(getApplicationContext(),
                            "Could not get transaction info!",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                GateTransactionInfo transactionInfo = gson.fromJson(result, GateTransactionInfo.class);

                ArrayList<SeatInfo> seatInfoArrayList = new ArrayList<SeatInfo>();

                Map<String, List<String>> entries = new HashMap<String, List<String>>();
                Map<String, List<String>> exits = new HashMap<String, List<String>>();

                if(transactionInfo.getSeat().length > 0) {
                    for(GateSeatInfo gate_seat : transactionInfo.getSeat()) {
                        SeatInfo seat = new SeatInfo(
                                gate_seat.getSeat_no(),
                                gate_seat.getTicket().getName().getEn()
                        );

                        if("1".equals(gate_seat.getIsRefunded()) ||
                                "1".equals(gate_seat.getIsScannedIn())) {
                            seat.setSeatStatus("Invalid");
                        }

                        seat.setConcession(transaction.isConcession());

                        seatInfoArrayList.add(seat);

                        entries = addTimeEntries(
                                gate_seat.getLog().getIn(),
                                entries,
                                gate_seat.getSeat_no());

                        exits = addTimeEntries(
                                gate_seat.getLog().getOut(),
                                exits,
                                gate_seat.getSeat_no());

                    }
                }

                SeatInfo[] seatInfo = seatInfoArrayList.toArray(new SeatInfo[seatInfoArrayList.size()]);
                transaction.setSeatInfoList(seatInfo);

                transaction.setLogIn(entries);
                transaction.setLogOut(exits);

                String json = gson.toJson(transaction);
                goNext(json, transaction.getTrans_id(), refType, "");

                break;
        }
    }


    private Map<String, List<String>> addTimeEntries(List<String> logs,
                                                     Map<String, List<String>> data,
                                                     String seatNo) {
        if(logs.size() == 0) {
            return  data;
        }

        for(String strDate : logs) {
            if(data.containsKey(strDate)) {
                List<String> tmp = data.get(strDate);
                if(!tmp.contains(seatNo)) {
                    tmp.add(seatNo);
                    data.put(strDate, tmp);
                }
            } else {
                List<String> tmp = new ArrayList<String>();
                tmp.add(seatNo);
                data.put(strDate, tmp);
            }
        }


        return data;
    }
}
