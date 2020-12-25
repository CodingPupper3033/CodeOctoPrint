package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView view = findViewById(R.id.textView);

        SettingsJSON settings = null;
        String SETTINGS_FILE_NAME = "settings.json";
        while (settings == null) {
            try {
                settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONObject settingsJSON = null;
        try {
            settingsJSON = settings.getSettingsJSON();
            view.setText("We got an api key!:" + settingsJSON.getString("api_key"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}