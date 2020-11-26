package com.mohitsharma.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    EditText email;
    EditText password;
    PhoneAuthCredential mCredential;
    String verificationID;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.get_number_layout);
        final EditText number = findViewById(R.id.number);
        Button nextButton =findViewById(R.id.nextButton);
        final CardView cardView = findViewById(R.id.laucher_card);
        nextButton.bringToFront();
     Animation   leftToright= AnimationUtils.loadAnimation(this,R.anim.lefttoright);
        nextButton.setAnimation(leftToright);

         intent = new Intent(LoginActivity.this, SingUpActivity.class);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String num = number.getText().toString();
               if(num.length()==13) {
                   ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(LoginActivity.this, cardView, "card");

                   intent.putExtra("NUM", num);
                   startActivity(intent, options.toBundle());
               }else{
                   Toast.makeText(LoginActivity.this, "Enter a 10 digit number", Toast.LENGTH_SHORT).show();
               }
            }
        });

    }


}
