package com.elhazent.picodiploma.online;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.elhazent.picodiploma.online.adapter.ListAdapter;
import com.elhazent.picodiploma.online.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    List<User> mUser;
    RecyclerView recyclerView;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recyclerView = findViewById(R.id.recylerCH);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mUser = new ArrayList<>();
        readUser();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(ChatActivity.this, DetailDriverActivity.class));
//    }

    private void readUser(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (search.getText().toString().equals("")) {
//                    mUser.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        mUser.add(user);
                    }
//                    }
//
                    adapter = new ListAdapter(getApplicationContext(), mUser, false);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
