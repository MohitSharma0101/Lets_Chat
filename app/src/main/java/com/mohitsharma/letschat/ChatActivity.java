package com.mohitsharma.letschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.gms.common.api.Api;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohitsharma.letschat.Adapters.MessageAdapter;
import com.mohitsharma.letschat.Model.Chat;
import com.mohitsharma.letschat.Model.Message;
import com.mohitsharma.letschat.Model.User;
import com.mohitsharma.letschat.Notifications.APIService;
import com.mohitsharma.letschat.Notifications.Client;
import com.mohitsharma.letschat.Notifications.Data;
import com.mohitsharma.letschat.Notifications.Response;
import com.mohitsharma.letschat.Notifications.Sender;
import com.mohitsharma.letschat.Notifications.Token;

import org.jetbrains.annotations.NotNull;

import java.sql.ClientInfoStatus;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    String childId;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<Message> messageList;
    DatabaseReference msgRef;
    ChildEventListener childEventListener;
    String key;
    Animation msgAnimation;
    APIService apiService;
    boolean notify =false;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageList = new ArrayList<>();
        final CircleImageView toolbar_dp = findViewById(R.id.toolbar_dp);
        final Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        Intent intent = getIntent();
        final String mstatus = getIntent().getStringExtra("status");
        final String Username = intent.getStringExtra("Username");
       final String profileUrl = intent.getStringExtra("profileUrl");
        final String mNumber = intent.getStringExtra("number");


        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,UserProfileAcitivity.class);
                intent.putExtra("Username",Username);
                intent.putExtra("profileUrl",profileUrl);
                intent.putExtra("mNumber",mNumber);
                intent.putExtra("status",mstatus);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, toolbar_dp, "dp");
                startActivity(intent, options.toBundle());
            }
        });
        final TextView onlineStatusView = findViewById(R.id.online_status);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    assert user != null;
                    if(user.getId().equals(mNumber)){
                            onlineStatusView.setVisibility(View.VISIBLE);
                            onlineStatusView.setText(user.getOnlineStatus());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference.addValueEventListener(valueEventListener);

        //API services
        apiService =Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        TextView username = findViewById(R.id.toolbar_name);
        username.setText(Username);

        ImageButton back_btn = findViewById(R.id.back_btn_chat);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        if(profileUrl.equals("default")){
            toolbar_dp.setImageResource(R.drawable.dp_default);
        }else{
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("dp/" + mNumber);
            Glide.with(ChatActivity.this)
                    .load(ref)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(profileUrl), getApplicationContext().getResources().getConfiguration().orientation))
                    .placeholder(R.drawable.dp_loading)
                    .into(toolbar_dp);
        }

        //FireBase Code..
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        final Long UNumber = Long.valueOf(firebaseUser.getPhoneNumber().substring(3));
        Long SNumber = Long.valueOf(mNumber.substring(3));

        if(UNumber>=SNumber){
        childId = SNumber + "_" + UNumber;
        }else{
            childId = UNumber + "_" + SNumber;
        }

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(childId);

        final DatabaseReference chatRefUser = FirebaseDatabase.getInstance()
                .getReference("Inbox")
                .child(firebaseUser.getPhoneNumber());
        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance()
                .getReference("Inbox")
                .child(mNumber);

        final EditText msgEditText = findViewById(R.id.editTextMessage);
        ImageButton sendMsgButton = findViewById(R.id.sendMsgButton);
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             String msg = msgEditText.getText().toString().trim();
             if(!msg.isEmpty()) {
                 try {
                     notify = true;
                     String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
                     key = databaseReference.push().getKey();
                     Message message = new Message(childId, firebaseUser.getPhoneNumber(), profileUrl, msg, currentTime);
                     databaseReference.child(key).setValue(message);
                     msgEditText.setText("");
                     chatRefUser.child(mNumber).setValue(childId);
                     chatRefReceiver.child(firebaseUser.getPhoneNumber()).setValue(childId);
                     sendNotification(mNumber, Username, msg);
                 }catch (Exception e){
                     e.printStackTrace();
                 }
              }

            }
        });

        msgRef  =  FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(childId);


        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message msg= snapshot.getValue(Message.class);
                messageList.add(msg);
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()-1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        msgRef.addChildEventListener(childEventListener);



        //Recycler View

        recyclerView = findViewById(R.id.msg_rec_view);
        messageAdapter = new MessageAdapter(messageList,ChatActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        recyclerView.setAdapter(messageAdapter);


        //API service

    }// onCreate END HERE...

    private void sendNotification(final String uNumber, final String username, final String msg) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");

        allTokens.child(uNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String token = ds.getValue(String.class);
                    Data data = new Data(firebaseUser.getPhoneNumber(),username+":"+msg,"New Message",uNumber,R.drawable.launcher_icon);
                    assert token != null;
                    final Sender sender = new Sender(data,token);
                    Log.d("response:",token);
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(@NotNull Call<Response> call, retrofit2.Response<Response> response) {
                                    assert response.body() != null;
                                    Log.d("response:",response.body().success);
                                    Toast.makeText(ChatActivity.this, ""+response.body().success, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NotNull Call<Response> call, @NotNull Throwable t) {

                                    t.printStackTrace();

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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