package com.example.webmastore.Activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webmastore.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.CharMatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 71;
    private Button btnEditProfile, btnSave;

    private TextInputLayout layoutStudentNumber, layoutUsername, layoutPhoneNumber;
    private TextInputEditText inputStudentNumber, inputUsername, inputPhoneNumber;

    private ImageButton btnBack;

    private CircleImageView btnUpload;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private FirebaseStorage storage;

    private Uri imageUri;

    private StorageReference storageReference;
    private String downloadImageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        layoutStudentNumber = findViewById(R.id.layout_edit_student_no);
        layoutUsername = findViewById(R.id.layout_edit_name);
        layoutPhoneNumber = findViewById(R.id.layout_edit_phone_no);

        inputStudentNumber = findViewById(R.id.edit_student_no);
        inputUsername = findViewById(R.id.edit_name);
        inputPhoneNumber = findViewById(R.id.edit_phone_no);

        btnUpload = findViewById(R.id.profile_button);
        btnSave = findViewById(R.id.button_edit_profile);
        btnBack = findViewById(R.id.backToPrevious);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        getUserInfo();

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ProfileActivity.this);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorHandling();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void getUserInfo() {
        final DocumentReference docRef = store.collection("Users").document(auth.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    inputStudentNumber.setText(snapshot.getString("studentNumber"));
                    inputUsername.setText(snapshot.getString("name"));
                    inputPhoneNumber.setText(snapshot.getString("phone").substring(3));
                    Picasso.get().load(snapshot.getString("imageRef")).into(btnUpload);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Picasso.get().load(imageUri).into(btnUpload);
        }
    }

    private void errorHandling(){

        String studentNumber = inputStudentNumber.getText().toString();
        String username = inputUsername.getText().toString();
        String phoneNumber = inputPhoneNumber.getText().toString();

        String nameRegex = "^[A-Z][a-z]*\\s[A-Z][a-z]*$";
        Pattern pattern = Pattern.compile(nameRegex);


        if (phoneNumber.isEmpty()) {
            layoutPhoneNumber.setErrorEnabled(true);
            layoutPhoneNumber.setError("Phone number field is required!");
            inputPhoneNumber.requestFocus();
        } else if (phoneNumber.length() != 10) {
            layoutPhoneNumber.setError("Invalid phone number format!");
            inputPhoneNumber.requestFocus();
        } else if (phoneNumber.charAt(0) != '9') {
            layoutPhoneNumber.setError("Mobile area code is not accepted!");
            inputPhoneNumber.requestFocus();
        } else {
            clearError(4);
        }

        Matcher matcher = pattern.matcher(inputUsername.getText().toString().trim());
        if (matcher.matches()) {
            clearError(2);
        } else {
            layoutUsername.setErrorEnabled(true);
            layoutUsername.setError("Invalid format!");
            inputUsername.requestFocus();
        }
        if (studentNumber.isEmpty()) {
            layoutStudentNumber.setErrorEnabled(true);
            layoutStudentNumber.setError("Name field is required!");
            inputStudentNumber.requestFocus();
        } else if (studentNumber.length() != 10) {
            layoutStudentNumber.setErrorEnabled(true);
            layoutStudentNumber.setError("Invalid Student Number");
            inputStudentNumber.requestFocus();
        } else {
            clearError(1);
        }

        if (withoutErrors()) {

            DocumentReference documentReference = store.collection("Users").document(auth.getUid());
            documentReference.update("name", username);
            documentReference.update("phone", "+63" + phoneNumber);
            documentReference.update("studentNum", "+63" + phoneNumber);

            if(imageUri != null)
            {
                storageReference.child("Users/" + auth.getUid() + ".jpg")
                        .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference.child("Users/" + auth.getUid() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        documentReference.update("imageRef", uri.toString());
                                        Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Uploading image failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else
            {
                Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutStudentNumber.setError(null);
            layoutStudentNumber.setErrorEnabled(false);
        } else if (field == 2) {
            layoutUsername.setError(null);
            layoutUsername.setErrorEnabled(false);
        } else {
            layoutPhoneNumber.setError(null);
            layoutPhoneNumber.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutStudentNumber.isErrorEnabled() && !layoutUsername.isErrorEnabled() && !layoutPhoneNumber.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }
}