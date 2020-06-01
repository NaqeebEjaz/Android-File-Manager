package com.example.androidfilemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {


    Button buttonSave;
    private RadioGroup radioGroup;
    private RadioButton radioButtonAES, radioButtonBLOWFISH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButtonAES = (RadioButton) findViewById(R.id.radioButtonAES);
        radioButtonBLOWFISH = (RadioButton) findViewById(R.id.radioButtonBLOWFISH);

        setCurrentSettings();


        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    public void setCurrentSettings() {

        String currentAlgo = getDefaultAlgo(getApplicationContext());
        switch (currentAlgo) {

            case "AES":
                radioButtonAES.setChecked(true);
                break;
            case "BLOWFISH":
                radioButtonBLOWFISH.setChecked(true);
                break;
            default:
                break;
        }
    }

    public void saveSettings() {

        String PREFES = "mysettings";
        RadioButton radioButtonTemp;
        if (radioGroup.getCheckedRadioButtonId() == -1)
        {
            Toast.makeText(getApplicationContext(),"Please select Algorithm type", Toast.LENGTH_SHORT).show();
            return;
        } else {
            int type = radioGroup.getCheckedRadioButtonId();
            radioButtonTemp = (RadioButton) findViewById(type);
            String value = radioButtonTemp.getText().toString();

            //Saves current Encryption Type Settings
            SharedPreferences preferences = getSharedPreferences(PREFES, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("type", value);
            editor.commit();

            Toast.makeText(Settings.this, value + " selected " + "Please make sure to decrypt files with the same encryption type you encrypted it", Toast.LENGTH_LONG).show();

        }
    }

    //This method return current algorithm type
    public static String getDefaultAlgo(Context context) {
        String PREFES = "mysettings";
        SharedPreferences preferences = context.getSharedPreferences(PREFES, 0);
        if (preferences.getString("type", "empty").equalsIgnoreCase("empty")) {
            return "empty";
        } else {
            return preferences.getString("type", "");
        }
    }
}
