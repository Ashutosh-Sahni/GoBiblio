package com.example.gobiblio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.gobiblio.MainActivity;
import com.example.gobiblio.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class RegisterActivity extends AppCompatActivity {

    TextView welcome, signIn;
    TextInputEditText email, password, confirmPassword;
    AppCompatButton createAccount;
    FirebaseAuth firebaseAuth;
    RelativeLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        initDesign();

        firebaseAuth = FirebaseAuth.getInstance();

        //Checking if already signed in
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private void initDesign() {

        welcome = findViewById(R.id.txt_welcome);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirmPassword);
        createAccount = findViewById(R.id.btn_create);
        signIn = findViewById(R.id.txt_signIn);
        progressLayout = findViewById(R.id.progressLayout);

        //Setting Two Colors
        Spannable first = new SpannableString("Welcome to\nGo");
        Spannable second = new SpannableString("Biblio");
        first.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack)), 0, first.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        second.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow)), 0, second.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        welcome.setText(first);
        welcome.append(second);

        //Creating Account
        createAccount.setOnClickListener(view -> registerUser());

        //Opening LoginActivity
        signIn.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
        });

    }

    private void registerUser() {
        String inputEmail = email.getText().toString().trim();
        String inputPassword = password.getText().toString().trim();
        String inputConfirmation = confirmPassword.getText().toString().trim();

        //Checking all conditions
        if (TextUtils.isEmpty(inputEmail)) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(inputPassword)) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(inputConfirmation)) {
            confirmPassword.setError("Confirmation is required");
            confirmPassword.requestFocus();
            return;
        }
        if (inputPassword.length() < 8) {
            password.setError("Password must have 8 or more characters");
            password.requestFocus();
            return;
        }
        if (!inputPassword.equals(inputConfirmation)) {
            confirmPassword.setError("Passwords do not match");
            return;
        }

        //Displaying progress bar
        progressLayout.setVisibility(View.VISIBLE);

        //Registering user to FireBase
        firebaseAuth.createUserWithEmailAndPassword(inputEmail, inputPassword).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        //Opening HomeActivity if Registration Successful
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                    } else {
                        Toast.makeText(this, "Uh-oh! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressLayout.setVisibility(View.GONE);
                    }
                }
        );
    }
}