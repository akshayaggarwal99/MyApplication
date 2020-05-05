package com.akshay.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private CircularProgressIndicator circularProgress;
    private EditText etProgress;
    private Button animateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circularProgress = findViewById(R.id.circular_progress);
        circularProgress.setMaxProgress(100);

        animateButton = findViewById(R.id.btn_animate);
        etProgress = findViewById(R.id.et_progress);

        animateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String progress = etProgress.getText().toString().trim();
                if(!progress.contentEquals("")){
                    int intProgress = Integer.parseInt(progress);
                    if(intProgress>100){
                        Toast.makeText(getApplicationContext(),"Please enter value between 0 and 100",Toast.LENGTH_SHORT).show();
                    }else{
                        circularProgress.setCurrentProgress(intProgress);
                    }
                }
            }
        });
    }
}
