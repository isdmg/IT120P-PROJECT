package com.example.webmastore.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.webmastore.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity2 extends AppCompatActivity {
    private Button btnProceed;
    private ImageButton btnBack;


    private TextInputLayout layoutPassword, layoutConfirmPassword;
    private TextInputEditText inputPassword, inputConfirmPassword;

    private Bundle bundle;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bundle = getIntent().getExtras();

        btnBack = findViewById(R.id.backToPrevious);
        btnProceed = findViewById(R.id.next);

        layoutPassword = findViewById(R.id.layout_create_password);
        layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        inputPassword = findViewById(R.id.create_password);
        inputConfirmPassword = findViewById(R.id.confirm_password);



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

    private void errorHandling() {
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setErrorEnabled(true);
            layoutConfirmPassword.setError("Passwords do not match!");
            inputConfirmPassword.requestFocus();
        } else {
            clearError(2);
        }

        if (password.isEmpty()) {
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Password field is required!");
            inputPassword.requestFocus();
        } else if (password.length() < 6) {
            layoutPassword.setError("Password should be at least 6 characters!");
            inputPassword.requestFocus();
        } else if (password.length() > 15) {
            layoutPassword.setError("Password limited to 15 characters!");
            inputPassword.requestFocus();
        } else {
            clearError(1);
        }

        if (withoutErrors()) {
            bundle.putString("password", password);
            sendUserToNextActivity();
        }

    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUpActivity2.this, SignUpActivity3.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutPassword.setError(null);
            layoutPassword.setErrorEnabled(false);
        } else {
            layoutConfirmPassword.setError(null);
            layoutConfirmPassword.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutPassword.isErrorEnabled() && !layoutConfirmPassword.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }
}