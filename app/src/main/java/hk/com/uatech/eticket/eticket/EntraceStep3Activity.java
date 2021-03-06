package hk.com.uatech.eticket.eticket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.android.internal.util.Predicate;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import hk.com.uatech.eticket.eticket.database.Entrance;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.pojo.SeatInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketTrans;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import hk.com.uatech.eticket.eticket.utils.Utils;

public class EntraceStep3Activity extends AppCompatActivity implements NetworkRepository.ObservableCallback {

    private ProgressDialog loading = null;

    public List ticketTypeList = new ArrayList();
    public List ticketList = new ArrayList();
    public List ticketIdList = new ArrayList();
    public List ticketState = new ArrayList();

    private String encryptRefNo = "";
    private String refType = "";

    private String accessMode;


    private TicketTrans ticketTrans;
    private List<SeatInfo> entranceList = new ArrayList<SeatInfo>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrace_step3);

        // Get the grace period
        String gracePeriodFrom = PreferencesController.getInstance().getGracePeriodFrom();
        String gracePeriodTo = PreferencesController.getInstance().getGracePeriodTo();

        int gpFrom = Integer.parseInt(gracePeriodFrom);
        int gpTo = Integer.parseInt(gracePeriodTo);
        accessMode = PreferencesController.getInstance().getAccessMode();


        LinearLayout bgLayer = (LinearLayout) findViewById(R.id.bglayer);
        if ("offline".equals(accessMode)) {
            //tv.setVisibility(View.VISIBLE);
            bgLayer.setBackgroundColor(Color.rgb(255, 179, 179));

        } else {
            //tv.setVisibility(View.GONE);
            bgLayer.setBackgroundColor(Color.rgb(244, 244, 244));

        }

        // Get the object
        final String jsonstr = getIntent().getExtras().getString("json");
        final JSONObject jsonObj;

        encryptRefNo = getIntent().getExtras().getString("encryptRefNo");
        refType = getIntent().getExtras().getString("refType");
        //

        String movieTransId = "";
        String movieTitle = "";
        String movieCategory = "";
        String cinemaName = "";
        String showDate = "";
        String showTime = "";
        String displayFileDateTime = "";
        String displayFilmDate = "";
        String displayFilmTime = "";
        String seatIds = "";
        String resultMsg = "";


        // For Food
        List foodIds = new ArrayList();
        List foodValues = new ArrayList();
        List foodQty = new ArrayList();

        try {

            // Check jsonstr is defined
            // Create ticketTrans from jsonstr
            if(jsonstr != null && !(" ").equals(jsonstr)) {
                Gson gson = new Gson();
                ticketTrans = gson.fromJson(jsonstr, TicketTrans.class);
            }

            // Debug log checking
//            if(ticketTrans != null) {
//                for(SeatInfo seat : ticketTrans.getSeatInfoList()) {
//                    Log.d(EntraceStep3Activity.class.toString(), seat.getSeatId() + "---" + seat.getSeatStatus() + "---" + seat.isChecked());
//                }
//            }

            jsonObj = new JSONObject(jsonstr);

            resultMsg = jsonObj.getString("resultMsg");


            JSONArray trans = jsonObj.getJSONArray("transInfoList");
            JSONArray seats = jsonObj.getJSONArray("seatInfoList");

            JSONObject tran = trans.getJSONObject(0);
            // Check whether the information is valid or invalid
            movieTransId = tran.getString("transId");
            movieTitle = tran.getString("movieTitle");
            movieCategory = tran.getString("movieCategory");
            cinemaName = tran.getString("houseName");
            showDate = tran.getString("showDate");
            showTime = tran.getString("showTime");


            // For Seat Info
            if (seats != null) {
                for (int y = 0; y < seats.length(); y++) {
                    JSONObject seat = seats.getJSONObject(y);

                    // Get the food information
                    //JSONArray foods = seat.getJSONArray("fnbList");
                    JSONArray foods;
                    try {
                        foods = seat.getJSONArray("fnbList");
                    } catch (JSONException je) {
                        foods = null;
                    }

                    if (foods != null) {
                        for (int z = 0; z < foods.length(); z++) {

                            boolean findInArray = false;
                            String tmpValue = foods.getJSONObject(z).getString("fnbId");
                            String tmpFoodName = foods.getJSONObject(z).getString("fnbName");
                            int tmpFoodQty = foods.getJSONObject(z).getInt("fnbQty");
                            for (int t = 0; t < foodIds.size(); t++) {
                                if (foodIds.get(t).toString().compareTo(tmpValue) == 0) {
                                    findInArray = true;


                                    String tmpQtyToChange = foodQty.get(t).toString();

                                    if (tmpFoodQty == 1) {
                                        tmpQtyToChange += "," + seat.getString("seatId");
                                        foodQty.set(t, tmpQtyToChange);
                                    } else {
                                        tmpQtyToChange += "," + seat.getString("seatId") + " x " + String.valueOf(tmpFoodQty);

                                        foodQty.set(t, tmpQtyToChange);
                                    }

                                    break;
                                }

                            }

                            if (!findInArray) {
                                foodIds.add(tmpValue);
                                foodValues.add(tmpFoodName);

                                if (tmpFoodQty == 1) {
                                    foodQty.add(seat.getString("seatId"));
                                } else {
                                    String tmpQty = seat.getString("seatId") + " x " + String.valueOf(tmpFoodQty);
                                    foodQty.add(tmpQty);
                                }

                            }
                        }
                    }

                    // Concat the Seat ID
                    if (!TextUtils.isEmpty(seat.getString("seatId"))) {
                        if (!TextUtils.isEmpty(seatIds))
                            seatIds += ",";

                        seatIds += seat.getString("seatId");
                    }

                    //  Ticket Type
                    String ticketType = seat.getString("ticketType");
                    String seatStatus = seat.getString("seatStatus");
                    int seatIcon = 0;
//                    if ("Valid".compareTo(seatStatus) == 0) {
//                        seatIcon = 1;
//                    } else if ("Invalid".compareTo(seatStatus) == 0) {
//                        seatIcon = 0;
//                    } else {
//                        seatIcon = 2;
//                    }


                    ScanType seat_action_type = ScanType.valueOf(seat.getString("action"));

                    if(seat_action_type == ScanType.IN || seat_action_type == ScanType.REFUND) {
                        seatIcon = 0;
                    } else if(seat_action_type == ScanType.OUT) {
                        seatIcon = 1;
                    } else {
                        seatIcon = 2;
                    }


                    // Check whether the ticket already exist or not
                    boolean findInTypeArray = false;
                    int ticketTypeInd = -1;
                    for (ticketTypeInd = 0; ticketTypeInd < ticketTypeList.size(); ticketTypeInd++) {

                        String tmpTicketType = ticketTypeList.get(ticketTypeInd).toString();

                        if (tmpTicketType != null && tmpTicketType.compareTo(ticketType) == 0) {
                            findInTypeArray = true;
                            break;
                        }

                    }

                    if (!findInTypeArray) {

                        // Not Found
                        ticketTypeList.add(ticketType);
                        ticketTypeInd = ticketTypeList.size() - 1;

                        ArrayList newList = new ArrayList();
                        ArrayList newIdList = new ArrayList();
                        ArrayList newState = new ArrayList();

                        ticketList.add(newList);
                        ticketIdList.add(newIdList);
                        ticketState.add(newState);

                    }

                    // Add the list
                    ArrayList tmpList = (ArrayList) ticketList.get(ticketTypeInd);
                    ArrayList tmpIdList = (ArrayList) ticketIdList.get(ticketTypeInd);
                    ArrayList tmpState = (ArrayList) ticketState.get(ticketTypeInd);

                    tmpList.add(seatIcon);
                    tmpIdList.add(seat.getString("seatId"));

                    if ("Valid".compareTo(seatStatus) == 0) {
                        tmpState.add(0);
                    } else {
                        tmpState.add(0);
                    }

                    ticketList.set(ticketTypeInd, tmpList);
                    ticketIdList.set(ticketTypeInd, tmpIdList);
                    ticketState.set(ticketTypeInd, tmpState);


                }
            }
        } catch (Exception ec) {
            // Show Message
            Context context = getApplicationContext();
            CharSequence text = "Login Fail, please try again";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            return;
        }

        LinearLayout ticketContainer = (LinearLayout) findViewById(R.id.ticketContainer);
        ticketContainer.removeAllViews();


        ((TextView) findViewById(R.id.movieTitle)).setText(movieTitle);
        ((TextView) findViewById(R.id.cinemaName)).setText(cinemaName);

        boolean isValid = true;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.US); //new SimpleDateFormat("dd MMM yyyy (E) hh:mma");
        //DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        //DateTime dt = formatter.parseDateTime(string);
        Date dt;
        Date currentDt = new Date();

        if (!TextUtils.isEmpty(showDate) && !TextUtils.isEmpty(showDate)) {
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

        ImageView imgValid = (ImageView) findViewById(R.id.imgInvalid);
        ImageView imgInvalid = (ImageView) findViewById(R.id.imgValid);

        boolean isSuccess = resultMsg.compareToIgnoreCase("success") == 0;


        //if (isValid) {
        if ("".compareTo(showDate) == 0) {
            if (!isSuccess) {
                imgValid.setVisibility(View.GONE);
            } else {
                imgInvalid.setVisibility(View.GONE);
            }
        } else {
            if (isValid) {
                imgValid.setVisibility(View.GONE);
            } else {
                imgInvalid.setVisibility(View.GONE);

            }
        }

        ((TextView) findViewById(R.id.showDate)).setText(displayFilmDate);
        ((TextView) findViewById(R.id.showTime)).setText(displayFilmTime);

        findViewById(R.id.confirm).setOnClickListener(new ImageView.OnClickListener() {

            @Override

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EntraceStep3Activity.this);


                if(entranceList.size() == 0) {
                    builder.setTitle("Notice");
                    builder.setMessage("Please select the ticket to update!");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }



                builder.setTitle("Confirm");
                builder.setMessage("Confirm to update the transaction?");

                if (loading == null) {
                    loading = new ProgressDialog(v.getContext());

                    loading.setCancelable(true);
                    loading.setMessage("Loading");
                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog


                        loading.show();

                        if ("offline".compareTo(accessMode) == 0) {
                            // Update to Database
                            ArrayList items = new ArrayList();

                            for (int x = 0; x < ticketTypeList.size(); x++) {
                                ArrayList tmpList = (ArrayList) ticketList.get(x);
                                ArrayList tmpIdList = (ArrayList) ticketIdList.get(x);
                                ArrayList tmpState = (ArrayList) ticketState.get(x);

                                if (tmpList != null) {
                                    for (int y = 0; y < tmpList.size(); y++) {
                                        //if (((int)tmpList.get(y)) == 2) {
                                        if (((int) tmpState.get(y)) == 1) {

                                            Item item = new Item();

                                            item.setRefNo(encryptRefNo);
                                            item.setSeatId(tmpIdList.get(y).toString());

                                            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            //String currentDateandTime = sdf.format(new Date());

                                            //seat.put("timestamp", currentDateandTime);

                                            if (((int) tmpList.get(y)) == 1) {
                                                // Valid
                                                //seat.put("action", "C");
//                                                item.setSeatStatus("Invalid");
                                                item.setSeatStatus("1");
                                            } else if (((int) tmpList.get(y)) == 0) {
                                                // InValid
                                                //seat.put("action", "R");
//                                                item.setSeatStatus("Valid");
                                                item.setSeatStatus("0");
                                            }

                                            item.setTicketType(ticketTypeList.get(x).toString());

                                            items.add(item);

                                        }
                                    }
                                }

                            }

                            OfflineDatabase offline = new OfflineDatabase(EntraceStep3Activity.this);

                            try {
                                // Hide offline saving
                                offline.accept(items);

                                // Add Entrance log
                                addEntranceLog();

                                // Check result
                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, "Save Successful", duration);
                                toast.show();

                                finish();

                                //Item rec = offline.getRecordBySeatId(encryptRefNo,  tmpIdList.get(y).toString());
                            } catch (Exception esql) {
                                // Error during execution
                                Context context = getApplicationContext();
                                String reason = esql.getMessage();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, reason, duration);
                                toast.show();
                            }


                            dialog.dismiss();
                        } else {

                            /**
                             * Original Code
                             */
                            /*
                            JSONObject jsonvalue = new JSONObject();

                            // Get the String from Text Control
                            try {
                                jsonvalue.put("refNo", encryptRefNo);
                                jsonvalue.put("method", refType);

                                JSONArray jsonArr = new JSONArray();

                                for (int x = 0; x < ticketTypeList.size(); x++) {
                                    ArrayList tmpList = (ArrayList) ticketList.get(x);
                                    ArrayList tmpIdList = (ArrayList) ticketIdList.get(x);
                                    ArrayList tmpState = (ArrayList) ticketState.get(x);

                                    if (tmpList != null) {
                                        for (int y = 0; y < tmpList.size(); y++) {
                                            //if (((int)tmpList.get(y)) == 2) {
                                            if (((int) tmpState.get(y)) == 1) {
                                                JSONObject seat = new JSONObject();
                                                seat.put("seatId", tmpIdList.get(y).toString());

                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
                                                String currentDateandTime = sdf.format(new Date());

                                                seat.put("timestamp", currentDateandTime);

                                                if (((int) tmpList.get(y)) == 1) {
                                                    // Valid
                                                    seat.put("action", "C");
                                                } else if (((int) tmpList.get(y)) == 0) {
                                                    // InValid
                                                    seat.put("action", "R");
                                                }

                                                jsonArr.put(seat);

                                            }
                                        }
                                        //}


                                    }

                                }


                                jsonvalue.put("updateTicketType", jsonArr);

                            } catch (JSONException jsonEx) {
                                Log.e("Accept", jsonEx.getMessage());

                            }

                            //JSONObject jsonObject = getJSONObjectFromURL(urlString, jsonvalue);
                            NetworkRepository.getInstance().updateTicketType(jsonvalue.toString(), EntraceStep3Activity.this);
                             */
                            /**
                             * End of original code
                             */


                            /**
                             * Handling Ticket update
                             * use for Boardway
                             */
                            try {
                                ArrayList<JSONObject> params = new ArrayList<>();

                                String cinemaID = PreferencesController.getInstance().getCinemaId();

                                for(SeatInfo seat: entranceList) {
                                    Log.d(EntraceStep3Activity.class.toString(), seat.getSeatId() + "--"
                                            + seat.getSeatStatus() + "--" + seat.isChecked());

                                    Log.d(EntraceStep3Activity.class.toString(), String.valueOf("Valid".equals(seat.getSeatStatus())));
                                    JSONObject json = new JSONObject();
                                    json.put("trans_id", ticketTrans.getTrans_id());

                                    json.put("is_concession", seat.isConcession() ? 1 : 0);

//                                    if(seat.getSeatStatus().equalsIgnoreCase("invalid")) {
//                                        json.put("type", "out");
//                                    } else if(seat.getSeatStatus().equalsIgnoreCase("valid")) {
//                                        json.put("type", "in");
//                                    }

                                    json.put("type", seat.getAction() == ScanType.IN ? "in" : "out");

                                    json.put("seat_no", seat.getSeatId());
                                    json.put("cinema_id", cinemaID);

                                    params.add(json);
                                }

//                                Log.d(EntraceStep3Activity.class.toString(), params.toString());


                                if(params.size() > 0) {
                                    NetworkRepository.getInstance().multipleValidateTicket(
                                            params,
                                            EntraceStep3Activity.this);
                                }


                            } catch (Exception e) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Error! Could not update ticket",
                                        Toast.LENGTH_SHORT).show();
                            }

                            dialog.dismiss();
                            //  Go back to Login Page
                            //finish();
                        }
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

            }

        });

        findViewById(R.id.cancel).setOnClickListener(new ImageView.OnClickListener() {

            @Override

            public void onClick(View v) {
                finish();
            }

        });


        for (int x = 0; x < ticketTypeList.size(); x++) {

            String ticketTypeName = ticketTypeList.get(x).toString();
            int paddingPixel = 10;
            float density = EntraceStep3Activity.this.getResources().getDisplayMetrics().density;
            int paddingDp = (int) (paddingPixel * density);

            TextView tv = new TextView(this);
            LinearLayout.LayoutParams paramsTV = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(paramsTV);
            tv.setEms(10);
            //tv.setInputType();
            //tv.setPadding(paddingDp, paddingDp, paddingDp, 0);
            tv.setPadding(paddingDp, 5, paddingDp, 0);

            tv.setText(ticketTypeName);
            tv.setTextColor(Color.parseColor("#aaaaaa"));
            tv.setTextSize(14);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            GridView gv = new GridView(EntraceStep3Activity.this);

            // gv.setNumColumns();
            gv.setLayoutParams(params);
            gv.setHorizontalSpacing(3);
            gv.setPadding(paddingDp, 0, paddingDp, 0);
            gv.setColumnWidth(120);
            //gv.setNumColumns(GridView.AUTO_FIT);
            gv.setNumColumns(8);
            gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

            // set the onitemclicklistener
            setOnClick(gv, x);


            ArrayList tmpList = (ArrayList) ticketList.get(x);
            ImageAdapter adapter = new ImageAdapter(this, tmpList);
            //gvAdult.setAdapter(imageAdultAdapter);
            gv.setAdapter(adapter);
            adapter.notifyDataSetChanged();


            int noOfLine = tmpList.size();
            noOfLine = (noOfLine / 8);

            if ((tmpList.size()) % 8 > 0)
                noOfLine ++;

            if (noOfLine > 1) {
                int totalHeight = 0;
                totalHeight = 116 * noOfLine;

                ViewGroup.LayoutParams params4 = gv.getLayoutParams();
                params.height = totalHeight;
                gv.setLayoutParams(params4);
            }

            ticketContainer.addView(tv);
            ticketContainer.addView(gv);
        }
    }



    @Override
    public void onResponse(ResponseType responseType, String result) {
        loading.dismiss();

        switch (responseType) {
            case UPDATE_TICKET_TYPE:

                if (TextUtils.isEmpty(result)) {
                    Context context = getApplicationContext();
                    CharSequence text = "Login Fail, please try again";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } else if (result.startsWith("ERROR")) {
                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");
                    Toast.makeText(
                            getApplicationContext(),
                            displayErr,
                            Toast.LENGTH_SHORT)
                    .show();
                } else {

                    Log.d("Return", result);
                    //
                    try {
                        new JSONObject(result);
                        Toast.makeText(getApplicationContext(),
                                "Save Successful",
                                Toast.LENGTH_SHORT)
                                .show();

                        finish();


                    } catch (Exception ec) {
                        Toast.makeText(getApplicationContext(),
                                        "Error: (" + ec.getMessage() + ")",
                                        Toast.LENGTH_SHORT).show();
                        return;
                    }


                }

                break;
            case GATE_VALIDATE_TICKET:
                Log.d(EntraceStep3Activity.class.toString(), "hahaha---->");
                break;
        }
    }


    private void setOnClick(final GridView gv, final int index) {
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View imgView, int position, long id) {

                ArrayList tmpList = (ArrayList) EntraceStep3Activity.this.ticketList.get(index);
                ArrayList tmpIdList = (ArrayList) EntraceStep3Activity.this.ticketIdList.get(index);
                ArrayList tmpState = (ArrayList) EntraceStep3Activity.this.ticketState.get(index);

                int status = (int) tmpList.get(position);
                int state = (int) tmpState.get(position);
                final String seatID = (String)tmpIdList.get(position);

                List<SeatInfo> seats = Arrays.asList(ticketTrans.getSeatInfoList());


                Predicate<SeatInfo> matchSeatID = new Predicate<SeatInfo>() {
                    @Override
                    public boolean test(SeatInfo seatInfo) {
                        return seatInfo.getSeatId().equals(seatID) && seatInfo.getAction() != ScanType.REFUND;
                    }

//                    @Override
//                    public boolean apply(SeatInfo seatInfo) {
//                        return seatInfo.getSeatId().equals(seatID) && seatInfo.getAction() != ScanType.REFUND;
//                    }
                };

                Collection<SeatInfo> result = Utils.filter(seats, matchSeatID);

                if(result.size() == 0) return;


                ImageView image = (ImageView) imgView;
                if (status == 0) {
                    // Invalid
                    if (state == 0) {

                        image.setImageResource(R.mipmap.free);
                        tmpState.set(position, 1);
                    } else {
                        image.setImageResource(R.mipmap.notavailable);
                        tmpState.set(position, 0);
                    }

                    addToEntranceList(seatID);
                } else {
                    if (state == 0) {

                        image.setImageResource(R.mipmap.available);
                        tmpState.set(position, 1);
                    } else {
                        image.setImageResource(R.mipmap.free);
                        tmpState.set(position, 0);
                    }

                    addToEntranceList(seatID);
                }

                ticketState.set(index, tmpState);

            }
        });
    }


    /**
     * Add to Entrance list with based on seat id
     * @param seatID
     */
    private void addToEntranceList(final String seatID) {
        if(ticketTrans == null) return;

        List<SeatInfo> seats = Arrays.asList(ticketTrans.getSeatInfoList());

        Predicate<SeatInfo> matchSeatID = new Predicate<SeatInfo>() {
            @Override
            public boolean test(SeatInfo seatInfo) {
                return seatInfo.getSeatId().equals(seatID);
            }

//            @Override
//            public boolean apply(SeatInfo seatInfo) {
//                return seatInfo.getSeatId().equals(seatID);
//            }
        };

        Collection<SeatInfo> result = Utils.filter(seats, matchSeatID);

        if(result.size() == 0) return;

        for(SeatInfo seat : result) {
            seat.setChecked(!seat.isChecked());
            seat.setAction(seat.getAction() == ScanType.IN ? ScanType.OUT : ScanType.IN);
        }

        entranceList.clear();
        for(SeatInfo seat : seats){
            if(seat.isChecked()) {
                entranceList.add(seat);
            }
        }

        for(SeatInfo seat : entranceList) {
            Log.d(EntraceStep3Activity.class.toString(), "SeatInfo: " +
                    seat.getSeatId() + " " + seat.getAction() + " " + seat.getSeatStatus());
        }
    }

    /**
     * Add records to SQLite
     */
    private void addEntranceLog() {
        if(entranceList.size() == 0) return;

        Entrance entrance = new Entrance(EntraceStep3Activity.this);

        try {
            entrance.add(encryptRefNo, entranceList);
        } catch (Exception e) {
            String msg = e.getMessage();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        } finally {
            loading.dismiss();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        this.setResult(0);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.setResult(0);
    }


    private void offlineHandling() {
        // Update to Database
        ArrayList items = new ArrayList();

        for (int x = 0; x < ticketTypeList.size(); x++) {
            ArrayList tmpList = (ArrayList) ticketList.get(x);
            ArrayList tmpIdList = (ArrayList) ticketIdList.get(x);
            ArrayList tmpState = (ArrayList) ticketState.get(x);

            if (tmpList != null) {
                for (int y = 0; y < tmpList.size(); y++) {
                    //if (((int)tmpList.get(y)) == 2) {
                    if (((int) tmpState.get(y)) == 1) {

                        Item item = new Item();

                        item.setRefNo(encryptRefNo);
                        item.setSeatId(tmpIdList.get(y).toString());

                        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //String currentDateandTime = sdf.format(new Date());

                        //seat.put("timestamp", currentDateandTime);

                        if (((int) tmpList.get(y)) == 1) {
                            // Valid
                            //seat.put("action", "C");
                            item.setSeatStatus("Invalid");
                        } else if (((int) tmpList.get(y)) == 0) {
                            // InValid
                            //seat.put("action", "R");
                            item.setSeatStatus("Valid");
                        }

                        item.setTicketType(ticketTypeList.get(x).toString());

                        items.add(item);

                    }
                }
            }

        }

        OfflineDatabase offline = new OfflineDatabase(EntraceStep3Activity.this);

        try {
            // Hide offline saving
            offline.accept(items);

            // Add Entrance log
            addEntranceLog();

            // Check result
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, "Save Successful", duration);
            toast.show();

            finish();

            //Item rec = offline.getRecordBySeatId(encryptRefNo,  tmpIdList.get(y).toString());
        } catch (Exception esql) {
            // Error during execution
            Context context = getApplicationContext();
            String reason = esql.getMessage();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, reason, duration);
            toast.show();
        }
    }

    /**
     * Cancel Handling
     * @param view
     */
    public void cancelHandler(View view) {
        finish();
    }


    /**
     * Enter Handling
     * @param view
     */
    public void enterHandler(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntraceStep3Activity.this);


    }


    /**
     * Exit Handling
     * @param view
     */
    public void exitHandler(View view) {
    }


    @Override
    public void handleResults(ResponseType responseType, List<String> result) {
        if(loading != null) {
            loading.dismiss();
        }

        Toast.makeText(getApplicationContext(),
                "Update successfully!",
                Toast.LENGTH_SHORT).show();

        finish();
    }

    @Override
    public void handleError(Throwable t) {
        Log.d(EntraceStep3Activity.class.toString() + "handelError", "Throw!!!");

        if(loading != null) {
            loading.dismiss();
        }

        Toast.makeText(getApplicationContext(),
                "Error on update the ticket!",
                Toast.LENGTH_SHORT).show();
    }

}
