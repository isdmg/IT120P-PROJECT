package com.example.webmastore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.example.webmastore.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private TextInputLayout layputInputEmail, layputInputPassword;

    private Button btnLogIn, btnSignUpRedirect;

    private FirebaseAuth auth;

    private FirebaseFirestore store;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        layputInputEmail = findViewById(R.id.layout_email_address);
        layputInputPassword = findViewById(R.id.layout_enter_password);

        inputEmail = findViewById(R.id.email_address);
        inputPassword = findViewById(R.id.enter_password);

        btnLogIn = findViewById(R.id.log_in_button);
        btnSignUpRedirect = findViewById(R.id.sign_up_redirect_button);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAuth();
            }
        });

        btnSignUpRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    private void performAuth() {
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Signing in...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty()) {
            layputInputEmail.setErrorEnabled(true);
            layputInputEmail.setError("Email field is required!");
            inputEmail.requestFocus();
        } else if (!isEmailValid(email)) {
            layputInputEmail.setErrorEnabled(true);
            layputInputEmail.setError("Invalid email address!");
            inputEmail.requestFocus();
        } else {
            clearError(1);
        }

        if (password.isEmpty()) {
            layputInputPassword.setErrorEnabled(true);
            layputInputPassword.setError("Password field is required!");
            inputPassword.requestFocus();
        } else if (password.length() < 6) {
            layputInputPassword.setError("Password should be at least 6 characters!");
            inputPassword.requestFocus();
        } else if (password.length() > 15) {
            layputInputPassword.setError("Password limited to 15 characters!");
            inputPassword.requestFocus();
        } else {
            clearError(2);
        }

        if (withoutErrors()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        // Cannot log in when admin
                        DocumentReference documentReference = store.collection("Users").document(auth.getUid());
                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.getBoolean("isAdmin")) {
                                    auth.signOut();
                                    Toast.makeText(LoginActivity.this, "An administration cannot log in as a customer", Toast.LENGTH_SHORT).show();
                                    Log.d("LoginActivity", "Admin cannot log in as a customer");
                                    progressDialog.dismiss();
                                    return;
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, CustomerActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        });

                    } else {
                        try {
                            progressDialog.dismiss();
                            throw task.getException();
                        } catch (Exception e) {
                            progressDialog.dismiss();
                            if (e.getMessage() == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                                Toast.makeText(LoginActivity.this, "There is no user record corresponding to this identifier", Toast.LENGTH_SHORT).show();
                            } else if (e.getMessage() == "The password is invalid or the user does not have a password.") {
                                Toast.makeText(LoginActivity.this, "The password is invalid or the user does not have a password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        } else {
            progressDialog.dismiss();
        }
    }

    private void clearError(int field) {
        if (field == 1) {
            layputInputEmail.setError(null);
            layputInputEmail.setErrorEnabled(false);
        } else if (field == 2) {
            layputInputPassword.setError(null);
            layputInputPassword.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layputInputEmail.isErrorEnabled() && !layputInputPassword.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches();
    }
}