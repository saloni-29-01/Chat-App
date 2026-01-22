package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivitySigninBinding;

import database.ChatRepository;
import database.UserEntity;

public class SigninActivity extends AppCompatActivity {

    ActivitySigninBinding activitySigninBinding;
    ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activitySigninBinding = ActivitySigninBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(activitySigninBinding.getRoot());

        chatRepository = new ChatRepository(this);
        activitySigninBinding.progressBar.setVisibility(View.GONE);


        activitySigninBinding.hidePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(activitySigninBinding.signinPassword.getTransformationMethod()!=null)
                     activitySigninBinding.signinPassword.setTransformationMethod(null);
                else activitySigninBinding.signinPassword.setTransformationMethod(new PasswordTransformationMethod());

            }
        });


        activitySigninBinding.signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = activitySigninBinding.signinMail.getText().toString().trim();
                String password = activitySigninBinding.signinPassword.getText().toString().trim();


                if(!email.isEmpty() && !password.isEmpty()) {
                    activitySigninBinding.progressBar.setVisibility(View.VISIBLE);

                    // Login using local database
                    chatRepository.loginUser(email, password, new ChatRepository.OnUserLoginListener() {
                        @Override
                        public void onSuccess(UserEntity user) {
                            runOnUiThread(() -> {
                                // Save user credentials to SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("userId", user.getUserId());
                                editor.putString("userName", user.getUserName());
                                editor.putString("userEmail", user.getUserEmail());
                                editor.apply();

                                activitySigninBinding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(SigninActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                activitySigninBinding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(SigninActivity.this, error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });

                } else {
                    activitySigninBinding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SigninActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}