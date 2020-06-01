package com.example.androidfilemanager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Encrypt_Decrypt extends AppCompatActivity {
    TextView Encrypted;
    TextView Decrypt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_dcrypt);

        Encrypted = (TextView) findViewById(R.id.Encryptedfile);
        Encrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseEncrypted();

            }

            private void chooseEncrypted() {

                Intent i = new Intent(Encrypt_Decrypt.this, FilesActivity.class);
                startActivity(i);
            }
        });

        Decrypt = (TextView) findViewById(R.id.Decryptedfiles);
        Decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDecrypted();

            }

            private void chooseDecrypted() {
                Intent j = new Intent(Encrypt_Decrypt.this, Decrypted.class);
                startActivity(j);
            }
        });








    }
}