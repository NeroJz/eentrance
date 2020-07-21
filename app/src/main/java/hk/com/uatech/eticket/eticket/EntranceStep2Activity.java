package hk.com.uatech.eticket.eticket;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.mysql.jdbc.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import hk.com.uatech.eticket.eticket.database.Entrance;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.pojo.Counter;
import hk.com.uatech.eticket.eticket.pojo.SeatInfo;
import hk.com.uatech.eticket.eticket.pojo.TicketTrans;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import hk.com.uatech.eticket.eticket.qrCode.QRActivity;
import hk.com.uatech.eticket.eticket.utils.Utils;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;


public class EntranceStep2Activity extends QRActivity implements NetworkRepository.QueryCallback /*, ReceiveListener */ {

    enum ScanStatus {
        NONE,
        REDEEMED,
        VALID,
        INVALID,
    }

    private static boolean IS_DEBUG = false;

    private Printer mPrinter = null;
    private final int MY_PERMISSIONS_CAMERA = 10099;

    private final boolean isLocalDebug = false;
    private final boolean isLocalSkipPrint = false;

    private static final int PRINTERS_COUNT = 5;
    private List<Printer> mPrinters = new ArrayList<>(PRINTERS_COUNT);

    private List<String> mPrinterIPList = new ArrayList<>();


    private int printerName = Printer.TM_M30;
    private int printerLang = Printer.MODEL_CHINESE;

    private boolean isFirstTimeUpdate = false;
    private boolean containsFood = false;
    private ArrayList containsComboFoods = new ArrayList();

    private final int REQ_CODE = 16500;

    private ProgressDialog loading = null;

    public List ticketTypeList = new ArrayList();
    public List ticketList = new ArrayList();
    public List ticketIdList = new ArrayList();
    public List ticketState = new ArrayList();

    // List for Print Out
    public List printFoodref = new ArrayList();
    public List printItemId = new ArrayList();
    public List printCategory = new ArrayList();
    public List printQuantity = new ArrayList();
    public List printEngDesc = new ArrayList();
    public List printChiDesc = new ArrayList();
    public List printDept = new ArrayList();
    public List printRemark = new ArrayList();
    public List printSeat = new ArrayList();


    // For Consolidate
    public List cprintFoodref = new ArrayList();
    public List cprintItemId = new ArrayList();
    public List cprintCategory = new ArrayList();
    public List cprintQuantity = new ArrayList();
    public List cprintEngDesc = new ArrayList();
    public List cprintChiDesc = new ArrayList();
    public List cprintDept = new ArrayList();
    public List cprintRemark = new ArrayList();
    public List cprintSeat = new ArrayList();


    public List printerADept = new ArrayList();
    public List printerAIp = new ArrayList();
    public List printerAName = new ArrayList();
    public List printerADesc = new ArrayList();

    public String consolidateIp = "";
    public String consolidateName = "";
    public String consolidateDesc = "";

    public ArrayList foodOrderIds = new ArrayList();
    public ArrayList foodOrderStatuses = new ArrayList();
    public ArrayList foodOrderIds2 = new ArrayList();
    private int currentFoodRefNoPointer = 0;
    private int currentFoodRefNoActionPointer = 0;
    private ArrayList orderTypes = new ArrayList();

    private TableLayout tl;


    private GridView gvAdult;
    private GridView gvBigi;
    private GridView gvChild;
    private GridView gvSenior;

    private ImageView accept;
    private ImageView reject;
    private ImageView update;

    private ImageAdapter imageAdultAdapter;
    private ImageAdapter imageBigiAdapter;
    private ImageAdapter imageChildAdapter;
    private ImageAdapter imageSeniorAdapter;

    private String encryptRefNo = "";
    private String refType = "";

    private String jsonSource = "";

    private String movieTransId = "";
    private String accessMode = "";

    private boolean outsideGracePeriod = false;


    private DecoratedBarcodeView barcodeView;
    private String lastText;
    private ImageView imgValid;
    private ImageView imgInvalid;
    private View cardBack;
    private TextView scanStatus;

    // TicketTran Info
    private TicketTrans ticketTrans;

    private List<SeatInfo> checkedItems = new ArrayList<SeatInfo>();

