package com.mohitsharma.letschat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohitsharma.letschat.Model.User;
import com.mohitsharma.letschat.Notifications.Token;

import androidx.annotation.NonNull;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final float END_SCALE = 0.7f;
    FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private DrawerLayout drawerLayout;
    private View contentView;
    private AppBarConfiguration mAppBarConfiguration;
    StorageReference storageReference;
    FirebaseStorage storage;
    DatabaseReference onlineRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);
        final ImageView userDp = headerView.findViewById(R.id.mainDp);
        userDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,FullScreenImgViewer.class));
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                   if(user!=null){
                TextView navName = headerView.findViewById(R.id.nav_name);
                navName.setText(user.getName());
                TextView navstatus = headerView.findViewById(R.id.nav_status);
                navstatus.setText(user.getStatus());
                if (user.getProfileUrl().equals("default")) {
                    userDp.setImageResource(R.drawable.dp_default);
                } else {
                    final StorageReference ref = storageReference.child("dp/" + firebaseUser.getPhoneNumber());
                    Glide.with(getApplicationContext())
                            .load(ref)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(user.getProfileUrl()), MainActivity.this.getResources().getConfiguration().orientation))
                            .placeholder(R.drawable.dp_loading)
                            .into(userDp);
                }
              }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inbox");
        toolbar.setTitleTextColor(getResources().getColor(R.color.darkBlue));
       toolbar.setElevation(0);
        toolbar.setContentInsetStartWithNavigation(0);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
       //  final NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_global_chat, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

       // Drawer Animation Code:
        {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            contentView = findViewById(R.id.contentMain);
            final CardView cardViewMain = findViewById(R.id.contentMain);

            toolbar.setNavigationIcon(new DrawerArrowDrawable(this));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {
                                                         if (drawerLayout.isDrawerOpen(navigationView)) {
                                                             drawerLayout.closeDrawer(navigationView);
                                                         } else {
                                                             drawerLayout.openDrawer(navigationView);
                                                         }
                                                     }
                                                 }
            );

            drawerLayout.setScrimColor(Color.TRANSPARENT);
            drawerLayout.setDrawerElevation(0);
            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                                               @Override
                                               public void onDrawerSlide(View drawerView, float slideOffset) {
                                                   getSupportActionBar().hide();
                                                   final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                                                   final float offsetScale = 1 - diffScaledOffset;
                                                   contentView.setScaleX(offsetScale);
                                                   contentView.setScaleY(offsetScale);
                                                   // Translate the View, accounting for the scaled width
                                                   final float xOffset = drawerView.getWidth() * slideOffset;
                                                   final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                                                   final float xTranslation = xOffset - xOffsetDiff;
                                                   contentView.setTranslationX(xTranslation);
                                                   cardViewMain.setRadius(50);

                                               }

                                               @Override
                                               public void onDrawerClosed(View drawerView) {
                                                  getSupportActionBar().show();
                                                   cardViewMain.setRadius(0);
                                               }
                                           }
            );
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());

        //UPDATE ONLINE STATUS
        onlineRef = databaseReference.child("onlineStatus");
         onlineRef.setValue("Online");

    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mtoken = new Token(token);
        ref.child(Objects.requireNonNull(firebaseUser.getPhoneNumber())).setValue(mtoken);
        SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Current_USERID",firebaseUser.getPhoneNumber());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onlineRef = databaseReference.child("onlineStatus");
        String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
        onlineRef.setValue("Last seen: "+currentTime);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        onlineRef = databaseReference.child("onlineStatus");
//
//        String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
//        onlineRef.setValue("Last seen: "+currentTime);
//
//    }

}