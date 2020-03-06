package hk.com.uatech.eticket.eticket.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import hk.com.uatech.eticket.eticket.R;
import hk.com.uatech.eticket.eticket.utils.Utils;

public class PreferencesController {
    private static final PreferencesController instance = new PreferencesController();
    private SharedPreferences sharedPreferences;
    private static final String KEY_GRACE_FROM = "grace_period_from";
    private static final String KEY_GRACE_TO = "grace_period_to";
    private static final String KEY_HOUSING = "housing";
    private static final String KEY_HOUSING_NAME = "housingName";
    private static final String KEY_PRINTER_NAME_1 = "printer_name_1";
    private static final String KEY_PRINTER_NAME_2 = "printer_name_2";
    private static final String KEY_PRINTER_NAME_3 = "printer_name_3";
    private static final String KEY_PRINTER_NAME_4 = "printer_name_4";
    private static final String KEY_PRINTER_NAME_5 = "printer_name_5";

    private static final String KEY_USE_SHARE = "use_share";
    private static final String KEY_USE_PRINTER = "use_printer";

    private static final String KEY_PRINTER_IP_1 = "printer_ip_1";
    private static final String KEY_PRINTER_IP_2 = "printer_ip_2";
    private static final String KEY_PRINTER_IP_3 = "printer_ip_3";
    private static final String KEY_PRINTER_IP_4 = "printer_ip_4";
    private static final String KEY_PRINTER_IP_5 = "printer_ip_5";

    private static final String KEY_PRINTER_ID_1 = "printer_id_1";
    private static final String KEY_PRINTER_ID_2 = "printer_id_2";
    private static final String KEY_PRINTER_ID_3 = "printer_id_3";
    private static final String KEY_PRINTER_ID_4 = "printer_id_4";
    private static final String KEY_PRINTER_ID_5 = "printer_id_5";

    private static final String KEY_NETWORK = "network";
    private static final String KEY_NETWORK_USER = "network_user";
    private static final String KEY_NETWORK_PASSWORD = "network_password";
    private static final String KEY_OFFLINE_PASSWORD = "offline_password";
    private static final String KEY_ACCESS_MODE = "access_mode";
    private static final String KEY_ENTRANCE = "entrance";
    private static final String KEY_FB = "fb";
    private static final String KEY_SHOW_SCAN = "show_scan";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_SERVER_IP_ADDRESS = "server_ip_address";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_USER_RANK = "user_rank";
    private static final String KEY_USE_PASSWORD_SETTINGS = "use_password_settings";
    private static final String KEY_USE_PASSWORD_CHANGE_SEAT = "use_password_change_seat";
    private static final String KEY_SETTINGS_ENABLED = "settings_enabled";
    private static final String KEY_UPLOAD_ENABLED = "upload_enabled";
    private static final String KEY_CHECK_UPLOAD_BY_ROLE = "upload_by_role";
    private static final String KEY_CHECK_UPLOAD_BY_PERMISSION = "upload_by_permission";

    private static String SAMBA_PATH;
    private static String SAMBA_USERNAME;
    private static String SAMBA_PASSWORD;
    private static String OFFLINE_PASSWORD;


    private static final String CINEMA_ID = "cinema_id";



