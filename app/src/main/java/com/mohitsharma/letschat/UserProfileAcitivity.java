package com.mohitsharma.letschat;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohitsharma.letschat.Model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class UserProfileAcitivity extends AppCompatActivity {

    String status = "";
    String profileUrl = "default";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        Intent intent = getIntent();
        String name = intent.getStringExtra("Username");
        profileUrl = intent.getStringExtra("profileUrl");
        final String mNumber = intent.getStringExtra("mNumber");
        final String mstatus = intent.getStringExtra("s");
        ImageView toolbar_dp = findViewById(R.id.toolbar_dp);
        TextView userNumber = findViewById(R.id.userNumber);
        final TextView userStatus = findViewById(R.id.userStatus);
        assert mstatus != null;

        userNumber.setText(mNumber);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
      ValueEventListener valueEventListener = new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                  User user = snapshot1.getValue(User.class);
                  if(user.getId().equals(mNumber)){
                      status = user.getStatus();
                      userStatus.setText(status);
                  }
              }
          }
          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      };
      databaseReference.addListenerForSingleValueEvent(valueEventListener);


        if(profileUrl.equals("default")){
            toolBarLayout.setContentScrimResource(R.drawable.dp_default);
        }else{
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("dp/" + mNumber);
            Glide.with(UserProfileAcitivity.this)
                    .load(ref)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(profileUrl), getApplicationContext().getResources().getConfiguration().orientation))
                    .placeholder(R.drawable.dp_loading)
                    .into(toolbar_dp);
        }


        toolBarLayout.setTitle(name);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}