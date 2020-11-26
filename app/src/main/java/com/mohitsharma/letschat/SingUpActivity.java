package com.mohitsharma.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class SingUpActivity extends AppCompatActivity {
    String verificationID;
    private FirebaseAuth auth;
    EditText otp;
  FrameLayout pg;
    PhoneAuthCredential mCredential;
    TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_otp_layout);
        pg = findViewById(R.id.loading);
        timer = findViewById(R.id.timer);
        Intent intent = getIntent();
        String num = intent.getStringExtra("NUM");

        sendOtp(num);

        auth = FirebaseAuth.getInstance();
        Button nextButton1 = findViewById(R.id.nextButton1);
        otp = findViewById(R.id.otp);
        Animation leftToright = AnimationUtils.loadAnimation(this, R.anim.lefttoright);
        nextButton1.setAnimation(leftToright);
        nextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = otp.getText().toString();
                if (code.isEmpty() || code.length() < 6) {
                    otp.setError("Enter Correct Code...");
                    otp.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });
    }   // ON CREATE ENDS HERE...

    public void sendOtp(String n){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                n,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

  public  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
            Log.d("TAG", "onVerificationCompleted:" + credential);
            String code = "";
            try {
                code = credential.getSmsCode();
                otp.setText(code);
                verifyCode(code);
            }catch (Exception e){
                e.printStackTrace();
            }

            //mCredential=credential;
           // signInWithPhoneAuthCredential(credential);

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w("TAG", "onVerificationFailed", e);
            timer.setText("Error");

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(SingUpActivity.this, " Enter correct number", Toast.LENGTH_LONG).show();
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(SingUpActivity.this, "Too many OTP request!", Toast.LENGTH_SHORT).show();
                // ...
            }
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d("TAG", "onCodeSent:" + verificationId);
            Toast.makeText(SingUpActivity.this, "Verification code sent!", Toast.LENGTH_SHORT).show();
            new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    timer.setText( millisUntilFinished / 1000 + "s");
                }
                public void onFinish() {
                    timer.setVisibility(View.INVISIBLE);
                }

            }.start();
            verificationID = verificationId;
        }
    };

    public void verifyCode(String code) {
        PhoneAuthCredential credential0 = PhoneAuthProvider.getCredential(verificationID, code);
            pg.setVisibility(View.VISIBLE);
            signInWithPhoneAuthCredential(credential0);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(SingUpActivity.this, UserDetailActivity2.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(SingUpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}