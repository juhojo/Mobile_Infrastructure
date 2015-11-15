package com.example.juhojokela.qr_reader_application;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// zxings import library
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // the button to start scanning, runs the openQRScanner method
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQRScanner();
            }
        });

        CharSequence text = "Ready for scanning..";
        Toast ready = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        ready.show();
    }

    // opens the QR scanner
    private void openQRScanner(){
        // IntentIntegrator -> provided by zxings library
        IntentIntegrator integrator = new IntentIntegrator(this);
        // only search for QR codes
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a QR code");
        // Start scan
        integrator.initiateScan();
    }

    // Get result of the QR scanner
    // Called when QR code is found on camera
    @Override
    protected void onActivityResult(int callcode, int returncode, Intent intent){
        // Get result
        IntentResult intentResult = IntentIntegrator.parseActivityResult(callcode, returncode, intent);
        String contents = intentResult.getContents();

        // Result handler
        if(isUrl(contents)) {
            // Open URL
            Intent toWebIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(contents));
            startActivity(toWebIntent);
        }else {
            // Set text
            TextView textView = (TextView) findViewById(R.id.text);
            textView.setText(contents);
        }
    }

    private boolean isUrl(String value){
        boolean event = true;
        try {
            // Checks start with "http(s)://"
            URL url = new URL(value);
            // Checks buildup of url "text.text"
            url.toURI();
        } catch (MalformedURLException e){
            event = false;
        } catch (URISyntaxException e) {
            event = false;
        }
        return event;
    }

}
