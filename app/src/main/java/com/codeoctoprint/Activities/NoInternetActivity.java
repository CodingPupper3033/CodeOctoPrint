package com.codeoctoprint.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codeoctoprint.R;
import com.codeoctoprint.Useful.SettingsReader;
import com.codeoctoprint.Useful.URLCleanser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.codeoctoprint.Activities.MainActivity.SETTINGS_FILE_NAME;

public class NoInternetActivity extends AppCompatActivity {
    SettingsReader settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_no_internet);

        TextView errorView = findViewById(R.id.errorMessage);

        // No internet at all
        if (!checkIfConnectedToInternet()) errorView.setText("Cannot connect. Check your internet connection.");

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

    public void onClickRetry(View v) {
        // Open Main Activity again, basically as if the app just opened
        Intent intent = new Intent(NoInternetActivity.this, MainActivity.class); // Your list's Intent
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
        startActivity(intent);
        finish();
    }

    // TODO Host stuff
    public void onClickHost(View v) {
        Context context = this;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setCancelable(true)
                .setTitle("Host Settings")
                .setMessage("Which would you like to update:")
                .setPositiveButton("API key",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder
                                    .setCancelable(true)
                                    .setTitle("Are you sure?")
                                    .setMessage("This will delete the current API key!")
                                    .setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        JSONObject settingsJSON = settings.getSettingsJSON();
                                                        settingsJSON.remove("host");
                                                        settingsJSON.remove("api_key");
                                                        settings.setSettingsJSON(settingsJSON);
                                                        Toast.makeText(getApplicationContext(),"API Key was deleted",Toast.LENGTH_SHORT).show();

                                                        Intent intent = new Intent(NoInternetActivity.this, MainActivity.class); // Your list's Intent
                                                        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
                                                        startActivity(intent);
                                                        finish();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            })
                                    .setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                    .setNeutralButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                    .show();
                        }
                    })
                .setNegativeButton("host",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                JSONObject settingsJSON = settings.getSettingsJSON();

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Change host");

                                // Set up the input
                                final EditText input = new EditText(context);
                                input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                                input.setText(settingsJSON.getString("host"));
                                builder.setView(input);

                                // Set up the buttons
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            // URL
                                            URLCleanser cleaner = new URLCleanser();
                                            String url = input.getText().toString();
                                            url = cleaner.clean(url);

                                            // Is url empty?
                                            if (!input.getText().toString().isEmpty()) {
                                                settingsJSON.put("host",url);
                                                settings.setSettingsJSON(settingsJSON);
                                                Toast.makeText(getApplicationContext(),"Host was updated",Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),"Host was not set. Nothing changed",Toast.LENGTH_SHORT).show();
                                            }


                                            Intent intent = new Intent(NoInternetActivity.this, MainActivity.class); // Your list's Intent
                                            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
                                            startActivity(intent);
                                            finish();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    })
                .setNeutralButton("cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                .show();

    }

    public boolean checkIfConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
        return connected;
    }
}