    public static PreferencesController getInstance() {
        return instance;
    }

    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences("E-TICKET", Context.MODE_PRIVATE);
        setDefaults(context);
    }


    private synchronized void setDefaults(Context context) {
        SAMBA_PATH = Utils.getConfigValue(context, "samba_path");
        SAMBA_USERNAME = Utils.getConfigValue(context, "samba_username");
        SAMBA_PASSWORD = Utils.getConfigValue(context, "samba_password");
        OFFLINE_PASSWORD = Utils.getConfigValue(context, "offline_password");
    }

    public String getGracePeriodFrom() {
        return sharedPreferences.getString(KEY_GRACE_FROM, "30");
    }

    public void setGracePeriodFrom(String value) {
        sharedPreferences.edit().putString(KEY_GRACE_FROM, value).apply();
    }

    public String getGracePeriodTo() {
        return sharedPreferences.getString(KEY_GRACE_TO, "30");
    }

    public void setGracePeriodTo(String value) {
        sharedPreferences.edit().putString(KEY_GRACE_TO, value).apply();
    }

    public String getHousing() {
        return sharedPreferences.getString(KEY_HOUSING, "");
    }

    public void setHousing(String value) {
        sharedPreferences.edit().putString(KEY_HOUSING, value).apply();
    }

    public String getHousingName() {
        return sharedPreferences.getString(KEY_HOUSING_NAME, "");
    }

    public void setHousingName(String value) {
        sharedPreferences.edit().putString(KEY_HOUSING_NAME, value).apply();
    }

    public String getUseShare() {
        return sharedPreferences.getString(KEY_USE_SHARE, "Y");
    }

    public void setUseShare(String value) {
        sharedPreferences.edit().putString(KEY_USE_SHARE, value).apply();
    }

    public String getUsePrinter() {
        return sharedPreferences.getString(KEY_USE_PRINTER, "Y");
    }

    public void setUsePrinter(String value) {
        sharedPreferences.edit().putString(KEY_USE_PRINTER, value).apply();
    }


    public String getPrinterName1() {
        return sharedPreferences.getString(KEY_PRINTER_NAME_1, "001 - By Hot Dog");
    }


    public void setPrinterName1(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_NAME_1, value).apply();
    }


    public String getPrinterName2() {
        return sharedPreferences.getString(KEY_PRINTER_NAME_2, "002 - By Sandwich Fridge");
    }

    public void setPrinterName2(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_NAME_2, value).apply();
    }

    public String getPrinterName3() {
        return sharedPreferences.getString(KEY_PRINTER_NAME_3, "003 - By Hot Cabinet");
    }

    public void setPrinterName3(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_NAME_3, value).apply();
    }

    public String getPrinterName4() {
        return sharedPreferences.getString(KEY_PRINTER_NAME_4, "004 - By Drinks");
    }

    public void setPrinterName4(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_NAME_4, value).apply();
    }

    public String getPrinterName5() {
        return sharedPreferences.getString(KEY_PRINTER_NAME_5, "005 - By Consolidation Table");
    }

    public void setPrinterName5(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_NAME_5, value).apply();
    }


    public String getPrinterId1() {
        return sharedPreferences.getString(KEY_PRINTER_ID_1, "1");
    }

    public void setPrinterId1(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_ID_1, value).apply();
    }


    public String getPrinterId2() {
        return sharedPreferences.getString(KEY_PRINTER_ID_2, "2");
    }

    public void setPrinterId2(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_ID_2, value).apply();
    }

    public String getPrinterId3() {
        return sharedPreferences.getString(KEY_PRINTER_ID_3, "3");
    }

    public void setPrinterId3(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_ID_3, value).apply();
    }

    public String getPrinterId4() {
        return sharedPreferences.getString(KEY_PRINTER_ID_4, "4");
    }

    public void setPrinterId4(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_ID_4, value).apply();
    }

    public String getPrinterId5() {
        return sharedPreferences.getString(KEY_PRINTER_ID_5, "5");
    }

    public void setPrinterId5(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_ID_5, value).apply();
    }


    public String getPrinterIp1() {
        return sharedPreferences.getString(KEY_PRINTER_IP_1, "10.81.9.11");
    }

    public void setPrinterIp1(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_IP_1, value).apply();
    }

    public String getPrinterIp2() {
        return sharedPreferences.getString(KEY_PRINTER_IP_2, "10.81.9.12");
    }

    public void setPrinterIp2(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_IP_2, value).apply();
    }

    public String getPrinterIp3() {
        return sharedPreferences.getString(KEY_PRINTER_IP_3, "10.81.9.13");
    }

    public void setPrinterIp3(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_IP_3, value).apply();
    }

    public String getPrinterIp4() {
        return sharedPreferences.getString(KEY_PRINTER_IP_4, "10.81.9.14");
    }

    public void setPrinterIp4(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_IP_4, value).apply();
    }

    public String getPrinterIp5() {
        return sharedPreferences.getString(KEY_PRINTER_IP_5, "10.81.9.15");
    }

    public void setPrinterIp5(String value) {
        sharedPreferences.edit().putString(KEY_PRINTER_IP_5, value).apply();
    }

    public String getNetwork() {
        return sharedPreferences.getString(KEY_NETWORK, SAMBA_PATH);
    }

    public void setNetwork(String value) {
        sharedPreferences.edit().putString(KEY_NETWORK, value).apply();
    }

    public String getNetworkUser() {
        return sharedPreferences.getString(KEY_NETWORK_USER, SAMBA_USERNAME);
    }

    public void setNetworkUser(String value) {
        sharedPreferences.edit().putString(KEY_NETWORK_USER, value).apply();
    }

    public String getNetworkPassword() {
        return sharedPreferences.getString(KEY_NETWORK_PASSWORD, SAMBA_PASSWORD);
    }

    public void setNetworkPassword(String value) {
        sharedPreferences.edit().putString(KEY_NETWORK_PASSWORD, value).apply();
    }

    public String getOfflinePassword() {
        return sharedPreferences.getString(KEY_OFFLINE_PASSWORD, OFFLINE_PASSWORD);
    }


    public void setOfflinePassword(String value) {
        sharedPreferences.edit().putString(KEY_OFFLINE_PASSWORD, value).apply();
    }


    public String getAccessMode() {
        return sharedPreferences.getString(KEY_ACCESS_MODE, "online");
    }


    public void setAccessMode(String value) {
        sharedPreferences.edit().putString(KEY_ACCESS_MODE, value).apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    public void setAccessToken(String value) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, value).apply();
    }

    public String getServerIpAddress() {
        return sharedPreferences.getString(KEY_SERVER_IP_ADDRESS, "");
    }

    public void setServerIpAddress(String value) {
        sharedPreferences.edit().putString(KEY_SERVER_IP_ADDRESS, value).apply();
    }

    public String getEntrance() {
        return sharedPreferences.getString(KEY_ENTRANCE, "");
    }

    public void setEntrance(String value) {
        sharedPreferences.edit().putString(KEY_ENTRANCE, value).apply();
    }

    public String getFb() {
        return sharedPreferences.getString(KEY_FB, "");
    }

    public void setFb(String value) {
        sharedPreferences.edit().putString(KEY_FB, value).apply();
    }

    public boolean isShowScan() {
        return sharedPreferences.getBoolean(KEY_SHOW_SCAN, true);
    }

    public void setIsShowScan(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_SCAN, value).apply();
    }

    public String getUserPassword() {
        return sharedPreferences.getString(KEY_USER_PASSWORD, "");
    }

    public void setUserPassword(String value) {
        sharedPreferences.edit().putString(KEY_USER_PASSWORD, value).apply();
    }

    public String getUserRank() {
        return sharedPreferences.getString(KEY_USER_RANK, "");
    }

    public void setUserRank(String value) {
        sharedPreferences.edit().putString(KEY_USER_RANK, value).apply();
    }

    public boolean isUsePasswordForSettings() {
        return sharedPreferences.getBoolean(KEY_USE_PASSWORD_SETTINGS, false);
    }

    public void setUsePasswordSettings(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_USE_PASSWORD_SETTINGS, value).apply();
    }


    public boolean isUsePasswordForChangeSeat() {
        return sharedPreferences.getBoolean(KEY_USE_PASSWORD_CHANGE_SEAT, false);
    }

    public void setUsePasswordChangeSeat(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_USE_PASSWORD_CHANGE_SEAT, value).apply();
    }

    public boolean isSettingsEnabled() {
        return sharedPreferences.getBoolean(KEY_SETTINGS_ENABLED, false);
    }

    public void setSettingsEnabled(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_SETTINGS_ENABLED, value).apply();
    }

    public boolean isUploadingEnabled() {
        return sharedPreferences.getBoolean(KEY_UPLOAD_ENABLED, false);
    }

    public void setUploadingEnabled(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_UPLOAD_ENABLED, value).apply();
    }


    public boolean isCheckByRole() {
        return sharedPreferences.getBoolean(KEY_CHECK_UPLOAD_BY_ROLE, true);
    }

    public void setCheckByRole(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_CHECK_UPLOAD_BY_ROLE, value).apply();
    }

    public boolean isCheckByPermission() {
        return sharedPreferences.getBoolean(KEY_CHECK_UPLOAD_BY_PERMISSION, false);
    }

    public void setCheckByPermission(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_CHECK_UPLOAD_BY_PERMISSION, value).apply();
    }

    /**
     * Set / Get cinema id
     * @return
     */
    public String getCinemaId() {
        return sharedPreferences.getString(CINEMA_ID, "");
    }

    public void setCinemaId(String value) {
        sharedPreferences.edit().putString(CINEMA_ID, value).apply();
    }

}
