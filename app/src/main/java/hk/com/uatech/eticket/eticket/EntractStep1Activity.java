package hk.com.uatech.eticket.eticket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import hk.com.uatech.eticket.eticket.qrCode.QRActivity;
import hk.com.uatech.eticket.eticket.utils.Utils;


public class EntractStep1Activity extends QRActivity {

    private static boolean IS_DEBUG = false;
    final private static String QR_CODE_TEXT = "ih49VWLnJgY0h9sy/6L+7ClE5TtTYXKGQ99mg4laBC+50imaHzl" +
            "OY4hsNW25pF03BthVCHHsC/Wf3+a+IMLMnviG1lGSc7EghhHPnn70JcXRxj8t1VTJ2Dd5e1vhjfsnLR" +
            "1TNmrGdjCLUM0rrIbQ65sDve3RkjFo1/30w7BOYs3tqWzL/s0ZRAm7HG8XKZywfV6IBXW5TUX5Mb" +
            "B6xN2Zj5ydz0CukNFwyMayMEDzyinC/pHl5B4exc+FoTIf1xM1Nqm9r5jFXg0sWJ3+nI13aTa3WelS" +
            "VichuWFrDyyUdGOXyENcKCq8HsE2VnA7k+nd";

    final private static String EMPTY_QR_CODE_MSG = "The QR Code cannot EMPTY!";

//    private ProgressDialog loading = null;
    private Button btnQRCode;
    private Button btnBack;
    private final int TASK_COMPLETED = 100;
    private final int TASK_FAILED = -100;
    private final int RETURN_FROM_ADMIT_PAGE = 10282;
    private EditText edtQrCode;
    private String encryptRefNo = "";
    private String refType = "";
    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;
    private TextView scanResults;

    private String foodRefNo = "";

    private Uri imageUri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    private String allowedHouse = "";

    private ScanType scanType = ScanType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entract_step1);
        edtQrCode = (EditText) findViewById(R.id.edtQRCode);

        IS_DEBUG = Utils.isDebug(getApplicationContext());

        LinearLayout bgLayer = (LinearLayout) findViewById(R.id.bglayer);
        if ("offline".equals(PreferencesController.getInstance().getAccessMode())) {
            bgLayer.setBackgroundColor(Color.rgb(255, 179, 179));

        } else {
            bgLayer.setBackgroundColor(Color.rgb(255, 255, 255));

        }
        findViewById(R.id.btnBack).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loading != null && loading.isShowing()) {
                    loading.dismiss();
                }

                finish();
            }
        });

        CheckBox showHideContent = (CheckBox) findViewById(R.id.hideContent);
        showHideContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (((CheckBox) v).isChecked()) {
                    edtQrCode.setTransformationMethod(new PasswordTransformationMethod());
                } else {
                    edtQrCode.setTransformationMethod(new HideReturnsTransformationMethod());
                }
            }
        });

        showHideContent.setChecked(true);

        edtQrCode.setTransformationMethod(new PasswordTransformationMethod());
        edtQrCode.setEnabled(true);
//        edtQrCode.setEnabled(!"offline".equals(PreferencesController.getInstance().getAccessMode()));

        View btnQRInCode = findViewById(R.id.btnScanInQR);
        btnQRInCode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scanType = ScanType.IN;
//                        new IntentIntegrator(EntractStep1Activity.this).initiateScan();

                        if(!IS_DEBUG) {
                            String inputText = ((EditText) findViewById(R.id.edtQRCode)).getEditableText().toString();

                            if("".equals(inputText)) {
                                Toast.makeText(getApplicationContext(), EMPTY_QR_CODE_MSG, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            handleQrCode(inputText, true);
                            Log.d(EntractStep1Activity.class.toString(), "Stopped!");
                        }else{
                            handleQrCode(QR_CODE_TEXT, true);
                        }
                    }
                });

        View btnQROutCode = findViewById(R.id.btnScanOutQR);
        btnQROutCode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scanType = ScanType.OUT;
//                        new IntentIntegrator(EntractStep1Activity.this).initiateScan();

                        if(!IS_DEBUG) {
                            String inputText = ((EditText) findViewById(R.id.edtQRCode)).getEditableText().toString();

                            if("".equals(inputText)) {
                                Toast.makeText(getApplicationContext(), EMPTY_QR_CODE_MSG, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            handleQrCode(inputText, true);
                            Log.d(EntractStep1Activity.class.toString(), "Stopped!");
                        }else{
                            handleQrCode(QR_CODE_TEXT, true);
                        }
                    }
                });


        findViewById(R.id.btnSendQR).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_scan);
                int selectedId = radioGroup.getCheckedRadioButtonId();

                RadioButton rb = (RadioButton) findViewById(selectedId);

                if(rb.getText().toString().equalsIgnoreCase("scan in")) {
                    scanType = ScanType.IN;
                }else {
                    scanType = ScanType.OUT;
                }

                handleQrCode(((EditText) findViewById(R.id.edtQRCode)).getEditableText().toString(), true);
            }
        });

        //SharedPreferences sp = getSharedPreferences("E-TICKET", MODE_PRIVATE);
        allowedHouse = PreferencesController.getInstance().getHousingName();
        edtQrCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.toString().contains("\n")) {
                    System.out.println(s.toString());
//                    handleQrCode(edtQrCode.getEditableText().toString(), true);

                }

            }
        });
        edtQrCode.setText("");
        edtQrCode.requestFocus();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                edtQrCode.setText(result.getContents());
                handleQrCode(edtQrCode.getEditableText().toString(), true);
            }
        } else {
            switch (requestCode) {
                case RETURN_FROM_ADMIT_PAGE:
                    edtQrCode.setText("");
                    edtQrCode.requestFocus();
                    encryptRefNo = "";
                    refType = "";
                    foodRefNo = "";
                    break;
            }
        }
    }

    protected IBinder getToken() {
        return edtQrCode.getWindowToken();
    }

    @Override
    protected void goNext(String json, String encryptRefNo, String refType, String foodRefNo) {
        Intent intent = new Intent();
        intent.setClass(this, EntranceStep2Activity.class);
        intent.putExtra("json", json);
        intent.putExtra("encryptRefNo", encryptRefNo);
        intent.putExtra("refType", refType);
        intent.putExtra("foodRefNo", foodRefNo);
        intent.putExtra("showScanner", false);
        intent.putExtra("scanType", scanType.toString());
        // startActivity(intent);
        startActivityForResult(intent, RETURN_FROM_ADMIT_PAGE);
    }

    protected void cleanData() {
        edtQrCode.setText("");
    }


}
