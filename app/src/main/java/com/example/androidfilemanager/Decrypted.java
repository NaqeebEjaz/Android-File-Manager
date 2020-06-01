package com.example.androidfilemanager;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.androidfilemanager.FileManager.isImage;

public class Decrypted extends AppCompatActivity{


    private ListView mListView;
    private ArrayAdapter<String> directoryList;
    private EditText editTextSearch;
    private TextView textViewVault;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decypted);
        editTextSearch = (EditText) findViewById(R.id.editSearch);
        textViewVault = (TextView) findViewById(R.id.textViewVault);

        textViewVault.setVisibility(View.GONE);


        final FileManager fileManager = new FileManager();
        //Getting file path to Vault's folder
        File path = new File(Environment.getExternalStorageDirectory().getPath() + "/Decrypted/");

        //This snippet gets the file list available from Vault's folder
        final ArrayList<String> fileList = fileManager.ListDir(path);
        mListView = (ListView) findViewById(R.id.listView);



        if (fileList.size() != 0) {

            directoryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
            mListView.setAdapter(directoryList);

            textViewVault.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getApplicationContext(), "No Files are Decrypted", Toast.LENGTH_SHORT).show();
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int j, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/Decrypted"));

                    intent.setDataAndType(uri, "*/*");
                    startActivity(Intent.createChooser(intent, "Open folder"));






            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                Decrypted.this.directoryList.getFilter().filter(cs);
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








}