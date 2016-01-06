package com.example.jussi.myalarm;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class TestActivity extends Activity {

    TextView output;
    ProgressBar pb;
    List<MyTask> tasks;

    private String text;
    private CharSequence userAnswer;
    private String usersAnswer;
    private TextView answerTextView;
    String val = null;
    String answer = null;
    String canceli = null;

    //IP's
    String IP = "192.168.43.146:8080";
    //String IP = "10.0.3.2:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//		Initialize the TextView for vertical scrolling
        output = (TextView) findViewById(R.id.questionView);
        output.setMovementMethod(new ScrollingMovementMethod());

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        tasks = new ArrayList<>();

        int n = (int)(Math.random() * 50 + 1);

        //if(isOnline()) {
            if (n <= 25) {
                requestData("http://"+IP+"/WakeUpBackend/webresources/com.wakeupproject.question/1");
            } else {
                requestData("http://"+IP+"/WakeUpBackend/webresources/com.wakeupproject.question/2");
            }
        /*
        }else{
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
        */
        //
        final Button button = (Button) findViewById(R.id.sendAnswer);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                answerTextView = (TextView) findViewById(R.id.answerField);
                userAnswer = answerTextView.getText();
                usersAnswer = userAnswer.toString();
                compareAnswer(answer, val, usersAnswer);
               //Log.i("", userAnswer.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_get_data) {
            if (isOnline()) {
                requestData("http://10.0.3.2:8080/WakeUpBackend/webresources/com.wakeupproject.question/1");
            } else {
                Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
*/

    private void requestData(String uri) {
        MyTask task = new MyTask();
        task.execute(uri);
    }

    protected void getAnswer(String val){
        Log.i("", val);

        if("1".equals(val)) {
            Log.i("", "Hesa");
            requestData("http://"+IP+"/WakeUpBackend/webresources/com.wakeupproject.answer/1");
        }else if("2".equals(val)){
            Log.i("", "Stokis");
            requestData("http://"+IP+"/WakeUpBackend/webresources/com.wakeupproject.answer/2");
        }else{
            Log.i("","Error");
        }
    }

    protected void compareAnswer(String answer, String val, String usersAnswer){
        if(usersAnswer.equals("Helsinki") && answer.equals("Helsinki") && val.equals("1")) {
            canceli = "cancel";

            Intent intent = new Intent(TestActivity.this, AlarmActivity.class);
            intent.putExtra("canceli", "cancel");
            startActivity(intent);

            Log.i("", "Toimi: " + usersAnswer + ", " + answer + ", " + val + " " + canceli);
        }else if(usersAnswer.equals("Stockholm") && answer.equals("Stockholm") && val.equals("2")){
            canceli = "cancel";

            Intent intent = new Intent(TestActivity.this, AlarmActivity.class);
            intent.putExtra("canceli", "cancel");
            startActivity(intent);

            Log.i("", "Toimi: " + usersAnswer + ", " + answer + ", " + val + " " + canceli);
        }else{
            Toast.makeText(TestActivity.this, "Wrong answer!", Toast.LENGTH_SHORT).show();
            Log.i("","EI toiminut: "+ usersAnswer + ", " + answer + ", " + val  + " " + canceli);
        }
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
                    if(tagname.equalsIgnoreCase("questionText")){
                        val = text;
                        Log.i("", "name is " + val);
                        output.append(val);
                    }else if(tagname.equalsIgnoreCase("questionId")){
                        val = text;
                        getAnswer(val);
                    }/*else if(tagname.equalsIgnoreCase("answerId")){
                        answer = text;
                        compareAnswer(answer);
                    }*/else if(tagname.equalsIgnoreCase("answerText")){
                        answer = text;
                        if("Helsinki".equals(answer)){
                            val = "1";
                        }else{
                            val = "2";
                        }
                        compareAnswer(answer, val, usersAnswer);
                    }
                }
                eventType = xpp.next();

               // output.append(message + "\n");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
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

            String content = HttpManager.getData(params[0]);
            return content;
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

            updateDisplay(values[0]);
        }

    }

}
