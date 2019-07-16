package com.elhazent.picodiploma.online.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.elhazent.picodiploma.online.R;
import com.elhazent.picodiploma.online.model.ChatItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ChatItem> listuser;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<ChatItem> listuser) {
        this.context = context;
        this.listuser = listuser;
    }

    @NonNull
    @Override
    public MessageAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, viewGroup, false);
            return new MessageAdapter.Holder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, viewGroup, false);
            return new MessageAdapter.Holder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.Holder holder, int i) {
        ChatItem chatItem = listuser.get(i);
        holder.message.setText(chatItem.getMessage());


        if (i == listuser.size()-1){
            if (chatItem.isIsseen()){
                holder.txtseen.setText("Seen");
            } else {
                holder.txtseen.setText("Delivered");
            }
        } else {
            holder.txtseen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listuser.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView message, txtseen;
        ImageView image;

        public Holder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.textmsg);
            txtseen = itemView.findViewById(R.id.txtseen);
            image = itemView.findViewById(R.id.profile);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (listuser.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
