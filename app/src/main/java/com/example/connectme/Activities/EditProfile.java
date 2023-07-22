package com.example.connectme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.connectme.Models.User;
import com.example.connectme.R;
import com.example.connectme.databinding.EditProfileBinding;
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

public class EditProfile extends AppCompatActivity {
    EditProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    Uri selectedImage;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Editing Your Profile...");
        dialog.setCancelable(false);

        getSupportActionBar().hide();

        database.getReference("users").child(auth.getUid()).child("profilePic")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String imageURL = dataSnapshot.getValue(String.class);
                        if (imageURL != null && !imageURL.isEmpty()) {
                            // If the user has a profile picture, load it into the profileImage ImageView
                            Glide.with(EditProfile.this)
                                    .load(imageURL)
                                    .into(binding.profileImage);
                        } else {
                            // If the user doesn't have a profile picture, show a placeholder image
                            binding.profileImage.setImageResource(R.drawable.avatar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

// Get a reference to the "users" node in the database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
       // static String MYNAME="";
// Attach a ValueEventListener to read the data for the current user
        usersRef.child(currentUserUID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve the name value from the snapshot
                String name = dataSnapshot.getValue(String.class);

                // Store the name in the MYNAME variable
              //  MYNAME = name;
                TextView textView = findViewById(R.id.textView);

                // Update the TextView with the name value
                if (name != null) {
                    textView.setText(name); // You can customize the text as desired
                } else {
                    textView.setText("Edit Your Profile"); // Default text if name is null
                }

                // Now you can use the MYNAME variable wherever you need it
                // For example, you can set the TextView's text to the name
                // textView.setText(MYNAME);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur while reading the data
            }
        });

        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = binding.nameTextField.getText().toString();

                if (userName.isEmpty() && selectedImage == null) {
                    binding.nameTextField.setError("Please Type a Name");
                    Toast.makeText(EditProfile.this, "Please Type a Name OR Select a new Picture", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(selectedImage!=null){
                    dialog.show();
                    TextView textView = findViewById(R.id.textView);
                    String currentName = textView.getText().toString();
                 //   Toast.makeText(EditProfile.this, "selected image"+ selectedImage, Toast.LENGTH_SHORT).show();
                    StorageReference reference =storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                //Toast.makeText(EditProfile.this, "Enteredddd", Toast.LENGTH_SHORT).show();
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                     //   Toast.makeText(EditProfile.this, "ONSUCCESS", Toast.LENGTH_SHORT).show();
                                        String imageURL = uri.toString();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = currentName;
                                        String uid = auth.getUid();

                                        User user = new User(uid,name,phone,imageURL);

                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        dialog.dismiss();
                                                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    DatabaseReference nameRef = database.getReference("users").child(auth.getUid()).child("name");
                    String newName= binding.nameTextField.getText().toString();
// Use the setValue method to update the name in the database
                    nameRef.setValue(newName)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Name update successful
                                        Toast.makeText(EditProfile.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                                        TextView textView = findViewById(R.id.textView);
                                        textView.setText(newName);
                                        binding.nameTextField.setText("");

                                    } else {
                                        // Name update failed
                                        Toast.makeText(EditProfile.this, "Failed to update name. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });


        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null){
                binding.profileImage.setImageURI(data.getData());
                selectedImage = data.getData();

            }
        }
    }
}