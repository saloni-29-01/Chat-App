package com.example.chatapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ActivityContactListsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import adapters.DeviceContactsAdapter;
import database.ChatRepository;
import database.UserEntity;
import models.UserModel;

public class ContactListsActivity extends AppCompatActivity {

    ActivityContactListsBinding activityContactListsBinding;
    ArrayList<UserModel> searchedUser = new ArrayList<>(1);
    String currentUserId;
    ChatRepository chatRepository;
    AlertDialog deviceContactsDialog;
    Button importContactsButton;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        activityContactListsBinding = ActivityContactListsBinding.inflate(getLayoutInflater());
        setContentView(activityContactListsBinding.getRoot());
        activityContactListsBinding.newUserDisplay.setVisibility(View.GONE);

        chatRepository = new ChatRepository(this);

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", "");

        // Add button to import contacts
        setupImportContactsButton();

        activityContactListsBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                activityContactListsBinding.searchView.clearFocus();
                searchedUser.clear();

                // Search user by email in local database
                chatRepository.searchUserByEmail(query, new ChatRepository.OnUserSearchListener() {
                    @Override
                    public void onSuccess(UserEntity user) {
                        runOnUiThread(() -> {
                            if (user.getUserId().equals(currentUserId)) {
                                Toast.makeText(ContactListsActivity.this, "Cannot add yourself", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            UserModel userModel = new UserModel(user.getUserName(), user.getUserEmail(), user.getProfilePic());
                            userModel.setUserId(user.getUserId());
                            searchedUser.add(userModel);

                            activityContactListsBinding.userName.setText(user.getUserName());
                            activityContactListsBinding.usermail.setText(user.getUserEmail());

                            String profilePic = user.getProfilePic();
                            if (profilePic != null && !profilePic.isEmpty()) {
                                Picasso.get().load(profilePic)
                                    .fit()
                                    .centerCrop()
                                    .error(R.drawable.user)
                                    .placeholder(R.drawable.user)
                                    .into(activityContactListsBinding.profilePicImageview);
                            } else {
                                activityContactListsBinding.profilePicImageview.setImageResource(R.drawable.user);
                            }

                            activityContactListsBinding.newUserDisplay.setVisibility(View.VISIBLE);

                            activityContactListsBinding.addContactBtn.setOnClickListener(v -> {
                                String contactId = searchedUser.get(0).getUserId();

                                // Add contact to local database
                                chatRepository.addContact(currentUserId, contactId, new ChatRepository.OnContactAddedListener() {
                                    @Override
                                    public void onSuccess() {
                                        runOnUiThread(() -> {
                                            Toast.makeText(ContactListsActivity.this, "Contact added", Toast.LENGTH_SHORT).show();
                                            activityContactListsBinding.newUserDisplay.setVisibility(View.GONE);
                                            searchedUser.clear();
                                            finish();
                                        });
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(ContactListsActivity.this, error, Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                            });
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ContactListsActivity.this, error, Toast.LENGTH_SHORT).show();
                            activityContactListsBinding.newUserDisplay.setVisibility(View.GONE);
                        });
                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void setupImportContactsButton() {
        // Check if button exists in layout, if not we'll show it programmatically
        importContactsButton = findViewById(R.id.import_contacts_button);

        if (importContactsButton == null) {
            // Button doesn't exist in layout, create programmatically or just use a click handler
            android.util.Log.d("ContactListsActivity", "Import button not found in layout");
        } else {
            importContactsButton.setOnClickListener(v -> {
                checkPermissionAndShowContacts();
            });
        }

        // Auto-show contacts if permission already granted (optional - comment out if you don't want this)
        if (ContactsHelper.hasContactPermission(this)) {
            // Delay slightly to ensure activity is fully loaded
            new android.os.Handler().postDelayed(() -> {
                showDeviceContactsDialog();
            }, 300);
        }
    }

    private void checkPermissionAndShowContacts() {
        if (!ContactsHelper.hasContactPermission(this)) {
            ContactsHelper.requestContactPermission(this);
        } else {
            showDeviceContactsDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        android.util.Log.d("ContactListsActivity", "Permission result received");

        if (ContactsHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            android.util.Log.d("ContactListsActivity", "Permission GRANTED - showing contacts");
            Toast.makeText(this, "Contacts permission granted! Loading your contacts...", Toast.LENGTH_SHORT).show();

            // Delay slightly to ensure permission is fully granted
            new android.os.Handler().postDelayed(() -> {
                showDeviceContactsDialog();
            }, 500);
        } else {
            android.util.Log.d("ContactListsActivity", "Permission DENIED");
            Toast.makeText(this, "Contacts permission denied. You can still search by email.", Toast.LENGTH_LONG).show();
        }
    }


    private void showDeviceContactsDialog() {
        android.util.Log.d("ContactListsActivity", "showDeviceContactsDialog called");

        // Get device contacts with emails
        List<ContactsHelper.DeviceContact> deviceContacts = ContactsHelper.getDeviceContacts(this);

        android.util.Log.d("ContactListsActivity", "Found " + deviceContacts.size() + " device contacts");

        if (deviceContacts.isEmpty()) {
            // Show a more informative message
            new AlertDialog.Builder(this)
                .setTitle("No Contacts Found")
                .setMessage("Your device has no contacts with email addresses.\n\nTo test this feature:\n1. Open your device Contacts app\n2. Add a new contact with an email address\n3. Return to this app and try again\n\nYou can still search users manually by email.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_device_contacts, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.device_contacts_recyclerview);
        TextView countText = dialogView.findViewById(R.id.contacts_count_text);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        countText.setText("Found " + deviceContacts.size() + " contacts. Select one to check if they're on the app:");

        // Setup adapter
        DeviceContactsAdapter adapter = new DeviceContactsAdapter(deviceContacts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        deviceContactsDialog = builder.create();

        // Handle contact selection
        adapter.setOnContactClickListener(contact -> {
            deviceContactsDialog.dismiss();
            android.util.Log.d("ContactListsActivity", "Selected contact: " + contact.getName() + " - " + contact.getEmailOrPhone());
            // Search for this contact in the app database
            searchUserByEmailFromContact(contact.getEmailOrPhone());
        });

        cancelButton.setOnClickListener(v -> deviceContactsDialog.dismiss());

        android.util.Log.d("ContactListsActivity", "About to show dialog");
        deviceContactsDialog.show();
        android.util.Log.d("ContactListsActivity", "Dialog shown");
    }

    private void searchUserByEmailFromContact(String email) {
        // Search user by email in local database
        chatRepository.searchUserByEmail(email, new ChatRepository.OnUserSearchListener() {
            @Override
            public void onSuccess(UserEntity user) {
                runOnUiThread(() -> {
                    if (user.getUserId().equals(currentUserId)) {
                        Toast.makeText(ContactListsActivity.this, "This is your email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    searchedUser.clear();
                    UserModel userModel = new UserModel(user.getUserName(), user.getUserEmail(), user.getProfilePic());
                    userModel.setUserId(user.getUserId());
                    searchedUser.add(userModel);

                    activityContactListsBinding.userName.setText(user.getUserName());
                    activityContactListsBinding.usermail.setText(user.getUserEmail());

                    String profilePic = user.getProfilePic();
                    if (profilePic != null && !profilePic.isEmpty()) {
                        Picasso.get().load(profilePic)
                            .fit()
                            .centerCrop()
                            .error(R.drawable.user)
                            .placeholder(R.drawable.user)
                            .into(activityContactListsBinding.profilePicImageview);
                    } else {
                        activityContactListsBinding.profilePicImageview.setImageResource(R.drawable.user);
                    }

                    activityContactListsBinding.newUserDisplay.setVisibility(View.VISIBLE);

                    activityContactListsBinding.addContactBtn.setOnClickListener(v -> {
                        String contactId = searchedUser.get(0).getUserId();

                        // Add contact to local database
                        chatRepository.addContact(currentUserId, contactId, new ChatRepository.OnContactAddedListener() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    Toast.makeText(ContactListsActivity.this, "Contact added successfully!", Toast.LENGTH_SHORT).show();
                                    activityContactListsBinding.newUserDisplay.setVisibility(View.GONE);
                                    searchedUser.clear();
                                    finish();
                                });
                            }

                            @Override
                            public void onFailure(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ContactListsActivity.this, error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    });

                    Toast.makeText(ContactListsActivity.this, "Great! This contact is on the app", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ContactListsActivity.this, "This contact is not on the app yet", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}