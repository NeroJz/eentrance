package hk.com.uatech.eticket.eticket.preferences;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hk.com.uatech.eticket.eticket.Item;
import hk.com.uatech.eticket.eticket.ListAdapter;
import hk.com.uatech.eticket.eticket.OfflineDatabase;
import hk.com.uatech.eticket.eticket.R;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.pojo.GateHouse;
import hk.com.uatech.eticket.eticket.pojo.House;

public class SettingActivity extends AppCompatActivity implements NetworkRepository.QueryCallback {
    private Button btnUpload;
    private String previousRefNo = "";

    private ProgressDialog loading = null;

    List<String> list;
    List<String> listvalue;
    List<String> listSelected;
    ListView listview;
    List<Boolean> listShow;    // 這個用來記錄哪幾個 item 是被打勾的
    private String houseSetting;
    private String accessMode;
    private String setupMode = "";

    private List<String> refNos;
    private int offlinePointer = -1;

    private int successCount = 0;
    private int failCount = 0;


    private EditText edtGracePeriodFrom;
    private EditText edtGracePeriodTo;

    private EditText edtPrintName1;
    private EditText edtPrintName2;
    private EditText edtPrintName3;
    private EditText edtPrintName4;
    private EditText edtPrintName5;

    private EditText edtIPAddress;

    private EditText edtEntrance;

    private EditText edtFb;

    private EditText edtPrintIP1;
    private EditText edtPrintIP2;
    private EditText edtPrintIP3;
    private EditText edtPrintIP4;
    private EditText edtPrintIP5;

    private EditText edtPrintId1;
    private EditText edtPrintId2;
    private EditText edtPrintId3;
    private EditText edtPrintId4;
    private EditText edtPrintId5;

    private EditText edtNetwork;
    private EditText edtNetworkUser;
    private EditText edtNetworkPassword;
    private EditText edtOfflinePassword;
    private EditText edtUseShare;
    private EditText edtUsePrinter;


    private Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        edtGracePeriodFrom = (EditText) findViewById(R.id.edtGracePeriodFrom);
        edtGracePeriodTo = (EditText) findViewById(R.id.edtGracePeriodTo);

        edtPrintName1 = (EditText) findViewById(R.id.printerName1);
        edtPrintName2 = (EditText) findViewById(R.id.printerName2);
        edtPrintName3 = (EditText) findViewById(R.id.printerName3);
        edtPrintName4 = (EditText) findViewById(R.id.printerName4);
        edtPrintName5 = (EditText) findViewById(R.id.printerName5);

        edtIPAddress = (EditText) findViewById(R.id.ipaddress);

        edtEntrance = (EditText) findViewById(R.id.entrance);

        edtFb = (EditText) findViewById(R.id.fb);

        edtPrintIP1 = (EditText) findViewById(R.id.printerIP1);
        edtPrintIP2 = (EditText) findViewById(R.id.printerIP2);
        edtPrintIP3 = (EditText) findViewById(R.id.printerIP3);
        edtPrintIP4 = (EditText) findViewById(R.id.printerIP4);
        edtPrintIP5 = (EditText) findViewById(R.id.printerIP5);

        edtPrintId1 = (EditText) findViewById(R.id.deptId1);
        edtPrintId2 = (EditText) findViewById(R.id.deptId2);
        edtPrintId3 = (EditText) findViewById(R.id.deptId3);
        edtPrintId4 = (EditText) findViewById(R.id.deptId4);
        edtPrintId5 = (EditText) findViewById(R.id.deptId5);

        edtNetwork = (EditText) findViewById(R.id.network);

        edtNetworkUser = (EditText) findViewById(R.id.networkuser);

        edtNetworkPassword = (EditText) findViewById(R.id.networkpassword);

        edtOfflinePassword = (EditText) findViewById(R.id.offlinepassword);


        edtUseShare = (EditText) findViewById(R.id.useshare);

        edtUsePrinter = (EditText) findViewById(R.id.useprinter);


