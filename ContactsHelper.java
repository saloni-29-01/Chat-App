package com.example.chatapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ContactsHelper {

    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 100;

    // Check if contact permission is granted
    public static boolean hasContactPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request contact permission
    public static void requestContactPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                CONTACTS_PERMISSION_REQUEST_CODE);
    }

    // Get all contacts from device
    public static List<DeviceContact> getDeviceContacts(Context context) {
        List<DeviceContact> contactsList = new ArrayList<>();

        android.util.Log.d("ContactsHelper", "Starting to get device contacts");

        if (!hasContactPermission(context)) {
            android.util.Log.d("ContactsHelper", "No permission to read contacts");
            return contactsList;
        }

        android.util.Log.d("ContactsHelper", "Permission granted, querying contacts");

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " ASC"
        );

        android.util.Log.d("ContactsHelper", "Cursor result: " + (cursor != null ? cursor.getCount() : "null"));

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME);
                int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                int photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI);

                String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "";
                String email = emailIndex >= 0 ? cursor.getString(emailIndex) : "";
                String photoUri = photoIndex >= 0 ? cursor.getString(photoIndex) : "";

                if (name != null && !name.isEmpty() && email != null && !email.isEmpty()) {
                    DeviceContact contact = new DeviceContact(name, email, photoUri);
                    contactsList.add(contact);
                    android.util.Log.d("ContactsHelper", "Added contact: " + name + " - " + email);
                }
            }
            cursor.close();
        }

        android.util.Log.d("ContactsHelper", "Total contacts found: " + contactsList.size());
        return contactsList;
    }

    // Get contacts with phone numbers
    public static List<DeviceContact> getContactsWithPhones(Context context) {
        List<DeviceContact> contactsList = new ArrayList<>();

        if (!hasContactPermission(context)) {
            return contactsList;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

                String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "";
                String phone = phoneIndex >= 0 ? cursor.getString(phoneIndex) : "";
                String photoUri = photoIndex >= 0 ? cursor.getString(photoIndex) : "";

                if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty()) {
                    DeviceContact contact = new DeviceContact(name, phone, photoUri);
                    contactsList.add(contact);
                }
            }
            cursor.close();
        }

        return contactsList;
    }

    // Device Contact Model
    public static class DeviceContact {
        private String name;
        private String emailOrPhone;
        private String photoUri;

        public DeviceContact(String name, String emailOrPhone, String photoUri) {
            this.name = name;
            this.emailOrPhone = emailOrPhone;
            this.photoUri = photoUri;
        }

        public String getName() {
            return name;
        }

        public String getEmailOrPhone() {
            return emailOrPhone;
        }

        public String getPhotoUri() {
            return photoUri;
        }
    }

    // Handle permission result
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
}
