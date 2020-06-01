package com.example.androidfilemanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.PropertyPermission;
import java.util.UUID;


public class CloudStorage extends AppCompatActivity {

    int pStatus = 0;
    private Handler handler = new Handler();
    TextView tv;

    File localFile = null;
    TextView textViewCloud;
    private ListView mListView;
    private ArrayAdapter<String> fileAdapter;
    private DatabaseReference databaseReference;
    private ArrayList<String> fileList;
    private FirebaseUser user;
    private EditText editTextSearch;

    private static final String TAG = CloudStorage.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);

        editTextSearch = (EditText) findViewById(R.id.editSearch);

        String gmail = null;

        mListView = (ListView) findViewById(R.id.listView);
        textViewCloud = (TextView) findViewById(R.id.textViewCloud);


        textViewCloud.setVisibility(View.GONE);


        if (isOnline()) {

            Toast.makeText(getApplicationContext(), "Please wait, fetching files list from cloud storage", Toast.LENGTH_SHORT).show();

            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference.child(currentFirebaseUser.getUid()).orderByChild("currentEmail").equalTo(gmail);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    fileList = new ArrayList<String>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        fileList.add(String.valueOf(snapshot.child("name").getValue())); //add result into array list

                    }
                    refreshView();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "No internet, please try again", Toast.LENGTH_LONG).show();
        }



        editTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                CloudStorage.this.fileAdapter.getFilter().filter(cs);
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
















    private void refreshList() {

        fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
        mListView.setAdapter(fileAdapter);
    }

    private void refreshView() {

        if (fileList.size() != 0) {
            fileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
            mListView.setAdapter(fileAdapter);
            textViewCloud.setVisibility(View.VISIBLE);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onItemClick(final AdapterView<?> adapterView, final View view, final int j, long l) {


                    final AlertDialog.Builder builder = new AlertDialog.Builder(CloudStorage.this);
                    builder.setCancelable(true);



                    // Set the alert dialog title
                    builder.setTitle("Choose");
                    // Initializing an array of choices
                    final String[] cloud_options = new String[]{
                            "Download File","Share File","Delete File"
                    };
                    // Set a single choice items list for alert dialog
                    builder.setSingleChoiceItems(
                            cloud_options, // Items list
                            -1, // Index of checked item (-1 = no selection)
                            new DialogInterface.OnClickListener() // Item click listener
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Get the alert dialog selected item's text
                                    String selectedItem = Arrays.asList(cloud_options).get(i);

                                    // Display the selected item's text
                                    if (selectedItem.equalsIgnoreCase("Download File")) {
                                        String filename = (String) adapterView.getItemAtPosition(j);
                                        if (isOnline()) {
                                            downloadFile(filename);
                                            dialogInterface.dismiss();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No Internet, Please try with an active Internet", Toast.LENGTH_LONG).show();

                                        }





                                    }
                                    if (selectedItem.equalsIgnoreCase("Share File")) {
                                        String filename = (String) adapterView.getItemAtPosition(j);

                                            ShareUrl(filename);






                                    }




                                    else if (selectedItem.equalsIgnoreCase("Delete File")) {
                                        final String filename = (String) adapterView.getItemAtPosition(j);

                                        if (isOnline()) {

                                            AlertDialog.Builder a_builder = new AlertDialog.Builder(CloudStorage.this);
                                            a_builder.setTitle("Confirm Delete?");
                                            a_builder.setMessage("File once deleted cannot be recoverd")
                                                    .setCancelable(true)
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            DeleteFile(filename);
                                                            refreshList();
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
                                            dialogInterface.dismiss();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No Internet, Please try with an active Internet", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                }


                            });

                    // Create the alert dialog
                    builder.create();
                    // Finally, display the alnert dialog
                    builder.show();

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No Files in Cloud ", Toast.LENGTH_SHORT).show();

        }
    }

    private void ShareUrl(String filename) {
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        StorageReference storageReferencess = FirebaseStorage.getInstance().getReference();
        storageReferencess.child(currentFirebaseUser.getUid()+"/"+filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Log.d(TAG,"onSuccess: download url:"+uri.toString());
                Toast.makeText(CloudStorage.this,"Url recived",Toast.LENGTH_SHORT).show();

                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                // Add data to the intent, the receiving app will decide
                // what to do with it.

                share.putExtra(Intent.EXTRA_SUBJECT, "Title Of The Post");
                share.putExtra(Intent.EXTRA_TEXT, uri.toString());

                startActivity(Intent.createChooser(share, "Share link!"));
                // Got the download URL for 'users/me/profile.png'
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.d(TAG,"onFailure: Error: "+exception.getMessage());
                Toast.makeText(CloudStorage.this,"Url Not recived",Toast.LENGTH_SHORT).show();

                // Handle any errors
            }
        });
    }







    private void DeleteFile(final String filename) {

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference deleteStoragePathReference = storageReference.child(currentFirebaseUser.getUid() + "/"+ filename);

        final ProgressDialog progressDialog = new ProgressDialog(CloudStorage.this);
        progressDialog.setTitle("Deleting");
        progressDialog.setCancelable(true);
        progressDialog.show();
        progressDialog.setMessage("Deleting");

        deleteStoragePathReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                progressDialog.dismiss();
                deleteFromRealTimeDatabase(filename);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Cannot Delete", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void deleteFromRealTimeDatabase(final String filename) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("Vault").orderByChild("name").equalTo(filename);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    dataSnapshot1.getRef().removeValue();
                }


                Toast.makeText(getApplicationContext(), filename + " deleted", Toast.LENGTH_SHORT).show();
                fileList.remove(filename);
                refreshList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(), "Cannot Delete " + filename, Toast.LENGTH_SHORT).show();


            }
        });


    }

    @SuppressLint("ResourceType")
    private void downloadFile(String filename) {


        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        StorageReference downloadPathReference = storageReference.child(currentFirebaseUser.getUid() + "/"+ filename);
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1);
        String[] fileparts = filename.split("\\.");
        String filanemWithoutExtension = fileparts[0];

        try {
            File filePath = new File(Environment.getExternalStorageDirectory().getPath() + "/Vault/");
            localFile = File.createTempFile(filanemWithoutExtension, "." + fileExtension, filePath);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

        }
       final ProgressDialog progressDialog =new ProgressDialog(CloudStorage.this);

        progressDialog.getWindow().setBackgroundDrawable(new
                ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);




        progressDialog.setContentView(R.layout.activity_prograss);
        progressDialog.setTitle("Downloading");
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.show();




        downloadPathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {


                progressDialog.dismiss();
                try {
                    localFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Downloaded Successfull", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                progressDialog.dismiss();
                // Handle any errors
                Toast.makeText(getApplicationContext(), "Cannot Download", Toast.LENGTH_LONG).show();

            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                //displaying percentage in progress dialog
                progressDialog.setMessage("Downloading " + ((int) progress) + "%...");
            }
        });
    }



    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }



}