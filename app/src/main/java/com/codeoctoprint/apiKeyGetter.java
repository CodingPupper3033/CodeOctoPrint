package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

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

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.codeoctoprint.MainActivity.SETTINGS_FILE_NAME;

public class apiKeyGetter extends AppCompatActivity {
    SettingsJSON settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_api_key_getter);

        // Set the settings file
        while (settings == null) {
            try {
                settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClick(View v) {
        // Hide Keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // Hide WebView (It may annoy people that they see the accept button then it go poof)
        WebView myWebView = (WebView) findViewById(R.id.octoprintWebView);
        myWebView.setVisibility(View.INVISIBLE);

        // Request Queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Cleanser
        URLCleanser cleaner = new URLCleanser();

        // Get the IP
        EditText ipInputTextEditor = findViewById(R.id.IPInputEditText);

        // If the ip box is not empty
        if (!ipInputTextEditor.getText().toString().isEmpty()) {
            String url = ipInputTextEditor.getText().toString();
            url = cleaner.clean(url);

            //Probe for workflow support | GET /plugin/appkeys/probe
            String probeURL = cleaner.combineURL(url, "plugin/appkeys/probe");

            // Request a probe response from the probe URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, probeURL, new probeWorkflowResponse(), new probeWorkflowError());

            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } else {
            // Toast the user trying to tell them to set a url
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), "URL/IP Was not set", duration);
            toast.show();
        }
    }



    // Probe Workflow
    private class probeWorkflowError implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            int duration = Toast.LENGTH_LONG;
            Toast toast;

            if (error instanceof NoConnectionError) {
                toast = Toast.makeText(getApplicationContext(), "Unable to connect: Check the URL & Your internet connection", duration);
            } else if (error instanceof TimeoutError) {
                toast = Toast.makeText(getApplicationContext(), "Unable to connect: Is this an Octoprint server?", duration);
            } else if (error.networkResponse.statusCode == 404) {
                toast = Toast.makeText(getApplicationContext(), "This does not seem to be an OctoPrint server", duration);
            } else {
                // TODO I have no clue what would get us here
                toast = Toast.makeText(getApplicationContext(), "Got an error from the server: dunno man", duration);
            }
            toast.show();
        }
    }

    private class probeWorkflowResponse implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            Log.d("Probe Workflow", "Probed Successfully: " + response);

            // Ask for the API Key
            // Request Queue
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            // Cleanser
            URLCleanser cleaner = new URLCleanser();

            // Get the IP
            EditText ipInputTextEditor = findViewById(R.id.IPInputEditText);
            String url = ipInputTextEditor.getText().toString();
            url = cleaner.clean(url);

            //Start authorization process | POST /plugin/appkeys/request, (app_name, user_id)
            String authURL = cleaner.combineURL(url, "plugin/appkeys/request");

            // App Name (CodeOctoPrint)
            Map<String, String> params = new HashMap<String, String>();
            params.put("app", "CodeOctoPrint");

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, authURL, new JSONObject(params), new authResponse(), new authResponseError());

            // Add to queue
            queue.add(jsonRequest);
        }
    }



    // Auth Response
    private class authResponseError implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d("Auth Response", "onErrorResponse: " + error);
        }
    }

    private class authResponse implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            try {
                // Save Response
                JSONObject settingsJSON = settings.getSettingsJSON();
                settingsJSON.put("temp_app_token", response.get("app_token"));
                settings.setSettingsJSON(settingsJSON);

                // Cleanser
                URLCleanser cleaner = new URLCleanser();

                // Get the IP
                EditText ipInputTextEditor = findViewById(R.id.IPInputEditText);
                String url = ipInputTextEditor.getText().toString();
                url = cleaner.clean(url);

                // Show the Server (So the user can accept)
                WebView myWebView = (WebView) findViewById(R.id.octoprintWebView);
                myWebView.setWebViewClient(new WebViewClient());
                WebSettings webSettings = myWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setUseWideViewPort(true);
                myWebView.loadUrl(url);
                myWebView.setVisibility(View.VISIBLE);

                // Start Polling process
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new pollResponseTimerTask(timer), 0, 1000);

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }



    // Poll for Response
    private class pollResponseTimerTask extends TimerTask {
        private Timer timer;
        public pollResponseTimerTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            try {
                Log.d("Poll Response", "Polling ");
                JSONObject settingsJSON = settings.getSettingsJSON();
                String appToken = settingsJSON.getString("temp_app_token");

                // Request Queue
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                // Cleanser
                URLCleanser cleaner = new URLCleanser();

                // Get the IP
                EditText ipInputTextEditor = findViewById(R.id.IPInputEditText);
                String url = ipInputTextEditor.getText().toString();
                url = cleaner.clean(url);

                // Poll for decision | GET /plugin/appkeys/request/<str:app_token>
                String pollURL = cleaner.combineURL(url, "plugin/appkeys/request/");
                pollURL = cleaner.combineURL(pollURL, appToken);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, pollURL, (String)null, new pollResponse(timer), new pollResponseError(timer));

                queue.add(jsonObjectRequest);

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class pollResponseError implements Response.ErrorListener {
        private Timer timer;

        public pollResponseError(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            timer.cancel();
            timer = null;
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), "User denied access; Unable to get API Key", duration);
            toast.show();
        }
    }

    private class pollResponse implements Response.Listener<JSONObject> {
        private Timer timer;
        public pollResponse(Timer timer) {
            this.timer = timer;
        }
        @Override
        public void onResponse(JSONObject response) {
            try {
                String apiKey = response.getString("api_key");
                timer.cancel();
                timer = null;

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
                Intent i = new Intent(apiKeyGetter.this, MainActivity.class); // Your list's Intent
                i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                startActivity(i);
                finish();

            } catch (JSONException | IOException e) {
                e.printStackTrace();
                Log.d("Poll Response", "Still Awaiting Decision");
            }

        }
    }

}