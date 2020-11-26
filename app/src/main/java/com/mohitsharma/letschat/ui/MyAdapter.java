package com.mohitsharma.letschat.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohitsharma.letschat.Adapters.ContactsAdapter;
import com.mohitsharma.letschat.ChatActivity;
import com.mohitsharma.letschat.Model.Chat;
import com.mohitsharma.letschat.Model.Contact;
import com.mohitsharma.letschat.Model.User;
import com.mohitsharma.letschat.R;
import com.mohitsharma.letschat.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "StaggerRecyclerViewAd";
    public ArrayList<Chat> chatArrayList;
    public static ArrayList<Chat> chatArrayListFull;
    public Context context;

    public MyAdapter( Context context, ArrayList<Chat> chatArrayList) {
        this.chatArrayList = chatArrayList;
        this.context = context;
        chatArrayListFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolder : called" );
        ImageView dp=holder.dp;
        TextView name=holder.name;
        TextView timestamp = holder.timeStamp;
        timestamp.setText(chatArrayList.get(position).getTimeStamp());
      name.setText(chatArrayList.get(position).getUserName());
        TextView msg = holder.msg;
        msg.setText(chatArrayList.get(position).getLastMsg());

        if(chatArrayList.get(position).getProfileUrl().equals("default")){
            dp.setImageResource(R.drawable.dp_default);
        }else{
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("dp/" +chatArrayList.get(position).getUserNumber());
            Glide.with(context)
                    .load(ref)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(chatArrayList.get(position).getProfileUrl()), context.getResources().getConfiguration().orientation))
                    .placeholder(R.drawable.dp_loading)
                    .into(dp);
        }

        holder.item_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("Username", chatArrayList.get(position).getUserName());
                intent.putExtra("profileUrl", chatArrayList.get(position).getProfileUrl());
                intent.putExtra("number", chatArrayList.get(position).getUserNumber());

                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return chatFilter;
    }
    public Filter chatFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Chat> filteredList = new ArrayList<>();
            if(charSequence == null || charSequence.length() == 0){
                filteredList.addAll(chatArrayListFull);
            }else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Chat item : chatArrayListFull){
                    if(item.getUserName().toLowerCase().contains(filterPattern) || item.getUserNumber().contains(filterPattern) || item.getLastMsg().contains(filterPattern)){
                        filteredList.add(item);

                    }
                    Log.d("item",item.getUserName());
                }

            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            chatArrayList.clear();
            chatArrayList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView dp;
        TextView name;
        View item_view;
        TextView msg;
        TextView timeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.dp = itemView.findViewById(R.id.dp);
            this.name = itemView.findViewById(R.id.name);
            this.msg=itemView.findViewById(R.id.msg);
            this.timeStamp = itemView.findViewById(R.id.timestamp_view);
            item_view=itemView;

        }
    }

    public String FormatNo(String n){
        if(n.substring(0,3).equals("+91")){
            return n;
        }else{
            return "+91"+n;
        }
    }

}
