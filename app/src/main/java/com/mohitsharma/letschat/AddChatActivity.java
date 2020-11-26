package com.mohitsharma.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mohitsharma.letschat.Adapters.ContactsAdapter;
import com.mohitsharma.letschat.Model.Contact;
import com.mohitsharma.letschat.Model.User;
import com.mohitsharma.letschat.ui.MyAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;


public class AddChatActivity extends AppCompatActivity {
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    ArrayList<Contact> contactArrayList;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    RecyclerView recyclerView;
    ContactsAdapter contactsAdapter;
    ArrayList<String> userId;
    HashMap<String,String> profileUrl;
    HashMap<String,String> userStatus;
    boolean shdRemove = false;
    ProgressBar progressBar;
   MenuItem searchMenu;
    String userNo ="";
    String formatNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);
          progressBar=findViewById(R.id.pBar);
        contactArrayList = new ArrayList<>();
        try {
            requestContactPermission();
        }catch (Exception e){
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.contacts_rec_view);
        contactsAdapter = new ContactsAdapter(AddChatActivity.this,contactArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddChatActivity.this));
        recyclerView.setAdapter(contactsAdapter);
        userId = new ArrayList<>();
        profileUrl  = new HashMap<>();
        userStatus = new HashMap<>();

        //Toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar_contact);
        toolbar.setTitle("Contact");
        toolbar.setBackgroundColor(getResources().getColor(R.color.darkBlue));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //getting contacts from database
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//         databaseReference = FirebaseDatabase.getInstance().getReference("Users");
//        valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
//                    User user = snapshot1.getValue(User.class);
//                    assert user != null;
//                    userId.add(user.getId());
//
//                    assert firebaseUser != null;
//                    if(!user.getId().equals(firebaseUser.getPhoneNumber())) {
//                         userNo = user.getId();
//                    }
//                    Log.d("UserId",user.getId());
//                    userStatus.put(user.getId(),user.getStatus());
//                    profileUrl.put(user.getId(),user.getProfileUrl());
//                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
//                    assert phones != null;
//                    while (phones.moveToNext())
//                    {
//                        String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//
//                        String formatNo = FormatNo(phoneNumber);
//                        if(userNo.equals(formatNo) && !phoneNumber.equals(lastNum)) {
//                            lastNum=formatNo;
//                            Contact con = new Contact((formatNo), name, profileUrl.get(lastNum), userStatus.get(lastNum));
//                            if(!contactArrayList.contains(con)) {
//                                Log.i("COntacts", "Phone Number: " + phoneNumber);
//                                Log.i("COntacts", "contact NO " + con.getId());
//                                contactArrayList.add(con);
//                                contactsAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    }
//                    phones.close();
//
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        };
//        databaseReference.addValueEventListener(valueEventListener);



    }

    // onCreate ENDS HERE..

    public void getContacts(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    assert user != null;
                    userId.add(user.getId());
                    assert firebaseUser != null;
                    if(!user.getId().equals(firebaseUser.getPhoneNumber())) {
                        userNo = user.getId();
                         formatNo = FormatNo(userNo);
                    Log.d("UserId",user.getId());
                    userStatus.put(user.getId(),user.getStatus());
                    profileUrl.put(user.getId(),user.getProfileUrl());

                    String name0 = getContactName(formatNo,getApplicationContext());
                    if(!name0.isEmpty()){
                    Contact con = new Contact((formatNo), name0, profileUrl.get(formatNo), userStatus.get(formatNo));
                    if(!contactArrayList.contains(con)) {
                        Log.d("UserId", name0);
                        contactArrayList.add(con);
                        ContactsAdapter.contactsFull.add(con);

                        Log.d("con", con.toString());
                        contactsAdapter.notifyDataSetChanged();
                        }
                      }
                    }
               }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseReference.addValueEventListener(valueEventListener);

        shdRemove=true;
    }


    public void requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_CONTACTS)) {
                requestPermissions(
                        new String[]
                                {android.Manifest.permission.READ_CONTACTS}
                        , PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            try {
                getContacts();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        getContacts();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
                }
               return;
            }
        }
    }

    public String FormatNo(String n){
        if(n.substring(0,3).equals("+91")){
            return n;
        }else if(n.substring(0,2).equals("91") && n.length()==12){
          return "+" + n;
        }else {
            return "+91"+n;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if(shdRemove){
                databaseReference.removeEventListener(valueEventListener);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_chat_menu, menu);
        searchMenu = menu.getItem(0);
        searchMenu.setEnabled(true);
        final androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSubmitButtonEnabled(false);

        searchView.setQueryHint("Search Contact..");
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                Log.d("arrayList",contactArrayList.toString());
                contactsAdapter.getFilter().filter(newText);
                return false;
            }
        });


        return true;
    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

}