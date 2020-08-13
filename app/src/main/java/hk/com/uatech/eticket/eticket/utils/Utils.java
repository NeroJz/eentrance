package hk.com.uatech.eticket.eticket.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

//import com.android.internal.util.Predicate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import hk.com.uatech.eticket.eticket.EntractStep1Activity;
import hk.com.uatech.eticket.eticket.R;

public class Utils {

    public static boolean qrIsValid(Context context, String data) {
        boolean isValid = true;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US); //new SimpleDateFormat("dd MMM yyyy (E) hh:mma");
        //DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        //DateTime dt = formatter.parseDateTime(string);
        Date dt;
        Date currentDt = new Date();
        String movieTitle = "";
        String movieCategory = "";
        String cinemaName = "";
        String cinemaName2 = "";
        String showDate = "";
        String showTime = "";
        String displayFileDateTime = "";
        String displayFilmDate = "";
        String displayFilmTime = "";
        String seatIds = "";
        String resultMsg = "";
        String movieTransId = "";

        // For Food
        List foodIds = new ArrayList();
        List foodValues = new ArrayList();
        List foodQty = new ArrayList();

        int totalSeats = 0;
        int validSeats = 0;
        String jsonstr = data;
        JSONObject jsonObj;
        SharedPreferences sp = context.getSharedPreferences("E-TICKET", context.MODE_PRIVATE);
        String gracePeriodFrom = sp.getString("grace_period_from", "30");
        String gracePeriodTo = sp.getString("grace_period_to", "120");

        int gpFrom = Integer.parseInt(gracePeriodFrom);
        int gpTo = Integer.parseInt(gracePeriodTo);
        //
        try {
            jsonObj = new JSONObject(jsonstr);


            resultMsg = jsonObj.getString("resultMsg");


            JSONArray trans = jsonObj.getJSONArray("transInfoList");
            JSONArray seats = jsonObj.getJSONArray("seatInfoList");

            if (seats != null) {
                totalSeats = seats.length();
            }
            JSONObject tran = trans.getJSONObject(0);
            // Check whether the information is valid or invalid
            movieTransId = tran.getString("transId");
            movieTitle = tran.getString("movieTitle");
            movieCategory = tran.getString("movieCategory");
            cinemaName = tran.getString("houseName");
            cinemaName2 = tran.getString("cinemaName");
            showDate = tran.getString("showDate");
            showTime = tran.getString("showTime");


            if (seats != null) {
                for (int y = 0; y < seats.length(); y++) {
                    JSONObject seat = seats.getJSONObject(y);
                    String seatStatus = seat.getString("seatStatus");
                    if ("Valid".compareTo(seatStatus) == 0) {
                        validSeats++;
                    }
                }
            }
            if ("".compareTo(showDate) == 0 || "".compareTo(showTime) == 0) {
                // Nothing to do
            } else {
                try {
                    //dt = format.parse("10 August 2017 (Thu)" + " " + "12:20pm");
                    dt = format.parse(showDate + " " + showTime);
                    displayFileDateTime = dt.toString();

                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy (E) hh:mma", java.util.Locale.US);
                    displayFileDateTime = displayFormat.format(dt);

                    SimpleDateFormat displayFormatDate = new SimpleDateFormat("dd MMM yyyy (E)", java.util.Locale.US);
                    displayFilmDate = displayFormatDate.format(dt);

                    SimpleDateFormat displayFormatTime = new SimpleDateFormat("hh:mma", java.util.Locale.US);
                    displayFilmTime = displayFormatTime.format(dt);

                    long diff = currentDt.getTime() - dt.getTime();
                    long seconds = diff / 1000;
                    long minutes = seconds / 60;
                    //long hours = minutes / 60;

                    if (minutes > 0) {
                        // Current Date is greater than the show date time
                        if (minutes > gpTo) {
                            isValid = false;
                        }
                    } else {
                        if (Math.abs(minutes) > gpFrom) {
                            isValid = false;
                        }

                    }

                } catch (Exception exdate) {
                    Log.d("exception", "exception");

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean r = resultMsg.compareToIgnoreCase("success") == 0;


        return (("".compareTo(showDate) == 0 && r) || isValid) && validSeats != 0;
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


    public static boolean isDebug(Context context) {
        try {
            return getConfigValue(context, "is_debug").equals("1") ? true : false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Filter collection
     * @param col
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> Collection<T> filter(Collection<T> col, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for(T element: col) {
            if(predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

}
