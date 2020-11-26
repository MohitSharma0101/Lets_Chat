package com.mohitsharma.letschat.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mohitsharma.letschat.Model.Message;
import com.mohitsharma.letschat.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
int lastPosition = -1;
    int msgType;
    int msg_type_left = 0;
    int msg_type_right = 1;
    FirebaseUser firebaseUser;
    List<Message> messageList = new ArrayList<>();
    Context context;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==msg_type_left){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_msg_card_view,parent,false);
            return new ViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_msg_card_view,parent,false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("TAG","onBindViewHolderContacts : called" );

        TextView msgView = holder.msgView;
        TextView timestamp = holder.timestampView;
        msgView.setText(messageList.get(position).getMessage());
        timestamp.setText(messageList.get(position).getTime());

        //setAnimation(holder.itemView, position);
    }
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView msgView;
        TextView timestampView;
        View item_view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.msgView = itemView.findViewById(R.id.msg_textView);
            this.timestampView = itemView.findViewById(R.id.timestamp_view);
            item_view=itemView;

        }
;
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        String phn=firebaseUser.getPhoneNumber();
        assert phn != null;
        if(messageList.get(position).getSenderId().contains(phn)){
            return msg_type_right;
        }else {
          return msg_type_left;
        }
    }
}
