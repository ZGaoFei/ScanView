package com.zgf.scanview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zgf.scanview.view.ScanView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ScanView scanView = findViewById(R.id.scan_view);
        scanView.setOrientation(ScanView.ScanOrientation.HORIZONTAL);
        scanView.setBgColor(
                ContextCompat.getColor(this, R.color.color_FFEE4000),
                ContextCompat.getColor(this, R.color.color_CCEE4000),
                ContextCompat.getColor(this, R.color.color_88EE4000),
                ContextCompat.getColor(this, R.color.color_44EE4000),
                ContextCompat.getColor(this, R.color.color_00EE4000));
        // scanView.setBgCoe(1);
        // scanView.setDurationCoe(3f);

        Button button = findViewById(R.id.change_orientation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanView.setOrientation(ScanView.ScanOrientation.VERTICAL);
            }
        });
    }
}
