package com.example.androidfilemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Main extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView buttonEncrypt = (TextView) findViewById(R.id.file);
        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseEncrypt();

            }
        });

        TextView buttonVault = (TextView) findViewById(R.id.vault);
        buttonVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chooseVault();
            }
        });
        TextView buttonCloud = (TextView) findViewById(R.id.cloud);
        buttonCloud.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               chooseCloud();
                                           }
                                       });


        createFolders();
        setDefaultSettings();


    }


    private void chooseEncrypt() {
        Intent i = new Intent(Main.this, Encrypt.class);
        startActivity(i);
    }

    private void chooseVault() {
        Intent i = new Intent(Main.this, Encrypt_Decrypt.class);
        startActivity(i);
    }
    private void chooseCloud() {
        Intent i = new Intent(Main.this, CloudStorage.class);
        startActivity(i);
    }

    public void createFolders() {

        File dirVault = new File(Environment.getExternalStorageDirectory().getPath() + "/Vault");
        File dirDecrypted = new File(Environment.getExternalStorageDirectory().getPath() + "/Decrypted");

        try {

            if (!dirVault.exists()) {
                if (dirVault.mkdir()) {
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot create folders, please Manually create Folder named Vault and Decrypted in your internal storage", Toast.LENGTH_SHORT).show();
                }
            }

            if (!dirDecrypted.exists()) {
                if (dirDecrypted.mkdir()) {
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot create folders, please Manually create Folder named Vault and Decrypted in your internal storage", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDefaultSettings() {


        String algoType = Settings.getDefaultAlgo(getApplicationContext());
        if (algoType.equalsIgnoreCase("empty")) {
            String PREFES = "mysettings";
            SharedPreferences preferences = getSharedPreferences(PREFES, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("type", "AES");
            editor.commit();
        }

    }

    @Override
    public void onBackPressed() {
        signOut();
    }

    private void signOut() {

        AlertDialog.Builder a_builder = new AlertDialog.Builder(Main.this);
        a_builder.setTitle("Confirm Exit?");
        a_builder.setMessage("Log out from application?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = a_builder.create();

        alert.show();
        alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), Settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