    private ScanStatus scan_status = ScanStatus.NONE;


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                return;
            }
            lastText = result.getText();
            barcodeView.setStatusText(result.getText());
            getIntent().putExtra("json", lastText);

            handleQrCode(lastText, true);
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_step2);

        IS_DEBUG = Utils.isDebug(getApplicationContext());

        tl = (TableLayout) findViewById(R.id.detailview);
        barcodeView = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.decodeContinuous(callback);
        scanStatus = (TextView) findViewById(R.id.scanStatus);
        init();
    }


    public void debugMsg1() {
        String debugLogContent = "Check ArrayList\r\n";


        debugLogContent += "printFoodref(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printFoodref != null) {

            for (int ll = 0; ll < printFoodref.size(); ll++) {
                debugLogContent += "" + printFoodref.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printItemId(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printItemId != null) {

            for (int ll = 0; ll < printItemId.size(); ll++) {
                debugLogContent += "" + printItemId.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printCategory(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printCategory != null) {

            for (int ll = 0; ll < printCategory.size(); ll++) {
                debugLogContent += "" + printCategory.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printQuantity(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printQuantity != null) {

            for (int ll = 0; ll < printQuantity.size(); ll++) {
                debugLogContent += "" + printQuantity.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printEngDesc(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printEngDesc != null) {

            for (int ll = 0; ll < printEngDesc.size(); ll++) {
                debugLogContent += "" + printEngDesc.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printChiDesc(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printChiDesc != null) {

            for (int ll = 0; ll < printChiDesc.size(); ll++) {
                debugLogContent += "" + printChiDesc.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printDept(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printDept != null) {

            for (int ll = 0; ll < printDept.size(); ll++) {
                debugLogContent += "" + printDept.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printSeat(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printSeat != null) {

            for (int ll = 0; ll < printSeat.size(); ll++) {
                debugLogContent += "" + printSeat.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printRemark(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printRemark != null) {

            for (int ll = 0; ll < printRemark.size(); ll++) {
                debugLogContent += "" + printRemark.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printerADept(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printerADept != null) {

            for (int ll = 0; ll < printerADept.size(); ll++) {
                debugLogContent += "" + printerADept.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printerAIp(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printerAIp != null) {

            for (int ll = 0; ll < printerAIp.size(); ll++) {
                debugLogContent += "" + printerAIp.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printerAName(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printerAName != null) {

            for (int ll = 0; ll < printerAName.size(); ll++) {
                debugLogContent += "" + printerAName.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        debugLogContent += "printerADesc(s):\r\n";
        debugLogContent += "------------------\r\n";
        if (printerADesc != null) {

            for (int ll = 0; ll < printerADesc.size(); ll++) {
                debugLogContent += "" + printerADesc.get(ll).toString() + "\r\n";
            }
        }
        debugLogContent += "------------------\r\n";


        MainActivity.appendLog(debugLogContent);
    }

    private void init() {
        if (loading == null) {
            loading = new ProgressDialog(EntranceStep2Activity.this);

            loading.setCancelable(true);
            loading.setMessage("Loading");
            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        }
        ticketIdList.clear();
        ticketList.clear();
        ticketState.clear();
        ticketTypeList.clear();
        loading.show();
        View actionPanel = findViewById(R.id.detailActionPanel);
        View navPanel = findViewById(R.id.navPanel);
        View mainPanel = findViewById(R.id.mainPanel);

        if (getIntent().getBooleanExtra("showScanner", false)) {
            barcodeView.setVisibility(View.VISIBLE);
            actionPanel.setVisibility(View.GONE);
            navPanel.setVisibility(View.GONE);

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_CAMERA);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_CAMERA);
                }
            }

        } else {
            barcodeView.setVisibility(View.GONE);
            actionPanel.setVisibility(View.VISIBLE);
            navPanel.setVisibility(View.VISIBLE);
        }

        if (StringUtils.isNullOrEmpty(getIntent().getExtras().getString("json"))) {
            mainPanel.setVisibility(View.GONE);
            loading.dismiss();
            return;
        } else {
            mainPanel.setVisibility(View.VISIBLE);
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String gracePeriodFrom = PreferencesController.getInstance().getGracePeriodFrom();
        String gracePeriodTo = PreferencesController.getInstance().getGracePeriodTo();
        accessMode = PreferencesController.getInstance().getAccessMode();


        LinearLayout bgLayer = (LinearLayout) findViewById(R.id.bglayer);
        if ("offline".equals(accessMode)) {
            //tv.setVisibility(View.VISIBLE);
//            bgLayer.setBackgroundColor(Color.rgb(255, 179, 179));

        } else {
            //tv.setVisibility(View.GONE);
//            bgLayer.setBackgroundColor(Color.rgb(244, 244, 244));

        }

        int gpFrom = Integer.parseInt(gracePeriodFrom);
        int gpTo = Integer.parseInt(gracePeriodTo);

        // Get the object
        final String jsonstr = getIntent().getExtras().getString("json");
        JSONObject jsonObj;

        foodRefNo = getIntent().getExtras().getString("foodRefNo");

        encryptRefNo = getIntent().getExtras().getString("encryptRefNo");
        refType = getIntent().getExtras().getString("refType");

        jsonSource = jsonstr;
        //

        // Populate the ticket trans variable
        if(jsonSource != null && !jsonSource.equals(" ")) {
            Gson gson = new Gson();
            ticketTrans = gson.fromJson(jsonSource, TicketTrans.class);
        }

//        Log.d(EntranceStep2Activity.class.toString(), String.valueOf(ticketTrans.getLogIn().size()));
//        Log.d(EntranceStep2Activity.class.toString(), String.valueOf(ticketTrans.getLogOut().size()));

        TableLayout tlEnter = (TableLayout) findViewById(R.id.tlEnterDetail);
        TextView tvClickEnter = (TextView) findViewById(R.id.tvClickEnter);

        ArrayList<String> compareWith = new ArrayList<String>(Arrays.asList(ticketTrans.getSeats().split(",")));

        if(ticketTrans != null && ticketTrans.getLogIn().size() > 0) {
            for(String strDate: ticketTrans.getLogIn().keySet()) {

                TableRow tableRow = new TableRow(this);
                LayoutParams tableRowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                tableRow.setLayoutParams(tableRowParams);

                TextView dateTv = createDateTextView(strDate);

                List<String> seatNo = ticketTrans.getLogIn().get(strDate);

                TextView seatsTv = createSeatTextView(strDate, seatNo, compareWith);

                if(!seatsTv.getText().toString().equals("")) {
                    tableRow.addView(dateTv);
                    tableRow.addView(seatsTv);
                    tlEnter.addView(tableRow);
                }

            }
        } else {
            tlEnter.setVisibility(View.GONE);
            tvClickEnter.setVisibility(View.GONE);
        }


        TableLayout tlExit = (TableLayout) findViewById(R.id.tlExitDetail);
        TextView tvClickExit = (TextView) findViewById(R.id.tvClickExit);
        if(ticketTrans != null && ticketTrans.getLogOut().size() > 0) {
            for(String strDate : ticketTrans.getLogOut().keySet()) {
                TableRow tr = new TableRow(this);
                LayoutParams trLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                tr.setLayoutParams(trLayoutParams);

                TextView dateTv = createDateTextView(strDate);
                List<String> seatNo = ticketTrans.getLogOut().get(strDate);
                TextView seatsTv = createSeatTextView(strDate, seatNo, compareWith);

                if(!seatsTv.getText().toString().equals("")) {
                    tr.addView(dateTv);
                    tr.addView(seatsTv);
                    tlExit.addView(tr);
                }

            }
        } else {
            tlExit.setVisibility(View.GONE);
            tvClickExit.setVisibility(View.GONE);
        }

        boolean hasConcession = false;
        boolean hasNormal = false;

        int totalConcession = 0;
        int totalNormal = 0;

        int scanned_concession = 0;
        int scanned_normal = 0;

        for(SeatInfo seat : ticketTrans.getSeatInfoList()) {
            if(seat.getAction() == ScanType.REFUND) continue;

            if(seat.isConcession() && seat.getAction() != ScanType.REFUND) {
                hasConcession = true;
                totalConcession += 1;

                if(seat.getAction() == ScanType.IN) {
                    scanned_concession += 1;
                }

            } else {
                hasNormal = true;
                totalNormal += 1;

                if(seat.getAction() == ScanType.IN) {
                    scanned_normal += 1;
                }
            }
        }

        TableRow trTotalRemainingConcession = (TableRow) findViewById(R.id.trTotalRemainingConcession);
        TableRow trTotalRemainingNormal = (TableRow) findViewById(R.id.trTotalRemainingNormal);


        if(!hasConcession) {
            trTotalRemainingConcession.setVisibility(View.GONE);
        } else {
            TextView tvTotalRemaining = (TextView) findViewById(R.id.tvTotalRemainingConcession);
            int remain = totalConcession - scanned_concession;
            String info = remain + " / " + totalConcession;
            tvTotalRemaining.setText(info);
        }

        if(!hasNormal) {
            trTotalRemainingNormal.setVisibility(View.GONE);
        } else {
            TextView tvTotalRemaining = (TextView) findViewById(R.id.tvTotalRemainingNormal);
            int remain = totalNormal - scanned_normal;
            String info = remain + " / " + totalNormal;
            tvTotalRemaining.setText(info);
        }

        /*
        TableRow trTotalRemaining = (TableRow) findViewById(R.id.trTotalRemaining);

        // Set Total / Remaining info
        if(ticketTrans == null || ticketTrans.getCounter() == null) {
            trTotalRemaining.setVisibility(View.GONE);
        } else {
            TextView tvTotalRemaining = (TextView) findViewById(R.id.tvTotalRemaining);
            Counter counter = ticketTrans.getCounter();
            String info = counter.getRemain_ticket() + " / " + counter.getTotal_ticket();
            tvTotalRemaining.setText(info);
        }
         */



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

        String fullSeat = "";

        // For Food
        List foodIds = new ArrayList();
        List foodValues = new ArrayList();
        List foodQty = new ArrayList();

        int totalSeats = 0;
        int validSeats = 0;

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

            // 20170929, change to get the food Order Id from Json
            try {
                JSONArray aFood = tran.getJSONArray("fbOrderId");

                if (aFood != null) {
                    for (int z = 0; z < aFood.length(); z++) {
                        foodOrderIds.add(aFood.get(z).toString());
                    }
                }
            } catch (Exception efoodOrder) {
                // Nothing to do
            }

            final String strType = getIntent().getExtras().getString("scanType");
            ScanType scanType = ScanType.valueOf(strType);

            // For Seat Info
            if (seats != null) {
                for (int y = 0; y < seats.length(); y++) {
                    JSONObject seat = seats.getJSONObject(y);

                    // Get the food information

                    JSONArray foods = null;
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
                                        // Get the original qty
                                        String tmpArr[] = tmpQtyToChange.split(",");
                                        if (tmpArr.length > 0) {
                                            tmpQtyToChange = "";

                                            String reformat = "";
                                            for (int t1 = 0; t1 < tmpArr.length; t1++) {
                                                String tmpArr2[] = tmpArr[t1].split(" ");
                                                reformat = tmpArr[t1];

                                                if (seat.getString("seatId").compareTo(tmpArr2[0]) == 0) {


                                                    int orgQty = 0;

                                                    if (tmpArr2.length > 1) {
                                                        // pattern is "A6 x 2"
                                                        // reformat =
                                                        orgQty = Integer.parseInt(tmpArr2[2]);
                                                    } else {
                                                        // patter is "A6"
                                                        orgQty = 1;
                                                    }

                                                    orgQty += tmpFoodQty;

                                                    reformat = seat.getString("seatId") + " x " + String.valueOf(orgQty);
                                                }

                                                if (tmpQtyToChange.length() > 0) {
                                                    tmpQtyToChange += ",";
                                                }

                                                tmpQtyToChange += reformat;
                                            }
                                        }


                                        //tmpQtyToChange += "," + seat.getString("seatId") + " x " + String.valueOf(tmpFoodQty);

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

                    if (foodIds != null && foodIds.size() > 0) {
                        containsFood = true;
                    } else {
                        containsFood = false;
                    }

                    // Concat the Seat ID
                    if (seat.getString("seatId") != "") {
                        if (seatIds != "")
                            seatIds += ",";

                        seatIds += seat.getString("seatId");
                    }

                    //  Ticket Type
                    String ticketType = seat.getString("ticketType");
                    String seatStatus = seat.getString("seatStatus");
                    int seatIcon = 0;

                    Counter counter = ticketTrans.getCounter();

                    // If scanned ticket type (normal/concession)
                    // equals to seat ticket type (normal/concession)
                    // set seat is selected

                    /*
                    if(scanType == ScanType.IN || scanType == ScanType.NONE) {
                        if(ticketTrans.isConcession() == seat.getBoolean("isConcession") &&
                                "Valid".compareTo(seatStatus) == 0) {
                            seatIcon = 2;
                            validSeats++;
                            setSeatChecked(true, y);
                        } else {
                            seatIcon = 0;
                            setSeatChecked(false, y);
                        }
                    } else if(scanType == ScanType.OUT) {
                        if(ticketTrans.isConcession() == seat.getBoolean("isConcession") &&
                                "Invalid".compareTo(seatStatus) == 0) {
                            seatIcon = 2;
                            validSeats++;
                            setSeatChecked(true, y);
                        } else {
                            seatIcon = 0;
                            setSeatChecked(false, y);
                        }
                    }
                     */

                    /*
                    if(scanType == ScanType.IN &&
                            ScanType.valueOf(seat.getString("action")) == ScanType.OUT &&
                            ticketTrans.isConcession() == seat.getBoolean("isConcession")) {
                        seatIcon = 2;
                        validSeats++;
                        setSeatChecked(true, y);
                    }else if(scanType == ScanType.IN &&
                            ScanType.valueOf(seat.getString("action")) == ScanType.NONE &&
                            ticketTrans.isConcession() == seat.getBoolean("isConcession")) {
                        seatIcon = 2;
                        validSeats++;
                        setSeatChecked(true, y);
                    } else if(scanType == ScanType.OUT &&
                            ScanType.valueOf(seat.getString("action")) == ScanType.IN &&
                            ticketTrans.isConcession() == seat.getBoolean("isConcession")) {
                        seatIcon = 2;
                        validSeats++;
                        setSeatChecked(true, y);
                    } else {
                        seatIcon = 0;
                        setSeatChecked(false, y);
                    }
                     */


                    if((scanType == ScanType.IN &&
                            ((ScanType.valueOf(seat.getString("action")) == ScanType.OUT) ||
                            ScanType.valueOf(seat.getString("action")) == ScanType.NONE) ||
                            (scanType == ScanType.OUT &&
                                    ScanType.valueOf(seat.getString("action")) == ScanType.IN)) &&
                            ticketTrans.isConcession() == seat.getBoolean("isConcession")) {
                        seatIcon = 2;
                        validSeats++;
                        setSeatChecked(true, y);
                    } else {
                        seatIcon = 0;
                        setSeatChecked(false, y);
                    }

                    /**
                    if ("Valid".compareTo(seatStatus) == 0) {
                        //seatIcon = 1;
                        // Original is 1, but after chaing the requirement @20170822, all valid seat should be selected
                        seatIcon = 2;
                        validSeats++;

                        setSeatChecked(true, y);
                    } else if ("Invalid".compareTo(seatStatus) == 0) {
                        seatIcon = 0;
                        setSeatChecked(false, y);

                    } else {
                        seatIcon = 2;
                        setSeatChecked(true, y);
                    }
                     */



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

                    if(scanType == ScanType.IN || scanType == ScanType.NONE) {
                        if ("Valid".compareTo(seatStatus) == 0) {
                            tmpState.add(1);
                        } else {
                            tmpState.add(0);
                        }
                    } else {
                        if ("Invalid".compareTo(seatStatus) == 0) {
                            tmpState.add(1);
                        } else {
                            tmpState.add(0);
                        }
                    }

                    ticketList.set(ticketTypeInd, tmpList);
                    ticketIdList.set(ticketTypeInd, tmpIdList);
                    ticketState.set(ticketTypeInd, tmpState);
                }
            }

            // Get full seat info
//            fullSeat = tran.getString("fullSeat");


        } catch (Exception ec) {
            // Show Message
            Context context = getApplicationContext();
            CharSequence text = ec.getMessage(); //"Login Fail, please try again";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            return;
        }

        LinearLayout ticketContainer = (LinearLayout) findViewById(R.id.ticketContainer);
        ticketContainer.removeAllViews();

        ((TextView) findViewById(R.id.movieTitle)).setText(movieTitle);
        ((TextView) findViewById(R.id.cinemaName)).setText(cinemaName);


        ((TextView) findViewById(R.id.movieTransId)).setText(movieTransId);
        ((TextView) findViewById(R.id.cinemaName2)).setText(cinemaName2);
        ((TextView) findViewById(R.id.movieCategory)).setText(movieCategory);

        // If scanned ticket is Concession
        ArrayList<String> lst_concession = new ArrayList<String>();
        ArrayList<String> lst_normal = new ArrayList<String>();
        for(SeatInfo seat : ticketTrans.getSeatInfoList()) {
            if(seat.isConcession()) {
                lst_concession.add(seat.getSeatId());
            }else{
                lst_normal.add(seat.getSeatId());
            }
        }


        if(lst_concession.size() > 0) {
            String str_concession = TextUtils.join(", ", lst_concession);
            ((TextView) findViewById(R.id.concession_seats)).setText(str_concession);
        } else {
            (findViewById(R.id.tblRowConcession)).setVisibility(View.GONE);
        }

        if(lst_normal.size() > 0) {
            String str_normal = TextUtils.join(", ", lst_normal);
            ((TextView) findViewById(R.id.normal_seats)).setText(str_normal);
        } else {
            (findViewById(R.id.tblRowNormal)).setVisibility(View.GONE);
        }


//        ((TextView) findViewById(R.id.seatIds)).setText(seatIds);
//        ((TextView) findViewById(R.id.seatIds)).setText(fullSeat);


        TableLayout dtlTbl = (TableLayout) findViewById(R.id.detailview);
        if (foodIds.size() > 0) {
            for (int i = 0; i < foodIds.size(); i++) {
                TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                TableRow tr = new TableRow(this);
                tr.setPadding(0, 5, 0, 0);
                tr.setLayoutParams(new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView tvFoodName = new TextView(this);
                tvFoodName.setTextSize(14);
                tvFoodName.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                tvFoodName.setText(foodValues.get(i).toString());
                tvFoodName.setTextColor(Color.parseColor("#aaaaaa"));
                tvFoodName.setEms(10);
                tvFoodName.setLayoutParams(paramsExample);


                TextView tvFoodDesc = new TextView(this);
                tvFoodDesc.setTextSize(14);
                tvFoodDesc.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                tvFoodDesc.setTextColor(Color.parseColor("#a9987a"));
                tvFoodDesc.setText(foodQty.get(i).toString());
                tvFoodDesc.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                tvFoodDesc.setEms(10);

                tvFoodDesc.setLayoutParams(paramsExample);

                tr.addView(tvFoodName);
                tr.addView(tvFoodDesc);


                dtlTbl.addView(tr);

            }

        }
        boolean isValid = true;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US); //new SimpleDateFormat("dd MMM yyyy (E) hh:mma");
        //DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        //DateTime dt = formatter.parseDateTime(string);
        Date dt;
        Date currentDt = new Date();

        if ("".compareTo(showDate) == 0 || "".compareTo(showTime) == 0) {
            // Nothing to do
        } else {
            try {
                //dt = format.parse("10 August 2017 (Thu)" + " " + "12:20pm");
                dt = format.parse(showDate + " " + showTime);
                displayFileDateTime = dt.toString();

                /**
                 * Test Case
                 */

                if(IS_DEBUG) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.MINUTE, 5);
                    dt = cal.getTime();
                }
                /**
                 * End of test case
                 */

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

        imgValid = (ImageView) findViewById(R.id.imgValid);
        imgInvalid = (ImageView) findViewById(R.id.imgInvalid);
        cardBack = findViewById(R.id.card_back);

        boolean isInvalid = resultMsg.compareToIgnoreCase("success") == 0;

        outsideGracePeriod = !isValid;

        if (TextUtils.isEmpty(showDate)) {
            changeMode(!isInvalid);
        } else {
            changeMode(isValid);
        }

        final String strType = getIntent().getExtras().getString("scanType");
        ScanType scanType = ScanType.valueOf(strType);


        if(scanType == ScanType.OUT) {
            if(validSeats > 0) {
                scan_status = ScanStatus.VALID;
                changeMode(true);
            } else {
                scan_status = ScanStatus.INVALID;
                changeMode(false);
            }
        } else {
            if(!outsideGracePeriod && validSeats == 0) {
                scan_status = ScanStatus.REDEEMED;
//                imgInvalid.setImageResource(R.mipmap.redeemed);
                imgValid.setVisibility(View.GONE);
                changeMode(false);
            }else if(!outsideGracePeriod && validSeats > 0) {
                scan_status = ScanStatus.VALID;
                changeMode(true);
            } else {
                scan_status = ScanStatus.INVALID;
                changeMode(false);
            }
        }


        ((TextView) findViewById(R.id.showDate2)).setText(displayFilmDate);
        ((TextView) findViewById(R.id.showDate)).setText(displayFilmDate);
        ((TextView) findViewById(R.id.showTime)).setText(displayFilmTime);
        ((TextView) findViewById(R.id.showTime2)).setText(displayFilmTime);

        // Invisible the Detail TableView
        tl = (TableLayout) findViewById(R.id.detailview);
        tl.setVisibility(View.GONE);

        ImageView ivDetail = (ImageView) findViewById(R.id.detail);
        ivDetail.setOnClickListener(new ImageView.OnClickListener() {

            @Override

            public void onClick(View v) {
                // Show / Hide the Detail View
                if (tl.getVisibility() == View.VISIBLE) {
                    tl.setVisibility(View.GONE);
                } else {
                    tl.setVisibility(View.VISIBLE);
                }

            }

        });



        ImageView acceptIV = (ImageView) findViewById(R.id.accept);
        if(scanType == ScanType.IN) {
            acceptIV.setImageResource(R.mipmap.scan_in);
        } else if(scanType == ScanType.OUT) {
            acceptIV.setImageResource(R.mipmap.scan_out);
        }

        findViewById(R.id.accept).setOnClickListener(new ImageView.OnClickListener() {
            @Override

            public void onClick(View v) {
                showAcceptDialog();
            }

        });


        findViewById(R.id.reject).setOnClickListener(new ImageView.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

        View update = findViewById(R.id.update);
        update.setOnClickListener(new ImageView.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (PreferencesController.getInstance().isUsePasswordForChangeSeat()) {
                    if ("MANAGER".equals(PreferencesController.getInstance().getUserRank())) {
                        showPasswordDialog();
                    } else {
                        Toast.makeText(
                                EntranceStep2Activity.this,
                                "unauthorized",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
//                    if ("MANAGER".equals(PreferencesController.getInstance().getUserRank())) {
//                        editSeatStatus();
//                    } else {
//                        Toast.makeText(
//                                EntranceStep2Activity.this,
//                                "unauthorized",
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
                    editSeatStatus();
                }
//
//                if ("MANAGER".equals(PreferencesController.getInstance().getUserRank())) {
//                    if (PreferencesController.getInstance().isUsePasswordForChangeSeat()) {
//                        showPasswordDialog();
//                    } else {
//                        editSeatStatus();
//                    }
//                } else {
//                    if ("offline".compareTo(accessMode) == 0) {
//                        Toast.makeText(
//                                EntranceStep2Activity.this,
//                                "Sorry, User don't have the access right to edit in Offline Mode",
//                                Toast.LENGTH_LONG)
//                                .show();
//
//                    } else {
//                        // Show Login Dialog
//                        showLoginDialog();
//                    }
//
//                }

            }

        });

        // Hide the Update button or not
        if (validSeats == totalSeats) {
            //update.setVisibility(View.GONE);
            update.setEnabled(false);
            isFirstTimeUpdate = true;
        }

        for (int x = 0; x < ticketTypeList.size(); x++) {

            String ticketTypeName = ticketTypeList.get(x).toString();
            int paddingPixel = 10;
            float density = EntranceStep2Activity.this.getResources().getDisplayMetrics().density;
            int paddingDp = (int) (paddingPixel * density);

            TextView tv = new TextView(EntranceStep2Activity.this);
            LayoutParams paramsTV = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(paramsTV);
            tv.setEms(10);
            //tv.setInputType();
            //tv.setPadding(paddingDp, paddingDp, paddingDp, 0);
            tv.setPadding(paddingDp, 5, paddingDp, 0);

            tv.setText(ticketTypeName);
            tv.setTextColor(Color.parseColor("#aaaaaa"));
            tv.setTextSize(14);

            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            GridView gv = new GridView(EntranceStep2Activity.this);

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
                noOfLine++;

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

        // Last, to check for the FOOD
        if (isFirstTimeUpdate && containsFood) {

            // Contain Food Information, need to call the FB API listOrder to get the F&B order tail

            if (loading == null) {
                loading = new ProgressDialog(EntranceStep2Activity.this);

                loading.setCancelable(true);
                loading.setMessage("Loading");
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);


            }
            loading.show();


            JSONObject jsonvalue = new JSONObject();

            try {

                if (foodOrderIds.size() > 0) {
                    currentFoodRefNoPointer = 0;
                    jsonvalue.put("refNo", foodOrderIds.get(currentFoodRefNoPointer).toString());

                }
            } catch (Exception ejson) {
                ejson.printStackTrace();
            }
            NetworkRepository.getInstance().listOrder(jsonvalue.toString(), this);
        } else {
            loading.dismiss();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.hideSoftInputFromWindow(EntranceStep2Activity.this.getCurrentFocus().getWindowToken(), 0);
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void changeMode(boolean isValid) {
        if (isValid) {
            playSuccess();
            imgValid.setVisibility(View.VISIBLE);
            imgInvalid.setVisibility(View.GONE);
//            cardBack.setBackgroundColor(getResources().getColor(R.color.valid));
        } else {
            playBuzzer();
            if (getIntent().getBooleanExtra("showScanner", false)) {
                scanStatus.setVisibility(View.VISIBLE);
                scanStatus.setText("Invalid ticket");
            }

            imgInvalid.setVisibility(View.VISIBLE);
            imgValid.setVisibility(View.GONE);
//            cardBack.setBackgroundColor(getResources().getColor(R.color.invalid));
        }
    }


    @Override
    public void onResponse(ResponseType responseType, String result) {
        super.onResponse(responseType, result);
        boolean canDismiss = true;

        switch (responseType) {
            case LIST_ORDER:
                if (result == "") {


                    Context context = getApplicationContext();
                    CharSequence text = "Error occur during FB_LIST, please contact administrator";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } else if (result.startsWith("ERROR")) {

                    Context context = getApplicationContext();

                    int duration = Toast.LENGTH_SHORT;

                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");


                    Toast toast = Toast.makeText(context, displayErr, duration);
                    toast.show();
                } else {
                    Log.d("Return", result);
                    JSONObject jsonObj;
                    //
                    try {
                        jsonObj = new JSONObject(result);

                        // Parse the food information

                        if (jsonObj != null) {
                            orderTypes.add(jsonObj.getString("orderType"));
                            foodOrderIds2.add(jsonObj.getString("orderId"));
                            foodOrderStatuses.add(jsonObj.getString("orderStatus"));
                            String orderType = jsonObj.getString("orderType");

                            if ("V".compareTo(orderType) == 0) {
                                JSONArray pr = jsonObj.getJSONArray("orderDetail");

                                if (pr != null) {
                                    int cd = 0;
                                    for (int j = 0; j < pr.length(); j++) {
                                        JSONObject obj = (JSONObject) pr.get(j);


                                        boolean foundDept = false;
                                        for (int m = 0; m < printDept.size(); m++) {
                                            if ((obj.getString("dept").compareTo(printDept.get(m).toString()) == 0) && foodOrderIds.get(currentFoodRefNoPointer).toString().compareTo(printFoodref.get(m).toString()) == 0) {
                                                foundDept = true;
                                                break;
                                            }
                                        }

                                        if (!foundDept) {
                                            cd++;

                                        }

                                        // Check having the same record or not (Department, EngDesc)
                                        boolean found = false;
                                        int updateIndex = -1;
                                        int orgQty = -1;

                                        for (int l = 0; l < printItemId.size(); l++) {
                                            if (
                                                    (foodOrderIds.get(currentFoodRefNoPointer).toString().compareTo(printFoodref.get(l).toString()) == 0) &&
                                                            (printSeat.get(l).toString().compareTo(obj.getString("seatId")) == 0) &&
                                                            (printDept.get(l).toString().compareTo(obj.getString("dept")) == 0) &&
                                                            (printEngDesc.get(l).toString().compareTo(obj.getString("engDesc")) == 0)
                                                    ) {
                                                found = true;
                                                updateIndex = l;
                                                orgQty = Integer.parseInt(printQuantity.get(l).toString());
                                                break;
                                            }
                                        }

                                        if (obj.getString("itemId") == null || "null".compareTo(obj.getString("itemId")) == 0 || "".compareTo(obj.getString("itemId")) == 0) {
                                            if (!foundDept) {
                                                cd--;
                                            }
                                        } else {
                                            if (found) {
                                                int newQty = Integer.parseInt(obj.getString("quantity")) + orgQty;
                                                printQuantity.set(updateIndex, String.valueOf(newQty));

                                            } else {
                                                printFoodref.add(foodOrderIds.get(currentFoodRefNoPointer).toString());
                                                printItemId.add(obj.getString("itemId"));
                                                printCategory.add(obj.getString("category"));
                                                printQuantity.add(obj.getString("quantity"));
                                                printEngDesc.add(obj.getString("engDesc"));
                                                printChiDesc.add(obj.getString("cnDesc"));
                                                //printDept.add(obj.getString("dept"));
                                                String tmpDept = "";
                                                if ("0".compareTo(obj.getString("dept")) == 0) {
                                                    printDept.add("1");
                                                    tmpDept = "1";
                                                } else {
                                                    printDept.add(obj.getString("dept"));
                                                    tmpDept = obj.getString("dept");
                                                }

                                                printSeat.add(obj.getString("seatId"));

                                                printRemark.add(obj.getString("remarks"));


                                                // Check and add the printer if needed
                                                boolean needAddPrinter = true;
                                                JSONArray arrP = obj.getJSONArray("printer");

                                                if (arrP.length() > 0) {

                                                    for (int jj = 0; jj < arrP.length(); jj++) {
                                                        JSONObject jObj = arrP.getJSONObject(jj);

                                                        if (jObj != null) {

                                                            if (jObj.getString("desc").toLowerCase().indexOf("consolidation") >= 0) {
                                                                if ("".compareTo(consolidateIp) == 0) {
                                                                    consolidateIp = jObj.getString("ip");
                                                                    consolidateName = jObj.getString("name");
                                                                    consolidateDesc = jObj.getString("desc");
                                                                }
                                                            } else {

                                                                // Not Consolidate Table
                                                                for (int zz = 0; zz < printerADept.size(); zz++) {
                                                                    if (tmpDept.compareTo(printerADept.get(zz).toString()) == 0) {
                                                                        needAddPrinter = false;
                                                                        break;
                                                                    }

                                                                }

                                                                if (needAddPrinter) {

                                                                    printerADept.add(tmpDept);

                                                                    String tmpIP = jObj.getString("ip");
                                                                    if (isLocalDebug) {
                                                                        tmpIP = tmpIP.replaceAll("20.10.1.142", "192.168.1.36");
                                                                    }
                                                                    printerAIp.add(tmpIP /*jObj.getString("ip")*/);
                                                                    printerAName.add(jObj.getString("name"));
                                                                    printerADesc.add(jObj.getString("desc"));
                                                                }
                                                            }
                                                        }


                                                    }

                                                }


                                            }
                                        }


                                    }

                                    if (cd > 1) {
                                        containsComboFoods.add("Y");
                                    } else {
                                        containsComboFoods.add("N");
                                    }


                                    // For Consolidate Food
                                    for (int j = 0; j < pr.length(); j++) {
                                        JSONObject obj = (JSONObject) pr.get(j);


                                        boolean foundDept = false;
                                        for (int m = 0; m < cprintDept.size(); m++) {
                                            if ((obj.getString("dept").compareTo(cprintDept.get(m).toString()) == 0) && foodOrderIds.get(currentFoodRefNoPointer).toString().compareTo(cprintFoodref.get(m).toString()) == 0) {
                                                foundDept = true;
                                                break;
                                            }
                                        }

                                        if (!foundDept) {
                                            cd++;

                                        }

                                        // Check having the same record or not (Department, EngDesc)
                                        boolean found = false;
                                        int updateIndex = -1;
                                        int orgQty = -1;

                                        for (int l = 0; l < cprintItemId.size(); l++) {
                                            if (
                                                    (foodOrderIds.get(currentFoodRefNoPointer).toString().compareTo(cprintFoodref.get(l).toString()) == 0) &&
                                                            (cprintDept.get(l).toString().compareTo(obj.getString("dept")) == 0) &&
                                                            (cprintSeat.get(l).toString().compareTo(obj.getString("seatId")) == 0) &&
                                                            (cprintEngDesc.get(l).toString().compareTo(obj.getString("engDesc")) == 0)
                                                    ) {
                                                found = true;
                                                updateIndex = l;
                                                orgQty = Integer.parseInt(cprintQuantity.get(l).toString());
                                                break;
                                            }
                                        }

                                        if (obj.getString("itemId") == null || "null".compareTo(obj.getString("itemId")) == 0 || "".compareTo(obj.getString("itemId")) == 0) {
                                            if (!foundDept) {
                                                cd--;
                                            }
                                        } else {
                                            if (found) {
                                                int newQty = Integer.parseInt(obj.getString("quantity")) + orgQty;
                                                cprintQuantity.set(updateIndex, String.valueOf(newQty));

                                            } else {
                                                cprintFoodref.add(foodOrderIds.get(currentFoodRefNoPointer).toString());
                                                cprintItemId.add(obj.getString("itemId"));
                                                cprintCategory.add(obj.getString("category"));
                                                cprintQuantity.add(obj.getString("quantity"));
                                                cprintEngDesc.add(obj.getString("engDesc"));
                                                cprintChiDesc.add(obj.getString("cnDesc"));
                                                //printDept.add(obj.getString("dept"));
                                                String tmpDept = "";
                                                if ("0".compareTo(obj.getString("dept")) == 0) {
                                                    cprintDept.add("1");
                                                    tmpDept = "1";
                                                } else {
                                                    cprintDept.add(obj.getString("dept"));
                                                    tmpDept = obj.getString("dept");
                                                }

                                                cprintSeat.add(obj.getString("seatId"));

                                                cprintRemark.add(obj.getString("remarks"));

                                            }
                                        }

                                    }
                                }

                            }

                            // Check to call the API again
                            if ((currentFoodRefNoPointer + 1) < foodOrderIds.size()) {
                                JSONObject jsonvalue = new JSONObject();
                                try {

                                    if (foodOrderIds.size() > 0) {
                                        currentFoodRefNoPointer++;
                                        jsonvalue.put("refNo", foodOrderIds.get(currentFoodRefNoPointer).toString());

                                    }
                                } catch (Exception ejson) {
                                    ejson.printStackTrace();
                                }
                                NetworkRepository.getInstance().listOrder(jsonvalue.toString(), this);
                                canDismiss = false;
                            }
                        }

                    } catch (Exception ejson) {
                        ejson.printStackTrace();
                    }
                }


                if (loading != null) {
                    if (canDismiss) {
                        loading.dismiss();
                    }
                }

                break;
            case FB_ORDER_ACTION:
                if (TextUtils.isEmpty(result)) {
                    CharSequence text = "Error occur, please contact administrator";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                } else if (result.startsWith("ERROR")) {

                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");


                    Toast toast = Toast.makeText(getApplicationContext(), displayErr, Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    // Check result
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "Save Successful", duration);
                    toast.show();

                    finish();

                }
                //Intent intent2 = new Intent();
                // intent2.setClass(EntranceStep2Activity.this, PrintActivity.class);

                //startActivity(intent2);
                //startActivityForResult(intent2, 12334);
                if (loading != null) {
                    loading.dismiss();
                }

                break;
            case FB_ORDERACTION_4_UAT:
                Toast.makeText(
                        getApplicationContext(),
                        "Save Successful",
                        Toast.LENGTH_SHORT).show();

                finish();
                if (loading != null) {
                    loading.dismiss();
                }

                break;
            case AUTH:
                if (TextUtils.isEmpty(result)) {
                    Context context = getApplicationContext();
                    CharSequence text = "Error occur, please contact administrator";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } else if (result.startsWith("ERROR")) {

                    Context context = getApplicationContext();

                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, "Manger Login / Password Incorrect", duration);
                    toast.show();

                    showLoginDialog();
                } else {
                    JSONObject jsonObjVer;
                    // Check is manager grade login
                    try {
                        jsonObjVer = new JSONObject(result);


                        if (jsonObjVer.getInt("isManager") == 1) {

                            // Check result
                            Intent intent = new Intent();
                            intent.setClass(EntranceStep2Activity.this, EntraceStep3Activity.class);
                            intent.putExtra("json", jsonSource);
                            intent.putExtra("encryptRefNo", encryptRefNo);
                            intent.putExtra("refType", refType);
                            // startActivityForResult(intent, ;
                            startActivityForResult(intent, REQ_CODE);
                        } else {
                            // Show Message
                            Context context = getApplicationContext();

                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, "Manger Login / Password Incorrect", duration);
                            toast.show();

                            showLoginDialog();
                        }
                    } catch (Exception ec) {
                        // Show Message
                        Context context = getApplicationContext();

                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, "Manger Login / Password Incorrect", duration);
                        toast.show();

                        showLoginDialog();


                    }


                }

                if (loading != null) {
                    loading.dismiss();
                }

                break;
            case ORDER_ACTION:
            case CONFIRM_TICKET:
                if (getIntent().getBooleanExtra("showScanner", false)) {
                    scanStatus.setVisibility(View.VISIBLE);
                    if (loading!=null) {
                        loading.dismiss();
                    }
                    if (TextUtils.isEmpty(result)
                            || result.startsWith("ERROR")) {
                        scanStatus.setText("Admission: Fail");
                    } else {
                        scanStatus.setText("Seat status updated");
                    }
                } else {
                    if (TextUtils.isEmpty(result)) {
                        Context context = getApplicationContext();
                        CharSequence text = "Login Fail, please try again";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        if (loading != null) {
                            loading.dismiss();
                        }

                    } else if (result.startsWith("ERROR")) {

                        Context context = getApplicationContext();

                        int duration = Toast.LENGTH_SHORT;

                        String displayErr = result;
                        displayErr = displayErr.replace("ERROR (400) : ", "");
                        displayErr = displayErr.replace("ERROR (401) : ", "");
                        displayErr = displayErr.replace("ERROR (402) : ", "");


                        Toast toast = Toast.makeText(context, displayErr, duration);
                        toast.show();

                        if (loading != null) {
                            loading.dismiss();
                        }
                    } else {

                        Log.d("Return", result);
                        try {


                            SharedPreferences sp = getSharedPreferences("E-TICKET", MODE_PRIVATE);

                            String orderType = "";

                            String foodOrderIdForSamba = "";
                            String foodOrderStatus = "";

                            if (orderTypes.size() > 0) {
                                orderType = orderTypes.get(currentFoodRefNoActionPointer).toString();
                                foodOrderStatus = foodOrderStatuses.get(currentFoodRefNoActionPointer).toString();
                                foodOrderIdForSamba = foodOrderIds2.get(currentFoodRefNoActionPointer).toString();

                                if (isLocalDebug) {
                                    foodOrderStatus = "P";
                                }

                            }

                            if (isFirstTimeUpdate && containsFood && ("V".compareTo(orderType) == 0) && ("P".compareTo(foodOrderStatus) == 0)) {

                                if (loading != null) {
                                    loading.show();
                                }

                                // Print the Item
                                // Form the Content for Each Department

                                // Loop by different food ref no

                                if (printDept != null && printDept.size() > 0) {


                                    ArrayList arrD = new ArrayList();
                                    ArrayList arrItem = new ArrayList();
                                    ArrayList arrRemarks = new ArrayList();
                                    ArrayList arrDepartName = new ArrayList();
                                    ArrayList arrSeats = new ArrayList();
                                    ArrayList arrQtys = new ArrayList();

                                    int ii = 0;

                                    for (int z = 0; z < printDept.size(); z++) {

                                        if (printFoodref.get(z).toString().compareTo(foodOrderIds.get(currentFoodRefNoActionPointer).toString()) != 0) {
                                            continue;
                                        }


                                        String printerip = "";
                                        String departmentName = "";
                                        boolean found = false;

                                        for (int x = 0; x < printerADept.size(); x++) {
                                            String tmpPrinterName = printerADept.get(x).toString(); //  sp.getString("printer_id_" + x, "");

                                            if (tmpPrinterName.compareTo(printDept.get(z).toString()) == 0) {
                                                printerip = printerAIp.get(x).toString(); // sp.getString("printer_ip_" + x, "");
                                                departmentName = printerADesc.get(x).toString(); // sp.getString("printer_name_" + x, "");
                                                break;
                                            }
                                        }

                                        String t1 = printDept.get(z).toString();

                                        String remarks = "";
                                        String itemdesc = "";

                                        itemdesc = printEngDesc.get(z).toString(); // + " X " + printQuantity.get(z).toString() + "\n";
                                        remarks = printRemark.get(z).toString() + "\n";


                                        if (!found) {

                                            ii = arrD.size();
                                            arrD.add(t1);
                                            arrItem.add(itemdesc);
                                            arrRemarks.add(remarks);
                                            arrDepartName.add(departmentName);
                                            arrSeats.add(printSeat.get(z).toString());
                                            arrQtys.add(printQuantity.get(z));
                                        }

                                    }

                                    // Found Distinct Department
                                    ArrayList arrTrimPrintDept = new ArrayList();
                                    for (int z = 0; z < printDept.size(); z++) {

                                        if (printFoodref.get(z).toString().compareTo(foodOrderIds.get(currentFoodRefNoActionPointer).toString()) != 0) {
                                            continue;
                                        }


                                        boolean foundDept = false;

                                        for (int zz = 0; zz < arrTrimPrintDept.size(); zz++) {
                                            if (arrTrimPrintDept.get(zz).toString().compareTo(printDept.get(z).toString()) == 0) {
                                                foundDept = true;
                                                break;
                                            }
                                        }

                                        if (!foundDept) {
                                            arrTrimPrintDept.add(printDept.get(z).toString());
                                        }

                                    }

                                    //for (int z = 0; z < printDept.size() ; z++) {
                                    for (int z = 0; z < arrTrimPrintDept.size(); z++) {

                                        String printerip = "";
                                        String departmentName = "";
                                        boolean found = false;


                                        for (int x = 0; x < printerADept.size(); x++) {
                                            String tmpPrinterName = printerADept.get(x).toString(); //  sp.getString("printer_id_" + x, "");

                                            if (tmpPrinterName.compareTo(arrTrimPrintDept.get(z).toString()) == 0) {
                                                printerip = printerAIp.get(x).toString(); // sp.getString("printer_ip_" + x, "");
                                                departmentName = printerADesc.get(x).toString(); // sp.getString("printer_name_" + x, "");
                                                break;
                                            }
                                        }

                                    /*
                                    for (int x = 1; x <= 5; x++) {
                                        String tmpPrinterName = sp.getString("printer_id_" + x, "");

                                        if (tmpPrinterName.compareTo(arrTrimPrintDept.get(z).toString()) == 0) {
                                            printerip = sp.getString("printer_ip_" + x, "");
                                            departmentName = sp.getString("printer_name_" + x, "");
                                            break;
                                        }
                                    }
                                    */

                                        // Print the document
                                        if ("".compareTo(printerip) != 0) {
                                            // Get the department name
                                            // String departmentName = MainActivity.getConfigValue(EntranceStep2Activity.this, "department_name_" + arrD.get(z).toString());
                                            try {


                                                //SharedPreferences sp = getSharedPreferences("E-TICKET", MODE_PRIVATE);
                                                String chkUsePrinter = sp.getString("use_printer", "");
                                                if ("Y".compareTo(chkUsePrinter) == 0) {
                                                    boolean chkPrint = runPrintDepartmentReceipt(foodOrderIds.get(currentFoodRefNoActionPointer).toString(), foodOrderIds2.get(currentFoodRefNoActionPointer).toString(), printerip, departmentName, arrItem, arrQtys, arrRemarks, arrDepartName, arrSeats);
                                                }

                                            /*
                                            //testPrint(EntranceStep2Activity.this, encryptRefNo, printerip, departmentName, arrItem, arrRemarks);
                                           // Intent intent = new Intent(EntranceStep2Activity.this, PrinterService.class);
                                            //startService(intent);
                                            if (!chkPrint) {

                                                couldPrintLoopExit = true;
                                                couldStartReprint = false;
                                                boolean showReprintDialog = true;
                                                do {

                                                    if (showReprintDialog) {

                                                        if (loading != null)
                                                            loading.dismiss();

                                                        mHandler.post(new Runnable() {
                                                            public void run() {
                                                                new AlertDialog.Builder(EntranceStep2Activity.this)
                                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                                        .setTitle("Closing Activity")
                                                                        .setMessage("Are you sure you want to close this activity?")
                                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                couldStartReprint = true;
                                                                            }

                                                                        })
                                                                        .setNegativeButton("No", null)
                                                                        .show();
                                                            }

                                                        }
                                                        );

                                                        showReprintDialog = false;

                                                    }

                                                    Thread.sleep(1000);

                                                    if (couldStartReprint) {
                                                        loading.show();
                                                        chkPrint = runPrintDepartmentReceipt(foodOrderIds.get(currentFoodRefNoActionPointer).toString(), foodOrderIds2.get(currentFoodRefNoActionPointer).toString(), printerip, departmentName, arrItem, arrQtys, arrRemarks, arrDepartName, arrSeats);

                                                        if (chkPrint) {
                                                            couldPrintLoopExit = false;
                                                        } else {

                                                            showReprintDialog = true;
                                                            couldPrintLoopExit = true;
                                                            couldStartReprint = false;
                                                        }

                                                    }

                                                } while (couldPrintLoopExit);

                                            }
*/


                                            } catch (Exception ep) {
                                                Context context = getApplicationContext();
                                                CharSequence text = "Error: (" + ep.getMessage() + ")";
                                                int duration = Toast.LENGTH_SHORT;

                                                Toast toast = Toast.makeText(context, text, duration);
                                                toast.show();

                                            }
                                        }
                                    }

                                    // Finially, print the consolidate receipt
                                    String containsComboFood = containsComboFoods.get(currentFoodRefNoActionPointer).toString();
                                    if ("Y".compareTo(containsComboFood) == 0) {


                                        arrDepartName.clear();
                                        arrItem.clear();
                                        arrRemarks.clear();
                                        arrSeats.clear();
                                        arrQtys.clear();
                                        String departmentName = ""; /*MainActivity.getConfigValue(EntranceStep2Activity.this, "department_name_5"); */

                                        String consolidatePrinter = "";

                                        consolidatePrinter = consolidateIp;
                                        departmentName = consolidateDesc;


                                        for (int z = 0; z < cprintDept.size(); z++) {

                                            if (cprintFoodref.get(z).toString().compareTo(foodOrderIds.get(currentFoodRefNoActionPointer).toString()) != 0) {
                                                continue;
                                            }

                                            // Check if the same seat first

                                            boolean sameSeat = false;
                                            int index = 0;


                                            for (int l1 = 0; l1 < arrSeats.size(); l1++) {
                                                if (arrSeats.get(l1).toString().compareTo(cprintSeat.get(z).toString()) == 0) {
                                                    sameSeat = true;
                                                    index = l1;
                                                    break;
                                                }
                                            }


                                            String itemdesc = ""; //arrItem.get(ii).toString();
                                            String remarks = ""; //arrRemarks.get(ii).toString();


                                            itemdesc += cprintEngDesc.get(z).toString() + " X " + cprintQuantity.get(z).toString() + "\n";

                                            if ("".compareTo(cprintRemark.get(z).toString()) != 0)
                                                remarks += cprintRemark.get(z).toString() + "\n";

                                            if (sameSeat) {
                                                // Next, need to check the food item
                                                String tmpQtyToChange = arrItem.get(index).toString();

                                                String tmpArr[] = tmpQtyToChange.split("\n");

                                                int tmpFoodQty = 0;
                                                if (tmpArr.length > 0) {
                                                    tmpQtyToChange = "";


                                                    tmpFoodQty = 0;
                                                    String reformat = "";
                                                    //String reformatRemark = "";

                                                    boolean needInsertAdd = true;

                                                    for (int t1 = 0; t1 < tmpArr.length; t1++) {
                                                        String tmpArr2[] = tmpArr[t1].split(" ");
                                                        if (tmpArr2.length > 3) {
                                                            String vqty = tmpArr2[tmpArr2.length - 1];
                                                            String vitem = "";
                                                            for (int zz = 0; zz < tmpArr2.length - 2; zz++) {
                                                                vitem += tmpArr2[zz].toString();

                                                                if (zz != (tmpArr2.length) - 3) {
                                                                    vitem += " ";
                                                                }
                                                            }

                                                            tmpArr2 = new String[3];
                                                            tmpArr2[0] = vitem;
                                                            tmpArr2[1] = "X";
                                                            tmpArr2[2] = vqty;
                                                        }

                                                        reformat = tmpArr[t1];


                                                        if (cprintEngDesc.get(z).toString().trim().compareTo(tmpArr2[0].trim()) == 0) {


                                                            int orgQty = 0;

                                                            if (tmpArr2.length > 1) {
                                                                // pattern is "A6 x 2"
                                                                // reformat =
                                                                orgQty = Integer.parseInt(tmpArr2[2]);
                                                            } else {
                                                                // patter is "A6"
                                                                orgQty = 1;
                                                            }

                                                            orgQty += tmpFoodQty;

                                                            reformat = cprintEngDesc.get(z).toString() + " X " + String.valueOf(orgQty);

                                                            needInsertAdd = false;

                                                        }

                                                        tmpQtyToChange += reformat;

                                                        if (reformat.length() >= 0) {
                                                            tmpQtyToChange += "\n";
                                                        }
                                                    }

                                                    if (needInsertAdd) {
                                                        tmpQtyToChange += itemdesc;

                                                    }


                                                    //String orgRemark = arrRemarks.get(index).toString();

                                                    arrItem.set(index, tmpQtyToChange);

                                                    String tmpRemarkToChange = "";
                                                    if ("".compareTo(arrRemarks.get(index).toString()) != 0) {
                                                        tmpRemarkToChange = arrRemarks.get(index).toString() + "\n" + remarks;
                                                    } else {
                                                        tmpRemarkToChange = remarks;
                                                    }

                                                    arrRemarks.set(index, tmpRemarkToChange);
                                                }

                                            } else {
                                                index = arrSeats.size();

                                                arrSeats.add(cprintSeat.get(z).toString());
                                                arrRemarks.add(remarks);
                                                arrDepartName.add(departmentName);
                                                arrItem.add(itemdesc);
                                                arrQtys.add(cprintQuantity.get(z).toString());
                                            }


                                            //arrItem.add(itemdesc);
                                            //arrRemarks.add(remarks);

                                            //arrDepartName.add(departmentName);

                                        }


                                        if (!TextUtils.isEmpty(consolidatePrinter)) {
                                            try {
                                                // runPrintDepartmentReceipt(encryptRefNo, consolidatePrinter, departmentName, arrItem, arrRemarks);
                                                String chkUsePrinter = sp.getString("use_printer", "");
                                                if ("Y".equals(chkUsePrinter)) {
                                                    if (!runPrintDepartmentReceipt(
                                                            foodOrderIds.get(currentFoodRefNoActionPointer).toString(),
                                                            foodOrderIds2.get(currentFoodRefNoActionPointer).toString(),
                                                            consolidatePrinter,
                                                            departmentName,
                                                            arrItem,
                                                            arrQtys,
                                                            arrRemarks,
                                                            arrDepartName,
                                                            arrSeats)) {
                                                        Toast.makeText(getApplicationContext(),
                                                                "runPrintDepartmentRecipt with ConsolidationPrinter fail [" + consolidateDebugMsg + "]",
                                                                Toast.LENGTH_SHORT)
                                                                .show();

                                                    }
                                                }
                                                //testPrint(EntranceStep2Activity.this, encryptRefNo, consolidatePrinter, departmentName, arrItem, arrRemarks);
                                            } catch (Exception ep) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Error: (" + ep.getMessage() + ")",
                                                        Toast.LENGTH_SHORT)
                                                        .show();

                                            }
                                        }
                                    }
                                }
                                try {
                                    // Original is foodRefNo
                                    String chkUseShare = sp.getString("use_share", "");
                                    if ("Y".equals(chkUseShare)) {
                                        String sambaRet = mssqlLogUpdate(
                                                foodOrderIds.get(currentFoodRefNoActionPointer).toString(),
                                                foodOrderIdForSamba);

                                        if (!TextUtils.isEmpty(sambaRet)) {
                                            Toast.makeText(getApplicationContext(), sambaRet, Toast.LENGTH_LONG)
                                                    .show();
                                        }

                                    }
                                } catch (Exception eSamba) {
                                    eSamba.printStackTrace();
                                    Toast.makeText(
                                            getApplicationContext(),
                                            eSamba.getMessage(),
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                                // Call the FB API to update the result
                                JSONObject jsonvalue = new JSONObject();

                                try {
                                    jsonvalue.put("refNo", foodOrderIds.get(currentFoodRefNoActionPointer).toString());  /* Original is foodRefNo */
                                    jsonvalue.put("type", "S");
                                    //jsonvalue.put("refNo", movieTransId);
                                } catch (Exception ejson) {
                                    ejson.printStackTrace();
                                }
                                currentFoodRefNoActionPointer++;

                                if (currentFoodRefNoActionPointer >= foodOrderIds.size()) {
                                    // Last Times
                                    NetworkRepository.getInstance().orderActionFb(jsonvalue.toString(), this);
                                } else {

                                    // Still inside the loop
                                    NetworkRepository.getInstance().orderAction(jsonvalue.toString(), this);
                                }


                            } else {


                                // Check result
                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, "Save Successful", duration);
                                toast.show();

                                if (loading != null) {
                                    loading.dismiss();
                                }

                                finish();
                            }

                        } catch (Exception ec) {
                            // Show Message
                            Context context = getApplicationContext();
                            CharSequence text = "Error: (" + ec.getMessage() + ")";
                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();

                            if (loading != null) {
                                loading.dismiss();
                            }

                            return;
                        }


                    }
                }
                break;
            case ORDERACTION_4_UAT:
            case CONFIRM_TICKET_4UAT:


                Log.d("Return", result);
                //JSONObject jsonObj;
                //
                try {


                    // Finally , added the print action
                    // if need to print, we need to print and call the update API


                    SharedPreferences sp = getSharedPreferences("E-TICKET", MODE_PRIVATE);

                    String orderType = "";
                    String foodOrderIdForSamba = "";
                    String foodOrderStatus = "";

                    if (orderTypes.size() > 0) {
                        orderType = orderTypes.get(currentFoodRefNoActionPointer).toString();
                        foodOrderIdForSamba = foodOrderIds2.get(currentFoodRefNoActionPointer).toString();
                        foodOrderStatus = foodOrderStatuses.get(currentFoodRefNoActionPointer).toString();
                    }

                    if (isFirstTimeUpdate && containsFood && ("V".compareTo(orderType) == 0) && ("P".compareTo(foodOrderStatus) == 0)) {

                        if (loading != null) {
                            loading.show();
                        }

                        // Print the Item
                        // Form the Content for Each Department

                        // Loop by different food ref no

                        if (printDept != null && printDept.size() > 0) {


                            ArrayList arrD = new ArrayList();
                            ArrayList arrItem = new ArrayList();
                            ArrayList arrRemarks = new ArrayList();
                            ArrayList arrDepartName = new ArrayList();
                            ArrayList arrSeats = new ArrayList();
                            ArrayList arrQtys = new ArrayList();

                            for (int z = 0; z < printDept.size(); z++) {

                                if (printFoodref.get(z).toString().compareTo(foodOrderIds.get(currentFoodRefNoActionPointer).toString()) != 0) {
                                    continue;
                                }

                                String departmentName = "";
                                boolean found = false;

                                for (int x = 1; x <= 5; x++) {
                                    String tmpPrinterName = sp.getString("printer_id_" + x, "");

                                    if (tmpPrinterName.compareTo(printDept.get(z).toString()) == 0) {
                                        departmentName = sp.getString("printer_name_" + x, "");
                                        break;
                                    }
                                }

                                String t1 = printDept.get(z).toString();


                                String remarks = "";
                                String itemdesc = "";

                                itemdesc = printEngDesc.get(z).toString(); // + " X " + printQuantity.get(z).toString() + "\n";
                                remarks = printRemark.get(z).toString() + "\n";


                                if (!found) {
                                    arrD.add(t1);
                                    arrItem.add(itemdesc);
                                    arrRemarks.add(remarks);
                                    arrDepartName.add(departmentName);
                                    arrSeats.add(printSeat.get(z).toString());
                                    arrQtys.add(printQuantity.get(z));
                                }

                            }

                            // Found Distinct Department
                            ArrayList arrTrimPrintDept = new ArrayList();
                            for (int z = 0; z < printDept.size(); z++) {

                                if (printFoodref.get(z).toString().compareTo(foodOrderIds.get(currentFoodRefNoActionPointer).toString()) != 0) {
                                    continue;
                                }


                                boolean foundDept = false;

                                for (int zz = 0; zz < arrTrimPrintDept.size(); zz++) {
                                    if (arrTrimPrintDept.get(zz).toString().compareTo(printDept.get(z).toString()) == 0) {
                                        foundDept = true;
                                        break;
                                    }
                                }

                                if (!foundDept) {
                                    arrTrimPrintDept.add(printDept.get(z).toString());
                                }

                            }

                            //for (int z = 0; z < printDept.size() ; z++) {
                            for (int z = 0; z < arrTrimPrintDept.size(); z++) {

                                String printerip = "";
                                String departmentName = "";

                                for (int x = 1; x <= 5; x++) {
                                    String tmpPrinterName = sp.getString("printer_id_" + x, "");

                                    if (tmpPrinterName.compareTo(arrTrimPrintDept.get(z).toString()) == 0) {
                                        printerip = sp.getString("printer_ip_" + x, "");
                                        departmentName = sp.getString("printer_name_" + x, "");
                                        break;
                                    }
                                }

                                // Print the document
                                if (!TextUtils.isEmpty(printerip)) {
                                    // Get the department name
                                    // String departmentName = MainActivity.getConfigValue(EntranceStep2Activity.this, "department_name_" + arrD.get(z).toString());
                                    try {

                                        String chkUsePrinter = sp.getString("use_printer", "");
                                        if ("Y".equals(chkUsePrinter)) {
                                            runPrintDepartmentReceipt(foodOrderIds.get(currentFoodRefNoActionPointer).toString(), foodOrderIds2.get(currentFoodRefNoActionPointer).toString(), printerip, departmentName, arrItem, arrQtys, arrRemarks, arrDepartName, arrSeats);
                                        }

                                    } catch (Exception ep) {
                                        Context context = getApplicationContext();
                                        CharSequence text = "Error: (" + ep.getMessage() + ")";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();

                                    }
                                }
                            }

                            // Finially, print the consolidate receipt
                            String containsComboFood = containsComboFoods.get(currentFoodRefNoActionPointer).toString();
                            if ("Y".equals(containsComboFood)) {


                                arrDepartName.clear();
                                arrItem.clear();
                                arrRemarks.clear();
                                arrSeats.clear();
                                arrQtys.clear();
                                String departmentName = ""; /*MainActivity.getConfigValue(EntranceStep2Activity.this, "department_name_5"); */

                                String consolidatePrinter = "";

                                consolidatePrinter = consolidateIp;
                                departmentName = consolidateDesc;


                                for (int z = 0; z < cprintDept.size(); z++) {

                                    if (cprintFoodref.get(z).toString().compareTo(foodOrderIds.get(currentFoodRefNoActionPointer).toString()) != 0) {
                                        continue;
                                    }

                                    // Check if the same seat first

                                    boolean sameSeat = false;
                                    int index = 0;


                                    for (int l1 = 0; l1 < arrSeats.size(); l1++) {
                                        if (arrSeats.get(l1).toString().compareTo(cprintSeat.get(z).toString()) == 0) {
                                            sameSeat = true;
                                            index = l1;
                                            break;
                                        }
                                    }


                                    String itemdesc = ""; //arrItem.get(ii).toString();
                                    String remarks = ""; //arrRemarks.get(ii).toString();


                                    itemdesc += cprintEngDesc.get(z).toString() + " X " + cprintQuantity.get(z).toString() + "\n";

                                    if ("".compareTo(cprintRemark.get(z).toString()) != 0)
                                        remarks += cprintRemark.get(z).toString() + "\n";

                                    if (sameSeat) {
                                        // Next, need to check the food item
                                        String tmpQtyToChange = arrItem.get(index).toString();

                                        String tmpArr[] = tmpQtyToChange.split("\n");

                                        int tmpFoodQty = 0;
                                        if (tmpArr.length > 0) {
                                            tmpQtyToChange = "";


                                            tmpFoodQty = 0;
                                            String reformat = "";
                                            //String reformatRemark = "";

                                            boolean needInsertAdd = true;

                                            for (int t1 = 0; t1 < tmpArr.length; t1++) {
                                                String tmpArr2[] = tmpArr[t1].split(" ");
                                                if (tmpArr2.length > 3) {
                                                    String vqty = tmpArr2[tmpArr2.length - 1];
                                                    String vitem = "";
                                                    for (int zz = 0; zz < tmpArr2.length - 2; zz++) {
                                                        vitem += tmpArr2[zz].toString();

                                                        if (zz != (tmpArr2.length) - 3) {
                                                            vitem += " ";
                                                        }
                                                    }

                                                    tmpArr2 = new String[3];
                                                    tmpArr2[0] = vitem;
                                                    tmpArr2[1] = "X";
                                                    tmpArr2[2] = vqty;
                                                }

                                                reformat = tmpArr[t1];


                                                if (cprintEngDesc.get(z).toString().compareTo(tmpArr2[0]) == 0) {


                                                    int orgQty = 0;

                                                    if (tmpArr2.length > 1) {
                                                        // pattern is "A6 x 2"
                                                        // reformat =
                                                        orgQty = Integer.parseInt(tmpArr2[2]);
                                                    } else {
                                                        // patter is "A6"
                                                        orgQty = 1;
                                                    }

                                                    orgQty += tmpFoodQty;

                                                    reformat = cprintEngDesc.get(z).toString() + " X " + String.valueOf(orgQty);

                                                    needInsertAdd = false;

                                                }


                                                tmpQtyToChange += reformat;

                                                if (reformat.length() >= 0) {
                                                    tmpQtyToChange += "\n";
                                                }
                                            }

                                            if (needInsertAdd) {
                                                tmpQtyToChange += itemdesc;

                                            }


                                            //String orgRemark = arrRemarks.get(index).toString();

                                            arrItem.set(index, tmpQtyToChange);

                                            String tmpRemarkToChange = "";
                                            if ("".compareTo(arrRemarks.get(index).toString()) != 0) {
                                                tmpRemarkToChange = arrRemarks.get(index).toString() + "\n" + remarks;
                                            } else {
                                                tmpRemarkToChange = remarks;
                                            }

                                            arrRemarks.set(index, tmpRemarkToChange);
                                        }

                                    } else {

                                        arrSeats.add(cprintSeat.get(z).toString());
                                        arrRemarks.add(remarks);
                                        arrDepartName.add(departmentName);
                                        arrItem.add(itemdesc);
                                        arrQtys.add(cprintQuantity.get(z).toString());
                                    }

                                }


                                if (!TextUtils.isEmpty(consolidatePrinter)) {

                                    try {
                                        if ("Y".compareTo(PreferencesController.getInstance().getUsePrinter()) == 0) {
                                            // runPrintDepartmentReceipt(encryptRefNo, consolidatePrinter, departmentName, arrItem, arrRemarks);
                                            runPrintDepartmentReceipt(foodOrderIds.get(currentFoodRefNoActionPointer).toString(), foodOrderIds2.get(currentFoodRefNoActionPointer).toString(), consolidatePrinter, departmentName, arrItem, arrQtys, arrRemarks, arrDepartName, arrSeats);
                                            //testPrint(EntranceStep2Activity.this, encryptRefNo, consolidatePrinter, departmentName, arrItem, arrRemarks);
                                        }
                                    } catch (Exception ep) {
                                        CharSequence text = "Error: (" + ep.getMessage() + ")";
                                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }
                        }

                        try {
                            if ("Y".equals(PreferencesController.getInstance().getUseShare())) {
                                // Original is foodRefNo
                                //createFile(smbUser, smbPassword, smbUrl, foodOrderIdForSamba /*foodOrderIds.get(currentFoodRefNoActionPointer).toString() */ + "\r\n");

                                String sambaRet = mssqlLogUpdate(foodOrderIds.get(currentFoodRefNoActionPointer).toString(), foodOrderIdForSamba);

                                if (!TextUtils.isEmpty(sambaRet)) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            sambaRet,
                                            Toast.LENGTH_LONG)
                                            .show();
                                }

                            }
                        } catch (Exception eSamba) {
                            eSamba.printStackTrace();
                            Toast.makeText(
                                    getApplicationContext(),
                                    eSamba.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                        JSONObject jsonvalue = new JSONObject();

                        try {
                            jsonvalue.put("refNo", foodOrderIds.get(currentFoodRefNoActionPointer).toString());  /* Original is foodRefNo */
                            jsonvalue.put("type", "S");
                            //jsonvalue.put("refNo", movieTransId);
                        } catch (Exception ejson) {
                            ejson.printStackTrace();
                        }

                        currentFoodRefNoActionPointer++;

                        if (currentFoodRefNoActionPointer >= foodOrderIds.size()) {
                            // Last Times
                            NetworkRepository.getInstance().orderActionFb4UAT(jsonvalue.toString(), true, this);

                            //new myAsyncTask(EntranceStep2Activity.this, TASK_FB_ORDERACTION_4_UAT, true).execute(urlString, jsonvalue.toString(), token);
                        } else {

                            // Still inside the loop
                            NetworkRepository.getInstance().orderAction4UAT(jsonvalue.toString(), true, this);
                            //new myAsyncTask(EntranceStep2Activity.this, TASK_COMPLETED_4_UAT, true).execute(urlString, jsonvalue.toString(), token);
                        }


                    } else {
                        // Check result
                        Toast.makeText(getApplicationContext(),
                                "Save Successful",
                                Toast.LENGTH_SHORT)
                                .show();

                        if (loading != null) {
                            loading.dismiss();
                        }

                        finish();
                    }

                } catch (Exception ec) {
                    // Show Message
                    CharSequence text = "Error: (" + ec.getMessage() + ")";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

                    if (loading != null) {
                        loading.dismiss();
                    }

                    return;
                }


                break;

            /**
             * Callback to handle validation
             * use on Boardway
             */
            case GATE_VALIDATE_TICKET:

                if(loading != null) {
                    loading.dismiss();
                }

                if(TextUtils.isEmpty(result) || result.startsWith("ERROR")) {


                    Toast.makeText(
                            getApplicationContext(),
                            "Error! Please try another ticket",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Save Successful",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

                break;


            default:
                // Show some error code
        }
    }


    private void setOnClick(final GridView gv, final int index) {
        gv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View imgView, int position, long id) {

                ArrayList tmpList = (ArrayList) EntranceStep2Activity.this.ticketList.get(index);
                ArrayList tmpIdList = (ArrayList) EntranceStep2Activity.this.ticketIdList.get(index);
                ArrayList tmpState = (ArrayList) EntranceStep2Activity.this.ticketState.get(index);

                int status = (int) tmpList.get(position);
                if (status == 0) {
                    return;
                }

                int state = (int) tmpState.get(position);

                ImageView image = (ImageView) imgView;

                String seatID = (String) tmpIdList.get(position);

                if (state == 0) {

                    image.setImageResource(R.mipmap.available);
                    tmpState.set(position, 1);

                    // set isChecked based on seatID
                    setSeatChecked(true, seatID);

                } else {
                    image.setImageResource(R.mipmap.free);
                    tmpState.set(position, 0);

                    // set isChecked based on seatID
                    setSeatChecked(false, seatID);
                }

                EntranceStep2Activity.this.ticketState.set(index, tmpState);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            finish();
        }
    }


    private void showLoginDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.login_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(prompt);
        final EditText user = (EditText) prompt.findViewById(R.id.login_name);
        final EditText pass = (EditText) prompt.findViewById(R.id.login_password);
        //user.setText(Login_USER); //login_USER and PASS are loaded from previous session (optional)
        //pass.setText(Login_PASS);
        alertDialogBuilder.setTitle("Manager Login Confirmation");
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String password = pass.getText().toString();
                        String username = user.getText().toString();
                        try {
                            if (username.length() < 2 || password.length() < 2) {
                                Toast.makeText(EntranceStep2Activity.this,
                                        "Invalid username or password",
                                        Toast.LENGTH_LONG)
                                        .show();
                                showLoginDialog();
                            } else {
                                //TODO here any local checks if password or user is valid

                                // Call the API
                                if (loading == null) {
                                    loading = new ProgressDialog(EntranceStep2Activity.this);

                                    loading.setCancelable(true);
                                    loading.setMessage("Loading");
                                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                }
                                JSONObject jsonvalue = new JSONObject();


                                jsonvalue.put("username", username);
                                jsonvalue.put("password", password);

                                loading.show();


                                NetworkRepository.getInstance().auth(jsonvalue.toString(), EntranceStep2Activity.this);

                                //this will do the actual check with my back-end server for valid user/pass and callback with the response
                                //new CheckLoginAsync(MainActivity.this,username,password).execute("","");
                            }
                        } catch (Exception e) {
                            Toast.makeText(EntranceStep2Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

            }
        });

        alertDialogBuilder.show();

    }


    private void showPasswordDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.password_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(prompt);

        final EditText pass = (EditText) prompt.findViewById(R.id.editTextDialogUserInput);
        //user.setText(Login_USER); //login_USER and PASS are loaded from previous session (optional)
        //pass.setText(Login_PASS);
        alertDialogBuilder.setTitle("Password Confirmation");
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Check if username and password correct


                        String password = pass.getText().toString();

                        try {
                            if (password.length() < 2) {
                                Toast.makeText(EntranceStep2Activity.this, "Invalid  password", Toast.LENGTH_LONG).show();
                                showPasswordDialog();
                            } else {

                                String user_pwd = PreferencesController.getInstance().getUserPassword();

                                if (password.compareTo(user_pwd) == 0) {
                                    //dialog.dismiss();

                                    editSeatStatus();
                                } else {
                                    Toast.makeText(EntranceStep2Activity.this,
                                            "Password Incorrect, please try again",
                                            Toast.LENGTH_LONG).show();
                                    // pass.setText("");
                                    // pass.requestFocus();

                                    showPasswordDialog();
                                }


                                //TODO here any local checks if password or user is valid


                                //this will do the actual check with my back-end server for valid user/pass and callback with the response
                                //new CheckLoginAsync(MainActivity.this,username,password).execute("","");
                            }
                        } catch (Exception e) {
                            Toast.makeText(EntranceStep2Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

            }
        });

        alertDialogBuilder.show();


    }

    private void editSeatStatus() {
        Intent intent = new Intent();
        intent.setClass(EntranceStep2Activity.this, EntraceStep3Activity.class);
        intent.putExtra("json", jsonSource);
        intent.putExtra("encryptRefNo", encryptRefNo);
        intent.putExtra("refType", refType);
        // startActivityForResult(intent, ;
        startActivityForResult(intent, REQ_CODE);
    }

    protected void onDestroy() {
        try {
            for (Printer printer : mPrinters) {
                PrinterUtils.disconnectPrinter(this, printer);
            }
            mPrinters.clear();
        } catch (Exception ee) {
        }
        super.onDestroy();
    }

    @Override
    protected IBinder getToken() {
        return tl.getWindowToken();
    }

    @Override
    protected void goNext(String json, String encryptRefNo, String refType, String foodRefNo) {
        getIntent().putExtra("json", json);
        getIntent().putExtra("encryptRefNo", encryptRefNo);
        getIntent().putExtra("refType", refType);
        getIntent().putExtra("foodRefNo", foodRefNo);
        init();
        boolean isValid = Utils.qrIsValid(EntranceStep2Activity.this, json);
        if (isValid) {
            playSuccess();
            accept(null);
        } else {
            playBuzzer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Sorry, the application need the Camera access right",
                            Toast.LENGTH_LONG);
                    toast.show();

                    finish();
                }
                return;
            }
        }
    }

    private void showAcceptDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(EntranceStep2Activity.this);

        if(scan_status == ScanStatus.REDEEMED) {
            builder.setTitle("Notice");
            builder.setMessage("Could not proceed. All seats are redeemed.");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
            return;
        }

        builder.setTitle("Confirm");
        builder.setMessage("Confirm to admit the transaction?");

        final String strType = getIntent().getExtras().getString("scanType");
        ScanType scanType = ScanType.valueOf(strType);


        if (outsideGracePeriod && scanType == ScanType.IN) {
            if ("Y".compareTo(Utils.getConfigValue(EntranceStep2Activity.this, "allow_invalid_save")) != 0) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, "Cannot save record as it's not within the grace period", duration);
                toast.show();
                return;
            }
        }

        if (loading == null) {
            loading = new ProgressDialog(this);

            loading.setCancelable(true);
            loading.setMessage("Loading");
            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                accept(dialog);
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    private void accept(@Nullable DialogInterface dialogInterface) {

        final String strScanType = getIntent().getExtras().getString("scanType");
        ScanType intentScanType = ScanType.valueOf(strScanType);

        if (outsideGracePeriod && intentScanType == ScanType.IN) {
            if ("Y".compareTo(Utils.getConfigValue(EntranceStep2Activity.this, "allow_invalid_save")) != 0) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, "Cannot save record as it's not within the grace period", duration);
                toast.show();
                return;
            }
        }


        if(checkedItems.isEmpty()) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, "Please select the seat!", duration);
            toast.show();
            return;
        }

        //debugMsg1();

        // Before show the loading, initialzie the printer first
        /**
         * Disable printing feature
         */
        /*
        if (isLocalDebug && isLocalSkipPrint) {


        } else {
            if (isFirstTimeUpdate && containsFood) {
                boolean couldConnectPrinter = true;


                for (Printer printer : mPrinters) {
                    PrinterUtils.disconnectPrinter(EntranceStep2Activity.this, printer);
                }
                mPrinters.clear();

                if (mPrinterIPList != null && mPrinterIPList.size() > 0) {
                    mPrinterIPList.clear();
                }


                // And initialize the printer
                if (printerAIp != null && printerAIp.size() > 0) {
                    for (int i = 0; i < printerAIp.size(); i++) {

                        String printerIPAddress = printerAIp.get(i).toString();

                        // Check the IP address also connect before
                        boolean couldAddIP = true;
                        for (int zz = 0; zz < mPrinterIPList.size(); zz++) {
                            if (printerIPAddress.compareTo(mPrinterIPList.get(zz).toString()) == 0) {
                                couldAddIP = false;
                                break;
                            }

                        }

                        if (couldAddIP) {

                            Printer printer = PrinterUtils.initializeObject(
                                    EntranceStep2Activity.this,
                                    printerName,
                                    printerLang);

                            if (printer != null) {
                                if (!PrinterUtils.connectPrinter(printer, printerIPAddress)) {
                                    couldConnectPrinter = false;
                                } else {
                                    mPrinterIPList.add(printerIPAddress);
                                }
                            } else {
                                couldConnectPrinter = false;
                            }
                            mPrinters.add(i, printer);
                        }
                    }

                    // Add the consolidate printer
                    if (!TextUtils.isEmpty(consolidateIp)) {

                        // Check the IP address also connect before
                        boolean couldAddIP = true;
                        for (int zz = 0; zz < mPrinterIPList.size(); zz++) {
                            if (consolidateIp.equals(mPrinterIPList.get(zz))) {
                                couldAddIP = false;
                                break;
                            }

                        }

                        int i;
                        String printerIPAddress = consolidateIp;
                        if (couldAddIP) {

                            i = mPrinterIPList.size();
                            Printer printer = PrinterUtils.initializeObject(
                                    EntranceStep2Activity.this,
                                    printerName,
                                    printerLang);

                            if (printer != null) {
                                if (!PrinterUtils.connectPrinter(printer, printerIPAddress)) {
                                    couldConnectPrinter = false;
                                } else {
                                    mPrinterIPList.add(printerIPAddress);
                                }
                            } else {
                                couldConnectPrinter = false;
                            }
                            mPrinters.add(i, printer);
                        }

                    }

                    if (!couldConnectPrinter) {

                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }

                        int duration = Toast.LENGTH_SHORT;
                        Toast toastReturn = Toast.makeText(EntranceStep2Activity.this, "Cannot connect to printer, please try again", duration);
                        toastReturn.show();

                        return;
                    }
                }
            }

        }
        */
        /**
         * End of Disable printing feature
         */


        // Do nothing but close the dialog
        loading.show();


        if (accessMode.equals("offline")) {

            ArrayList items = new ArrayList();

            final String strType = getIntent().getExtras().getString("scanType");
            ScanType scanType = ScanType.valueOf(strType);

            for(SeatInfo seat : ticketTrans.getSeatInfoList()) {
                Log.d(EntranceStep2Activity.class.toString(), seat.getTicketType() + " " + seat.getSeatId() + ticketTrans.getTrans_id());
            }


            /*
            for (int x = 0; x < ticketTypeList.size(); x++) {
                ArrayList tmpList = (ArrayList) ticketList.get(x);
                ArrayList tmpIdList = (ArrayList) ticketIdList.get(x);
                ArrayList tmpState = (ArrayList) ticketState.get(x);

                if (tmpList != null) {
                    for (int y = 0; y < tmpList.size(); y++) {
                        if (((int) tmpList.get(y)) == 2) {
                            if (((int) tmpState.get(y)) == 1) {
                                //valid.add(tmpIdList.get(y).toString());
                                Item item = new Item();
                                item.setSeatStatus(scanType == ScanType.IN || scanType == ScanType.NONE ? "1" : "0"); // Invalid
                                item.setTicketType(ticketTypeList.get(x).toString());
                                item.setSeatId(tmpIdList.get(y).toString());
                                item.setRefNo(encryptRefNo);

                                items.add(item);
                            } else {
                                // Valid but not selected
                                Item item = new Item();
                                item.setSeatStatus(scanType == ScanType.IN || scanType == ScanType.NONE ? "1" : "0"); // valid
                                item.setTicketType(ticketTypeList.get(x).toString());
                                item.setSeatId(tmpIdList.get(y).toString());
                                item.setRefNo(encryptRefNo);

                                items.add(item);
                            }
                        } else if (((int) tmpList.get(y)) == 0) {
                            // Original it's invalid
                            Item item = new Item();
                            item.setSeatStatus(scanType == ScanType.IN || scanType == ScanType.NONE ? "1" : "0"); // Invalid
                            item.setTicketType(ticketTypeList.get(x).toString());
                            item.setSeatId(tmpIdList.get(y).toString());
                            item.setRefNo(encryptRefNo);

                            items.add(item);
                        }
                    }
                }

            }
             */

            for(SeatInfo seat : checkedItems) {
                Item item = new Item();
                item.setSeatStatus(scanType == ScanType.IN ? "1" : "0");
                item.setTicketType(seat.getTicketType());
                item.setSeatId(seat.getSeatId());
                item.setRefNo(encryptRefNo);
                items.add(item);
            }

            // Selected, need to insert / update to DB as Invalid
            OfflineDatabase offline = new OfflineDatabase(EntranceStep2Activity.this);
            try {
                offline.accept(items);
                addEntranceLog(scanType == ScanType.IN || scanType == ScanType.NONE ? "in" : "out");
                Toast.makeText(getApplicationContext(), "Save Successful", Toast.LENGTH_SHORT).show();
                finish();

            } catch (Exception esql) {
                String reason = esql.getMessage();
                Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            } finally {
                loading.dismiss();
            }

        } else {

            /**
             * Original Code
             */
            /*
            JSONObject jsonvalue = new JSONObject();

            // Get the String from Text Control
            int countSelected = 0;
            try {

                jsonvalue.put("refNo", encryptRefNo);
                jsonvalue.put("type", "");
                jsonvalue.put("method", refType);

                JSONArray jsonArr = new JSONArray();

                for (int x = 0; x < ticketTypeList.size(); x++) {
                    ArrayList tmpList = (ArrayList) ticketList.get(x);
                    ArrayList tmpIdList = (ArrayList) ticketIdList.get(x);
                    ArrayList tmpState = (ArrayList) ticketState.get(x);

                    if (tmpList != null) {
                        for (int y = 0; y < tmpList.size(); y++) {
                            if (((int) tmpList.get(y)) == 2) {
                                if (((int) tmpState.get(y)) == 1) {
                                    countSelected++;

                                    JSONObject seat = new JSONObject();
                                    seat.put("seatId", tmpIdList.get(y).toString());

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
                                    String currentDateandTime = sdf.format(new Date());

                                    seat.put("timestamp", currentDateandTime);


                                    seat.put("action", "C");

                                    jsonArr.put(seat);

                                }
                            }
                        }

                    }
                }

                jsonvalue.put("confirmSeat", jsonArr);

            } catch (JSONException jsonEx) {
                Log.e("Accept", jsonEx.getMessage());

            }

            if (countSelected == 0) {

                CharSequence text = "You have not selected any seat!";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                if (loading != null)
                    loading.dismiss();

                return;
            }

            currentFoodRefNoActionPointer = 0;

            String needFBUpdate = Utils.getConfigValue(EntranceStep2Activity.this, "need_fb_api_updated");
            if ("Y".equals(needFBUpdate)) {
                NetworkRepository.getInstance().confirmTicket(jsonvalue.toString(), EntranceStep2Activity.this);
                //new myAsyncTask(EntranceStep2Activity.this, TASK_COMPLETED).execute(urlString, jsonvalue.toString(), token);
            } else {
                NetworkRepository.getInstance().confirmTicket4UAT(jsonvalue.toString(), EntranceStep2Activity.this);
                //new myAsyncTask(EntranceStep2Activity.this, TASK_COMPLETED_4_UAT).execute(urlString, jsonvalue.toString(), token);
            }
             */

            /**
             * End of Original Code
             */

            if(checkedItems.size() > 0) {
                List<String> seat_no = new ArrayList<String>();

                JSONArray seat_arr = new JSONArray();
                for(SeatInfo seat : checkedItems) {
//                    if("Valid".equals(seat.getSeatStatus())) {
//                        seat_arr.put(seat.getSeatId());
//                    }
                    seat_arr.put(seat.getSeatId());
                }


                final String strType = getIntent().getExtras().getString("scanType");
                ScanType scanType = ScanType.valueOf(strType);

                if(ticketTrans != null) {
                    try {
                        JSONObject jsonVal = new JSONObject();

                        String savedCinemaID = PreferencesController.getInstance().getCinemaId();
                        jsonVal.put("cinema_id", savedCinemaID);

                        jsonVal.put("trans_id", ticketTrans.getTrans_id());
                        jsonVal.put("is_concession", ticketTrans.isConcession() ? 1 : 0);

                        if(scanType == ScanType.IN || scanType == ScanType.NONE) {
                            jsonVal.put("type", "in");
                        } else if(scanType == ScanType.OUT) {
                            jsonVal.put("type", "out");
                        }

                        if(seat_arr.length() > 0) {
                            jsonVal.put("seat_no", seat_arr);
                        }

                        Log.d(EntranceStep2Activity.class.toString(), jsonVal.toString());

                        NetworkRepository.getInstance().getGateValidateTicket(jsonVal.toString(), this);

                    } catch (Exception e) {

                        if(loading != null) {
                            loading.dismiss();
                        }

                        Toast.makeText(getApplicationContext(),
                                e.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        return;
                    }
                }

            }


            if (dialogInterface != null) {
                dialogInterface.dismiss();
            }

        }

    }

    /**
     * Set the seat is checked
     * @param isChecked
     * @param position
     */
    private void setSeatChecked(boolean isChecked, int position) {
        if(ticketTrans == null) return;
        SeatInfo item = ticketTrans.getSeatInfoList()[position];

        Log.d(EntranceStep2Activity.class.toString(), item.getSeatId() + " "
                + item.getTicketType() + " "
                + item.isChecked());

        item.setChecked(isChecked);

        checkedItems.clear();
        for(SeatInfo seat : ticketTrans.getSeatInfoList()) {
            if(seat.isChecked()) {
                checkedItems.add(seat);
            }
        }
    }

    /**
     * Set the seat is checked based on seat id
     * @param isChecked
     * @param seatID
     */
    private void setSeatChecked(boolean isChecked, final String seatID){
        if(ticketTrans == null) return;

        List<SeatInfo> seats = Arrays.asList(ticketTrans.getSeatInfoList());

        Predicate<SeatInfo> matchSeatID = new Predicate<SeatInfo>() {
            @Override
            public boolean apply(SeatInfo seatInfo) {
                return seatInfo.getSeatId().equals(seatID);
            }
        };

        Collection<SeatInfo> result = Utils.filter(seats, matchSeatID);

        if(result.size() == 0) return;

        for(SeatInfo seat : result) {
            seat.setChecked(isChecked);
        }

        checkedItems.clear();
        for(SeatInfo seat : ticketTrans.getSeatInfoList()) {
            if(seat.isChecked()) {
                checkedItems.add(seat);
            }
        }
    }


    /**
     * Add EntranceLog to local db
     */
    private void addEntranceLog(String logType) {
        if(ticketTrans == null || checkedItems.size() == 0) return;

        Entrance entrance = new Entrance(EntranceStep2Activity.this);

        Log.d(EntranceStep2Activity.class.toString(), logType);

        try {
            entrance.add(encryptRefNo, checkedItems, logType);
        } catch (Exception e) {
            String reason = e.getMessage();
            Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
        } finally {
            loading.dismiss();
        }
    }


    /**
     * Create text view for Date
     * @param strDate
     * @return
     */
    private TextView createDateTextView(String strDate) {
        TextView dateTv = new TextView(this);
        dateTv.append(strDate);
        dateTv.setTextSize(14);
        dateTv.setEms(10);
        dateTv.setTextColor(Color.parseColor("#aaaaaa"));

        return dateTv;
    }

    /**
     * Create text view for Seats
     * @param strDate
     * @param seatNo
     * @return
     */
    private TextView createSeatTextView(String strDate, List<String> seatNo) {
        TextView seatsTv = new TextView(this);
        seatsTv.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        seatsTv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        seatsTv.setTextSize(14);
        seatsTv.setEms(10);
        seatsTv.setTextColor(Color.parseColor("#aaaaaa"));
        seatsTv.setPadding(0, 0, 10, 0);

        String seat = "";
        for(String s : seatNo) {
            seat += s + " ";
        }

        seat = seat.trim();
        seat = seat.replace(" ", ",");

        seatsTv.append(seat);

        return seatsTv;
    }


    private TextView createSeatTextView(String strDate, List<String> seatNo, List<String> compareWith) {
        TextView seatsTv = new TextView(this);
        seatsTv.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        seatsTv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        seatsTv.setTextSize(14);
        seatsTv.setEms(10);
        seatsTv.setTextColor(Color.parseColor("#aaaaaa"));
        seatsTv.setPadding(0, 0, 10, 0);

        String seat = "";

        StringBuilder sb = new StringBuilder();
        for(String s : seatNo) {
            if(compareWith.indexOf(s) != -1) {
                sb.append(s);
                sb.append(",");
            }
        }

        seat = sb.toString();

        if(!seat.equals("")){
            seat = seat.substring(0, seat.length() - 1);
        }

        seatsTv.append(seat);

        return seatsTv;
    }


    /**
     * Original Code
     * The following contains the functions that are not using in Boardway
     */

    private boolean createDepartmentData(String token, String foodRefId, String department, List<String> items, List<String> qtys, List<String> remarks, List<String> departmentNames, List<String> seats) {
        String method = "";
        //Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 3;
        final int barcodeHeight = 7;

        /*
        if (mPrinter == null) {
            return false;
        }
        */

        if (department.toLowerCase().contains("consolidation")) {
            consolidateDebugMsg += "P1";
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            /*
            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
*/


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.TRUE, Printer.PARAM_DEFAULT);

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);

            method = "addText";
            //mPrinter.addText(token + "\n"); /* foodRefNo */
            mPrinter.addText(foodRefId + "\n"); /* foodRefNo */

            method = "addTextSize";
            mPrinter.addTextSize(1, 1);


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);


            method = "addText";
            mPrinter.addText("_______________________________________________");

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);


            method = "addFeedLine";
            mPrinter.addFeedLine(2);


            if (department.toLowerCase().indexOf("consolidation") >= 0) {
                String displaySeats = "";

                // By Consolidation Table
                int noOfComma = 0;
                if (items.size() > 0) {
                    for (int l = 0; l < items.size(); l++) {

                        int chkInside = seats.get(l).toLowerCase().indexOf(displaySeats.toLowerCase());

                        if ("".compareTo(displaySeats) == 0) {


                            chkInside = -1;
                        }

                        if (chkInside >= 0) {
                            // Found

                        } else {
                            if ("".compareTo(displaySeats) != 0) {

                                if (noOfComma % 4 != 0) {
                                    displaySeats += ",";
                                }

                            }
                            displaySeats += seats.get(l);
                            displaySeats += seats.get(l).toString();

                            noOfComma++;

                            if (noOfComma % 4 == 0) {
                                displaySeats += "\n";
                            }


                        }
                    }
                }

                /*
                textData.append("        VIP             |             " + displaySeats + "          ");
                method = "addText";
                mPrinter.addText(textData.toString());
                //System.out.print(textData.toString());
                textData.delete(0, textData.length());
                 */

                textData.append("        VIP             |                       \n");
                method = "addText";
                mPrinter.addText(textData.toString());
                //System.out.print(textData.toString());
                textData.delete(0, textData.length());


                method = "addTextStyle";
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.TRUE, Printer.PARAM_DEFAULT);

                method = "addTextAlign";
                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);


                method = "addText";
                textData.append("" + displaySeats + "");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());


                // Restore original Style
                method = "addTextStyle";
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);

                method = "addTextAlign";
                mPrinter.addTextAlign(Printer.ALIGN_LEFT);

                //mPrinter.addTextAlign(Printer.ALIGN_CENTER);


            } else {
                String displaySeats = "";
                int noOfComma = 0;
                // Department
                if (seats.size() > 0) {
                    for (int l = 0; l < seats.size(); l++) {
                        String tmpDepartName = departmentNames.get(l);
                        if (tmpDepartName.compareTo(department) == 0) {

                            int chkInside = seats.get(l).toLowerCase().indexOf(displaySeats.toLowerCase());

                            if ("".compareTo(displaySeats) == 0) {
                                chkInside = -1;
                            }

                            if (chkInside >= 0) {
                                // Found

                            } else {
                                if ("".compareTo(displaySeats) != 0) {
                                    if (noOfComma % 4 != 0) {
                                        displaySeats += ",";
                                    }
                                }
                                displaySeats += seats.get(l).toString();

                                noOfComma++;

                                if (noOfComma % 4 == 0) {
                                    displaySeats += "\n";
                                }
                            }


                        }
                    }


                }

                //textData.append(displaySeats + "\n");
                /*
                textData.append("        VIP             |             " + displaySeats + "          ");
                method = "addText";
                mPrinter.addText(textData.toString());
                //System.out.print("        VIP             |             " + displaySeats + "          ");
                textData.delete(0, textData.length());
                */


                textData.append("        VIP             |                       \n");
                method = "addText";
                mPrinter.addText(textData.toString());
                //System.out.print(textData.toString());
                textData.delete(0, textData.length());


                method = "addTextStyle";
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.TRUE, Printer.PARAM_DEFAULT);

                method = "addTextAlign";
                mPrinter.addTextAlign(Printer.ALIGN_RIGHT);


                method = "addText";
                textData.append("" + displaySeats + "");
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());


                // Restore original Style
                method = "addTextStyle";
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


                method = "addTextAlign";
                mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            }


            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            if (department.toLowerCase().contains("consolidation")) {
                consolidateDebugMsg += "P2";
            }

            // method = "addText";
            // mPrinter.addText(textData.toString());
            // textData.delete(0, textData.length());


            //if ("Consolidate".compareTo(department) == 0) {
            if (department.toLowerCase().contains("consolidation")) {

                method = "addText";
                mPrinter.addText("_______________________________________________\n\n");

                method = "addSymbol";
                mPrinter.addSymbol(token, Printer.SYMBOL_PDF417_STANDARD, Printer.LEVEL_0, barcodeWidth, barcodeHeight, 0);

                /* foodRefNo */
/*
            method = "addText";
            mPrinter.addText("\n\n");

            method="addHLine";
            mPrinter.addHLine(0, 100, Printer.LINE_THIN);
*/

                method = "addTextAlign";
                mPrinter.addTextAlign(Printer.ALIGN_LEFT);


            }

            method = "addText";
            mPrinter.addText("_______________________________________________\n");


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("Department\n");


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            // Restore the Text Style
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);

            textData.append(department + "\n\n\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            if (department.toLowerCase().indexOf("consolidation") >= 0) {
                if (items.size() > 0) {
                    for (int l = 0; l < items.size(); l++) {

                        method = "addTextStyle";
                        mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);

                        method = "addTextAlign";
                        mPrinter.addTextAlign(Printer.ALIGN_LEFT);

                        textData.append(seats.get(l).toString() + "\n");

                        method = "addText";
                        mPrinter.addText(textData.toString());
                        //System.out.print(textData.toString());
                        textData.delete(0, textData.length());

                        method = "addTextStyle";
                        mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);

                        method = "addTextAlign";
                        mPrinter.addTextAlign(Printer.ALIGN_LEFT);

                        textData.append(items.get(l).toString() + "\n");
                        method = "addText";
                        mPrinter.addText(textData.toString());
                        //System.out.print(textData.toString());
                        textData.delete(0, textData.length());


                        if ("".compareTo(remarks.get(l).toString()) != 0) {

                            String tmpCheck = remarks.get(l).toString();

                            if (tmpCheck.trim().compareTo("") != 0) {
                                textData.append(remarks.get(l).toString() + "\n\n");

                                method = "addText";
                                mPrinter.addText(textData.toString());
                                //System.out.print(textData.toString());
                                textData.delete(0, textData.length());
                            }
                        }
                    }
                }
            } else {

                // Item
                method = "addTextStyle";
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);

                textData.append("Item Detail\n");
                method = "addText";
                mPrinter.addText(textData.toString());
                //System.out.print(textData.toString());
                textData.delete(0, textData.length());

                method = "addTextStyle";
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


                if (items.size() > 0) {
                    ArrayList items1 = new ArrayList();
                    ArrayList remarks1 = new ArrayList();
                    ArrayList qty1 = new ArrayList();

                    for (int l = 0; l < items.size(); l++) {
                        String tmpDepartName = departmentNames.get(l).toString();
                        if (tmpDepartName.compareTo(department) == 0) {
                            // Same Department, Check same item or not
                            boolean foundItems = false;
                            for (int y = 0; y < items1.size(); y++) {
                                if (items1.get(y).toString().compareTo(items.get(l).toString()) == 0) {
                                    foundItems = true;

                                    // Add the quantity
                                    int orgQty = Integer.parseInt(qty1.get(y).toString());
                                    orgQty += Integer.parseInt(qtys.get(l).toString());
                                    qty1.set(y, String.valueOf(orgQty));

                                    String tmpRemark = "";
                                    if ("".compareTo(remarks.get(l).toString().trim()) == 0) {
                                        tmpRemark = remarks1.get(y).toString();
                                    } else {
                                        tmpRemark = remarks1.get(y).toString() + "\n" + remarks.get(l).toString();
                                    }

                                    remarks1.set(y, tmpRemark);
                                    break;
                                }
                            }

                            if (!foundItems) {

                                items1.add(items.get(l).toString());
                                qty1.add(qtys.get(l).toString());
                                remarks1.add(remarks.get(l).toString().trim());

                            }


                        }
                    }

                    for (int k = 0; k < items1.size(); k++) {
                        String value = items1.get(k) + " X " + qty1.get(k).toString();
                        textData.append(value + "\n");
                        method = "addText";
                        mPrinter.addText(textData.toString());
                        //System.out.print(textData.toString());
                        textData.delete(0, textData.length());

                        if ("".compareTo(remarks1.get(k).toString()) != 0) {
                            textData.append(remarks1.get(k) + "\n\n");
                            method = "addText";
                            mPrinter.addText(textData.toString());
                            //System.out.print(textData.toString());
                            textData.delete(0, textData.length());
                        }
                    }
                }
            }

            if (department.toLowerCase().indexOf("consolidation") >= 0) {
                consolidateDebugMsg += "P3";
            }

            textData.append("\n\n\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            // Print Date
            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);


            textData.append("Print Date\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


            Date currentDt = new Date();
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
            String displayFileDateTime = displayFormat.format(currentDt);
            textData.append(displayFileDateTime + "\n\n");
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            // F&B Order ID
            method = "addTextStyle";

            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.TRUE, Printer.TRUE, Printer.PARAM_DEFAULT);
            mPrinter.addTextAlign(Printer.ALIGN_RIGHT);

            textData.append("F&B Order Id\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            method = "addTextStyle";
            mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.FALSE, Printer.FALSE, Printer.PARAM_DEFAULT);


            textData.append(token + "\n\n");


            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());


            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);

            if (department.toLowerCase().indexOf("consolidation") >= 0) {
                consolidateDebugMsg += "P4";
            }
        } catch (Exception e) {
            // ShowMsg.showException(e, method, mContext);
            String tmp = e.getMessage();
            return false;
        }

        textData = null;


        return true;
    }


    private boolean createReceiptData() {
        String method = "";
        //Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            /*
            method = "addImage";
            mPrinter.addImage(logoData, 0, 0,
                    logoData.getWidth(),
                    logoData.getHeight(),
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT,
                    Printer.COMPRESS_AUTO);
*/
            mPrinter.addText(movieTransId);
            mPrinter.addHLine(0, 100, 0);


            method = "addBarcode";
            mPrinter.addBarcode("01209457",

                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);


            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            textData.append("THE STORE 123 (555) 555 – 5555\n");
            textData.append("STORE DIRECTOR – John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("400 OHEIDA 3PK SPRINGF  9.99 R\n");
            textData.append("410 3 CUP BLK TEAPOT    9.99 R\n");
            textData.append("445 EMERIL GRIDDLE/PAN 17.99 R\n");
            textData.append("438 CANDYMAKER ASSORT   4.99 R\n");
            textData.append("474 TRIPOD              8.99 R\n");
            textData.append("433 BLK LOGO PRNTED ZO  7.99 R\n");
            textData.append("458 AQUA MICROTERRY SC  6.99 R\n");
            textData.append("493 30L BLK FF DRESS   16.99 R\n");
            textData.append("407 LEVITATING DESKTOP  7.99 R\n");
            textData.append("441 **Blue Overprint P  2.99 R\n");
            textData.append("476 REPOSE 4PCPM CHOC   5.49 R\n");
            textData.append("461 WESTGATE BLACK 25  59.99 R\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            mPrinter.addText("TOTAL    174.81\n");
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            textData.append("Purchased item total number\n");
            textData.append("Sign Up and Save !\n");
            textData.append("With Preferred Saving Card\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addBarcode";
            mPrinter.addBarcode("01209457",
                    Printer.BARCODE_CODE39,
                    Printer.HRI_BELOW,
                    Printer.FONT_A,
                    barcodeWidth,
                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        } catch (Exception e) {
            // ShowMsg.showException(e, method, mContext);
            return false;
        }

        textData = null;

        return true;
    }

    private static NtlmPasswordAuthentication createAuth(String aUser, String aPasswd) {
        StringBuffer sb = new StringBuffer(aUser);
        sb.append(':').append(aPasswd);

        return new NtlmPasswordAuthentication(sb.toString());
    }

    public static SmbFile createSmbFile(String aUser, String aPasswd, String aTarget) throws IOException {
        NtlmPasswordAuthentication auth = createAuth(aUser, aPasswd);
        return new SmbFile(aTarget, auth);
    }

    private static Timestamp getCurrentTimeStamp() {
        Date today = new Date();
        return new Timestamp(today.getTime());
    }

    private String mssqlLogUpdate(String aContent0, String aContent) {

        String server = PreferencesController.getInstance().getNetwork();
        String mssqlUserName = PreferencesController.getInstance().getNetworkUser();//sp.getString("network_user", "");  // alex
        String mssqlPassword = PreferencesController.getInstance().getNetworkPassword();//sp.getString("network_password", ""); // csl100323


        String debug = "";

        Connection con = null;

        String ret = "";


        try {


            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            //Class.forName("com.mysql.jdbc.Driver");
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            con = DriverManager.getConnection("jdbc:jtds:sqlserver://" + server + ":1433/db_tvdisplay;", mssqlUserName, mssqlPassword);
//;instance=SQLEXPRESS

            if (con != null && !con.isClosed()) {
                //con = DriverManager.getConnection("jdbc:sqlserver://" + server + ":1433; databaseName=db_tvdisplay; ", mssqlUserName, mssqlPassword);
                // instance=SQLEXPRESS
                //con = DriverManager.getConnection( "jdbc:mysql://mysql.station188.com:3306/conquerstars_association", mysqlUserName, mysqlPassword );

                //Statement stmt = con.createStatement();//创建Statement
                PreparedStatement statement = con.prepareStatement("INSERT INTO tb_tvdisplay (tvdrefno, tvdorder, tvdtype, tvddt, tvdstatus) VALUES (?,?,?,getdate(), ?)");
                statement.setString(1, aContent0);
                statement.setString(2, aContent);
                statement.setString(3, "V");
                //statement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                //statement.setTimestamp(4, getCurrentTimeStamp());
                statement.setString(4, "S");

                statement.execute();
                con.close();

            /*Context context = getApplicationContext();
            CharSequence text = "Connection OK"; //"Login Fail, please try again";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            */
            } else {

                ret = "ERROR: connection is closed";
                //ret = "jdbc:jtds:sqlserver://" + server + ":1433/db_tvdisplay; (" + mssqlUserName + ")(" + mssqlPassword + ")";

            }
        } catch (ClassNotFoundException ce) {
            System.out.println("Class Not Found!!");
            ret = "Error - CNF: (" + debug + "):  Class Not Found!!";
        } catch (SQLException se) {
            //System.out.println(se.getMessage() );
            ret = "Error - SE: (" + debug + "): " + se.getMessage();
        } catch (Exception connEx) {

            /*
            Context context = getApplicationContext();
            CharSequence text = connEx.getMessage(); //"Login Fail, please try again";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            */
            ret = "Error - EX: (" + debug + "): " + connEx.getMessage();
        }

        return ret;
    }


    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();


        mPrinter.setReceiveEventListener(null);

        try {
            mPrinter.disconnect();
        } catch (Exception ex) {
            // Do nothing
        }

        mPrinter = null;
    }


    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else if (status.getOnline() == Printer.FALSE) {
            return false;
        } else {
            ;//print available
        }

        return true;
    }

    private String consolidateDebugMsg = "";

    private boolean runPrintDepartmentReceipt(String token, String foodRefId, String ipaddress, String department, List<String> items, List<String> qtys, List<String> remarks, List<String> departNames, List<String> seats) {

        if (isLocalDebug && isLocalSkipPrint) {
            return true;
        }

        if (isLocalDebug) {
            ipaddress = "192.168.1.36";
        }

        // For which printer we need to use
        int index = -1;
        for (int ll = 0; ll < mPrinterIPList.size(); ll++) {
            if (ipaddress.compareTo(mPrinterIPList.get(ll)) == 0) {
                index = ll + 1;
                break;
            }
        }

        if (index == -1) {
            return false;
        }

        if (department.toLowerCase().contains("consolidation")) {
            consolidateDebugMsg += String.valueOf(index);
        }


        if (index - 1 > mPrinters.size() - 1 && index - 1 < 0) {
            return false;
        }
        Printer printer = mPrinters.get(index - 1);

        if (printer == null) {
            return false;
        } else {
            mPrinter = printer;
        }

        System.out.println("runPrintReceiptSequence - 2");

        if (department.toLowerCase().contains("consolidation")) {
            consolidateDebugMsg += "[PC]";
        }

        if (!createDepartmentData(token, foodRefId, department, items, qtys, remarks, departNames, seats)) {
            //finalizeObject();

            PrinterUtils.finalizeObject(printer);
            return false;
        }

        if (department.toLowerCase().contains("consolidation")) {
            consolidateDebugMsg += "[PC3]";
        }

        System.out.println("runPrintReceiptSequence - 3");

        if (!printData(index, ipaddress)) {
            finalizeObject();


            return false;
        }
        return true;
    }


    private boolean printData(int index, String ipaddress) {
        /*
        if (mPrinter == null) {
            return false;
        }
        */

        if (index - 1 > mPrinters.size() - 1 && index - 1 < 0) {
            return false;
        }
        Printer printer = mPrinters.get(index - 1);
        if (printer == null) {
            return false;
        }
        if (!PrinterUtils.beginTran(printer)) {
            System.out.println("Return - " + index);
            return false;
        }


        //PrinterStatusInfo status = mPrinter.getStatus();
        PrinterStatusInfo status = null;
        status = printer.getStatus();

        if (status == null) {
            System.out.println("Status == null in PrintData");
            return false;
        }
        //dispPrinterWarnings(status);

        if (!isPrintable(status)) {
            //ShowMsg.showMsg(makeErrorMessage(status), mContext);
            try {
                //mPrinter.disconnect();
                printer.disconnect();

            } catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            printer.sendData(Printer.PARAM_DEFAULT);

        } catch (Epos2Exception e) {
            //ShowMsg.showException(e, "sendData", mContext);
            try {
                printer.disconnect();
            } catch (Exception ex) {
                // Do nothing
            }
            return false;
        }
        PrinterUtils.endTran(this, printer);

        return true;
    }



}
