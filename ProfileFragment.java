package com.example.chatapp;

import static android.app.Activity.RESULT_OK;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.chatapp.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;

import database.ChatRepository;
import database.UserEntity;


public class ProfileFragment extends Fragment {

   FragmentProfileBinding binding;
   FragmentManager fragmentManager;
   String userId;
   ActivityResultLauncher<CropImageContractOptions> cropImage;
   ChatRepository chatRepository;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatRepository = new ChatRepository(requireContext());

        // Initialize the image cropper launcher
        cropImage = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri resultUri = result.getUriContent();

                Picasso.get().load(resultUri).fit().centerCrop().into(binding.profilePicImageview);

                // Save profile picture URI as string
                String profilePicUrl = resultUri.toString();
                chatRepository.updateUserProfile(userId, null, null, profilePicUrl, new ChatRepository.OnUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        fragmentManager = getChildFragmentManager();

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        binding.uid.setText(userId);

        // Load user profile from database
        loadUserProfile();

        binding.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.editFragContainer.setVisibility(View.VISIBLE);
                binding.editFragContainer.bringToFront();
                binding.uid.setVisibility(View.GONE);

                binding.text.setText("Enter new name");

                binding.edittext.requestFocus();
                binding.saveEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newUserName = binding.edittext.getText().toString().trim();

                        if (!newUserName.isEmpty()) {
                            // Update username in database
                            chatRepository.updateUserProfile(userId, newUserName, null, null, new ChatRepository.OnUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Username updated", Toast.LENGTH_SHORT).show();
                                    binding.username.setText(newUserName);
                                    binding.edittext.setText("");
                                    binding.editFragContainer.setVisibility(View.GONE);

                                    // Update SharedPreferences
                                    SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
                                    prefs.edit().putString("userName", newUserName).apply();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(getContext(), "Failed to update username", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        binding.about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.editFragContainer.setVisibility(View.VISIBLE);
                binding.editFragContainer.bringToFront();
                binding.uid.setVisibility(View.GONE);

                binding.text.setText("Enter about");
                binding.edittext.setHint("about");

                binding.edittext.requestFocus();
                binding.saveEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newAbout = binding.edittext.getText().toString().trim();

                        // Update about in database
                        chatRepository.updateUserProfile(userId, null, newAbout, null, new ChatRepository.OnUpdateListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "About updated", Toast.LENGTH_SHORT).show();
                                binding.about.setText(newAbout);
                                binding.edittext.setText("");
                                binding.editFragContainer.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(getContext(), "Failed to update about", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });

        binding.newPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Launch the image cropper with CanHub library
                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                cropImageOptions.aspectRatioX = 3;
                cropImageOptions.aspectRatioY = 3;
                cropImageOptions.fixAspectRatio = true;

                CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(null, cropImageOptions);
                cropImage.launch(cropImageContractOptions);

            }
        });

        return binding.getRoot();
    }

    private void loadUserProfile() {
        chatRepository.getUserById(userId, new ChatRepository.OnUserLoadedListener() {
            @Override
            public void onSuccess(UserEntity user) {
                requireActivity().runOnUiThread(() -> {
                    binding.username.setText(user.getUserName());
                    binding.usermail.setText(user.getUserEmail());

                    String about = user.getAbout();
                    if (about != null && !about.isEmpty()) {
                        binding.about.setText(about);
                    } else {
                        binding.about.setText("Hey there! I'm using Chat App");
                    }

                    String profilePic = user.getProfilePic();
                    if (profilePic != null && !profilePic.isEmpty()) {
                        Picasso.get().load(profilePic).error(R.drawable.user)
                                .placeholder(R.drawable.user).centerCrop().fit()
                                .into(binding.profilePicImageview);
                    } else {
                        binding.profilePicImageview.setImageResource(R.drawable.user);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}