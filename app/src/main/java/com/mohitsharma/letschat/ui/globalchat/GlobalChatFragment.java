package com.mohitsharma.letschat.ui.globalchat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohitsharma.letschat.Adapters.MessageAdapter;
import com.mohitsharma.letschat.ChatActivity;
import com.mohitsharma.letschat.InstantMsg;
import com.mohitsharma.letschat.Model.Chat;
import com.mohitsharma.letschat.Model.Message;
import com.mohitsharma.letschat.R;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GlobalChatFragment extends Fragment {

    private GlobalChatViewModel globalChatViewModel;
    private DatabaseReference databaseReference;
    RecyclerView recyclerView;
    int msg_type_left = 0;
    int msg_type_right = 1;
    FirebaseRecyclerAdapter adapter;
    ArrayList<String> itemType = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        globalChatViewModel =ViewModelProviders.of(this).get(GlobalChatViewModel.class);
        View root = inflater.inflate(R.layout.fragment_global_chat, container, false);
        TextView fragTitle =getActivity().findViewById(R.id.frag_title);
        fragTitle.setText("Global Chat");
        ImageButton sendButton = root.findViewById(R.id.sendButton);
        final EditText messageEdittext = root.findViewById(R.id.editTextMessage);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        final Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("GlobalChats")
                .limitToLast(50);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message msg = snapshot.getValue(Message.class);
                itemType.add(msg.getSenderId());
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
        });

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();

         adapter = new FirebaseRecyclerAdapter<Message, ChatHolder>(options) {
            @Override
            public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if(viewType==msg_type_left){
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_msg_card_view,parent,false);
                    return new ChatHolder(view);
                }else{
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_msg_card_view,parent,false);
                    return new ChatHolder(view);
                }
            }

            @Override
            protected void onBindViewHolder(@NotNull ChatHolder holder, int position, @NotNull  Message message) {
                Log.d("Firebase","onBindViewHolderContacts : called" );

                TextView msgView = holder.msgView;
                TextView timestamp = holder.timestampView;
                msgView.setText(message.getMessage());
                timestamp.setText(message.getTime());

            }

             @Override
             public int getItemViewType(int position) {
                if(!itemType.get(position).equals(firebaseUser.getPhoneNumber())){
                    return msg_type_left;
                }else {
                    return msg_type_right;
                }

             }
         };
        recyclerView = root.findViewById(R.id.global_chat_rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
            }

        });


        final DatabaseReference databaseReference0 = FirebaseDatabase.getInstance()
                .getReference("GlobalChats");


                sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              String msg=  messageEdittext.getText().toString();
              if(!msg.isEmpty()){
                  String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
               String key =  databaseReference0.push().getKey();
                  Message message = new Message("GlobalChat",firebaseUser.getPhoneNumber(),key,msg,currentTime);
                  databaseReference0.child(key).setValue(message);
                  messageEdittext.setText("");
              }
            }
        });

        return root;
    }// onCreate ends here

  public static  class ChatHolder extends RecyclerView.ViewHolder{
        TextView msgView;
        TextView timestampView;
        View item_view;
        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            this.msgView = itemView.findViewById(R.id.msg_textView);
            this.timestampView = itemView.findViewById(R.id.timestamp_view);
            item_view=itemView;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}