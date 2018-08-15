package qodrbee.com.loginodoo.presentation.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.MalformedURLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import qodrbee.com.loginodoo.R;
import qodrbee.com.loginodoo.presentation.AppMain;
import qodrbee.com.loginodoo.presentation.main.MainActivity;
import qodrbee.com.loginodoo.util.Constants;
import qodrbee.com.mylibrary.exceptions.OdooLoginException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static String PARAM_LOGOUT = "logout";
    private final String TAG = "LoginActivity";
    private SharedPreferences mStorage;

    @BindView(R.id.edtDBName)
    EditText edtDBName;
    @BindView(R.id.tilDBName)
    TextInputLayout tilDBName;
    @BindView(R.id.pbLogin)
    ProgressBar pbLogin;
    @BindView(R.id.edtURL)
    EditText edtURL;
    @BindView(R.id.tilURL)
    TextInputLayout tilURL;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.tilEmail)
    TextInputLayout tilEmail;
    @BindView(R.id.edtPassword)
    EditText edtPassword;
    @BindView(R.id.tilPassword)
    TextInputLayout tilPassword;
    @BindView(R.id.btn_calculate)
    Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mStorage = getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);

        if (mStorage.getInt("UserId", -1) > 0) {
            initApp();
        }

        if (getIntent().getBooleanExtra(PARAM_LOGOUT, Boolean.FALSE)) {
            logout();
        }

        pbLogin.setVisibility(View.GONE);
        btnCalculate.setOnClickListener(this);

        edtURL.setText(mStorage.getString("Host", ""));
        edtDBName.setText(mStorage.getString("DBName", ""));
        edtEmail.setText(mStorage.getString("Login", ""));
        edtPassword.setText(mStorage.getString("Pass", ""));
    }

    private void logout() {
        SharedPreferences.Editor editor = mStorage.edit();
        editor.remove("UserID");
        editor.remove("lastTaskID");
        editor.remove("lastIssueID");
        editor.remove("lastMessageID");
        editor.remove("Pass");
        editor.commit();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_calculate) {
            new LoginTask().execute(
                    edtURL.getText().toString(),
                    edtDBName.getText().toString(),
                    edtEmail.getText().toString(),
                    edtPassword.getText().toString());

            pbLogin.setVisibility(View.VISIBLE);
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        private Integer mUID;

        protected Boolean doInBackground(String... params) {
            try {
                ((AppMain) getApplication()).startOdooClient(params[0], params[1], -1, "");
                mUID = ((AppMain) getApplication()).OdooClient().loginIn(params[2], params[3]);
                return (mUID > 0);

            } catch (OdooLoginException e) {
                Log.d(TAG, "OdooLoginException: " + e);
            } catch (MalformedURLException e) {
                Log.d(TAG, "MalformedURLException: " + e);
            }

            return Boolean.FALSE;
        }

        protected void onPostExecute(Boolean res) {
            if (res) {
                SharedPreferences.Editor editor = mStorage.edit();
                editor.putInt("UserID", mUID);
                editor.putString("Host", edtURL.getText().toString());
                editor.putString("DBName", edtDBName.getText().toString());
                editor.putString("Login", edtEmail.getText().toString());
                editor.putString("Pass", edtPassword.getText().toString());

                editor.apply();

                initApp();
            } else {
                pbLogin.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "login failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initApp() {
        try {
            ((AppMain) getApplication()).startOdooClient(
                    mStorage.getString("Host", ""),
                    mStorage.getString("DBName", ""),
                    mStorage.getInt("UserID", -1),
                    mStorage.getString("Pass", ""));

        } catch (MalformedURLException e) {
            Log.d(TAG, "initApp: " + e);
        }

        startActivity(new Intent(this, MainActivity.class));
        sendBroadcast(new Intent(Constants.SHARED_PREFS_USER_INFO));
        finish();
    }

}
