package com.example.arpit.redcarpet2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    public static final int PERMISSIONS = 1;
    List<String[]> contact = new ArrayList<String[]>();
    private Button btn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);

        permissionsRequest();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(permissionsRequest()) {


                    Observable.fromCallable(new Callable<String>() {

                        @Override
                        public String call() throws Exception {
                            return contacts();
                        }
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> Snackbar.make(view, res+" Contact Zipped", Snackbar.LENGTH_LONG).show());

                }
                else
                    Toast.makeText(getApplicationContext() , "Require  permissions to CONTACT and STORAGE..", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private  boolean permissionsRequest() {
        int permissionMsg = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        int location_Permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> list = new ArrayList<>();
        if (location_Permission != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionMsg != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.READ_CONTACTS);
        }
        if (!list.isEmpty()) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[list.size()]),PERMISSIONS);
            return false;
        }
        return true;
    }

        public String contacts(){

            ContentResolver reslvr = getContentResolver();
            Cursor cursor = reslvr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            while (cursor.moveToNext()) {

                String str1[] = new String[2];

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor phoneCursor = reslvr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                str1[0] = name;
                int cnt = 0;
                String str = "";
                while (phoneCursor.moveToNext()) {
                    String phoneNum = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (cnt > 0)
                        str += ", " + phoneNum;
                    else
                        str = phoneNum;
                    cnt++;
                }

                str1[1] = str;
                contact.add(str1);
            }

            return make_CSV();
    }


    private String make_CSV(){

        String direct = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        File storageDir = new File(direct, "DataFiles");
        storageDir.mkdir();
        String file_Name = "Contacts.csv";
        String filePath = storageDir.getPath() + File.separator + file_Name;
        File f = new File(filePath);

        if(f.exists()){
            try {
                FileWriter mFileWriter = new FileWriter(filePath, true);
                CSVWriter writer = new CSVWriter(mFileWriter);
                writer.writeAll(contact);
                writer.close();

            }catch(Exception e){
                Log.i("ERROR", e.getMessage());
            }
        }
        else{
            try {
                CSVWriter writer = new CSVWriter(new FileWriter(filePath));
                writer.writeAll(contact);
                writer.close();
            }catch(Exception e){
                Log.i("ERROR", e.getMessage()+"xxxxx");
            }
        }

        ZipConverter.zip(storageDir.getPath()+File.separator, storageDir.getPath()+File.separator, "Contacts.zip", false);

        return storageDir.getPath();
    }



}
