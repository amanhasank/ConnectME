package com.example.connectme.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.connectme.databinding.ActivityPhoneNumberVerifyBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;

public class PhoneNumberVerify extends AppCompatActivity {

    ActivityPhoneNumberVerifyBinding binding;
    FirebaseAuth auth;
    String VerificationNo;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        getSupportActionBar().hide();
        binding.otpView.requestFocus();


        String myNumber = getIntent().getStringExtra("Number");

        binding.textView.setText("Verify "+ myNumber);
        String Finalnumber = "+91"+myNumber;
        Log.i("MyNumber",Finalnumber);


        auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(Finalnumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(PhoneNumberVerify.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {


                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {


                        Toast.makeText(PhoneNumberVerify.this, "Code Sent", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpView.requestFocus();
                        VerificationNo = s;

                    }
                }).build();
          PhoneAuthProvider.verifyPhoneNumber(options);



        binding.otpView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(String otp) {

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationNo,otp);

                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Toast.makeText(PhoneNumberVerify.this, "Entered into onComplete", Toast.LENGTH_SHORT).show();
                        if(task.isSuccessful()){

                          //  Toast.makeText(PhoneNumberVerify.this, "Login Sucessfull", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), ProfileSetup.class);
                            startActivity(intent);
                            finishAffinity();

                        }
                        else {
                            Toast.makeText(PhoneNumberVerify.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });

    }

}