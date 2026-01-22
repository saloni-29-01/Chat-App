package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding activitySettingBinding;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activitySettingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(activitySettingBinding.getRoot());

        fragmentManager = getSupportFragmentManager();

        activitySettingBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getSupportFragmentManager();
                if(fm.getBackStackEntryCount()>0) {
                    fm.popBackStack();
                    activitySettingBinding.profile.setVisibility(View.VISIBLE);
                    activitySettingBinding.logout.setVisibility(View.VISIBLE);
                }else {
                    Intent i = new Intent(SettingActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }
        });


        activitySettingBinding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activitySettingBinding.profile.setVisibility(View.GONE);
                activitySettingBinding.logout.setVisibility(View.GONE);

                fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.setting_container,ProfileFragment.class,null)
                                .addToBackStack(null)
                                .commit();


            }
        });

        activitySettingBinding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Clear user session from your backend if needed
                /* Original Firebase logout removed - implement with your own backend:

                // Make API call to logout if needed
                // yourBackendAPI.logout(userId);

                */

                // Clear user data from SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(SettingActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();

            }
        });



    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount()>0) {
            fm.popBackStack();
            activitySettingBinding.profile.setVisibility(View.VISIBLE);
            activitySettingBinding.logout.setVisibility(View.VISIBLE);
        }
        else
            super.onBackPressed();

//
    }
}