package com.example.jussi.myalarm;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpManager {

    public static String getData (String uri) {

        BufferedReader reader = null;

        try {

            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

    }

    public static String postData (String uri) {

        String posting = com.example.jussi.myalarm.AlarmActivity.posting;
        Long newAverage = com.example.jussi.myalarm.AlarmActivity.newAverage;
        String newAverageString = Long.toString(newAverage);

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/xml");
            con.setRequestProperty("Accept", "application/xml");

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                    "   <account> \n" +
                    "       <accountId>1</accountId> \n" +
                    "       <average>"+newAverageString+"</average> \n" +
                    "   </account> "));
            osw.flush();
            osw.close();
            System.err.println(con.getResponseCode());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return posting;
    }


}