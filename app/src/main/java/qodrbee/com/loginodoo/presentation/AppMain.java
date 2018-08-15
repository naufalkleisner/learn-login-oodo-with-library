package qodrbee.com.loginodoo.presentation;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.net.MalformedURLException;

import qodrbee.com.loginodoo.R;
import qodrbee.com.loginodoo.util.Constants;
import qodrbee.com.mylibrary.JSONRPCClientOdoo;

public class AppMain extends Application {

    protected JSONRPCClientOdoo mOdooClient;
    private SharedPreferences mStorage;

    public JSONRPCClientOdoo OdooClient() {
        return mOdooClient;
    }

    public void startOdooClient(String host, String dbname, int uid, String pass) throws MalformedURLException {
        mOdooClient = new JSONRPCClientOdoo(host);
        mOdooClient.setConfig(dbname, uid, pass);
    }

    public Integer getUID() {
        return mStorage.getInt("UserID", -1);
    }

    @Override
    public void onCreate() {
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        mStorage = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);

        //Auto-Start
        int uid = getUID();
        if (uid != -1) {
            try {
                startOdooClient(mStorage.getString("Host", ""),
                        mStorage.getString("DBName", ""), uid,
                        mStorage.getString("Pass", ""));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
