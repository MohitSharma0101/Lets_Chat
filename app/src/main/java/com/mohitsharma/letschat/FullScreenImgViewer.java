package com.mohitsharma.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohitsharma.letschat.Model.Contact;
import com.mohitsharma.letschat.Model.User;
import com.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

import java.util.ArrayList;

public class FullScreenImgViewer extends AppCompatActivity {
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    FirebaseUser firebaseUser;
    StorageReference ref;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_full_screen_img_viewer);

        final int orientation = this.getResources().getConfiguration().orientation;

        //TOOLBAR
      final   Toolbar toolbar = findViewById(R.id.toolbar_dp);
      toolbar.setTitle("Profile Photo");
      toolbar.setBackgroundColor(Color.BLACK);
      toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final PhotoView photoView = (PhotoView) findViewById(R.id.fullscreen_content);

        //FOR CURRENT USER PROFILE PICTURE
          firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
         databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
         valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 user = snapshot.getValue(User.class);
                assert user != null;
                if(user.getProfileUrl().equals("default")){
                    photoView.setImageResource(R.drawable.dp_default);
                }else{
                    try {
                        ref = FirebaseStorage.getInstance().getReference().child("dp/" + firebaseUser.getPhoneNumber());
                        Glide.with(FullScreenImgViewer.this)
                                .load(ref)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.dp_loading)
                                .centerCrop()
                                .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(user.getProfileUrl()), orientation))
                                .into(photoView);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(FullScreenImgViewer.this, "Error Loading..", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(valueEventListener);



    }

    //onCreate ENDS HERE...

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.full_screen_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if(item.getItemId() == R.id.delete_photo){

            if(user.getProfileUrl().equals("default")){
                Toast.makeText(this, "Upload a Photo first..!", Toast.LENGTH_SHORT).show();
            }else{
                showDeleteAlert();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);

    }

    public void showDeleteAlert(){
        AlertDialog builder = new AlertDialog.Builder(FullScreenImgViewer.this).create();
        builder.setCancelable(true);
        builder.setMessage("Are you sure you want to delete your Profile Photo?");
        builder.setButton(android.content.DialogInterface.BUTTON_POSITIVE, "DELETE", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialogInterface, int i) {
                ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("profileUrl");
                        databaseReference.setValue("default");
                        Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            }
        });
        builder.setButton(android.content.DialogInterface.BUTTON_NEGATIVE, "No,I am Stupid!", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }
}