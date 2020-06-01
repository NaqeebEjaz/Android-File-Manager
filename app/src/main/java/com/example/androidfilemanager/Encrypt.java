package com.example.androidfilemanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

public class Encrypt extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    Button btnBrowse, btnEncrypt;
    String filepath;
    EditText etFilepath;
    String password;


    private ProgressDialog progressDialog;
    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.dismiss();
            String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
            Toast.makeText(getApplicationContext(), filename + " " + "encrypted", Toast.LENGTH_LONG).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt);


        btnEncrypt = (Button) findViewById(R.id.button10);
        btnBrowse = (Button) findViewById(R.id.browse);
        etFilepath = (EditText) findViewById(R.id.etFilePath);
        final TextInputLayout textPasswordWrapper = (TextInputLayout) findViewById(R.id.etPasswordWrapperTime);
        textPasswordWrapper.setHint("Password");



        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                password = textPasswordWrapper.getEditText().getText().toString();

                if (password.isEmpty()) {
                    textPasswordWrapper.setError("Enter Password");
                    return;
                } else {
                    textPasswordWrapper.setError("");
                }

                if (filepath == null) {
                    Toast.makeText(getApplicationContext(), "Select File", Toast.LENGTH_SHORT).show();
                    return;
                }


                String vaultPath = Environment.getExternalStorageDirectory().getPath() + "/Vault/";
                String decryptedPath = Environment.getExternalStorageDirectory().getPath() + "/decrypted/";

                String encryptionType = Settings.getDefaultAlgo(getApplicationContext());
                final Encryption encryption = new Encryption(encryptionType, vaultPath, decryptedPath);

                final String pasHash = password + "#";
                progressDialog = ProgressDialog.show(Encrypt.this, "", "Encrypting");

                encrypt(encryption, pasHash);
            }
        });


        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        if (ContextCompat.checkSelfPermission(Encrypt.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Encrypt.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(Encrypt.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        // Permissions Code ENDS here.


        // File Pick Code

    }
    private void encrypt(final Encryption encryption, final String pasHash) {

        new Thread() {
            public void run() {

                try {
                    encryption.encrypt(encryption.hashGenerator(pasHash), filepath);
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

    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                FileManager fileManager = new FileManager();
                String path = fileManager.getRealPathFromURI(getApplicationContext(), uri);
                filepath = path;
                String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
                etFilepath.setText(filename);

            }
        }
    }

    //    public void updateProgress(long fileSize, long completedBytes) {
//
//        double progress = (100.0 * completedBytes) / fileSize;
//
//        //displaying percentage in progress dialog
//        progressDialog.setMessage("Completed " + ((int) progress) + "%...");
//        progressDialog.show();
//
//    }


}
