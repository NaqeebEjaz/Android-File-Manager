package com.example.androidfilemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;


public class Decrypt extends AppCompatActivity {

    private ProgressDialog progressDialog;


    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Decrypted", Toast.LENGTH_LONG).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);


        final String filename = getIntent().getStringExtra("filename");


        TextView tvFileName = (TextView) findViewById(R.id.tvFileName);
        tvFileName.setText(filename);


        Button btnDecrypt = (Button) findViewById(R.id.btnDecrypt);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);


        final TextInputLayout textPasswordWrapper = (TextInputLayout) findViewById(R.id.etPasswordWrapper);
        textPasswordWrapper.setHint("Password");




        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password = textPasswordWrapper.getEditText().getText().toString();

                if (password.isEmpty()) {
                    textPasswordWrapper.setError("Enter Password");
                } else {

                    final String filepath = "/storage/emulated/0/Vault/" + filename;


                    final String pasHash = password + "#";

                    String vaultPath = Environment.getExternalStorageDirectory().getPath() + "/Vault/";
                    String decryptedPath = Environment.getExternalStorageDirectory().getPath() + "/decrypted/";

                    Settings settingsActivity = new Settings();
                    String encryptionType = settingsActivity.getDefaultAlgo(getApplicationContext());
                    final Encryption encryption = new Encryption(encryptionType, vaultPath, decryptedPath);

                    progressDialog = ProgressDialog.show(Decrypt.this, "", "Decrypting");
                    new Thread() {
                        public void run() {
                            try {
                                encryption.decrypt(encryption.hashGenerator(pasHash), filepath);
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (NoSuchAlgorithmException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (NoSuchPaddingException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (InvalidKeyException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (InvalidAlgorithmParameterException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            messageHandler.sendEmptyMessage(0);
                        }
                    }.start();


                }
            }
        });
    }
}