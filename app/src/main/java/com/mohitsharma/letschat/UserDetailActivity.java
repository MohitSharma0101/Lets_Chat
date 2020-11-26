package com.mohitsharma.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

public class UserDetailActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 0;
    FirebaseUser firebaseUser;
     DatabaseReference databaseReference;
    EditText name;
    EditText status;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri filePath;
    String profileurl;
ProgressBar pbar;
    User oldUser;
    String  profileUrl="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
            profileurl = "default";
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        name = findViewById(R.id.fullName);
        status = findViewById(R.id.status);
        pbar=findViewById(R.id.pBar);
        final ImageView dp = findViewById(R.id.dp);
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserDetailActivity.this,FullScreenImgViewer.class));
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        final ValueEventListener valueEventListener =new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    oldUser = snapshot.getValue(User.class);
//                        if (oldUser.getName() != null) {
                    name.setText(oldUser.getName());
                    status.setText(oldUser.getStatus());
                    StorageReference ref = storageReference.child("dp/" + firebaseUser.getUid());
                    Glide.with(UserDetailActivity.this)
                            .load(ref)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(oldUser.getProfileUrl()), UserDetailActivity.this.getResources().getConfiguration().orientation))
                            .placeholder(R.drawable.dp_loading)
                            .into(dp);
//                }
                }else {
                    Log.d("exist:","false");
                    profileUrl = "default";
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        Button nextButton2 = findViewById(R.id.nextButton2);
        nextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.removeEventListener(valueEventListener);
                final String name0 = name.getText().toString();
                final String status0 = status.getText().toString();
                if(name0.length()>4 && !status0.isEmpty()) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name0).build();
                    firebaseUser.updateProfile(profileUpdates);
                    final String id = firebaseUser.getPhoneNumber();

                    if(filePath!=null) {
                        new Thread(new Runnable() {
                            @Override
                        public void run() {
                        final StorageReference ref = storageReference.child("dp/" + firebaseUser.getUid());
                        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d("imgUpload", "Succes");
                                profileurl = ref.getDownloadUrl().toString();
                                long time  = System.currentTimeMillis();
                                User newUser = new User(id, name0, String.valueOf(time), status0);
                                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                                databaseReference.setValue(newUser);
                                pbar.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("imgUpload", "failed");
                                profileurl = "default";
                            }
                        }); }
                    }).start();
                    }else{
                        User newUser;
                        if(profileUrl.equals("default")){
                             newUser = new User(id, name0, oldUser.getProfileUrl(), status0);
                        }else {
                             newUser = new User(id, name0, "default", status0);
                        }
                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        databaseReference.setValue(newUser);
                    }
                    Intent intent = new Intent(UserDetailActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else {
                    Toast.makeText(UserDetailActivity.this, "Enter correct details", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Button chooseImg = findViewById(R.id.chooseImg);
        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    // OnCreate Ends Here...


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {
            filePath = data.getData();
            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ImageView imageView=findViewById(R.id.dp);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}