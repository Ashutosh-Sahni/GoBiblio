package com.example.gobiblio.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.example.gobiblio.MainActivity;
import com.example.gobiblio.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class LoginActivity extends AppCompatActivity {

    TextView greeting, forgotPassword, signUp;
    CoordinatorLayout snackBarLayout;
    TextInputEditText email, password;
    AppCompatButton signIn;
    FirebaseAuth firebaseAuth;
    RelativeLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        initDesign();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    private void initDesign() {

        greeting = findViewById(R.id.txt_greeting);
        snackBarLayout = findViewById(R.id.layout_snackbar);
        forgotPassword = findViewById(R.id.txt_forgotPassword);
        signUp = findViewById(R.id.txt_signUp);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        signIn = findViewById(R.id.btn_signIn);
        progressLayout = findViewById(R.id.progressLayout);

        //Setting two colors
        Spannable first = new SpannableString("Glad to see\nyou");
        Spannable second = new SpannableString(" back");
        first.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack)), 0, first.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        second.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow)), 0, second.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        greeting.setText(first);
        greeting.append(second);

        signIn.setOnClickListener(view -> loginUser());

        forgotPassword.setOnClickListener(view -> showPasswordDialog());

        //Moving to Sign Up
        signUp.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
        });

    }

    private void loginUser() {
        String inputEmail = Objects.requireNonNull(email.getText()).toString().trim();
        String inputPassword = Objects.requireNonNull(password.getText()).toString().trim();

        //Checking conditions
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
        //Displaying Progress bar
        progressLayout.setVisibility(View.VISIBLE);

        //Authenticating from FireBase
        firebaseAuth.signInWithEmailAndPassword(inputEmail, inputPassword).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Welcome to GoBiblio!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                    } else {
                        Toast.makeText(LoginActivity.this, "Uh-oh! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        progressLayout.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void showPasswordDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(LoginActivity.this);
        View view = layoutInflater.inflate(R.layout.forgot_password_alert, null);

        TextInputEditText email = view.findViewById(R.id.et_email);
        Button send = view.findViewById(R.id.btn_sendLink);
        TextView back = view.findViewById(R.id.txt_back);
        RelativeLayout progress = view.findViewById(R.id.progressLayout);

        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).setView(view).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setCancelable(false);

        send.setOnClickListener(v -> {
            String inputEmail = Objects.requireNonNull(email.getText()).toString().trim();
            progress.setVisibility(View.VISIBLE);
            firebaseAuth.sendPasswordResetEmail(inputEmail)
                    .addOnSuccessListener(aVoid -> {
                        progress.setVisibility(View.GONE);
                        alertDialog.dismiss();
                        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Reset link has been sent!", Snackbar.LENGTH_SHORT);
                        snackbar.setAnchorView(snackBarLayout);
                        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                        snackbar.show();
                    })
                    .addOnFailureListener(e -> Toast.makeText
                            (LoginActivity.this, "Uh-oh! " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        back.setOnClickListener(v -> alertDialog.dismiss());
    }
}