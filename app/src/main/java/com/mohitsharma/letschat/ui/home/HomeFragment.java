package com.mohitsharma.letschat.ui.home;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohitsharma.letschat.AddChatActivity;
import com.mohitsharma.letschat.LoginActivity;
import com.mohitsharma.letschat.Model.Chat;
import com.mohitsharma.letschat.Model.Message;
import com.mohitsharma.letschat.R;
import com.mohitsharma.letschat.ui.MyAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    Context context = getContext();
    DatabaseReference databaseReference;
    ChildEventListener childEventListener;
   public  SearchView searchView;
    int i =0;
    HashMap<String,Integer> index = new HashMap<>();
    public static ArrayList<Chat> chatArrayList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        TextView fragTitle =getActivity().findViewById(R.id.frag_title);
        fragTitle.setText("Inbox");


        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        myAdapter=new MyAdapter(getContext(), chatArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(myAdapter);

        //FireBase Code...
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        databaseReference =FirebaseDatabase.getInstance()
                .getReference("Inbox")
                .child(firebaseUser.getPhoneNumber());
       childEventListener = new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull final DataSnapshot snapshot0, @Nullable String previousChildName) {
             String chatId = snapshot0.getValue().toString();
             Log.d("chatId:" , chatId);
             DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Chats");
             final Query q = dr.child(chatId).orderByKey().limitToLast(1);
             q.addChildEventListener(new ChildEventListener() {
                 @Override
                 public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                     Message msg= snapshot.getValue(Message.class);
                     String name = getContactName(snapshot0.getKey(),getContext());
                     assert msg != null;
                     String number =snapshot0.getKey();
                     Chat chat = new Chat(number,msg.getMessageId(),msg.getMessage(),msg.getTime(),name);

                     int i=contains(chatArrayList,number);
                     if(i==-1){
                         chatArrayList.add(0,chat);
                     }else{
                         chatArrayList.remove(i);
                         chatArrayList.add(0,chat);
                     }

                         index.put(snapshot0.getKey(),i);
                     myAdapter.notifyDataSetChanged();
                     Log.d("snumber",number);

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

databaseReference.addChildEventListener(childEventListener);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddChatActivity.class));
            }
        });
        return root;
    }
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
//        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
//        searchView.setSubmitButtonEnabled(false);
//
//
//    searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setQueryHint("Search Contact..");
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                myAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });

        super.onCreateOptionsMenu(menu,inflater);
         }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout: {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity().getApplicationContext() , LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
       databaseReference.removeEventListener(childEventListener);
    }

    @Override
    public void onResume() {
        super.onResume();

    databaseReference.addChildEventListener(childEventListener);
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

    public int contains(ArrayList<Chat> arrayList,String number ){
        for(Chat c : arrayList){
            if(c.getUserNumber().equals(number)) {
             return arrayList.indexOf(c);
            }
        }
        return -1;
    }
}