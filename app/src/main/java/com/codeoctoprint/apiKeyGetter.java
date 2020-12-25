package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class apiKeyGetter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_api_key_getter);
    }
}