        btnUpload = (Button) findViewById(R.id.btnUpload);


        LinearLayout bglayer = (LinearLayout) findViewById(R.id.bglayer);
        TextView textView4 = (TextView) findViewById(R.id.textView4);

        CheckBox showScan = (CheckBox) findViewById(R.id.showScan);
        CheckBox verifySeatStatus = (CheckBox) findViewById(R.id.verifySeatStatus);
        CheckBox verifySettings = (CheckBox) findViewById(R.id.verifySettings);

        CheckBox checkUploadByRole = (CheckBox) findViewById(R.id.chbByRole);
        CheckBox checkUploadByPermission = (CheckBox) findViewById(R.id.chbByPermission);

        String gracePeriodFrom = PreferencesController.getInstance().getGracePeriodFrom();
        String gracePeriodTo = PreferencesController.getInstance().getGracePeriodTo();
        houseSetting = PreferencesController.getInstance().getHousing();

        if (getIntent().getExtras() != null) {
            setupMode = getIntent().getExtras().getString("setupMode", "");
        }

        ((TextView) findViewById(R.id.textView12)).setText("");

        String printName1 = PreferencesController.getInstance().getPrinterName1();
        String printName2 = PreferencesController.getInstance().getPrinterName2();
        String printName3 = PreferencesController.getInstance().getPrinterName3();
        String printName4 = PreferencesController.getInstance().getPrinterName4();
        String printName5 = PreferencesController.getInstance().getPrinterName5();

        String useshare = PreferencesController.getInstance().getUseShare();
        String useprinter = PreferencesController.getInstance().getUsePrinter();

        String printIP1 = PreferencesController.getInstance().getPrinterIp1();
        String printIP2 = PreferencesController.getInstance().getPrinterIp2();
        String printIP3 = PreferencesController.getInstance().getPrinterIp3();
        String printIP4 = PreferencesController.getInstance().getPrinterIp4();
        String printIP5 = PreferencesController.getInstance().getPrinterIp5();

        String printId1 = PreferencesController.getInstance().getPrinterId1();
        String printId2 = PreferencesController.getInstance().getPrinterId2();
        String printId3 = PreferencesController.getInstance().getPrinterId3();
        String printId4 = PreferencesController.getInstance().getPrinterId4();
        String printId5 = PreferencesController.getInstance().getPrinterId5();

        String network = PreferencesController.getInstance().getNetwork();
        String networkUser = PreferencesController.getInstance().getNetworkUser();
        String networkPassword = PreferencesController.getInstance().getNetworkPassword();

        String offlinepassword = PreferencesController.getInstance().getOfflinePassword();


        btnUpload = (Button) findViewById(R.id.btnUpload);
        accessMode = PreferencesController.getInstance().getAccessMode();

        String serverIP = PreferencesController.getInstance().getServerIpAddress();

        /**
         * Test
         */

        String dummyIP = new String("http://52.220.110.156");
        edtIPAddress.setText(dummyIP);

//        edtIPAddress.setText(serverIP);

        edtEntrance.setText(PreferencesController.getInstance().getEntrance());

        edtFb.setText(PreferencesController.getInstance().getFb());

