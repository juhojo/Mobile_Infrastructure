package com.example.juho.proximitysensortest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    TextView proxText;
    SensorManager sm;
    Sensor proxSensor;
    Date pressedTime;
    long timeClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        proxSensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxText = (TextView)findViewById(R.id.proximityTextView);

        sm.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        proxText.setText(String.valueOf(event.values[0]));

        if(event.values[0] < event.sensor.getMaximumRange()) {
            pressedTime = new Date();
        }else{
            timeClicked = new Date().getTime() - pressedTime.getTime();
        }

        if (timeClicked >= 3000){
            Log.d("MainActivity", "Success");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
