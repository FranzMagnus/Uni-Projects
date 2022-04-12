package com.example.fitnessteamtracker.Util;

/**
 * Created by Simon on 01.04.2020.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by Simon on 01.03.2018.
 */

public class HttpPoster extends AsyncTask<String, Void, String> {
    private String serverUrl = "";
    private String httpPostResult;
    private String jsonBody;
    private boolean withJson;
    private int responseCode = -1;

    public HttpPoster(String url) {
        this.serverUrl = url;
    }

    // Uses the default url specified in the Configuration class
    public HttpPoster(){
        this(Configuration.ServerURL);
    }

    public HttpPoster(Object jsonObject) {
        ObjectMapper o = new ObjectMapper();
        try {
            jsonBody = o.writeValueAsString(jsonObject);
            withJson = true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        this.serverUrl = Configuration.ServerURL;
    }

    @Override
    protected String doInBackground(String... params)
    {
        BufferedReader inBuffer = null;
        String url = this.serverUrl;
        String result = "fail";

        for(String parameter: params) {
            url = url + "/" + parameter;
        }

        try {
            // https://stackoverflow.com/questions/31433687/android-gradle-apache-httpclient-does-not-exist
            URL poster = new URL(url);
            Log.d("INFORMATION","URL: " + url);
            HttpURLConnection connection = (HttpURLConnection) poster.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if(withJson) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            result = connection.getResponseMessage();
            responseCode = connection.getResponseCode();
            if(responseCode == 400) {
                result = convertInputStreamToString(connection.getErrorStream());
            }
            Log.d("INFORMATION","Poster returned " + result);

        } catch(Exception e) {

            Log.e("INFORMATION","Post failed with " + e);
            Log.e("INFORMATION", Arrays.toString(e.getStackTrace()));
            /*
             * some useful exception handling should be here
             */
        } finally {
            if (inBuffer != null) {
                try {
                    inBuffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    protected void onPostExecute(String result) {
        // this is used to access the result of the HTTP GET lateron
        httpPostResult = result;
    }

    /**
     * Returns the result of the operation if needed lateron.
     * @return
     */

    public String getResult(){
        return httpPostResult;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
