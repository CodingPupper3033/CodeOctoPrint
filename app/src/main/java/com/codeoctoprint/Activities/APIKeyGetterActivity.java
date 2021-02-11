package com.codeoctoprint.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.codeoctoprint.APIConnections.APIKey.APIKeyGetterListener;
import com.codeoctoprint.APIConnections.APIKey.APIRequestAPIKey;
import com.codeoctoprint.R;
import com.codeoctoprint.Useful.SettingsReader;
import com.codeoctoprint.Useful.URLCleanser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.codeoctoprint.Activities.MainActivity.SETTINGS_FILE_NAME;

public class APIKeyGetterActivity extends AppCompatActivity {
    SettingsReader settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar; I don't like it
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}

        // Show this
        setContentView(R.layout.activity_api_key_getter);

        // Set the settings file, we kind of need it so keep trying until we get it
        while (settings == null) {
            try {
                settings = new SettingsReader(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                // How dare it fail
                e.printStackTrace();
            }
        }
    }

    /** On click of the request api key button
     * @param v The view responsible for button press
     */
    public void onClick(View v) {
        // Hide Keyboard (pain in the bum)
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // Hide WebView (It may annoy people that they see the accept button then it go poof, this doesn't seem to work but oh well)
        WebView myWebView = findViewById(R.id.octoprintWebView);
        // TODO Try fixing it so it doesn't show when it updates
        myWebView.setVisibility(View.INVISIBLE);

        // Request Queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Cleanser
        URLCleanser cleaner = new URLCleanser();

        // Get the IP
        EditText ipInputTextEditor = findViewById(R.id.IPInputEditText);

        // If the ip box is not empty
        if (!ipInputTextEditor.getText().toString().isEmpty()) {
            try {
                JSONObject settingsJSON = settings.getSettingsJSON();
                // Host
                settingsJSON.put("host", ipInputTextEditor.getText().toString());

                // Save
                settings.setSettingsJSON(settingsJSON);

                APIRequestAPIKey apiKeyGetter = new APIRequestAPIKey(getApplicationContext(), settings);
                apiKeyGetter.addKeyGetterListener(new APIKeyGetterListener() {
                    @Override
                    public void onObtainKey(String apiKey) {
                        try {
                            JSONObject settingsJSON = settings.getSettingsJSON();

                            // Key
                            settingsJSON.put("api_key", apiKey);

                            // Host
                            EditText ipInputTextEditor = findViewById(R.id.IPInputEditText);
                            settingsJSON.put("host", ipInputTextEditor.getText().toString());

                            // Save
                            settings.setSettingsJSON(settingsJSON);

                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(getApplicationContext(), "Obtained API Key", duration);
                            toast.show();

                            // Get to Activity Control activity
                            Intent i = new Intent(APIKeyGetterActivity.this, MainActivity.class); // Your list's Intent
                            i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                            startActivity(i);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDeniedKey(VolleyError error) {
                        WebView myWebView = findViewById(R.id.octoprintWebView);
                        myWebView.setVisibility(View.INVISIBLE);
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(getApplicationContext(), "User denied access; Unable to get API Key", duration);
                        toast.show();
                    }

                    @Override
                    public void onSuccessfulProbe(String host) {
                        Log.d("TAG", "onSuccessfulProbe: ");
                        WebView myWebView = findViewById(R.id.octoprintWebView);
                        myWebView.setWebViewClient(new WebViewClient());
                        WebSettings webSettings = myWebView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setLoadWithOverviewMode(true);
                        webSettings.setUseWideViewPort(true);
                        myWebView.loadUrl(host);
                    }

                    @Override
                    public void onSuccessfulAuth(String host) {
                        Log.d("TAG", "onSuccessfulAuth: ");
                        // Show the Server (So the user can accept)

                        myWebView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPoll(JSONObject response) {

                    }

                    @Override
                    public void onError(VolleyError error) {
                        WebView myWebView = findViewById(R.id.octoprintWebView);
                        myWebView.setVisibility(View.INVISIBLE);
                        Log.d("TAG", "onError: " + error.getMessage());
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(getApplicationContext(), "An Error Occurred, please try again", duration);
                        toast.show();
                    }
                });
                apiKeyGetter.obtainAPIKey();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Toast the user trying to tell them to set a url
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), "URL/IP Was not set", duration);
            toast.show();
        }
    }
}