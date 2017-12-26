package com.example.charlie.test12_26;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StopActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);
        Button button = (Button) findViewById(R.id.stop);
    }

    public void Stop(View view){
        Intent stop_cook = new Intent();
        stop_cook.setClass(StopActivity.this, MainActivity.class);
        StopActivity.this.startActivity(stop_cook);

    }

}