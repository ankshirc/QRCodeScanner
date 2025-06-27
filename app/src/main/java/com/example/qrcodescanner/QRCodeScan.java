package com.example.qrcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class QRCodeScan extends AppCompatActivity {

    Button scanbtn, uploadbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);


        scanbtn = (Button) findViewById(R.id.scanbtn);
        uploadbtn = (Button) findViewById(R.id.uploadBtn);

        scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QRCodeScan.this, QRCodeScanner.class);
                startActivity(intent);
            }
        });

        uploadbtn.setOnClickListener(view -> {
            Intent intent = new Intent(QRCodeScan.this, QRCodeScanner.class);
            intent.putExtra("action", "upload");
            startActivity(intent);
        });
    }
}