package com.elhazent.picodiploma.online.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.elhazent.picodiploma.online.MessageActivity;
import com.elhazent.picodiploma.online.R;
import com.elhazent.picodiploma.online.model.ChatItem;
import com.elhazent.picodiploma.online.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.Holder> {
    Context context;
    List<User> listuser;
    private boolean ischat;
    String lastmessage;

    public ListAdapter(Context context, List<User> listuser, boolean ischat) {
        this.context = context;
        this.listuser = listuser;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        /*
        conver layout yang akan kita gunakan sebagai view object baru di dalam layouy utama
         */
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, viewGroup, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        /*
        kita bind view - view yang ada didalam layout kita ke class model
         */
        final User user = listuser.get(i);
        holder.username.setText(user.getUsername());
//        if (user.getImageURL().equals("no")) {
//            holder.image.setImageResource(R.drawable.ic_launcher_background);
//        } else {
//            Picasso.get().load(user.getImageURL()).into(holder.image);
//        }

//        if (ischat) {
//            if (user.getStatus().equals("online")) {
//                holder.on.setVisibility(View.VISIBLE);
//                holder.off.setVisibility(View.GONE);
//            } else {
//                holder.on.setVisibility(View.GONE);
//                holder.off.setVisibility(View.VISIBLE);
//            }
//        } else {
//            holder.on.setVisibility(View.GONE);
//            holder.off.setVisibility(View.GONE);
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                context.startActivity(intent);
            }
        });

        if (ischat){
            lastMessage(user.getId(), holder.lastmsg);
        } else {
            holder.lastmsg.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listuser.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView username, lastmsg;
        ImageView image, on, off;

        public Holder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.useritem);
            image = itemView.findViewById(R.id.profileimage);
            on = itemView.findViewById(R.id.img_on);
            off = itemView.findViewById(R.id.img_off);
            lastmsg = itemView.findViewById(R.id.last_msg);

        }
    }

    // todo check for last message
    private void lastMessage(final String userid, final TextView lastmsg) {
        lastmessage = "last";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatItem chatItem = ds.getValue(ChatItem.class);
                    if (chatItem.getReceiver().equals(firebaseUser.getUid()) && chatItem.getSender().equals(userid) ||
                            chatItem.getReceiver().equals(userid) && chatItem.getSender().equals(firebaseUser.getUid())) {
                        lastmessage = chatItem.getMessage();
                    }
                }

                switch (lastmessage) {
                    case "last":
                        lastmsg.setText("No message");
                        break;
                    default:
                        lastmsg.setText(lastmessage);
                        break;
                }

                lastmessage = "last";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
