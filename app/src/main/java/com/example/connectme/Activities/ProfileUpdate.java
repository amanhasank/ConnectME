package com.example.connectme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.connectme.Models.User;
import com.example.connectme.R;
import com.example.connectme.databinding.ActivityProfileUpdateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileUpdate extends AppCompatActivity {
    ActivityProfileUpdateBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityProfileUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //User user = new User();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);


        String coolboy = getIntent().getStringExtra("username");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        DatabaseReference myRef = database.getReference();


        String uid = auth.getCurrentUser().getUid();
        myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name  = snapshot.child("name").getValue(String.class);

                String profilepic = snapshot.child("profilePic").getValue().toString();

                Log.i("hello", profilepic);

                Glide.with(ProfileUpdate.this).load(profilepic).placeholder(R.drawable.avatar).into(binding.profileImage);

                binding.textView.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });




        binding.setupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.nameTextField.getText().toString().equals("")) {

                    // Putting image on Storage
                    if (selectedImage != null) {
                        dialog.show();
                        StorageReference reference = storage.getReference().child("NewProfile").child(uid);
                        reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.i("hellobro", uri.toString());
                                            myRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                                                @Override

                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    snapshot.getRef().child("profilePic").setValue(uri.toString());
                                                    Glide.with(ProfileUpdate.this).load(uri).placeholder(R.drawable.avatar).into(binding.profileImage);
                                                    dialog.dismiss();

                                                    Toast.makeText(ProfileUpdate.this, "Profile Updated", Toast.LENGTH_SHORT).show();


                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }
                                    });
                                }
                            }
                        });
                    } else {

                    }


                }

                else {
                    myRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override

                        public void onDataChange(@NonNull DataSnapshot snapshot) {


                                String nameInData = binding.nameTextField.getText().toString();
                                snapshot.getRef().child("name").setValue(nameInData);
                                String Finalname = snapshot.child("name").getValue(String.class);
                                binding.textView.setText(Finalname);
                            }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    // Putting image on Storage
                    if (selectedImage != null) {
                        dialog.show();
                        StorageReference reference = storage.getReference().child("NewProfile").child(uid);
                        reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.i("hellobro", uri.toString());
                                            myRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                                                @Override

                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    snapshot.getRef().child("profilePic").setValue(uri.toString());
                                                    Glide.with(ProfileUpdate.this).load(uri).placeholder(R.drawable.avatar).into(binding.profileImage);
                                                    dialog.dismiss();
                                                    Toast.makeText(ProfileUpdate.this, "Profile Updated", Toast.LENGTH_SHORT).show();


                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        //  Toast.makeText(this, "Chuoop bc", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(ProfileUpdate.this, "Chup rhee", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });
    }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            if (data != null) {
                if (data.getData() != null) {
                    binding.profileImage.setImageURI(data.getData());
                    selectedImage = data.getData();

                }
            }
        }


}





