package com.example.gobiblio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gobiblio.ui.LibraryActivity;
import com.example.gobiblio.ui.LoginActivity;
import com.example.gobiblio.ui.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends AppCompatActivity {

    TextView user;
    ChipNavigationBar navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        setBottomNavigation();

        user = findViewById(R.id.user);

        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        user.setText(currentUser);

    }

    @SuppressLint("NonConstantResourceId")
    private void setBottomNavigation() {
        navigationBar = findViewById(R.id.navigationBar);
        navigationBar.setItemSelected(R.id.home, true);

        navigationBar.setOnItemSelectedListener(i -> {
            switch (i){
                case R.id.home:
                    break;
                case R.id.library:
                    startActivity(new Intent(MainActivity.this, LibraryActivity.class));
                    break;
                case R.id.profile:
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    break;
            }
        });
    }
}