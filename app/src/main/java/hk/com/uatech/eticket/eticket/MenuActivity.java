package hk.com.uatech.eticket.eticket;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import hk.com.uatech.eticket.eticket.database.Show;
import hk.com.uatech.eticket.eticket.delegate.showtime.ShowtimeEvent;
import hk.com.uatech.eticket.eticket.network.NetworkRepository;
import hk.com.uatech.eticket.eticket.network.ResponseType;
import hk.com.uatech.eticket.eticket.pojo.ShowPojo;
import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import hk.com.uatech.eticket.eticket.preferences.SettingActivity;

public class MenuActivity extends AppCompatActivity implements ShowtimeEvent, NetworkRepository.QueryCallback {

    public static ProgressDialog loading = null;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        LinearLayout bgLayer = (LinearLayout) findViewById(R.id.bglayer);

        //mode
        bgLayer.setBackgroundColor(
                "offline".equals(PreferencesController.getInstance().getAccessMode()) ?
                        Color.rgb(255, 179, 179) :
                        Color.rgb(255, 255, 255)
        );
        View settings = findViewById(R.id.btnSetting);
        settings.setEnabled(!"offline".equals(PreferencesController.getInstance().getAccessMode()));
        settings.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferencesController.getInstance().isUsePasswordForSettings()) {
                    if ("MANAGER".equals(PreferencesController.getInstance().getUserRank())) {
                        showPasswordDialog();
                    } else {
                        Toast.makeText(
                                MenuActivity.this,
                                //"Sorry, User don't have the access right to access Setting function",
                                "unauthorized",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    if (PreferencesController.getInstance().isSettingsEnabled()) {
                        startActivity(new Intent(MenuActivity.this, SettingActivity.class));
                    } else {
                        Toast.makeText(MenuActivity.this, "unauthorized", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

        findViewById(R.id.btnEntrance).setOnClickListener(new Button.OnClickListener() {

            @Override

            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, EntractStep1Activity.class));
            }

        });

        findViewById(R.id.btnQuickEntrance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferencesController.getInstance().getAccessMode().equals("offline")) {
                    Toast.makeText(
                            MenuActivity.this,
                            "Quick entrance doesn’t work during offline",
                            Toast.LENGTH_SHORT)
                            .show();

                } else {
                    Intent intent = new Intent(MenuActivity.this, EntranceStep2Activity.class);
                    intent.putExtra("showScanner", true);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.btnLogout).setOnClickListener(new Button.OnClickListener() {

            @Override

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Confirm to log out the system?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        // Remove the session
                        // Update the Preference
                        PreferencesController.getInstance().setAccessToken("");
                        PreferencesController.getInstance().setAccessMode("");
                        PreferencesController.getInstance().setUserRank("");
                        PreferencesController.getInstance().setUserPassword("");
                        dialog.dismiss();
                        finish();
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

        });

        String accessMode = PreferencesController.getInstance().getAccessMode();


        /*
        if(accessMode.equals("online")) {
            Log.d(MenuActivity.class.toString(), "AccessMode: " + accessMode);

            try {
                JSONObject jsonvalue = new JSONObject();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                final String str_date = df.format(new Date());

                jsonvalue.put("date", str_date);
                NetworkRepository.getInstance().getGateShowList(jsonvalue.toString(), this);

                showLoading();
            } catch (Exception e) {

                Toast.makeText(getApplication(),
                        e.getMessage(),
                        Toast.LENGTH_SHORT).show();

            } finally {
                dismissLoad();
            }


        } else if(accessMode.equals("offline")) {
            showLoading();

            ShowtimeNotifier notifier = new ShowtimeNotifier(this);

            notifier.doWork(this);
        }
         */
        
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
                                Toast.makeText(MenuActivity.this, "Invalid  password", Toast.LENGTH_LONG).show();
                                showPasswordDialog();
                            } else {
//                                SharedPreferences sp = getSharedPreferences("E-TICKET", MODE_PRIVATE);
//
//                                String user_pwd = sp.getString("user_password", "");

                                if (password.compareTo(PreferencesController.getInstance().getUserPassword()) == 0) {
                                    //dialog.dismiss();

                                    Intent intent = new Intent();
                                    intent.setClass(MenuActivity.this, SettingActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MenuActivity.this, "Password Incorrect, please try again", Toast.LENGTH_LONG).show();
                                    // pass.setText("");
                                    // pass.requestFocus();

                                    showPasswordDialog();
                                }


                                //TODO here any local checks if password or user is valid


                                //this will do the actual check with my back-end server for valid user/pass and callback with the response
                                //new CheckLoginAsync(MainActivity.this,username,password).execute("","");
                            }
                        } catch (Exception e) {
                            Toast.makeText(MenuActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
        /*
        if (Login_USER.length()>1) //if we have the username saved then focus on password field, be user friendly :-)
            pass.requestFocus();
            */

    }


    /**
     * Display loading spinner
     */
    private void showLoading() {
        if(!MenuActivity.this.isFinishing()) {
            if(loading == null) {
                loading = new ProgressDialog(MenuActivity.this);
                loading.setMessage("Loading");
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }

            loading.show();
        }
    }


    /**
     * Hide loading spinner
     */
    private void dismissLoad() {
        if(loading != null && loading.isShowing()) {
            loading.dismiss();
        }

        loading = null;
    }


    @Override
    public void completeHandler() {
        dismissLoad();
    }


    @Override
    protected void onDestroy() {
        if(loading != null && loading.isShowing()) {
            loading.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onResponse(ResponseType responseType, String result) {
        switch (responseType) {
            case GATE_SHOW_LIST:

                if(result.startsWith("ERROR")) {
                    String msg = "Failed to get show list.";

                    Toast.makeText(getApplicationContext(),
                            msg,
                            Toast.LENGTH_SHORT).show();

                    return;
                }

                try {
                    ShowPojo response = gson.fromJson(result, ShowPojo.class);

                    if(response != null && response.getShow().length > 0) {
                        Show model = new Show(getApplicationContext());
                        // Clear existing data
                        model.clear();

                        // Insert new data
                        model.add_shows(response.getShow());

                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "No show list available.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    return;
                }

                break;
        }
    }
}
