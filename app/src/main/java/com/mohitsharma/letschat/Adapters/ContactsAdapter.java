package com.mohitsharma.letschat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mohitsharma.letschat.AddChatActivity;
import com.mohitsharma.letschat.ChatActivity;
import com.mohitsharma.letschat.FullScreenImgViewer;
import com.mohitsharma.letschat.Model.Contact;
import com.mohitsharma.letschat.R;
import com.mohitsharma.letschat.UserDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "StaggerRecyclerViewAd";
    public ArrayList<Contact> contacts = new ArrayList<>();
    public static ArrayList<Contact> contactsFull;
    private Context context;

    public ContactsAdapter(Context context,ArrayList<Contact> contacts) {
        this.contacts = contacts;
        this.context=context;
        contactsFull = new ArrayList<>();
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, final int position) {
        Log.d(TAG,"onBindViewHolderContacts : called" );
        ImageView dp=holder.contactsDp;
        TextView name=holder.contactsName;
        TextView status = holder.contactsStatus;
        name.setText(contacts.get(position).getName());
        status.setText(contacts.get(position).getStatus());
        if(contacts.get(position).getProfileUrl().equals("default")){
            dp.setImageResource(R.drawable.dp_default);
        }else{
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("dp/" +contacts.get(position).getId());
            Glide.with(context)
                    .load(ref)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .signature(new MediaStoreSignature("image/jpeg", Long.parseLong(contacts.get(position).getProfileUrl()), context.getResources().getConfiguration().orientation))
                    .placeholder(R.drawable.dp_loading)
                    .into(dp);
        }
        holder.item_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("Username",contacts.get(position).getName());
                intent.putExtra("profileUrl",contacts.get(position).getProfileUrl());
                intent.putExtra("number",contacts.get(position).getId());

                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }
    private Filter contactFilter =new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Contact> filteredList = new ArrayList<>();
            if(charSequence == null || charSequence.length() == 0){
                filteredList.addAll(contactsFull);
            }else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Contact item : contactsFull){
                    if(item.getName().toLowerCase().contains(filterPattern) || item.getId().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
          contacts.clear();
          contacts.addAll((List) filterResults.values);
          notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView contactsDp;
        TextView contactsName;
        View item_view;
        TextView contactsStatus;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.contactsName = itemView.findViewById(R.id.conatctName);
            this.contactsStatus = itemView.findViewById(R.id.contactStatus);
            this.contactsDp=itemView.findViewById(R.id.contactDp);
            item_view=itemView;

        }
    }

}
