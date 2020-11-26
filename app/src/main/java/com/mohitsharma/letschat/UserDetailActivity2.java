package com.mohitsharma.letschat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mohitsharma.letschat.Model.User;

import java.io.IOException;

public class UserDetailActivity2 extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    EditText name;
    EditText status;
    ImageView dp;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri filePath;
    String profileUrl;
    ProgressBar pbar;
    Button nextButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details_layout);
        nextButton2 = findViewById(R.id.nextButton2);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        name = findViewById(R.id.fullName);
        status = findViewById(R.id.status);
        pbar = findViewById(R.id.pBar);
        dp = findViewById(R.id.dp);
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserDetailActivity2.this, FullScreenImgViewer.class));
            }
        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User oldUser = snapshot.getValue(User.class);
                    name.setText(oldUser.getName());
                    status.setText(oldUser.getStatus());
                    StorageReference ref = storageReference.child("dp/" + firebaseUser.getPhoneNumber());
                    if(oldUser.getProfileUrl().equals("default")){
                        dp.setImageResource(R.drawable.dp_default);
                        profileUrl = "default";
                    }else {
                        Glide.with(UserDetailActivity2.this)
                                .load(ref)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(oldUser.getProfileUrl()), UserDetailActivity2.this.getResources().getConfiguration().orientation))
                                .placeholder(R.drawable.dp_loading)
                                .into(dp);
                        profileUrl = oldUser.getProfileUrl();
                    }
                    nextButton2.setEnabled(true);
                } else {
                    Log.d("exist:", "false");
                    profileUrl = "default";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);


        nextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String name0 = name.getText().toString();
                final String status0 = status.getText().toString();
                final String id = firebaseUser.getPhoneNumber();
                if(name0.length()>2 && status0.length()>2) {
                    databaseReference.removeEventListener(valueEventListener);
                    User newUser = new User(id, name0, profileUrl, status0);
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    databaseReference.setValue(newUser);
                    Intent intent = new Intent(UserDetailActivity2.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(UserDetailActivity2.this, "Enter correct details", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button chooseImg = findViewById(R.id.chooseImg);
        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton2.setEnabled(false);
                chooseImage();
            }
        });
    }
    // onCreate ENDS HERE...

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ImageView imageView = findViewById(R.id.dp);
                imageView.setImageBitmap(bitmap);
                uplaodImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uplaodImage() {

        final StorageReference ref = storageReference.child("dp/" + firebaseUser.getPhoneNumber());
        UploadTask uploadTask = ref.putFile(filePath);
        Toast.makeText(getApplicationContext(), "Uploading..!", Toast.LENGTH_SHORT).show();
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                nextButton2.setEnabled(true);
                Log.d("imgUpload", "Success");
                Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
                //profileurl = ref.getDownloadUrl().toString();
                long time  = System.currentTimeMillis();
                profileUrl=String.valueOf(time);
//                DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("profileUrl");
//                databaseReference.setValue(String.valueOf(time));
                pbar.setVisibility(View.INVISIBLE);
             //   cancelUplaod.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("imgUpload", "failed");
                Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                pbar.setVisibility(View.INVISIBLE);
              //  cancelUplaod.setVisibility(View.INVISIBLE);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                nextButton2.setEnabled(false);
                pbar.setVisibility(View.VISIBLE);
                pbar.setProgress((int)progress,true);
              //  cancelUplaod.setVisibility(View.VISIBLE);

            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(getApplicationContext(), "Canceled!", Toast.LENGTH_SHORT).show();
                pbar.setVisibility(View.INVISIBLE);
               // cancelUplaod.setVisibility(View.INVISIBLE);
            }
        });
    }
}
