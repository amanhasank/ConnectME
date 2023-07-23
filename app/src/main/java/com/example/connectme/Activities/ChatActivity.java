package com.example.connectme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.connectme.Adapters.MessagesAdapter;
import com.example.connectme.Models.Message;
import com.example.connectme.R;
import com.example.connectme.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class  ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiveruid;

    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    private byte encryptionKey[]= {4, 25, 88, 24, 2, 8, -58, 89, 47, -29, -78, 87};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");

        receiveruid = getIntent().getStringExtra("uid");

        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.avatar).into(binding.picProfile);

        setSupportActionBar(binding.toolbar);
//        Log.i("receiveruid", receiveruid);

        database.getReference().child("presence").child(receiveruid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String OnlineStatus = snapshot.getValue(String.class);
                    if(!OnlineStatus.isEmpty()){
                        if(OnlineStatus.equals("Offline")){
                            binding.onlineStatus.setVisibility(View.GONE);
                        }
                        else {
                            binding.onlineStatus.setText(OnlineStatus);
                            binding.onlineStatus.setVisibility(View.VISIBLE);
                        }
                    }}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

           }
        }
        );


        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Image...");
        dialog.setCancelable(false);

        binding.arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final Handler handler = new Handler();
        binding.messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(stopprdtyping, 1000);
            }
            Runnable stopprdtyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");

                }
            };

        });

        //Working with Cryptography
        try {
            cipher =Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");


        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


         senderUid = FirebaseAuth.getInstance().getUid();

        messages = new ArrayList<>();
        senderRoom = senderUid + receiveruid;
        receiverRoom = receiveruid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom, receiverRoom);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));


// Reading Messages
       database.getReference().child("chats")
               .child(senderRoom)
               .child("messages")
               .addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       messages.clear();
                      // DemoMessage.clear();
                      // DemoMessage.addAll(messages);
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
                   try {
                       lastMsgObj.put("lastMsg", AESDecryption(message.getMessage()));
                   } catch (UnsupportedEncodingException e) {
                       e.printStackTrace();
                   }
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
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
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

    private String AESEncryption(String msg){
        byte[] stringByte = msg.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte =cipher.doFinal(stringByte);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString=null;
        try {
             returnString = new String(encryptedByte,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    private String AESDecryption(String msg) throws UnsupportedEncodingException {
        byte[] encryptedByte = msg.getBytes("ISO-8859-1");
        String decryptedmsg = msg;
        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE,secretKeySpec);
            decryption =decipher.doFinal(encryptedByte);
            decryptedmsg= new String(decryption);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decryptedmsg;
    }

    @Override
    protected void onResume() {
        String currentID = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentID).setValue("Online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        String currentID = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentID).setValue("Offline");
        super.onPause();
    }

    @Override
    protected void onStop() {
        String currentID = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentID).setValue("Offline");
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //dialog.setMessage("Sending Image...");
        if(requestCode==10){
            if(data!=null)
            {
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar cal = Calendar.getInstance();
                    StorageReference storageReference = storage.getReference().child("chats").child(cal.getTimeInMillis()+"");
                    dialog.show();
                    storageReference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String fileKAPath = uri.toString();
                //Sending Image
                                       // Toast.makeText(ChatActivity.this, fileKAPath, Toast.LENGTH_SHORT).show();
                                        String messageTxt = binding.messageText.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageURL(fileKAPath);
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
                                });
                            }
                        }
                    });

                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}