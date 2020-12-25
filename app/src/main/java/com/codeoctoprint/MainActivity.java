package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    final String SETTINGS_FILE_NAME = "settings.json";

    SettingsJSON settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the settings file
        try {
            settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Determine whether we need API Key

        try {
            if (settings.getSettingsFile().has("api_key")) {
                // TODO open app
            } else {
                // TODO Open api key place thing
                //Intent i = new Intent(MainActivity.this, apiKeyGetter.class); // Your list's Intent
                //i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                //startActivity(i);

            }
        } catch (IOException | JSONException e) {
            System.exit(0);
        }
    }
}