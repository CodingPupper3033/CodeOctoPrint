package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    final String SETTINGS_FILE_NAME = "settings.json";

    SettingsJSON settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_main);


        // Set the settings file
        while (settings == null) {
            try {
                settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Determine whether we need API Key

        try {
            if (settings.getSettingsJSON().has("api_key")) {
                // TODO open app

                // TEMP Activity to feel successful
                Intent i = new Intent(MainActivity.this, test.class); // Your list's Intent
                i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(MainActivity.this, apiKeyGetter.class); // Your list's Intent
                i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                startActivity(i);
                finish();
            }
        } catch (IOException | JSONException e) {
            System.exit(0);
        }
    }
}