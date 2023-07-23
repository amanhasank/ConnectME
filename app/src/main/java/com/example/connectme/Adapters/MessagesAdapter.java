package com.example.connectme.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectme.Models.Message;
import com.example.connectme.R;
import com.example.connectme.databinding.ItemSentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagesAdapter extends RecyclerView.Adapter{
    Context context;
    ArrayList<Message> messages;
    String senderRoom;
    String receiverRoom;



    final int ITEM_SENT =1;
    final int ITEM_RECEIVED =2;
    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom= senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent,false);
            return new SendViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_received, parent, false);
            return new ReceiverViewHolder(view);
        }

    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }
        else{
            return ITEM_RECEIVED;
        }

    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);
        if(holder.getClass() == SendViewHolder.class){
            SendViewHolder viewHolder =(SendViewHolder) holder;

            //Deleting Images
            viewHolder.binding.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Unsend Message..");
                    dialog.setMessage("Are You sure you want to Unsend this Message?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();

                    return false;
                }
            });


            // Deleting the messages
            viewHolder.binding.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Unsend Message..");
                    dialog.setMessage("Are You sure you want to Unsend this Message?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();



                    return false;
                }
            });

            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageURL())
                        .placeholder(R.drawable.placeholder).into(viewHolder.binding.image);
            }

            viewHolder.binding.message.setText(message.getMessage());
        }
        else{
            ReceiverViewHolder viewHolder =(ReceiverViewHolder) holder;

            //Deleting the images
            viewHolder.binding.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Unsend Message..");
                    dialog.setMessage("Are You sure you want to Unsend this Message?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();

                    return false;
                }
            });


            // Deleting the messages
            viewHolder.binding.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Unsend Message..");
                    dialog.setMessage("Are You sure you want to Unsend this Message?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();
                    //Toast.makeText(context, "Message selected", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageURL())
                        .placeholder(R.drawable.placeholder).into(viewHolder.binding.image);
            }


            viewHolder.binding.message.setText(message.getMessage());
        }
    }

    private void deleteMessage(int position) {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //String msgTIme = String.valueOf(messages.get(position).getTimetamp());
        long msg = messages.get(position).getTimetamp();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom).child("messages");
        Query query = reference.orderByChild("timetamp").equalTo(msg);

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom).child("messages");
        Query query2 = reference2.orderByChild("timetamp").equalTo(msg);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    
                    if(snapshot1.child("senderId").getValue().equals(myUID)){
                        snapshot1.getRef().removeValue();
                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, "You can only delete your messages", Toast.LENGTH_SHORT).show();
                    }

                   
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    if(snapshot1.child("senderId").getValue().equals(myUID)){
                        snapshot1.getRef().removeValue();
                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, "You can only delete your messages", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder{

        ItemSentBinding binding;
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemSentBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding  = ItemSentBinding.bind(itemView);

        }
    }
}