        showScan.setChecked(PreferencesController.getInstance().isShowScan());
        showScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesController.getInstance().setIsShowScan(b);
            }
        });

        verifySeatStatus.setChecked(PreferencesController.getInstance().isUsePasswordForChangeSeat());
        verifySeatStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesController.getInstance().setUsePasswordChangeSeat(b);
            }
        });

        verifySettings.setChecked(PreferencesController.getInstance().isUsePasswordForSettings());
        verifySettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesController.getInstance().setUsePasswordSettings(b);
            }
        });

        checkUploadByPermission.setChecked(PreferencesController.getInstance().isCheckByPermission());
        checkUploadByPermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesController.getInstance().setCheckByPermission(b);
            }
        });

        checkUploadByRole.setChecked(PreferencesController.getInstance().isCheckByRole());
        checkUploadByRole.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesController.getInstance().setCheckByRole(b);
            }
        });


        if ("Y".equals(setupMode)) {
            btnUpload.setEnabled(false);
            bglayer.setBackgroundColor(Color.rgb(230, 255, 250));

            textView4.setText("Allowed House(s) - Not support in first set up Mode");
        } else if ("offline".equals(accessMode)) {
            btnUpload.setEnabled(false);
            bglayer.setBackgroundColor(Color.rgb(255, 179, 179));

            textView4.setText("Allowed House(s) - Not support in Offline Mode");
        } else {

            // need to check any record
            OfflineDatabase database = new OfflineDatabase(SettingActivity.this);
            int totalRec = database.getCount();

            if (totalRec > 0) {
                btnUpload.setEnabled(true);
                btnUpload.setText("Upload (" + String.valueOf(totalRec) + ")");
            } else {
                btnUpload.setText("Upload (0)");
                btnUpload.setEnabled(false);
            }

            textView4.setText("Allowed House(s)");

            try {
                // Get the String from Text Control

                //String urlString = "http://13.228.58.92/thirdparty/api/e-entrance/staffAuth";
                if (loading == null) {
                    loading = new ProgressDialog(this);

                    loading.setCancelable(true);
                    loading.setMessage("Loading");
                    loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }
                JSONObject jv = new JSONObject();

                jv.put("test", "test");

                loading.show();
                //JSONObject jsonObject = getJSONObjectFromURL(urlString, jsonvalue);
//                NetworkRepository.getInstance().getHouseList(jv.toString(), this);

                NetworkRepository.getInstance().getGateAllHouse(this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        listview = (ListView) findViewById(R.id.houselist);
        listview.setOnItemClickListener(new OnItemClickListener() {
                                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                                CheckedTextView chkItem = (CheckedTextView) v.findViewById(R.id.check1);
                                                chkItem.setChecked(!chkItem.isChecked());

                                                String selected = listSelected.get(position);
                                                listSelected.set(position, "Y".equals(selected) ? "N" : "Y");
                                                listShow.set(position, chkItem.isChecked());
                                            }
                                        }
        );


        btnUpload.setOnClickListener(new ImageView.OnClickListener() {

            @Override

            public void onClick(View v) {

                if ((PreferencesController.getInstance().isCheckByRole()
                        && "MANAGER".equals(PreferencesController.getInstance().getUserRank()))
                        || (PreferencesController.getInstance().isCheckByPermission()
                        && PreferencesController.getInstance().isUploadingEnabled())) {
                    previousRefNo = "";

                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);

                    builder.setTitle("Confirm");
                    builder.setMessage("Confirm to upload the offline transaction?");


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
                            successCount = 0;
                            failCount = 0;

                            // First, form the Array for the sqllite data

                            OfflineDatabase db = new OfflineDatabase(SettingActivity.this);
                            refNos = db.getDistinctRefNo();
                            JSONObject jsonvalue;

                            if (refNos != null) {

                                if (refNos.size() > 0) {
                                    jsonvalue = getJsonForConfirmation(offlinePointer + 1);
                                    if (jsonvalue != null) {
                                        NetworkRepository.getInstance().confirmTicket(jsonvalue.toString(), SettingActivity.this);
                                    } else {
                                        Toast.makeText(
                                                SettingActivity.this,
                                                "Upload Successful",
                                                Toast.LENGTH_SHORT)
                                                .show();

                                        // Finial, remove all records from db
                                        db.removeAllRecords();

                                        // Also, need to disable the button
                                        btnUpload.setEnabled(false);
                                    }
                                    dialog.dismiss();
                                }
                            }

                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(SettingActivity.this, "unauthorized", Toast.LENGTH_SHORT).show();
                }
            }

        });

        findViewById(R.id.btnSave).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                String graceFrom = edtGracePeriodFrom.getEditableText().toString();
                String graceTo = edtGracePeriodTo.getEditableText().toString();


                String printName1 = edtPrintName1.getEditableText().toString();
                String printName2 = edtPrintName2.getEditableText().toString();
                String printName3 = edtPrintName3.getEditableText().toString();
                String printName4 = edtPrintName4.getEditableText().toString();
                String printName5 = edtPrintName5.getEditableText().toString();

                String serverIP = edtIPAddress.getEditableText().toString();

                String entrance = edtEntrance.getEditableText().toString();

                String fb = edtFb.getEditableText().toString();

                String printIP1 = edtPrintIP1.getEditableText().toString();
                String printIP2 = edtPrintIP2.getEditableText().toString();
                String printIP3 = edtPrintIP3.getEditableText().toString();
                String printIP4 = edtPrintIP4.getEditableText().toString();
                String printIP5 = edtPrintIP5.getEditableText().toString();
                String printId1 = edtPrintId1.getEditableText().toString();
                String printId2 = edtPrintId2.getEditableText().toString();
                String printId3 = edtPrintId3.getEditableText().toString();
                String printId4 = edtPrintId4.getEditableText().toString();
                String printId5 = edtPrintId5.getEditableText().toString();

                String network = edtNetwork.getEditableText().toString();

                String networkuser = edtNetworkUser.getEditableText().toString();

                String networkpassword = edtNetworkPassword.getEditableText().toString();

                String offlinepassword = edtOfflinePassword.getEditableText().toString();


                String useshare = edtUseShare.getEditableText().toString();

                String useprinter = edtUsePrinter.getEditableText().toString();


                try {
                    Integer.parseInt(graceFrom);
                    Integer.parseInt(graceTo);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(),
                            "Grace Period should be integer only",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                String housing = "";
                String housingName = "";

                if (listSelected != null) {
                    for (int z = 0; z < listSelected.size(); z++) {
                        String isSelected = listSelected.get(z);

                        if ("Y".equals(isSelected)) {
                            String id = listvalue.get(z);


                            if (!"".equals(housing)) {
                                housing += ",";
                                housingName += ",";
                            }
                            housing += id;
                            housingName += list.get(z);
                        }
                    }
                }

                PreferencesController.getInstance().setGracePeriodFrom(graceFrom);
                PreferencesController.getInstance().setGracePeriodTo(graceTo);

                PreferencesController.getInstance().setPrinterName1(printName1);
                PreferencesController.getInstance().setPrinterName2(printName2);
                PreferencesController.getInstance().setPrinterName3(printName3);
                PreferencesController.getInstance().setPrinterName4(printName4);
                PreferencesController.getInstance().setPrinterName5(printName5);

                PreferencesController.getInstance().setPrinterIp1(printIP1);
                PreferencesController.getInstance().setPrinterIp2(printIP2);
                PreferencesController.getInstance().setPrinterIp3(printIP3);
                PreferencesController.getInstance().setPrinterIp4(printIP4);
                PreferencesController.getInstance().setPrinterIp5(printIP5);

                PreferencesController.getInstance().setPrinterId1(printId1);
                PreferencesController.getInstance().setPrinterId2(printId2);
                PreferencesController.getInstance().setPrinterId3(printId3);
                PreferencesController.getInstance().setPrinterId4(printId4);
                PreferencesController.getInstance().setPrinterId5(printId5);


                PreferencesController.getInstance().setNetwork(network);

                PreferencesController.getInstance().setServerIpAddress(serverIP);

                PreferencesController.getInstance().setEntrance(entrance);
                PreferencesController.getInstance().setFb(fb);

                PreferencesController.getInstance().setNetworkUser(networkuser);
                PreferencesController.getInstance().setNetworkPassword(networkpassword);

                PreferencesController.getInstance().setOfflinePassword(offlinepassword);


                PreferencesController.getInstance().setUseShare(useshare);
                PreferencesController.getInstance().setUsePrinter(useprinter);


                if ("Y".compareTo(setupMode) != 0 &&
                        "offline".compareTo(accessMode) != 0) {
                    PreferencesController.getInstance().setHousing(housing);
                    PreferencesController.getInstance().setHousingName(housingName);
                }

                finish();

            }
        });

        findViewById(R.id.btnBack).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        edtGracePeriodFrom.setText(gracePeriodFrom);
        edtGracePeriodTo.setText(gracePeriodTo);

        edtUseShare.setText(useshare);

        edtUsePrinter.setText(useprinter);

        edtPrintName1.setText(printName1);
        edtPrintName2.setText(printName2);
        edtPrintName3.setText(printName3);
        edtPrintName4.setText(printName4);
        edtPrintName5.setText(printName5);

        edtPrintIP1.setText(printIP1);
        edtPrintIP2.setText(printIP2);
        edtPrintIP3.setText(printIP3);
        edtPrintIP4.setText(printIP4);
        edtPrintIP5.setText(printIP5);

        edtPrintId1.setText(printId1);
        edtPrintId2.setText(printId2);
        edtPrintId3.setText(printId3);
        edtPrintId4.setText(printId4);
        edtPrintId5.setText(printId5);

        edtNetwork.setText(network);

        edtNetworkUser.setText(networkUser);

        edtNetworkPassword.setText(networkPassword);

        edtOfflinePassword.setText(offlinepassword);
    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = (ListAdapter) listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void onResponse(ResponseType responseType, String result) {
        loading.dismiss();

        switch (responseType) {
            case CONFIRM_TICKET:
                if (result.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Login Fail, please try again",
                            Toast.LENGTH_SHORT)
                            .show();

                } else if (result.startsWith("ERROR - ADD THIS TO SKIP ALL ERERRO MESSAGE")) {
                    Toast.makeText(getApplicationContext(),
                            result,
                            Toast.LENGTH_SHORT)
                            .show();
                } else {

                    boolean resultIsFail = false;

                    if (result.startsWith("ERROR")) {
                        resultIsFail = true;
                        failCount++;
                    } else {
                        successCount++;
                    }

                    Log.d("Return", result);
                    try {
                        // Success and load the list

                        OfflineDatabase db = new OfflineDatabase(SettingActivity.this);
                        // Success and may need to clear the previous one db record
                        if (!resultIsFail) {
                            if ("".compareTo(previousRefNo) != 0) {
                                db.removeRecordsByRefNo(previousRefNo);

                                previousRefNo = "";
                            }
                        }
                        JSONObject jsonvalue;


                        if (refNos != null) {

                            if (refNos.size() > 0) {
                                jsonvalue = getJsonForConfirmation(0);
                                if (jsonvalue != null) {
                                    NetworkRepository.getInstance().confirmTicket(jsonvalue.toString(), this);
                                } else {

                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(SettingActivity.this, "Upload Complete (Fail: " + String.valueOf(failCount) + ", Success: " + String.valueOf(successCount) + ") ", duration);
                                    toast.show();
                                    btnUpload.setEnabled(false);

                                    if (loading != null)
                                        loading.dismiss();
                                }
                            }
                        }

                    } catch (Exception ec) {
                        Toast.makeText(getApplicationContext(),
                                "Error: " + ec.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }


                }

                break;

            case HOUSE_LIST:

                if (result.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Login Fail, please try again",
                            Toast.LENGTH_SHORT)
                            .show();

                } else if (result.startsWith("ERROR")) {
                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");

                    Toast.makeText(getApplicationContext(), displayErr, Toast.LENGTH_SHORT).show();
                } else {

                    Log.d("Return", result);
                    JSONObject jsonObj;
                    //
                    try {
                        // Success and load the list
                        listShow = new ArrayList<>();
                        list = new ArrayList<>();
                        listvalue = new ArrayList<>();
                        listSelected = new ArrayList<>();

                        String[] arrSel = houseSetting.split(",");

                        jsonObj = new JSONObject(result);
                        // Get Cinema Name
                        String cinemaName = jsonObj.getString("cinemaName");
                        if (cinemaName != null) {
                            ((TextView) findViewById(R.id.textView12)).setText(cinemaName);

                        }

                        JSONArray arr = jsonObj.getJSONArray("houseList");

                        if (arr != null) {

                            for (int x = 0; x < arr.length(); x++) {

                                JSONObject obj = arr.getJSONObject(x);
                                list.add(obj.getString("houseName"));
                                listvalue.add(obj.getString("houseId"));


                                boolean found = false;
                                for (String anArrSel : arrSel) {
                                    if (obj.getString("houseId").equals(anArrSel)) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (found) {
                                    listShow.add(true);
                                    listSelected.add("Y");
                                } else {
                                    listShow.add(false);
                                    listSelected.add("N");
                                }
                            }


                        }

                        if (arr != null && arr.length() > 0) {
                            ListAdapter adapterItem = new ListAdapter(this, list, listShow);
                            listview.setAdapter(adapterItem);

                            adapterItem.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(listview);
                        }


                    } catch (Exception ec) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Error: " + ec.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                }

                break;

            /**
             * Callbacks for the Turnstile
             * Author: Jz
             * Date: 05-03-2020
             * Version: 0.0.1
             */

            case GATE_ALL_HOUSE:

                if(result.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Login Fail, please try again",
                            Toast.LENGTH_SHORT).show();

                } else if(result.startsWith("ERROR")) {
                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");
                    Toast.makeText(getApplicationContext(), displayErr, Toast.LENGTH_SHORT).show();

                } else {

                    try {
                        GateHouse data = gson.fromJson(result, GateHouse.class);

                        listShow = new ArrayList<>();
                        list = new ArrayList<>();
                        listvalue = new ArrayList<>();
                        listSelected = new ArrayList<>();

                        String[] arrSel = houseSetting.split(",");

                        if(data != null && data.getHouse().length > 0) {
                            for(House house : data.getHouse()) {
                                list.add(house.getName().getEn());
                                listvalue.add(house.getId());

                                boolean found = false;

                                for (String anArrSel : arrSel) {
                                    if (house.getId().equals(anArrSel)) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (found) {
                                    listShow.add(true);
                                    listSelected.add("Y");
                                } else {
                                    listShow.add(false);
                                    listSelected.add("N");
                                }
                            }
                        }


                        if (data != null && data.getHouse().length > 0) {
                            ListAdapter adapterItem = new ListAdapter(this, list, listShow);
                            listview.setAdapter(adapterItem);

                            adapterItem.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(listview);
                        }


                    }catch (Exception ec) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Error: " + ec.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }


                }

                break;
        }
    }


    @Nullable
    private JSONObject getJsonForConfirmation(int startPosition) {
        boolean canStart = false;
        OfflineDatabase db = new OfflineDatabase(SettingActivity.this);
        JSONObject jsonvalue = new JSONObject();
        int countSelected = 0;
        for (int u = startPosition; u < refNos.size(); u++) {

            offlinePointer = u;

            try {
                jsonvalue.put("refNo", refNos.get(offlinePointer));
                jsonvalue.put("method", "A");
                jsonvalue.put("type", "B");

                previousRefNo = refNos.get(offlinePointer);
                // Get the Items by Refno
                List<Item> items = db.getRecordByRefNo(refNos.get(offlinePointer));

                JSONArray jsonArr = new JSONArray();

                countSelected = 0;
                if (items != null) {
                    for (int y = 0; y < items.size(); y++) {
                        if ("Invalid".compareTo(items.get(y).getSeatStatus()) == 0) {
                            countSelected++;

                            JSONObject seat = new JSONObject();
                            seat.put("seatId", items.get(y).getSeatId());

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String currentDateandTime = sdf.format(new Date());

                            seat.put("timestamp", currentDateandTime);

                            seat.put("action", "C");

                            jsonArr.put(seat);
                        }
                    }
                }

                jsonvalue.put("confirmSeat", jsonArr);
            } catch (Exception ex) {

            }

            canStart = countSelected > 0;
        }
        return canStart ? jsonvalue : null;

    }
}
