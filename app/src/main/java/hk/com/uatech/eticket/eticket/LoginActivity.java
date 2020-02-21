package hk.com.uatech.eticket.eticket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import hk.com.uatech.eticket.eticket.preferences.SettingActivity;
import hk.com.uatech.eticket.eticket.utils.Utils;

public class LoginActivity extends AppCompatActivity implements NetworkRepository.QueryCallback {
    private ProgressDialog loading = null;
    private static final int SETTINGS_CLOSED = 12;

    private static String OFFLINE_USERNAME = "";
    private static String OFFLINE_PASSWORD = "";
    private EditText edtLoginId;
    private EditText edtPassword;
    private Spinner domainSpinner;
    private Map<String, String> domains = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        OFFLINE_USERNAME = Utils.getConfigValue(LoginActivity.this, "offline_username");
        //OFFLINE_PASSWORD = MainActivity.getConfigValue(LoginActivity.this, "offline_password");
        OFFLINE_PASSWORD = PreferencesController.getInstance().getOfflinePassword(); // MainActivity.getConfigValue(LoginActivity.this, "api_url");
        edtLoginId = (EditText) findViewById(R.id.loginId);
        edtPassword = (EditText) findViewById(R.id.password);
        domainSpinner = (Spinner) findViewById(R.id.domains);
        NetworkRepository.getInstance().getDomains(this);

        findViewById(R.id.btnLogin).setOnClickListener(new Button.OnClickListener() {

            @Override

            public void onClick(View v) {
                try {
                    if (TextUtils.isEmpty(edtLoginId.getEditableText())) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Please input Login ID",
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    if (TextUtils.isEmpty(edtPassword.getEditableText())
                            && "offline".compareToIgnoreCase(edtLoginId.getEditableText().toString()) != 0) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Please input Password",
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    // Check access offline mode or not
                    if (OFFLINE_USERNAME.equals(edtLoginId.getEditableText().toString())) {

                        PreferencesController.getInstance().setAccessToken("");
                        PreferencesController.getInstance().setAccessMode("offline");

                        CharSequence text = "Offline Mode Detected, please upload the saved records in Setting page after you change back to Online Mode";
                        Toast.makeText(
                                getApplicationContext(),
                                text,
                                Toast.LENGTH_SHORT)
                                .show();

                        startActivity(new Intent(LoginActivity.this, MenuActivity.class));

                        edtLoginId.setText("");
                        edtPassword.setText("");

                    } else {

                        //String urlString = "http://13.228.58.92/thirdparty/api/e-entrance/staffAuth";
                        if (loading == null) {
                            loading = new ProgressDialog(v.getContext());

                            loading.setCancelable(true);
                            loading.setMessage("Loading");
                            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        }

                        JSONObject jsonvalue = new JSONObject();


                        jsonvalue.put("username", edtLoginId.getText());
                        jsonvalue.put("password", edtPassword.getText());
                        jsonvalue.put("domainID", domainSpinner.getSelectedView().getTag());

                        loading.show();
                        NetworkRepository.getInstance().auth(jsonvalue.toString(), LoginActivity.this);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        if (getIntent().getExtras().getString("showEditPage").compareTo("Y") == 0) {
            // Need to show the edit page first
            Intent intent = new Intent(this, SettingActivity.class);
            intent.putExtra("setupMode", "Y");

            startActivityForResult(intent, SETTINGS_CLOSED);

        }
    }

    @Override
    public void onResponse(ResponseType responseType, String result) {

        if (loading != null) {
            loading.dismiss();
        }
        JSONObject jsonObj;

        switch (responseType) {
            case GET_DOMAINS:
                try {
                    jsonObj = new JSONObject(result);
                    JSONArray domainList = (JSONArray) jsonObj.get("adDomainList");
                    domains.clear();
                    for (int i = 0; i < domainList.length(); i++) {
                        String type = domainList.getJSONObject(i).get("id").toString();
                        String domain = domainList.getJSONObject(i).get("domain").toString();
                        domains.put(type, domain);
                    }
                    HashMapAdapter adapter = new HashMapAdapter(domains);
                    domainSpinner.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AUTH:
                if (result.startsWith("ERROR")) {
                    String displayErr = result;
                    displayErr = displayErr.replace("ERROR (400) : ", "");
                    displayErr = displayErr.replace("ERROR (401) : ", "");
                    displayErr = displayErr.replace("ERROR (402) : ", "");

                    Toast.makeText(getApplicationContext(), displayErr, Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("Return", result);

                //
                try {
                    jsonObj = new JSONObject(result);
                    int resultCode = jsonObj.getInt("resultCode");


                    if (resultCode == 200) {
                        String token = jsonObj.getString("token");

                        String pwd = edtPassword.getText().toString();

                        PreferencesController.getInstance().setSettingsEnabled(jsonObj.getInt("isSetting") == 1);
                        PreferencesController.getInstance().setUploadingEnabled(jsonObj.getInt("isUpload") == 1);
                        PreferencesController.getInstance().setAccessToken(token);
                        PreferencesController.getInstance().setAccessMode("online");
                        PreferencesController.getInstance().setUserPassword(pwd);

                        if (jsonObj.getString("isManager") != null) {
                            int staffType = jsonObj.getInt("isManager");
                            PreferencesController.getInstance().setUserRank(staffType == 1 ? "MANAGER" : "OPERATION");
                        }

                        edtLoginId.setText("");
                        edtPassword.setText("");

                        startActivity(new Intent(this, MenuActivity.class));


                    } else {

                        String errMsg;
                        if (resultCode == 500) {
                            errMsg = "Server error, please contact administrator";
                        } else if (!TextUtils.isEmpty(jsonObj.getString("resultMsg"))) {
                            errMsg = jsonObj.getString("resultMsg");
                        } else {
                            errMsg = result;
                        }

                        Toast toast = Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT);
                        toast.show();


                    }
                } catch (Exception ec) {
                    Toast.makeText(getApplicationContext(),
                            "Error : " + ec.getMessage(),
                            Toast.LENGTH_SHORT)
                            .show();
                }

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_CLOSED) {
//            NetworkRepository.getInstance().getDomains(this);
        }
    }
}
