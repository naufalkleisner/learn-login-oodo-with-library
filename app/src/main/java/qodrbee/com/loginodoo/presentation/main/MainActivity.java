package qodrbee.com.loginodoo.presentation.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import qodrbee.com.loginodoo.R;
import qodrbee.com.loginodoo.presentation.AppMain;
import qodrbee.com.loginodoo.presentation.login.LoginActivity;
import qodrbee.com.loginodoo.util.Constants;
import qodrbee.com.mylibrary.exceptions.OdooSearchException;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @BindView(R.id.tvHost)
    TextView tvHost;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.btn_logout)
    Button btnLogout;
    private SharedPreferences mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mStorage = getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);

        if (savedInstanceState == null) {
            new AccountInfoTask().execute();

        } else {
            tvEmail.setText(savedInstanceState.getString("name").trim());
            tvHost.setText(savedInstanceState.getString("login").trim());
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToLogin(Boolean.TRUE);

            }
        });
    }

    private void backToLogin(Boolean logout) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(LoginActivity.PARAM_LOGOUT, logout);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", tvEmail.getText().toString());
        outState.putString("login", tvHost.getText().toString());
    }

    private class AccountInfoTask extends AsyncTask<String, Void, Boolean> {

        private Exception mException;
        private JSONArray mAccountInfo;

        @Override
        protected Boolean doInBackground(String... strings) {
            mException = null;
            try {
                mAccountInfo = ((AppMain) getApplication()).OdooClient().callExecute(
                        "read", "res.users", "[" + ((AppMain) getApplication()).getUID() + "]",
                        "['image_meidum', 'name', 'login' ]");
                return (mAccountInfo != null);

            } catch (OdooSearchException e) {
                e.printStackTrace();
            }

            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean res) {
            if (!res || mException != null)
                return;

            try {
                JSONObject jsonObj = mAccountInfo.getJSONObject(0);

                //Write Basic User Info
                tvEmail.setText(jsonObj.getString("name").trim());
                tvHost.setText(jsonObj.getString("login").trim());

            } catch (JSONException e) {
                Log.d(TAG, "JSONException: " + e);
            } catch (NullPointerException e) {
                Log.d(TAG, "NullPointerException: " + e);
            }

        }
    }
}
