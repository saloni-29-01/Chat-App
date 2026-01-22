package com.example.chatapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import database.ChatRepository;
import database.UserEntity;

/**
 * Test activity to manually add test contacts to database
 */
public class AddTestContactsActivity extends AppCompatActivity {

    ChatRepository chatRepository;
    String userId;
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create simple layout programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        statusText = new TextView(this);
        statusText.setText("Test Contact Creator\n\n");
        statusText.setTextSize(16);
        layout.addView(statusText);

        Button addTestUserButton = new Button(this);
        addTestUserButton.setText("Create Test User");
        addTestUserButton.setOnClickListener(v -> createTestUser());
        layout.addView(addTestUserButton);

        Button addTestContactButton = new Button(this);
        addTestContactButton.setText("Add Test Contact");
        addTestContactButton.setOnClickListener(v -> addTestContact());
        layout.addView(addTestContactButton);

        Button viewContactsButton = new Button(this);
        viewContactsButton.setText("View My Contacts");
        viewContactsButton.setOnClickListener(v -> viewMyContacts());
        layout.addView(viewContactsButton);

        setContentView(layout);

        chatRepository = new ChatRepository(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        updateStatus("Current User ID: " + userId + "\n\n");
    }

    private void createTestUser() {
        String testEmail = "testuser" + System.currentTimeMillis() + "@test.com";
        updateStatus("Creating test user: " + testEmail + "\n");

        chatRepository.registerUser("Test User", testEmail, "password123", new ChatRepository.OnUserRegisteredListener() {
            @Override
            public void onSuccess(UserEntity user) {
                runOnUiThread(() -> {
                    updateStatus("✓ Test user created!\nID: " + user.getUserId() + "\nEmail: " + user.getUserEmail() + "\n\n");
                    Toast.makeText(AddTestContactsActivity.this, "Test user created!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    updateStatus("✗ Failed: " + error + "\n\n");
                    Toast.makeText(AddTestContactsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addTestContact() {
        updateStatus("Looking for users to add as contact...\n");

        chatRepository.getAllUsers(users -> {
            runOnUiThread(() -> {
                if (users.size() < 2) {
                    updateStatus("✗ Need at least 2 users in database. Create a test user first!\n\n");
                    Toast.makeText(AddTestContactsActivity.this, "Create test user first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Find a user that's not the current user
                UserEntity contactUser = null;
                for (UserEntity user : users) {
                    if (!user.getUserId().equals(userId)) {
                        contactUser = user;
                        break;
                    }
                }

                if (contactUser == null) {
                    updateStatus("✗ No other users found\n\n");
                    return;
                }

                final UserEntity finalContactUser = contactUser;
                updateStatus("Adding contact: " + finalContactUser.getUserName() + "\n");

                chatRepository.addContact(userId, finalContactUser.getUserId(), new ChatRepository.OnContactAddedListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            updateStatus("✓ Contact added successfully!\nName: " + finalContactUser.getUserName() + "\nEmail: " + finalContactUser.getUserEmail() + "\n\n");
                            Toast.makeText(AddTestContactsActivity.this, "Contact added!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            updateStatus("✗ Failed to add contact: " + error + "\n\n");
                            Toast.makeText(AddTestContactsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        });
    }

    private void viewMyContacts() {
        updateStatus("Loading contacts...\n");

        chatRepository.getUserContacts(userId, contacts -> {
            runOnUiThread(() -> {
                updateStatus("=== MY CONTACTS ===\nTotal: " + contacts.size() + "\n\n");
                if (contacts.isEmpty()) {
                    updateStatus("No contacts found!\n\n");
                } else {
                    for (int i = 0; i < contacts.size(); i++) {
                        database.ContactEntity contact = contacts.get(i);
                        updateStatus((i + 1) + ". " + contact.getContactName() + "\n   " + contact.getContactEmail() + "\n\n");
                    }
                }
                Toast.makeText(AddTestContactsActivity.this, "Found " + contacts.size() + " contact(s)", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateStatus(String message) {
        statusText.append(message);
    }
}
