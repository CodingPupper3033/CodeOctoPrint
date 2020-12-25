package com.codeoctoprint;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SettingsJSON {
    final String TAG = "Settings";

    File file;
    JSONObject settingJSON;

    public SettingsJSON(File file) throws IOException {
        this.file = file;

        if (file.exists()) {
            Log.d(TAG, "File exists");
        } else {
            Log.d(TAG, "File does not exist : Creating");
            file.createNewFile();
        }
    }

    public SettingsJSON(String filePath) throws IOException {
        this.file = new File(filePath);

        if (file.exists()) {
            Log.d(TAG, "File exists");
        } else {
            Log.d(TAG, "File does not exist : Creating");
            file.createNewFile();
        }
    }

    public SettingsJSON(File filePath, String fileName) throws IOException {
        this.file = new File(filePath, fileName);

        if (file.exists()) {
            Log.d(TAG, "File exists");
        } else {
            Log.d(TAG, "File does not exist : Creating");
            file.createNewFile();
        }
    }

    public JSONObject getSettingsJSON() throws IOException, JSONException {
        Scanner myReader = new Scanner(file);
        if (myReader.hasNextLine()) {
            String fileString = myReader.nextLine();
            //Log.d(TAG, "File contains " + fileString);
            return new JSONObject(fileString);
        } else {
            Log.d(TAG, "File is empty");
            setSettingsJSON(new JSONObject("{}"));
            return new JSONObject("{}");
        }

    }

    public void setSettingsJSON(JSONObject jsonObject) throws IOException {
        String stringJSONObject = jsonObject.toString();
        FileWriter writer = new FileWriter(file);
        writer.write(stringJSONObject);
        writer.close();
    }

}
