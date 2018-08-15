// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package qodrbee.com.mylibrary;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by uchar on 10/09/16.
 */
public class JSONRPCClient {
    final static String USER_AGENT = "EIQUI JSON-RPC 0.1";
    final private static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    final static int NO_SSL_VERIFY = 2;
    final private URL mURL;
    private int mFlags = 0;

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // FIXME: NOT USE IN PRODUCTION!!
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public JSONRPCClient(String host, int flags) throws MalformedURLException {
        mURL = new URL(host);
        mFlags = flags;
    }

    private HttpURLConnection startConnection() throws IOException {
        HttpURLConnection http = null;

        if (mURL.getProtocol().toLowerCase().equals("https")) {
            if ((mFlags&NO_SSL_VERIFY) == NO_SSL_VERIFY)
                trustAllHosts();
            HttpsURLConnection https = (HttpsURLConnection) mURL.openConnection();
            if ((mFlags&NO_SSL_VERIFY) == NO_SSL_VERIFY)
                https.setHostnameVerifier(DO_NOT_VERIFY);
            http = https;
        } else {
            http = (HttpURLConnection) mURL.openConnection();
        }

        http.setConnectTimeout(5000);
        http.setReadTimeout(20000);

        //add reuqest header
        http.setRequestMethod("POST");
        http.setRequestProperty("User-Agent", USER_AGENT);
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Accept", "application/json");

        // Send post request
        http.setDoOutput(true);

        return http;
    }

    public JSONObject sendJSONObject(String method, JSONObject jsonObjParams) throws IOException {
        HttpURLConnection http = startConnection();
        if (http == null)
            return null;

        try {
            JSONObject jsonRPC = new JSONObject();
            jsonRPC.put("jsonrpc", "2.0");
            jsonRPC.put("id", (int) System.currentTimeMillis());
            jsonRPC.put("method", method);
            jsonRPC.put("params", jsonObjParams);

            http.setRequestProperty("Content-Length", Integer.toString(jsonRPC.toString().length()));
            DataOutputStream wr = new DataOutputStream(http.getOutputStream());
            wr.writeBytes(jsonRPC.toString());
            wr.flush();
            wr.close();

            int responseCode = http.getResponseCode();
            if (responseCode != 200)
                return null;

            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            http.disconnect();

            return new JSONObject(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
