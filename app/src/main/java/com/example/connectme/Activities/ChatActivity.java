package com.example.connectme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.connectme.Adapters.MessagesAdapter;
import com.example.connectme.Models.Message;
import com.example.connectme.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class  ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String name = getIntent().getStringExtra("name");

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String receiveruid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();

        messages = new ArrayList<>();
        senderRoom = senderUid + receiveruid;
        receiverRoom = receiveruid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom, receiverRoom);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));


       database = FirebaseDatabase.getInstance();

       database.getReference().child("chats")
               .child(senderRoom)
               .child("messages")
               .addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       messages.clear();
                       for(DataSnapshot snapshot1 : snapshot.getChildren())
                       {
                           Message message = snapshot1.getValue(Message.class);
                           message.setMessageId(snapshot1.getKey());
                       messages.add(message);
                       }
                       adapter.notifyDataSetChanged();
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });


//Working with the text messages

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if (binding.messageText.getText().toString().length()==0) {

               }
               else
               {
                   String messageTxt = binding.messageText.getText().toString();
                   Date date = new Date();
                   Message message = new Message(messageTxt, senderUid, date.getTime());
                   binding.messageText.setText("");

                   HashMap<String, Object> lastMsgObj = new HashMap<>();
                   lastMsgObj.put("lastMsg", message.getMessage());
                   lastMsgObj.put("lastMsgTime", date.getTime());

                   database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                   database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                   database.getReference().child("chats")
                           .child(senderRoom)
                           .child("messages")
                           .push()
                           .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {

                           database.getReference().child("chats")
                                   .child(receiverRoom)
                                   .child("messages")
                                   .push()
                                   .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {

                               }
                           });

                       }
                   });

               }
           }
       });

        //work with attachment
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        //work with camera
        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}