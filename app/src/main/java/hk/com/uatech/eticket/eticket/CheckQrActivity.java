package hk.com.uatech.eticket.eticket;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONObject;

import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;

public abstract class CheckQrActivity extends AppCompatActivity implements NetworkRepository.QueryCallback {
    protected final int RETURN_FROM_ADMIT_PAGE = 10282;

    protected String encryptRefNo = "";
    protected String refType = "";

    protected String foodRefNo = "";

    String allowedHouse = "";

    private ProgressDialog loading = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allowedHouse = PreferencesController.getInstance().getHousingName();
    }

    protected void handleQrCode(String refNo) {
        String foodRefNo = "";
        String fullString = refNo;
        String method = "A";
        refType = "A";
        //reO6kf0K5StX40jp,7okH2yYUUpLmxN-x3kWScyDA0o_|#Teresa Teng 2018 Tour Concert (Macau)|#WYNN MACAU GRAND BALLROOM|#Wynn|#2018-09-26 17:00:00|#CA22|#WM - Zone  C - 900
        //reO6kf0K5StX40jp,7okH2yYUUpLmxN-x3kWScyDA0o_|#Teresa Teng 2018 Tour Concert (Macau)|#WYNN MACAU GRAND BALLROOM|#Wynn|#2018-09-26 17:00:00|#CA22|#WM - Zone  C - 900

        if (refNo == null || "".compareTo(refNo) == 0) {
            return;
        } else {
            String[] arrStr = refNo.split("\\|"); //refNo.split("|");
            if (arrStr.length > 1) {
                refNo = arrStr[0];

                if (arrStr.length > 3) {
                    // allowedHouse
                    String house = arrStr[2].toString();
                    house = house.replace("#", "");
                    //house = house.replace("House ", "");
                    house = house.trim();

                    String[] arrSel = allowedHouse.split(",");

                    boolean found = false;
                    if (arrSel.length > 0) {
                        for (int k = 0; k < arrSel.length; k++) {
                            if (house.compareToIgnoreCase(arrSel[k]) == 0) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {

                        // Should stop and alert

                        Context context = getApplicationContext();
                        CharSequence text = "You are not allowed to update the data in this house!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        clearData();
                        // ((EditText) findViewById(R.id.edtQRCode)).setText("");
                        return;
                    }


                    // Get the Food Ref Id
                    foodRefNo = arrStr[(arrStr.length - 1)].toString();
                    foodRefNo = foodRefNo.replace("#", "");
                    foodRefNo = foodRefNo.trim();


                }
            } else {
                method = "M";
                refType = "M";
            }

            encryptRefNo = refNo;
        }


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getToken(), 0);

        if ("offline".compareTo(PreferencesController.getInstance().getAccessMode()) == 0) {
            // Offline Mode,
            // First, check whether the record exists in the DB and form the Json String
            // String fullString = ((EditText) findViewById(R.id.edtQRCode)).getEditableText().toString();
            String[] items = fullString.split("\\|"); //refNo.split("|");

            if (items.length <= 1) {
                Context context = getApplicationContext();
                CharSequence text = "Offline doesn't support manual input!  Please scan the QR code";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            String movieName = items[1];
            movieName = movieName.replace("#", "");
            movieName = movieName.trim();

            String movieHouse = items[2];
            movieHouse = movieHouse.replace("#", "");
            movieHouse = movieHouse.trim();

            String movieCinema = items[3];
            movieCinema = movieCinema.replace("#", "");
            movieCinema = movieCinema.trim();

            String movieCategory = "";

            String movieShowDateTime = items[4];
            movieShowDateTime = movieShowDateTime.replace("#", "");
            movieShowDateTime = movieShowDateTime.trim();

            String[] showDateInfo = movieShowDateTime.split(" ");
            String movieShowDate = "";
            String movieShowTime = "";

            if (showDateInfo.length >= 2) {
                movieShowTime = showDateInfo[1];

                String[] showDateOnlyInfo = showDateInfo[0].split("/");

                if (showDateOnlyInfo.length > 2) {
                    movieShowDate = showDateOnlyInfo[2] + "-" + showDateOnlyInfo[1] + "-" + showDateOnlyInfo[0];
                } else {
                    movieShowDate = showDateInfo[0];
                }
            }

            String movieSeatId = items[5];
            String movieTicketType = items[6];
            movieSeatId = movieSeatId.replace("#", "");
            movieSeatId = movieSeatId.trim();

            movieTicketType = movieTicketType.replace("#", "");
            movieTicketType = movieTicketType.trim();


            String jsonStr = "{\n" +
                    "    \"resultCode\": 200,\n" +
                    "    \"resultMsg\": \"Success\",\n" +
                    "    \"transInfoList\": [\n" +
                    "        {\n" +
                    "            \"transId\": \"" + encryptRefNo + "\",\n" +
                    "            \"movieTitle\": \"" + movieName + "\",\n" +
                    "            \"movieCategory\": \"" + movieCategory + "\",\n" +
                    "            \"houseNo\": \"" + movieHouse + "\",\n" +
                    "            \"houseName\": \"" + movieHouse + "\",\n" +
                    "            \"cinemaName\": \"" + movieCinema + "\",\n" +
                    "            \"showDate\": \"" + movieShowDate + "\",\n" +
                    "            \"showTime\": \"" + movieShowTime + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"seatInfoList\": [\n";


            String[] arrSeatIds = movieSeatId.split(",");
            String[] arrTicketTypes = movieTicketType.split(",");

            if (arrSeatIds.length > 0) {
                for (int z = 0; z < arrSeatIds.length; z++) {

                    String status = "Valid";
                    String seatId = arrSeatIds[z];
                    String ticket = arrTicketTypes[z];

                    // Need to get the status from DB
                    OfflineDatabase db = new OfflineDatabase(this);
                    Item item = db.getRecordBySeatId(encryptRefNo, seatId);

                    if (item != null) {

                        status = item.getSeatStatus();
                    }

                    jsonStr += "        {\n" +
                            "            \"seatStatus\": \"" + status + "\",\n" +
                            "            \"seatId\": \"" + seatId + "\",\n" +
                            "            \"ticketType\": \"" + ticket + "\",\n" +
                            "            \"fnbList \": [\n" +
                            "                \n" +
                            "            ]\n" +
                            "        }";

                    if (z != arrSeatIds.length - 1) {
                        jsonStr += ",";
                    }

                    jsonStr += "\n";
                }
            }

            jsonStr += "    ]\n" +
                    "}";


            goNext(jsonStr, encryptRefNo, refType, foodRefNo);

        } else {
            // Online Mode

            // Try to Call the verify API
            try {

                if (loading == null) {
                    loading = new ProgressDialog(this);

                    loading.setCancelable(true);
                    loading.setMessage("Loading");
                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }

                loading.show();

                JSONObject jsonvalue = new JSONObject();
                jsonvalue.put("refNo", refNo);
                jsonvalue.put("method", method);
                NetworkRepository.getInstance().verifyTicket(jsonvalue.toString(), this);


                clearData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onResponse(ResponseType responseType, String result) {
        loading.dismiss();
        switch (responseType) {
            case VERIFY_TICKET:
                if (TextUtils.isEmpty(result)) {
                    CharSequence text = "Error Occur, please try another ticket";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();

                } else if (result.startsWith("ERROR")) {
                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");

                    String errMsg;
                    try {
                        JSONObject jsonErr = new JSONObject(result);
                        errMsg = jsonErr.getString("resultMsg");
                    } catch (Exception eeeee) {
                        errMsg = displayErr;

                    }

                    Toast.makeText(getApplicationContext(),
                            errMsg,
                            Toast.LENGTH_SHORT)
                            .show();
                } else {

                    Log.d("Return", result);
                    try {
                        goNext(new JSONObject(result).toString(), encryptRefNo, refType, foodRefNo);

                    } catch (Exception ec) {
                        Toast.makeText(getApplicationContext(),
                                "Error: " + ec.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();

                        return;
                    }
                }
                break;
        }
    }

    protected abstract IBinder getToken();

    protected abstract void goNext(
            String json,
            String encryptRefNo,
            String refType,
            String foodRefNo);

    protected void playSuccess() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.valid);
        mp.start();
    }

    protected void playBuzzer() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.invalid);
        mp.start();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(500);
        }
    }

    protected void clearData() {
    }
}
