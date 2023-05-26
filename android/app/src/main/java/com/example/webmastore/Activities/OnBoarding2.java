package com.example.webmastore.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.webmastore.R;

public class OnBoarding2 extends AppCompatActivity {
    private RelativeLayout layout;

    private Button btnNext;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding2);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        layout = findViewById(R.id.boardinglayout);
        btnNext = findViewById(R.id.next);

        layout.setOnTouchListener(new OnSwipeTouchListener(OnBoarding2.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                //Toast.makeText(OnBoarding1.this, "Swipe Left gesture detected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OnBoarding2.this, OnBoarding3.class));
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                //Toast.makeText(OnBoarding1.this, "Swipe Right gesture detected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OnBoarding2.this, OnBoarding1.class));

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(OnBoarding2.this, OnBoarding3.class));
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}