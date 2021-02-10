package com.codeoctoprint.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.codeoctoprint.R;
import com.codeoctoprint.Useful.SettingsReader;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    final String SETTINGS_FILE_NAME = "settings.json";

    SettingsReader settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set the settings file
        while (settings == null) {
            try {
                settings = new SettingsReader(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}