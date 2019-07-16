package com.elhazent.picodiploma.online;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elhazent.picodiploma.online.adapter.MessageAdapter;
import com.elhazent.picodiploma.online.model.ChatItem;
import com.elhazent.picodiploma.online.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

//    CircleImageView imageView;
    TextView username;
    FirebaseUser firebaseUser;
    DatabaseReference root;
    ImageButton imageButton;
    EditText edtsend;
    MessageAdapter adapter;
    List<ChatItem> list;
    RecyclerView recyclerView;
    Intent intent;
    ValueEventListener seenListener;
    String userid;
//    APISevice apiSevice;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolber);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // and this
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

//        apiSevice = Client.getClient("https://fcm.googleapis.com/").create(APISevice.class);

        recyclerView = findViewById(R.id.recyclerM);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        imageButton = findViewById(R.id.btnsend);
        edtsend = findViewById(R.id.edtmessage);

//        imageView = findViewById(R.id.profileimgmsg);
        username = findViewById(R.id.usernamemsg);

        intent = getIntent();
        userid = intent.getStringExtra("userId");

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = edtsend.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }

                edtsend.setText("");
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        root = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
//                if (user.getImageURL().equals("no")){
//                    imageView.setImageResource(R.drawable.noimage);
//                } else {
//                    Picasso.get().load(user.getImageURL()).into(imageView);
//                }

                readMessage(firebaseUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MessageActivity.this, ChatActivity.class));
    }

    private void seenMessage(final String userId){
        root = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ChatItem chat = ds.getValue(ChatItem.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userId)){
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("isseen", true);
                        ds.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("isseen", false);

        reference.child("Chats").push().setValue(map);


        // add user to chat fragment
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatReference.child("id").setValue(userid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        final String msg = message;
//
//        root = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//        root.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                if (notify) {
//                    sendNotification(receiver, user.getUsername(), msg);
//                }
//                notify = false;
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }
//
//    private void sendNotification(final String receiver, final String username, final String msg) {
//        final DatabaseReference token = FirebaseDatabase.getInstance().getReference("Tokens");
//        Query query = token.orderByKey().equalTo(receiver);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds:dataSnapshot.getChildren()){
//                    Token token1 = ds.getValue(Token.class);
//                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, username + ": " +msg, "New Message", userid);
//                    Sender sender = new Sender(data, token1.getToken());
//                    apiSevice.sendNotification(sender)
//                            .enqueue(new Callback<MyResponse>() {
//                                @Override
//                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
//                                    if (response.code() == 200){
//                                        if (response.body().success != 1){
//                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<MyResponse> call, Throwable t) {
//
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void readMessage(final String id, final String userid, final String imgurl) {
        list = new ArrayList<>();
        root = FirebaseDatabase.getInstance().getReference("Chats");
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatItem chat = ds.getValue(ChatItem.class);
                    if (chat.getReceiver().equals(id) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(id)) {
                        list.add(chat);
                    }

                    adapter = new MessageAdapter(MessageActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void status(String status){
//        root = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//        HashMap<String, Object> map = new HashMap<String, Object>();
//        map.put("status", status);
//
//        root.updateChildren(map);
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        root.removeEventListener(seenListener);
//        status("offline");
    }
}