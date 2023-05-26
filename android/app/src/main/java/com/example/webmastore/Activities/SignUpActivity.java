package com.example.webmastore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webmastore.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.CharMatcher;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity {

    private FirebaseFirestore store;

    private Button btnProceed;
    private ImageButton btnBack;

    private TextInputLayout layoutStudentNumber, layoutUsername, layoutEmail, layoutPhoneNumber;
    private TextInputEditText inputStudentNumber, inputUsername, inputEmail, inputPhoneNumber;

    private Bundle bundle;

    private TextView countCharUsername, countCharStudNo;

    private boolean isMember, isExist;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        store = FirebaseFirestore.getInstance();

        bundle = new Bundle();

        btnBack = findViewById(R.id.backToPrevious);
        btnProceed = findViewById(R.id.next);

        layoutStudentNumber = findViewById(R.id.layout_student_no);
        layoutUsername = findViewById(R.id.layout_signup_name);
        layoutEmail = findViewById(R.id.layout_email_address);
        layoutPhoneNumber = findViewById(R.id.layout_phone_num);

        inputStudentNumber = findViewById(R.id.student_no);
        inputUsername = findViewById(R.id.signup_name);
        inputEmail = findViewById(R.id.email_address);
        inputPhoneNumber = findViewById(R.id.phone_num);

        progressDialog = new ProgressDialog(this);
        store = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorHandling();
            }
        });
    }


    private void errorHandling(){
        String nameRegex = "^[A-Z][a-z]*\\s[A-Z][a-z]*$";
        Pattern pattern = Pattern.compile(nameRegex);

        isMember = false;
        isExist = false;

        String username = inputUsername.getText().toString();
        String studentNumber = inputStudentNumber.getText().toString();
        String email = inputEmail.getText().toString();
        String phoneNumber = inputPhoneNumber.getText().toString();


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

        if (!isEmailValid(email)) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Invalid email address!");
            inputEmail.requestFocus();
        } else if (isExist) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email Address is registered to another User");
            inputEmail.requestFocus();
        } else {
            clearError(3);
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
            progressDialog.setTitle("Registration");
            progressDialog.setMessage("Checking Membership Status...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            store.collection("Members")
                    .whereEqualTo("studentNo", studentNumber)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    isMember = true;
                                }
                            } else {
                                isMember = false;
                            }
                            clearError(1);

                            progressDialog.dismiss();

                            progressDialog.setTitle("Verifying Email...");
                            progressDialog.show();

                            store.collection("Users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    isExist = true;
                                                }
                                            } else {
                                                isExist = false;
                                            }

                                            if (isExist) {
                                                layoutEmail.setErrorEnabled(true);
                                                layoutEmail.setError("Email Address is registered to another user");
                                                inputEmail.requestFocus();
                                            } else {
                                                clearError(3);
                                            }

                                            progressDialog.dismiss();

                                            if (withoutErrors()) {
                                                if (isMember) {
                                                    Toast.makeText(SignUpActivity.this, "WebMa membership verified", Toast.LENGTH_SHORT).show();
                                                }

                                                bundle.putString("studentNumber", studentNumber);
                                                bundle.putString("username", username);
                                                bundle.putString("email", email);
                                                bundle.putString("phoneNumber", phoneNumber);
                                                bundle.putBoolean("isMember", isMember);
                                                sendUserToNextActivity();
                                            }

                                        }
                                    });
                        }
                    });
        }
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutStudentNumber.setError(null);
            layoutStudentNumber.setErrorEnabled(false);
        } else if (field == 2) {
            layoutUsername.setError(null);
            layoutUsername.setErrorEnabled(false);
        } else if (field == 3) {
            layoutEmail.setError(null);
            layoutEmail.setErrorEnabled(false);
        } else {
            layoutPhoneNumber.setError(null);
            layoutPhoneNumber.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutStudentNumber.isErrorEnabled() && !layoutUsername.isErrorEnabled() && !layoutEmail.isErrorEnabled() && !layoutPhoneNumber.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches();
    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignUpActivity2.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}