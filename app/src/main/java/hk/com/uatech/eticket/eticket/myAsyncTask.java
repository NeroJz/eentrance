package hk.com.uatech.eticket.eticket;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alex_ on 17/07/2017.
 */

public class myAsyncTask extends AsyncTask<String, Void, String> {

    private myAsyncTaskCompletedListener listener;
    private int responseCode = 0;
    private boolean needSkip = false;

    public myAsyncTask() {
    }

    public myAsyncTask(myAsyncTaskCompletedListener listener, int responseCode) {
        this.listener = listener;
        this.responseCode = responseCode;
    }

    public myAsyncTask(myAsyncTaskCompletedListener listener, int responseCode, boolean needSkip) {
        this.listener = listener;
        this.responseCode = responseCode;
        this.needSkip = needSkip;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }



    @Override
    protected String doInBackground(String... params) {

        String data = "";


        if (needSkip) {
            return "";
        }


        HttpURLConnection httpURLConnection = null;
        try {



            httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
            httpURLConnection.setRequestMethod("POST");

           //httpURLConnection.setRequestProperty("Content-Type", "application/json");
           httpURLConnection.setRequestProperty("Accept", "*/*");

            //httpURLConnection.setInstanceFollowRedirects(false);
            //httpURLConnection.setDoInput(true);
            //httpURLConnection.setDoOutput(true);

            /*
            String urlParameters = "body"
                    + URLEncoder.encode(params[1].toString(), "UTF-8");
            httpURLConnection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
*/
            if (params.length >= 3) {
                // Have the token
                String token = params[2];
                httpURLConnection.addRequestProperty("Authorization", "Bearer " + token);
                httpURLConnection.addRequestProperty("Content-Type", "application/json");
            }

            Log.d("URL", params[1]);
            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
            //wr.writeBytes("PostData=" + params[1]);
            wr.writeBytes(params[1]);
            wr.flush();
            wr.close();

            //httpURLConnection.connect();

            int ret = httpURLConnection.getResponseCode();

            if (ret == 400) {
                data = ""; // + httpURLConnection.getResponseMessage();

                InputStream in = httpURLConnection.getErrorStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                String tmpData = "";
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    tmpData += current;
                }


                try {
                    JSONObject obj = new JSONObject(tmpData);

                    data = "ERROR (400) : " + obj.getString("resultMsg");


                } catch (Exception eJson) {

                    data = "ERROR (400) : " + httpURLConnection.getResponseMessage();
                }

            } else if (ret == 401) {
                data = ""; // + httpURLConnection.getResponseMessage();

                InputStream in = httpURLConnection.getErrorStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                String tmpData = "";
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    tmpData += current;
                }


                try {
                    JSONObject obj = new JSONObject(tmpData);

                    data = "ERROR (401) : " + obj.getString("resultMsg");


                } catch (Exception eJson) {

                    data = "ERROR (401) : " + httpURLConnection.getResponseMessage();
                }

            } else if (ret == 402) {
                data = ""; // + httpURLConnection.getResponseMessage();

                InputStream in = httpURLConnection.getErrorStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                String tmpData = "";
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    tmpData += current;
                }


                try {
                    JSONObject obj = new JSONObject(tmpData);

                    data = "ERROR (402) : " + obj.getString("resultMsg");


                } catch (Exception eJson) {

                    data = "ERROR (402) : " + httpURLConnection.getResponseMessage();
                }


            } else {
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            //data = "ERROR (402) : Cannot connect to API";

            data = "ERROR (402) : " + e.toString();

        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        Log.d("RETURN", data.toString());

        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data

        if (!isCancelled()) {
            if (listener != null) {
                listener.onMyAsynTaskCompleted(responseCode, result);
            }
        }

    }



}
