package com.mohitsharma.letschat.ui.settings;

import android.app.Activity;
import android.app.usage.NetworkStats;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
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
import com.mohitsharma.letschat.FullScreenImgViewer;
import com.mohitsharma.letschat.MainActivity;
import com.mohitsharma.letschat.Model.User;
import com.mohitsharma.letschat.R;
import com.mohitsharma.letschat.SplashScreen;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import me.echodev.resizer.Resizer;

public class  SettingFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 2;
    private SettingsViewModel settingsViewModel;
    public Uri filePath;
    UploadTask uploadTask;
    ImageButton cancelUplaod;
    FirebaseStorage storage;
    StorageReference storageReference;
     FirebaseUser firebaseUser;
    Bitmap bitmap;
    View root;
    ImageView userDp;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    ImageButton edit_name;
    ImageButton edit_status;
    TextInputEditText user_name;
    TextInputEditText user_status;
    TextInputEditText user_phone;
    boolean editingN=true;
    boolean editingS=true;
    ProgressBar pbar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        setHasOptionsMenu(true);
        TextView fragTitle =getActivity().findViewById(R.id.frag_title);
        fragTitle.setText("Settings");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
         userDp = root.findViewById(R.id.dp);
         user_name = root.findViewById(R.id.fullNameU);
         user_status = root.findViewById(R.id.statusU);
         user_phone =root.findViewById(R.id.phoneU);
         pbar=root.findViewById(R.id.pBar);
        user_status.setEnabled(false);
        user_name.setEnabled(false);
        user_phone.setEnabled(false);
       databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user_name.setText(user.getName());
                user_status.setText(user.getStatus());
                user_phone.setText(user.getId());
                if(user.getProfileUrl().equals("default")){
                    userDp.setImageResource(R.drawable.dp_default);
                }else{
                   StorageReference ref = storageReference.child("dp/" + firebaseUser.getPhoneNumber());
                   try {
                       Glide.with(getContext())
                               .load(ref)
                               .diskCacheStrategy(DiskCacheStrategy.ALL)
                               .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(user.getProfileUrl()), getActivity().getResources().getConfiguration().orientation))
                               .placeholder(R.drawable.dp_loading)
                               .into(userDp);
                   }catch (Exception e){
                       e.printStackTrace();
                   }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);


        Button chooseImg = root.findViewById(R.id.chooseImg);
        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                databaseReference.removeEventListener(valueEventListener);
            }
        });

        edit_name=root.findViewById(R.id.edit_name);
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editingN) {
                    user_name.setEnabled(true);
                    edit_name.setImageResource(R.drawable.ic_baseline_done_24);
                    editingN=false;
                }else {
                    user_name.setEnabled(false);
                    edit_name.setImageResource(R.drawable.ic_baseline_edit_24);
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("name");
                    databaseReference.setValue(user_name.getText().toString() );
                    editingN=true;
                }

            }
        });

        edit_status=root.findViewById(R.id.edit_status);
        edit_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editingS) {
                    user_status.setEnabled(true);
                    edit_status.setImageResource(R.drawable.ic_baseline_done_24);
                    editingS=false;
                }else {
                    user_status.setEnabled(false);
                    edit_status.setImageResource(R.drawable.ic_baseline_edit_24);
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("status");
                    databaseReference.setValue(user_status.getText().toString() );
                    editingS=true;
                }

            }
        });
         userDp.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        startActivity(new Intent(getActivity(),FullScreenImgViewer.class));
            }
         });
         cancelUplaod = root.findViewById(R.id.cancel_uplaoding);
       cancelUplaod.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(!uploadTask.isComplete()){
                   uploadTask.cancel();
               }else{
                   Toast.makeText(getContext(), "Unable to Cancel", Toast.LENGTH_SHORT).show();
               }
           }
       });



        return root;
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null) {
            filePath = data.getData();
            try
            {
                 bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                userDp.setImageBitmap(bitmap);
                databaseReference.addValueEventListener(valueEventListener);
                uploadImage();
                databaseReference.removeEventListener(valueEventListener);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadImage(){
            final StorageReference ref = storageReference.child("dp/" + firebaseUser.getPhoneNumber());
            uploadTask = ref.putFile(filePath);
        Toast.makeText(getContext(), "Uploading..!", Toast.LENGTH_SHORT).show();
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("imgUpload", "Success");
                    Toast.makeText(getActivity(), "Uploaded!", Toast.LENGTH_SHORT).show();
                    //profileurl = ref.getDownloadUrl().toString();
                    long time  = System.currentTimeMillis();
                  DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("profileUrl");
                    databaseReference.setValue(String.valueOf(time));
                    pbar.setVisibility(View.INVISIBLE);
                    cancelUplaod.setVisibility(View.INVISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("imgUpload", "failed");
                    Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    pbar.setVisibility(View.INVISIBLE);
                    cancelUplaod.setVisibility(View.INVISIBLE);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    pbar.setVisibility(View.VISIBLE);
                    pbar.setProgress((int)progress,true);
                    cancelUplaod.setVisibility(View.VISIBLE);


                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Toast.makeText(getActivity(), "Canceled!", Toast.LENGTH_SHORT).show();
                    pbar.setVisibility(View.INVISIBLE);
                    cancelUplaod.setVisibility(View.INVISIBLE);
                }
            });

    }

    @Override
    public void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);
    }
    @Override
    public void onResume() {
        super.onResume();
        databaseReference.addValueEventListener(valueEventListener);
    }

}