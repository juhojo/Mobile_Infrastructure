package com.example.jussi.myalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmActivity extends AppCompatActivity implements SensorEventListener {

    // Alarm Clock Variables
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker alarmTimePicker;
    private static AlarmActivity inst;
    private TextView alarmTextView;

    // ProximitySensor Variables

    TextView proxText;
    SensorManager sm;
    Sensor proxSensor;

    Date pressedTime = new Date();
    long timeClicked;

    Date stopTime = new Date();
    long alarmEnded;

    static long newAverage;

    String canceli = null;

    //IP's
    String IP = "192.168.43.146:8080";
    //String IP = "10.0.3.2:8080";

    //Database
    TextView output;
    List<MyTask> tasks;
    ProgressBar pb;
    Integer average;

    //POST
    static String posting;

    private String text;
    private TextView answerTextView;
    String val = null;


    // Question Intent
    public void sendMessage(View view) {
        // Ota pois kommentista niin saat toisen activityn alkamaan kun painat nappia
        //Intent intent = new Intent(AlarmActivity.this, TestActivity.class);
        //startActivity(intent);
    }

    public static AlarmActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        posting = "no";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //Alarm Clock
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        alarmTextView = (TextView) findViewById(R.id.alarmText);
        final Button alarmToggle = (Button) findViewById(R.id.alarmToggle);
        alarmToggle.setText("Set Alarm");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Proximity Sensor

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        proxSensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxText = (TextView)findViewById(R.id.proximityTextView);
        sm.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //Alarm Clock
        alarmToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                output.setText("");
                alarmToggle.setText("Alarm is set");
                view.setEnabled(false);
                view.setClickable(false);
                Log.d("MyActivity", "Alarm On");
                Calendar calendar = Calendar.getInstance(); //Alarm time
                Calendar now = Calendar.getInstance(); //Time now
                if(calendar.before(now)){
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
                Intent myIntent = new Intent(AlarmActivity.this, AlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(AlarmActivity.this, 0, myIntent, 0);
                alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
            }
        });

        Intent intent = getIntent();
        canceli = intent.getStringExtra("canceli");

        if("cancel".equals(canceli)){
            // Shut up the alarm
                setAlarmText("");
                Log.d("MyActivity", "Alarm Off");

                Intent myIntent = new Intent(AlarmActivity.this, AlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(AlarmActivity.this, 0, myIntent, 0);

                alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                pendingIntent.cancel();
                alarmManager.cancel(pendingIntent);

                Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }

                //Ringtone ringtone = com.example.jussi.myalarm.AlarmReceiver.ringtone;
                Ringtone ringtone = com.example.jussi.myalarm.AlarmService.ringtone;
                ringtone.stop();

                // Enable the button again & Change text
                alarmToggle.setText("Set Alarm");
                alarmToggle.setEnabled(true);
                alarmToggle.setClickable(true);

                stopTime = new Date();
                Date startTime = com.example.jussi.myalarm.AlarmService.startTime;

                alarmEnded = (stopTime.getTime() - startTime.getTime()) / 1000;

                Log.d("MainActivity", "alarmEnded at: "+alarmEnded+" seconds");
        }
        Log.i("", "" + canceli);

        // Average time
        output = (TextView) findViewById(R.id.average);
        output.setMovementMethod(new ScrollingMovementMethod());
        pb = (ProgressBar) findViewById(R.id.progressBar2);
        pb.setVisibility(View.INVISIBLE);

        tasks = new ArrayList<>();
        requestData("http://" + IP + "/WakeUpBackend/webresources/com.wakeupproject.account/1");

    }

    //Proximity Sensor
    @Override
    public void onSensorChanged(SensorEvent event) {

        proxText.setText(String.valueOf(event.values[0]));

        if(event.values[0] <= 0.2) {
            pressedTime = new Date();
            Log.d("MainActivity", "Pressed");
            Log.d("MainActivity", ""+pressedTime);
        } else {
            timeClicked = new Date().getTime() - pressedTime.getTime();
            Log.d("MainActivity", "Released");
            Log.d("MainActivity", ""+timeClicked);
        }

        // If user hovers / holds hand on device for 3seconds
        final Button alarmToggle = (Button) findViewById(R.id.alarmToggle);

        Log.d("","alarmToggle.getText(): "+alarmToggle.getText());

        if(timeClicked >= 3000 && alarmToggle.getText() == "Alarm is set" && alarmTextView.getText() == "Alarm! Wake up! Wake up!"){
            timeClicked = 0;
            alarmToggle.setText("Set Alarm");
            Log.d("MainActivity", "Success");

            // Ota pois kommentista niin saat toisen activityn alkamaan kun pidät kättä sensorilla 3s
            Intent intent = new Intent(AlarmActivity.this, TestActivity.class);
            startActivity(intent);
        }
        else if(timeClicked >= 3000 && alarmToggle.getText() == "Alarm is set"){
            timeClicked = 0;
            alarmToggle.setText("Set Alarm");
            alarmToggle.setEnabled(true);
            alarmToggle.setClickable(true);
            Log.d("MainActivity", "Success");

            Intent myIntent = new Intent(AlarmActivity.this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(AlarmActivity.this, 0, myIntent, 0);

            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            pendingIntent.cancel();
            alarmManager.cancel(pendingIntent);

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
/*
    protected void onResume() {
        super.onResume();
        sm.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }
*/
    // Change (Display Alarm) Text
    public void setAlarmText(String alarmText) {
        alarmTextView.setText(alarmText);
    }

    // Average and Last time

    private void requestData(String uri) {
        MyTask task = new MyTask();
        task.execute(uri);
    }

    protected void updateDisplay(String message) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(message));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = xpp.getName();
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    System.out.println("Start tag " + xpp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    text = xpp.getText();
                    // output.append("Text " + text);
                    // System.out.println("Text " + xpp.getText());
                } else if (eventType == XmlPullParser.END_TAG) {
                    //output.append("End tag " + xpp.getName());
                    if(tagname.equalsIgnoreCase("average")){
                        val = text;
                        //Log.i("", "name is " + val);
                        average = Integer.parseInt(val);
                        Log.d("MainActivity", "average before recount: "+average+" seconds");
                        Log.d("MainActivity", "alarmEnded at: "+alarmEnded+" seconds");
                        newAverage = (average + alarmEnded) / 2;
                        Log.d("MainActivity", "average after recount: " + newAverage + " seconds");
                        if(alarmEnded == 0) {
                            //output.append(val);
                            output.setText("Average: "+val);
                        }else{
                            String newAverageString;
                            newAverageString = Long.toString(newAverage);
                            Log.d("","average was "+average);
                            average = (int)newAverage;
                            Log.d("", "average is now " + average);
                            //output.append(newAverageString);
                            output.setText("Average: "+newAverageString);
                            posting = "yes";
                            requestData("http://" + IP + "/WakeUpBackend/webresources/com.wakeupproject.account/1");
                        }
                    }else if(tagname.equalsIgnoreCase("accountId")){
                        val = text;
                    }
                }
                eventType = xpp.next();

                // output.append(message + "\n");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            updateDisplay("Starting task");

            if (tasks.size() == 0) {
                pb.setVisibility(View.VISIBLE);
            }
            tasks.add(this);
        }

        @Override
        protected String doInBackground(String... params) {

            if("no".equals(posting)) {
                String content = HttpManager.getData(params[0]);
                return content;
            }else{
                String content = HttpManager.postData(params[0]);
                return content;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            updateDisplay(result);

            tasks.remove(this);
            if (tasks.size() == 0) {
                pb.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {

            if("yes".equals(posting)){
                posting = "no";
            }else {
                updateDisplay(values[0]);
            }
        }

    }


}