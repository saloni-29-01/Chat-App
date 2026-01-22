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

import com.example.chatapp.databinding.ActivitySignupBinding;

import database.ChatRepository;
import database.UserEntity;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding activitySignupBinding;
    private ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        if(!userId.isEmpty()){
            Intent intent = new Intent(SignupActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

        activitySignupBinding = ActivitySignupBinding.inflate(getLayoutInflater());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(activitySignupBinding.getRoot());

        chatRepository = new ChatRepository(this);
        activitySignupBinding.progressBar.setVisibility(View.GONE);

        activitySignupBinding.hidePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(activitySignupBinding.password.getTransformationMethod()!=null)
                    activitySignupBinding.password.setTransformationMethod(null);
                else activitySignupBinding.password.setTransformationMethod(new PasswordTransformationMethod());

            }
        });

        activitySignupBinding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = activitySignupBinding.username.getText().toString().trim();
                String email = activitySignupBinding.mail.getText().toString().trim();
                String password = activitySignupBinding.password.getText().toString().trim();

                if(!email.isEmpty() && !password.isEmpty() && !userName.isEmpty()) {
                    activitySignupBinding.progressBar.setVisibility(View.VISIBLE);

                    // Register user in local database
                    chatRepository.registerUser(userName, email, password, new ChatRepository.OnUserRegisteredListener() {
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

                                activitySignupBinding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                activitySignupBinding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });

                } else {
                    activitySignupBinding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        activitySignupBinding.moveToSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });
    }

}