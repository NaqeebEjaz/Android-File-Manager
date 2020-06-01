package com.example.androidfilemanager;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;

import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class FilesActivity extends AppCompatActivity {

    AlertDialog dialog;
    private ListView mListView;
    private ArrayAdapter<String> directoryList;
    private DatabaseReference mDatabase;
    private TextView textViewVault;
    private EditText editTextSearch;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        editTextSearch = (EditText) findViewById(R.id.editSearch);
        textViewVault = (TextView) findViewById(R.id.textViewVault);
        textViewVault.setVisibility(View.GONE);


        FileManager fileManager = new FileManager();
        //Getting file path to Vault's folder
        File path = new File(Environment.getExternalStorageDirectory().getPath() + "/Vault/");

        //This snippet gets the file list available from Vault's folder
        ArrayList<String> fileList = fileManager.ListDir(path);
        mListView = (ListView) findViewById(R.id.listView);


        if (fileList.size() != 0) {

            directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
            mListView.setAdapter(directoryList);
            textViewVault.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getApplicationContext(), "No files in Vault", Toast.LENGTH_SHORT).show();
        }











        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int j, long l) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(FilesActivity.this);

                // Set the alert dialog title
                builder.setTitle("Choose");

                // Initializing an array of choice
                final String[] vault_options = new String[]{
                        "Decrypt file",
                        "Upload File",
                        "Delete File",
                };

                // Set a single choice items list for alert dialog
                builder.setSingleChoiceItems(
                        vault_options, // Items list
                        -1, // Index of checked item (-1 = no selection)
                        new DialogInterface.OnClickListener() // Item click listener
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Get the alert dialog selected item's text
                                String selectedItem = Arrays.asList(vault_options).get(i);

                                // Display the selected item's text on snack bar
                                if (selectedItem.equalsIgnoreCase("Decrypt File")) {

                                    String filename = (String) adapterView.getItemAtPosition(j);
                                    decryptFile(filename);
                                    filename = null;

                                } else if (selectedItem.equalsIgnoreCase("Upload File")) {
                                    String filename = (String) adapterView.getItemAtPosition(j);
                                    if (isOnline()) {
                                        uploadFile(filename);
                                        filename = null;
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No Internet, Please try with an active Internet", Toast.LENGTH_LONG).show();

                                    }

                                } else if (selectedItem.equalsIgnoreCase("Delete File")) {

                                    String filename = (String) adapterView.getItemAtPosition(j);
                                    DeleteFile(filename);
                                    filename = null;


                                }
                            }
                        });

                // Create the alert dialog
                dialog = builder.create();
                // Finally, display the alnert dialog
                dialog.show();
            }
        });


        editTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                FilesActivity.this.directoryList.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }




    public void btnSpeech(View view) {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "HI speak something");
        try {
            startActivityForResult(intent, 1);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editTextSearch.setText(result.get(0));

                }
                break;
        }


    }










        private void DeleteFile(final String filename) {

        dialog.dismiss();
        AlertDialog.Builder a_builder = new AlertDialog.Builder(FilesActivity.this);
        a_builder.setTitle("Confirm Delete?");
        a_builder.setMessage("File once deleted cannot be recoverd")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String filePath = Environment.getExternalStorageDirectory().getPath() + "/Vault/" + filename;
                        File file = new File(filePath);
                        if (file.delete()) {
                            Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                            refreshList();

                        } else {
                            Toast.makeText(getApplicationContext(), "Cannot delete", Toast.LENGTH_SHORT).show();

                        }


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

    private void decryptFile(String filename) {

        dialog.dismiss();
        Intent i = new Intent(FilesActivity.this, Decrypt.class);
        i.putExtra("filename", filename);
        startActivity(i);


    }



    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void refreshList() {

        FileManager fileManager = new FileManager();
        //Getting file path to Vault's folder
        File path = new File(Environment.getExternalStorageDirectory().getPath() + "/Vault/");

        //This snippet gets the file list available from Vault's folder
        ArrayList<String> fileList = fileManager.ListDir(path);

        directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        mListView.setAdapter(directoryList);
    }


    private void uploadFile(final String filename) {
        dialog.dismiss();

        AlertDialog.Builder a_builder = new AlertDialog.Builder(FilesActivity.this);
        a_builder.setMessage("Do you want to upload this file to cloud storage?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(FilesActivity.this );

                        Uri fileUri = Uri.fromFile(new File("/storage/emulated/0/Vault/" + filename));


                        if (filename != null) {
                            final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

                            String DATABASE_PATH_UPLOADS = currentFirebaseUser.getUid();
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                            mDatabase = FirebaseDatabase.getInstance().getReference(DATABASE_PATH_UPLOADS);


                            StorageReference uploading = storageReference.child(currentFirebaseUser.getUid() + "/"+ filename);
                            //displaying a progress dialog while upload is going on
                            progressDialog.setTitle("Uploading");
                            progressDialog.show();
                            progressDialog.setCancelable(true);


                            uploading.putFile(fileUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            Uploading upload = new Uploading(filename,currentFirebaseUser.getUid());
                                            String uploadId = mDatabase.push().getKey();
                                            mDatabase.child(uploadId).setValue(upload);



                                            //and displaying a success toast
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "" + filename + " uploaded ", Toast.LENGTH_LONG).show();


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            //if the upload is not successfull
                                            //hiding the progress dialog
                                            progressDialog.dismiss();
                                            //and displaying error message
                                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            //calculating progress percentage
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                            //displaying percentage in progress dialog
                                            progressDialog.setMessage("Uploading " + ((int) progress) + "%...");
                                        }
                                    });
                        }
                        //if there is not any file
                        else {

                            Toast.makeText(getApplicationContext(), "No file Found", Toast.LENGTH_LONG).show();

                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = a_builder.create();
        alert.setTitle("Confirm to Upload");
        alert.show();
        alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
    }
}
